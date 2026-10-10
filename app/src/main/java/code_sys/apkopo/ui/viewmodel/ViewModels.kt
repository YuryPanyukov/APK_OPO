package code_sys.apkopo.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import code_sys.apkopo.data.local.entity.Commission
import code_sys.apkopo.di.AppContainer
import code_sys.apkopo.domain.repository.ReportFormat
import code_sys.apkopo.domain.usecase.AddRemarkUseCase
import code_sys.apkopo.util.GeoPoint
import code_sys.apkopo.util.PdfPreviewRenderer
import code_sys.apkopo.util.PhotoMeta
import code_sys.apkopo.util.PhotoStorage
import code_sys.apkopo.util.ReportGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** Зависимости для фабрик ViewModel. */
private fun AppContainer.viewModels(): AppContainer = this

/** Текстовые значения формы замечания (для merge при предзаполнении). */
data class RemarkFormText(
    val location: String,
    val objectName: String,
    val remarkType: String,
    val description: String
)

/** Признаки «поле изменено пользователем» — предзаполнение такие поля не затирает. */
data class RemarkFormDirty(
    val location: Boolean = false,
    val objectName: Boolean = false,
    val remarkType: Boolean = false,
    val description: Boolean = false,
    val geo: Boolean = false,
    val photos: Boolean = false
)

/**
 * Слияние предзаполнения (загружено из БД асинхронно) с текущим вводом:
 * загруженное значение применяется только к ещё не изменённым пользователем полям.
 */
internal fun mergePrefillText(
    current: RemarkFormText,
    loaded: RemarkFormText,
    dirty: RemarkFormDirty
): RemarkFormText = RemarkFormText(
    location = if (dirty.location) current.location else loaded.location,
    objectName = if (dirty.objectName) current.objectName else loaded.objectName,
    remarkType = if (dirty.remarkType) current.remarkType else loaded.remarkType,
    description = if (dirty.description) current.description else loaded.description
)

/** Список комиссий. */
class CommissionListViewModel(private val container: AppContainer) : ViewModel() {

    val commissions: StateFlow<List<Commission>> = container.commissionRepository
        .observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun create(title: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            container.createCommissionUseCase(title)
            onDone()
        }
    }

    fun delete(commission: Commission) {
        viewModelScope.launch { container.deleteCommissionUseCase(commission) }
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { CommissionListViewModel(container.viewModels()) }
        }
    }
}

