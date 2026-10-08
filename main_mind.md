**План разработки Android‑приложения «АПК ОПО» (Kotlin, MVVM + Clean Architecture)**
*Дата подготовки: 2026‑10‑08 (UTC)*

---

## 1. Краткое описание
Приложение позволяет инспектору:

1. **Создавать комиссию** (название + дата).
2. **Фиксировать замечания** внутри комиссии:
   - место (локация) + объект + тип замечания + описание;
   - прикрепление нескольких фотографий;
   - автоматическое сохранение GPS‑координат и времени съёмки из EXIF‑метаданных;
   - сохранение текущего геоположения пользователя при создании замечания.
3. **Генерировать отчёт** (PDF/HTML) со всеми данными комиссии и замечаний и отправлять его по e‑mail.

Требуемые разрешения: `ACCESS_FINE_LOCATION`, `CAMERA`, `READ_EXTERNAL_STORAGE`/`READ_MEDIA_IMAGES` (Android 13+), `WRITE_EXTERNAL_STORAGE` (если нужно сохранять файлы в внешнее хранилище).

---

## 2. Архитектура

```
┌─────────────────────┐
│   Presentation      │   ← Activity/Fragment, ViewModel, UI (Jetpack Compose или XML)
│   (MVVM)            │
└─────────┬───────────┘
          │
┌─────────▼───────────┐
│   Domain (UseCases) │   ← Чистая бизнес‑логика, интерфейсы репозиториев
└─────────┬───────────┘
          │
┌─────────▼───────────┐
│   Data Layer        │   ← Room DB, Network (если понадобится),
│   (Repositories)    │      Utils для камеры, галереи, EXIF, геолокации
└─────────────────────┘
```

