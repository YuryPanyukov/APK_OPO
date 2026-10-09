package code_sys.apkopo.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import code_sys.apkopo.ui.viewmodel.AddRemarkViewModel
import code_sys.apkopo.util.GeoPoint
import code_sys.apkopo.util.PhotoMeta
import code_sys.apkopo.util.PhotoStorage
import coil.compose.AsyncImage
import java.io.File

/** Теги элементов формы замечания (используются в UI-тестах). */
object AddRemarkTags {
    const val LOCATION = "remark_field_location"
    const val OBJECT = "remark_field_object"
    const val SAVE = "remark_save_button"
}

/** Состоятельная обёртка: связывает [AddRemarkViewModel] со stateless-контентом. */
@Composable
fun AddRemarkScreen(
    viewModel: AddRemarkViewModel,
    onBack: () -> Unit
) {
    val location by viewModel.location.collectAsStateWithLifecycle()
    val objectName by viewModel.objectName.collectAsStateWithLifecycle()
    val remarkType by viewModel.remarkType.collectAsStateWithLifecycle()
    val description by viewModel.description.collectAsStateWithLifecycle()
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val geo by viewModel.geo.collectAsStateWithLifecycle()
    val saving by viewModel.saving.collectAsStateWithLifecycle()
    val gpsAvailable by viewModel.gpsAvailable.collectAsStateWithLifecycle()

    var saved by rememberSaveable { mutableStateOf(false) }

    // Запрашиваем геолокацию при открытии экрана.
    // В режиме редактирования не затираем сохранённые координаты — только по кнопке GPS.
    LaunchedEffect(Unit) { if (!viewModel.isEdit) viewModel.fetchLocation() }
    LaunchedEffect(saved) { if (saved) onBack() }

    // Если GPS отсутствует — принудительно запрашиваем (повторная попытка).
    LaunchedEffect(viewModel.gpsAvailable) {
        if (!viewModel.gpsAvailable.value) viewModel.refreshLocation()
    }

    AddRemarkContent(
        title = if (viewModel.isEdit) "Редактирование замечания" else "Новое замечание",
        saveLabel = if (viewModel.isEdit) "Сохранить изменения" else "Сохранить замечание",
        location = location,
        objectName = objectName,
        remarkType = remarkType,
        description = description,
        photos = photos,
        geo = geo,
        saving = saving,
        onLocation = viewModel::onLocation,
        onObjectName = viewModel::onObjectName,
        onRemarkType = viewModel::onRemarkType,
        onDescription = viewModel::onDescription,
        onRemovePhoto = viewModel::removePhoto,
        onFetchLocation = viewModel::fetchLocation,
        onNewCameraFile = viewModel::newCameraFile,
        onCameraPhoto = viewModel::onCameraPhoto,
        onGalleryPhoto = viewModel::onGalleryPhoto,
        onSave = { viewModel.save(onDone = { saved = true }) },
        onBack = onBack
    )
}

/**
 * Форма нового замечания: поля, геолокация, фото из камеры/галереи.
 * Полностью stateless — всё состояние приходит сверху.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRemarkContent(
    title: String = "Новое замечание",
    saveLabel: String = "Сохранить замечание",
    location: String,
    objectName: String,
    remarkType: String,
    description: String,
    photos: List<PhotoMeta>,
    geo: GeoPoint?,
    saving: Boolean,
    onLocation: (String) -> Unit,
    onObjectName: (String) -> Unit,
    onRemarkType: (String) -> Unit,
    onDescription: (String) -> Unit,
    onRemovePhoto: (PhotoMeta) -> Unit,
    onFetchLocation: () -> Unit,
    onNewCameraFile: () -> File,
    onCameraPhoto: (File) -> Unit,
    onGalleryPhoto: (Uri) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    remarkTypes: List<String> = AddRemarkViewModel.REMARK_TYPES
) {
    var typeExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Файл для снимка камеры
    val cameraFile = remember { mutableStateOf<File?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraFile.value?.let(onCameraPhoto)
        }
        cameraFile.value = null
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) onGalleryPhoto(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = location,
                onValueChange = onLocation,
                label = { Text("Место") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(AddRemarkTags.LOCATION)
            )

            OutlinedTextField(
                value = objectName,
                onValueChange = onObjectName,
                label = { Text("Объект") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(AddRemarkTags.OBJECT)
            )

            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = it }
            ) {
                OutlinedTextField(
                    value = remarkType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Тип замечания") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    remarkTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                onRemarkType(type)
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = onDescription,
                label = { Text("Описание") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            // Геоположение
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onFetchLocation) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("GPS")
                }
                val gpsStatus = if (gpsAvailable) {
                    "GPS: недоступен — нажмите GPS"
                } else {
                    geo?.let {
                        "%.6f, %.6f".format(java.util.Locale.US, it.lat, it.lng)
                    } ?: "GPS: получено, но координаты не заданы"
                }
                Text(
                    text = gpsStatus,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (gpsAvailable) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            // Фото
            Text("Фото", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    val file = onNewCameraFile()
                    cameraFile.value = file
                    cameraLauncher.launch(PhotoStorage.uriFor(context, file))
                }) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Камера")
                }
                OutlinedButton(onClick = {
                    val request = androidx.activity.result.PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        .build()
                    galleryLauncher.launch(request)
                }) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Галерея")
                }
            }

            if (photos.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(photos) { meta ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AsyncImage(
                                model = File(meta.filePath),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(96.dp)
                                    .clickable { onRemovePhoto(meta) }
                            )
                            IconButton(onClick = { onRemovePhoto(meta) }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Убрать",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                PhotoStorage.formatTime(meta.time),
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            val canSave = objectName.isNotBlank() && location.isNotBlank() && !saving
            Button(
                onClick = onSave,
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(AddRemarkTags.SAVE)
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(saveLabel)
                }
            }

            if (objectName.isBlank() || location.isBlank()) {
                Text(
                    "Заполните «Место» и «Объект», чтобы сохранить.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