/** Детали комиссии: её сама + замечания. */
class CommissionDetailViewModel(
    private val container: AppContainer,
    commissionId: Long
) : ViewModel() {

    val commission = container.commissionRepository.observeById(commissionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val remarks = container.remarkRepository.observeByCommission(commissionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteCommission(onDone: () -> Unit) {
        viewModelScope.launch {
            commission.value?.let { container.deleteCommissionUseCase(it) }
            onDone()
        }
    }

    /** Переименование комиссии (название обрезается, пустое отклоняется). */
    fun rename(title: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val current = commission.value ?: return@launch
            withContext(Dispatchers.IO) {
                container.updateCommissionUseCase(current.copy(title = title))
            }
            onDone()
        }
    }

    companion object {
        fun factory(container: AppContainer, commissionId: Long) = viewModelFactory {
            initializer { CommissionDetailViewModel(container.viewModels(), commissionId) }
        }
    }
}

/** Добавление или редактирование замечания: форма + фото + геолокация. */
class AddRemarkViewModel(
    private val container: AppContainer,
    private val commissionId: Long,
    private val editRemarkId: Long? = null
) : ViewModel() {

    /** ImageLoader Coil из контейнера (для AsyncImage в списке фото). */
    val imageLoader: coil3.ImageLoader = container.imageLoader.imageLoader

    /** Режим редактирования существующего замечания (а не создания нового). */
    val isEdit: Boolean = editRemarkId != null

    private val _location = MutableStateFlow("")
    val location: StateFlow<String> = _location.asStateFlow()

    private val _objectName = MutableStateFlow("")
    val objectName: StateFlow<String> = _objectName.asStateFlow()

    private val _remarkType = MutableStateFlow(REMARK_TYPES.first())
    val remarkType: StateFlow<String> = _remarkType.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _photos = MutableStateFlow<List<PhotoMeta>>(emptyList())
    val photos: StateFlow<List<PhotoMeta>> = _photos.asStateFlow()

    private val _geo = MutableStateFlow<GeoPoint?>(null)
    val geo: StateFlow<GeoPoint?> = _geo.asStateFlow()

    /** GPS доступен: true, если получены координаты с разрешениями/сигналом. */
    val gpsAvailable: StateFlow<Boolean> = _geo.combine(geo) { geo, _ -> geo?.available == true }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), false
    )

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    /** Пути фото, которые уже есть в БД (при сохранении не пересоздаются). */
    private val existingPaths = mutableSetOf<String>()

    /** Пути фото, снятых с замечания в режиме редактирования. */
    private val removedPaths = mutableSetOf<String>()

    /** Признаки «поле уже изменено пользователем» (защита от затирания ввода). */
    private var dirty = RemarkFormDirty()

    /** Загрузка существующего замечания (только в режиме редактирования). */
    private val loadJob: Job? = editRemarkId?.let { id ->
        viewModelScope.launch {
            val loaded = withContext(Dispatchers.IO) {
                val remark = container.remarkRepository.getById(id)
                if (remark == null) {
                    null
                } else {
                    remark to container.photoRepository.getByRemark(id)
                }
            } ?: return@launch
            val (remark, photos) = loaded
            // Предзаполнение не затирает уже изменённые пользователем поля:
            // за время загрузки он мог начать вводить текст.
            val current = RemarkFormText(
                location = _location.value,
                objectName = _objectName.value,
                remarkType = _remarkType.value,
                description = _description.value
            )
            val prefill = RemarkFormText(
                location = remark.location,
                objectName = remark.objectName,
                remarkType = remark.remarkType,
                description = remark.description
            )
            val merged = mergePrefillText(current, prefill, dirty)
            _location.value = merged.location
            _objectName.value = merged.objectName
            _remarkType.value = merged.remarkType
            _description.value = merged.description
            // GPS-точка берётся только если пользователь не запросил свою (fetchLocation).
            if (!dirty.geo) {
                _geo.value = GeoPoint(remark.userLat, remark.userLng, remark.userTime)
            }
            existingPaths += photos.map { it.filePath }
            // Фото не затираем, если пользователь успел добавить свои.
            if (_photos.value.isEmpty()) {
                _photos.value = photos.map {
                    PhotoMeta(it.filePath, it.photoLat, it.photoLng, it.photoTime)
                }
            } else {
                existingPaths += _photos.value.map { it.filePath }
            }
        }
    }

    fun onLocation(v: String) {
        dirty = dirty.copy(location = true)
        _location.value = v
    }

    fun onObjectName(v: String) {
        dirty = dirty.copy(objectName = true)
        _objectName.value = v
    }

    fun onRemarkType(v: String) {
        dirty = dirty.copy(remarkType = true)
        _remarkType.value = v
    }

    fun onDescription(v: String) {
        dirty = dirty.copy(description = true)
        _description.value = v
    }

    fun removePhoto(meta: PhotoMeta) {
        dirty = dirty.copy(photos = true)
        _photos.value = _photos.value - meta
        if (meta.filePath in existingPaths) removedPaths += meta.filePath
    }

    /** Запрашиваем текущее положение пользователя. */
    fun fetchLocation() {
        dirty = dirty.copy(geo = true)
        viewModelScope.launch {
            _geo.value = container.locationProvider.currentPoint()
        }
    }

    /** Запускает принудительный запрос GPS (в том числе при повторной попытке). */
    fun refreshLocation() {
        fetchLocation()
    }

    /** Создаёт пустой файл-контейнер для снимка камеры. */
    fun newCameraFile(): java.io.File = container.photoStorage.newCaptureFile()

    /** Сохранение снимка камеры (файл уже записан в хранилище). */
    fun onCameraPhoto(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            val point = _geo.value
            val meta = container.photoStorage.readMeta(
                file,
                point?.lat ?: 0.0,
                point?.lng ?: 0.0
            )
            dirty = dirty.copy(photos = true)
            _photos.value = _photos.value + meta
        }
    }

    /** Сохранение фото из галереи (копия в хранилище приложения). */
    fun onGalleryPhoto(uri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val point = _geo.value
            val meta = container.photoStorage.saveFromUri(
                uri,
                point?.lat ?: 0.0,
                point?.lng ?: 0.0
            ) ?: return@launch
            dirty = dirty.copy(photos = true)
            _photos.value = _photos.value + meta
        }
    }

    fun save(onDone: () -> Unit) {
        if (_saving.value) return
        _saving.value = true
        viewModelScope.launch {
            // В режиме редактирования ждём загрузки, чтобы не затереть ввод пользователя
            loadJob?.join()
            val point = _geo.value
            val now = System.currentTimeMillis()
            withContext(Dispatchers.IO) {
                if (editRemarkId != null) {
                    val remark = container.remarkRepository.getById(editRemarkId)
                    if (remark != null) {
                        container.updateRemarkUseCase(
                            remark = remark.copy(
                                location = _location.value,
                                objectName = _objectName.value,
                                remarkType = _remarkType.value,
                                description = _description.value,
                                userLat = point?.lat ?: remark.userLat,
                                userLng = point?.lng ?: remark.userLng,
                                userTime = point?.time ?: remark.userTime
                            ),
                            addedPhotos = _photos.value
                                .filterNot { it.filePath in existingPaths },
                            removedPaths = removedPaths.toList()
                        )
                    }
                } else {
                    container.addRemarkUseCase(
                        commissionId = commissionId,
                        location = _location.value,
                        objectName = _objectName.value,
                        remarkType = _remarkType.value,
                        description = _description.value,
                        userLat = point?.lat ?: 0.0,
                        userLng = point?.lng ?: 0.0,
                        userTime = point?.time ?: now,
                        photoMetas = _photos.value
                    )
                }
            }
            _saving.value = false
            onDone()
        }
    }

    companion object {
        val REMARK_TYPES = listOf(
            "Нарушение", "Неисправность", "Отклонение", "Замечание", "Предписание"
        )

        fun factory(
            container: AppContainer,
            commissionId: Long = 0L,
            editRemarkId: Long? = null
        ) = viewModelFactory {
            initializer {
                AddRemarkViewModel(container.viewModels(), commissionId, editRemarkId)
            }
        }
    }
}