* **Presentation** – Jetpack Compose (рекомендуется) либо классический XML + ViewBinding.
* **Domain** – независимые `UseCase`‑классы (например, `CreateCommissionUseCase`, `AddRemarkUseCase`, `GenerateReportUseCase`).
* **Data** – реализация репозиториев через **Room** (локальная БД) и **CameraX**/**MediaStore** для фото.

---

## 3. Модели данных (Room Entities)

```kotlin
@Entity(tableName = "commissions")
data class Commission(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val date: Long,                     // timestamp в миллисекундах
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "remarks",
    foreignKeys = [
        ForeignKey(
            entity = Commission::class,
            parentColumns = ["id"],
            childColumns = ["commissionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Remark(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val commissionId: Long,
    val location: String,               // свободное описание места
    val objectName: String,
    val remarkType: String,
    val description: String,
    val remarkLat: Double,              // GPS где замечание зафиксировано
    val remarkLng: Double,
    val remarkTime: Long,               // время создания замечания
    val userLat: Double,                // GPS пользователя в момент создания
    val userLng: Double,
    val userTime: Long
)

@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = Remark::class,
            parentColumns = ["id"],
            childColumns = ["remarkId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Photo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remarkId: Long,
    val filePath: String,               // путь к файлу в внутреннем хранилище
    val photoLat: Double,               // из EXIF
    val photoLng: Double,
    val photoTime: Long                 // timestamp из EXIF
)
```

*Все `Long`‑поля с временем хранят Unix‑millis, что удобно для сортировки и отображения.*

---

## 4. Основные UseCases (пример)

```kotlin
// CreateCommissionUseCase.kt
class CreateCommissionUseCase(
    private val commissionDao: CommissionDao
) {
    suspend fun invoke(title: String): Long {
        val commission = Commission(title = title, date = System.currentTimeMillis())
        return commissionDao.insert(commission)
    }
}

// AddRemarkUseCase.kt
class AddRemarkUseCase(
    private val remarkDao: RemarkDao,
    private val photoDao: PhotoDao
) {
    suspend fun invoke(
        commissionId: Long,
        location: String,
        objectName: String,
        remarkType: String,
        description: String,
        remarkLat: Double,
        remarkLng: Double,
        userLat: Double,
        userLng: Double,
        photos: List<Uri>   // Uri из галереи/камеры
    ): Long {
        val remark = Remark(
            commissionId = commissionId,
            location = location,
            objectName = objectName,
            remarkType = remarkType,
            description = description,
            remarkLat = remarkLat,
            remarkLng = remarkLng,
            remarkTime = System.currentTimeMillis(),
            userLat = userLat,
            userLng = userLng,
            userTime = System.currentTimeMillis()
        )
        val remarkId = remarkDao.insert(remark)

        // Сохраняем фото и извлекаем EXIF
        photos.forEach { uri ->
            val (path, lat, lng, time) = extractExif(uri)   // реализация в Utils
            photoDao.insert(
                Photo(
                    remarkId = remarkId,
                    filePath = path,
                    photoLat = lat,
                    photoLng = lng,
                    photoTime = time
                )
            )
        }
        return remarkId
    }
}

// GenerateReportUseCase.kt
class GenerateReportUseCase(
    private val commissionDao: CommissionDao,
    private val remarkDao: RemarkDao,
    private val photoDao: PhotoDao
) {
    suspend fun invoke(commissionId: Long): File {
        val commission = commissionDao.getById(commissionId)!!
        val remarks = remarkDao.getByCommissionId(commissionId)
        val reportFile = File.createTempFile("report_${commission.id}", ".pdf", context.cacheDir)

        // Простейший PDF‑генератор через Android PdfDocument
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(
            reportFile.width, reportFile.height, 1
        ).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        // Заголовок
        val paint = Paint().apply {
            textSize = 24f
            isAntiAlias = true
        }
        canvas.drawText("Комиссия: ${commission.title}", 50f, 100f, paint)
        canvas.drawText("Дата: ${Date(commission.date)}", 50f, 150f, paint)

        var y = 200f
        remarks.forEach { remark ->
            canvas.drawText("Замечание #${remark.id}", 50f, y, paint)
            y += 30f
            canvas.drawText("  Локация: ${remark.location}", 50f, y, paint)
            y += 25f
            canvas.drawText("  Объект: ${remark.objectName}", 50f, y, paint)
            y += 25f
            canvas.drawText("  Тип: ${remark.remarkType}", 50f, y, paint)
            y += 25f
            canvas.drawText("  Описание: ${remark.description}", 50f, y, paint)
            y += 25f
            canvas.drawText("  GPS замечания: (${remark.remarkLat}, ${remark.remarkLng})", 50f, y, paint)
            y += 25f
            canvas.drawText("  GPS пользователя: (${remark.userLat}, ${remark.userLng})", 50f, y, paint)
            y += 35f

            // Фото (если есть)
            val photos = photoDao.getByRemarkId(remark.id)
            photos.forEach { photo ->
                // Вставляем миниатюру (упрощённо)
                val bitmap = BitmapFactory.decodeFile(photo.filePath)
                canvas.drawBitmap(bitmap, 50f, y, null)
                y += bitmap.height + 10f
                canvas.drawText(
                    "Снимок сделан: ${Date(photo.photoTime)} (${photo.photoLat}, ${photo.photoLng})",
                    50f,
                    y + bitmap.height + 5f,
                    paint
                )
                y += bitmap.height + 30f
            }
            y += 20f   // отступ между замечаниями
        }

        pdf.finishPage(page)
        pdf.writeTo(FileOutputStream(reportFile))
        pdf.close()

        return reportFile
    }
}
```

*Код выше демонстрирует ключевые логики; в реальном проекте вынесем работу с Bitmap в отдельный `ImageUtils`, а генерацию PDF – в dedicated `ReportGenerator` (можно использовать библиотеку **iText** или **PdfiumAndroid**).*

---

## 5. UI‑поток (экраны)

| Экран | Описание | Основные компоненты |
|-------|----------|---------------------|
| **SplashScreen** | Логотип + проверка разрешений | `Compose` + `ActivityResultContracts.RequestMultiplePermissions` |
| **CommissionList** | Список всех комиссий (название, дата) | `LazyColumn`, кнопка **+** → `CreateCommissionDialog` |
| **CreateCommissionDialog** | Ввод названия, автоматическая дата | `TextField`, `Button` |
| **CommissionDetail** | Просмотр комиссии, список замечаний, кнопка **+ Замечание** | `LazyColumn` remarок, `FloatingActionButton` |
| **AddRemarkScreen** | Форма замечания + галерея/камера | Поля: Место, Объект, Тип (Spinner), Описание; кнопка **Прикрепить фото** → `ActivityResultContracts.GetContent` / `TakePicture`; предпросмотр выбранных фото |
| **RemarkDetailScreen** (опционально) | Просмотр конкретного замечания со всеми фото на карте (можно использовать `Google Maps SDK` или `Mapbox`) | `ImageSlider` (Coil/Glide), карта с пинами |
| **ReportScreen** | Предпросмотр отчёта (PDF preview via `PdfRenderer` или WebView) + кнопка **Отправить по e‑mail** | `Intent.ACTION_SEND` с `EXTRA_STREAM` (Uri к файлу) |
| **Settings** (необязательно) | Настройка таймаутов GPS, качества фото, выбор формата отчёта (PDF/HTML) | `Switch`, `SeekBar` |

**Навигация** – Jetpack Navigation Component с `NavHost` и безопасными аргументами (Safe Args).

---

## 6. Разрешения и работа с аппаратными возможностями

| Разрешение | Где используется | Как запрашиваем |
|------------|------------------|-----------------|
| `ACCESS_FINE_LOCATION` | Получение текущих координат пользователя при создании замечания и при съёмке фото (если хотим сохранятьLocation из фото) | `ActivityResultContracts.RequestPermission` |
| `CAMERA` | Снимок через `CameraX` (Preview + ImageCapture) | `ActivityResultContracts.RequestPermission` |
| `READ_MEDIA_IMAGES` (Android 13+) / `READ_EXTERNAL_STORAGE` (старше) | Доступ к галерее для выбора существующих фото | `ActivityResultContracts.RequestPermission` |
| `WRITE_EXTERNAL_STORAGE` (если сохраняем в внешнюю папку) | Сохранение временных файлов отчёта |同上 |

После получения всех нужных разрешений инициализируем `FusedLocationProviderClient` (для пользовательского GPS) и `CameraX` (для съёмки).

---

## 7. Хранение фото

* Сохраняем изображение в **internal storage** (`context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)`), чтобы не требовалось дополнительное разрешение на запись во внешнее хранилище.
* Путь к файлу сохраняем в таблице `photos`.
* При необходимости можно загружать фото в облако (Firebase Storage) – но это выходит за рамки базового MVP.

---

## 8. Генерация отчёта

### 8.1 Формат

* **PDF** – удобно для печати и отправки; легко встраивать текст, таблицы и изображения.
* **HTML** (альтернатива) – можно открыть в любом почтовом клиенте; проще стилизовать через CSS.

**Рекомендуемый подход:** генерировать **PDF** основным, а также предоставлять опцию экспорта в HTML (через `StringBuilder` + шаблон Thymeleaf или просто ручная разметка).

### 8.2 Что включаем в отчёт

1. **Шапка** – название комиссии, дата создания, идентификатор.
2. **Таблица замечаний** – каждое замечание как строка:
   - ID, Локация, Объект, Тип, Описание,
   - GPS замечания (широта, долгота),
   - GPS пользователя,
   - Время создания замечания.
3. **Фото** – для каждого замечания выводятся все прикреплённые изображения с подписью: дата/время съёмки и GPS‑координаты из EXIF.
4. **Итого** – общее количество замечаний, список уникальных типов, etc.

### 8.3 Техническая реализация (PDF)

* Используем `android.graphics.pdf.PdfDocument` (встроено в API) для простых документов.
* Для более сложной вёрстки (таблицы, переносы) можно подключить **PdfiumAndroid** или **iText 7** (через Gradle).

Пример записи изображения в PDF (упрощённый):

```kotlin
val bitmap = BitmapFactory.decodeFile(photo.filePath)
val width = bitmap.width
val height = bitmap.height
canvas.drawBitmap(bitmap, left, top, null)
```

---

## 9. Технологический стек (Gradle зависимости)

```gradle
dependencies {
    // Kotlin & AndroidX
    implementation "org.jetbrains.kotlin:kotlin-stdlib:1.9.0"
    implementation "androidx.core:core-ktx:1.13.0"
    implementation "androidx.appcompat:appcompat:1.7.0"
    implementation "com.google.android.material:material:1.12.0"
    implementation "androidx.constraintlayout:constraintlayout:2.1.4"
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.8.0"
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0"
    implementation "androidx.activity:activity-compose:1.8.0"
    implementation "androidx.navigation:navigation-compose:2.7.7"

    // Room
    implementation "androidx.room:room-runtime:2.6.1"
    implementation "androidx.room:room-ktx:2.6.1"
    kapt "androidx.room:room-compiler:2.6.1"

    // CameraX
    def camerax_version = "1.3.0"
    implementation "androidx.camera:camera-core:$camerax_version"
    implementation "androidx.camera:camera-camera2:$camerax_version"
    implementation "androidx.camera:camera-lifecycle:$camerax_version"
    implementation "androidx.camera:camera-view:$camerax_version"
    implementation "androidx.camera:camera-extensions:$camerax_version"

    // Location
    implementation "com.google.android.gms:play-services-location:21.3.0"

    // Image loading (Coil)
    implementation "io.coil-kt:coil-compose:2.6.0"

    // PDF (PdfDocument – built‑in, optional iText)
    // implementation "com.itextpdf:itext7-core:7.2.5"

    // Coroutines
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0"

    // Testing
    testImplementation "junit:junit:4.13.2"
    androidTestImplementation "androidx.junit.ext:junit:1.1.5"
    androidTestImplementation "androidx.test.espresso:espresso-core:3.5.1"
}
```

---

## 10. План реализации (спринты)

| Спринт | Цель | Основные задачи |
|--------|------|-----------------|
| **0** | Настройка проекта | Создать модуль app, настроить Gradle, добавить зависимости, настроить `navigation-compose`, базовый `Theme`. |
| **1** | Логин/разрешения + Splash | Экран приветствия, запрос всех нужных разрешений, проверка GPS. |
| **2** | Работа с комиссиями | CRUD комиссии (Room + ViewModel), список комиссий, диалог создания. |
| **3** | Снимки и галерея | Интеграция CameraX + выбор из галереи, сохранение фото, чтение EXIF (библиотека `exifinterface`). |
| **4** | Замечания | Форма добавления замечания, привязка к комиссии, сохранение remark + фото, отображение списка замечаний в детали комиссии. |
| **5** | Карта и предпросмотр фото | Показ фото в замечании, опционально показать точку на карте (Google Maps SDK). |
| **6** | Генерация отчёта | PDF‑генератор, предпросмотр, отправка по e‑mail (`Intent.ACTION_SEND`). |
| **7** | Улучшения UI/тестирование | Дизайн refinements, unit-тесты для UseCases и Repository, UI‑тесты с Espresso Compose. |
| **8** | Релиз | Подготовка подписанного APK, загрузка в Google Play (внутреннее тестирование), сбор обратной связи. |

---

## 11. Обеспечение конфиденциальности и безопасности

* Все персональные данные (координаты, фото) хранятся **только на устройстве** в приватной папке приложения.
* При отправке отчёта по e‑mail пользователь явно выбирает приложение‑клиент и подтверждает отправку.
* При необходимости можно добавить опцию **шифрования** базы Room (библиотека `SQLCipher`).

---

## 12. Вывод

План покрывает все заявленные функции: создание комиссии, фиксацию замечаний с геоданными и фото, хранение в локальной БД, генерацию структурированного отчёта и отправку его по e‑mail.
Архитектура построена на современных Android‑компонентах (Jetpack Compose, Navigation, Room, CameraX, Coroutines), что обеспечивает тестируемость, масштабируемость и лёгкую поддержку будущих расходов (например, экспорт в облачное хранилище или интеграция с системой управления ОПО).

Если потребуется детализация конкретного модуля (например, работа с EXIF или настройка PDF‑генератора), дайте знать — подготовлю отдельные фрагменты кода и рекомендации по библиотекам.

---

*Документ подготовлен 2026‑10‑08 (UTC).*