/** Просмотр одного замечания: данные + фото. */
class RemarkDetailViewModel(
    private val container: AppContainer,
    remarkId: Long
) : ViewModel() {

    /** ImageLoader Coil из контейнера (для AsyncImage при просмотре фото). */
    val imageLoader: coil3.ImageLoader = container.imageLoader.imageLoader

    val remark = container.remarkRepository.observeById(remarkId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val photos = container.photoRepository.observeByRemark(remarkId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Удаление замечания вместе с фото; после — закрываем экран. */
    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            val current = remark.value ?: return@launch
            withContext(Dispatchers.IO) { container.deleteRemarkUseCase(current) }
            onDone()
        }
    }

    companion object {
        fun factory(container: AppContainer, remarkId: Long) = viewModelFactory {
            initializer { RemarkDetailViewModel(container, remarkId) }
        }
    }
}

/** Генерация PDF-отчёта. */
class ReportViewModel(
    private val container: AppContainer,
    @Suppress("unused") private val commissionId: Long
) : ViewModel() {

    sealed interface State {
        data object Idle : State
        data object Working : State

        /**
         * Отчёт готов: файл + растровые страницы для предпросмотра
         * (страницы есть только для [ReportFormat.PDF]).
         */
        data class Ready(
            val file: File,
            val format: ReportFormat,
            val pages: List<Bitmap> = emptyList()
        ) : State

        data object Error : State
    }

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _format = MutableStateFlow(ReportFormat.PDF)
    val format: StateFlow<ReportFormat> = _format.asStateFlow()

    val commission = container.commissionRepository.observeById(commissionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Смена формата сбрасывает уже сгенерированный отчёт. */
    fun setFormat(format: ReportFormat) {
        if (_format.value == format) return
        if (_state.value is State.Working) return
        _format.value = format
        recyclePages(_state.value)
        _state.value = State.Idle
    }

    fun generate() {
        if (_state.value is State.Working) return
        val previous = _state.value
        val format = _format.value
        _state.value = State.Working
        viewModelScope.launch {
            _state.value = try {
                val ready = withContext(Dispatchers.IO) {
                    container.generateReportUseCase(commissionId, format)?.let { file ->
                        val pages = if (format == ReportFormat.PDF) {
                            PdfPreviewRenderer.render(file)
                        } else {
                            emptyList()
                        }
                        State.Ready(file, format, pages)
                    }
                }
                recyclePages(previous)
                ready ?: State.Error
            } catch (e: Exception) {
                recyclePages(previous)
                State.Error
            }
        }
    }

    override fun onCleared() {
        recyclePages(_state.value)
    }

    /** Освобождает растры предыдущего предпросмотра. */
    private fun recyclePages(state: State) {
        (state as? State.Ready)?.pages?.forEach { if (!it.isRecycled) it.recycle() }
    }

    companion object {
        fun factory(container: AppContainer, commissionId: Long) = viewModelFactory {
            initializer { ReportViewModel(container.viewModels(), commissionId) }
        }
    }
}
