# АПК ОПО

Мобильное приложение для инспектора: фиксация замечаний при осмотре объектов
(опасных производственных объектов) с геолокацией и фото, хранение в локальной
базе данных и генерация отчёта (PDF/HTML) с отправкой по e-mail.

Детальный план разработки — в [main_mind.md](main_mind.md).

## Возможности

- **Комиссии** — создание, переименование, удаление, поиск по названию.
- **Замечания** — место, объект, тип, описание; привязка к комиссии;
  добавление, редактирование и удаление.
- **Фото** — снимок камеры или выбор из галереи (системный photo picker,
  разрешение на доступ к медиатеке не требуется); из EXIF извлекаются
  координаты и время съёмки.
- **Геолокация** — GPS пользователя в момент создания/редактирования замечания;
  кнопка «Показать на карте» открывает точку во внешнем картографическом приложении.
- **Просмотр замечания** — слайдер фото со свайпом, счётчиком и зумом,
  подпись с EXIF-метаданными.
- **Отчёт** — PDF (предпросмотр страниц) или самодостаточный HTML
  (фото встроены как base64); отправка по e-mail через системный шаринг.
- **Безопасность** — все данные хранятся только на устройстве (Room + приватное
  хранилище приложения).

## Стек

| Слой | Технологии |
|------|-----------|
| UI | Jetpack Compose (Material 3), Navigation Compose |
| Архитектура | MVVM + Clean Architecture (presentation / domain / data) |
| Данные | Room (KSP), Coroutines + Flow |
| Камера/фото | TakePicture + photo picker, ExifInterface, Coil |
| Гео | Play Services Location (FusedLocationProvider) |
| Отчёты | `android.graphics.pdf.PdfDocument`, `PdfRenderer` (предпросмотр) |
| DI | простой контейнер `AppContainer` (без Hilt) |
| Тесты | JUnit4 + kotlinx-coroutines-test, Compose UI Test |

## Структура

```
app/src/main/java/code_sys/apkopo/
├── MainActivity.kt          # точка входа, создание AppContainer
├── di/AppContainer.kt       # контейнер зависимостей
├── data/
│   ├── local/               # Room: AppDatabase, DAO, Entity
│   └── repository/          # реализации репозиториев
├── domain/
│   ├── repository/          # интерфейсы репозиториев + ReportBuilder
│   └── usecase/             # бизнес-логика (Create/Update/Delete, Report)
├── ui/
│   ├── AppNav.kt            # маршруты и NavHost
│   ├── screens/             # экраны (список, детали, форма, отчёт…)
│   ├── viewmodel/ViewModels.kt
│   └── theme/               # тема Material 3
└── util/                    # PhotoStorage, LocationProvider,
                             # ReportGenerator, PdfPreviewRenderer
```

## Требования

- JDK 17+ (используется toolchain-версия из `gradle/gradle-daemon-jvm.properties`)
- Android SDK (compileSdk 37, minSdk 24)
- Для UI-тестов — устройство или эмулятор

## Сборка

```bash
# Отладочный APK
./gradlew :app:assembleDebug

# Релиз (без keystore.properties собирается unsigned — для локальной проверки)
./gradlew :app:assembleRelease
```

### Подпись релиза

1. Создайте ключ (один раз):

   ```bash
   keytool -genkeypair -v -keystore release.jks -keyalg RSA \
     -keysize 2048 -validity 10000 -alias apkopo
   ```

2. Скопируйте `keystore.properties.example` в `keystore.properties`
   и заполните `storeFile`, `storePassword`, `keyAlias`, `keyPassword`.
   Файл и `*.jks` игнорируются git (см. `.gitignore`).

3. Соберите: `./gradlew :app:assembleRelease`.

### Версия из командной строки / CI

```bash
./gradlew :app:assembleRelease -PappVersionCode=2 -PappVersionName=1.1.0
```

## Тесты

```bash
# Unit-тесты (домен: use-cases + in-memory репозитории)
./gradlew :app:testDebugUnitTest

# Compose UI-тесты — требуют устройство/эмулятор
./gradlew :app:connectedDebugAndroidTest

# Линт (в проекте — 0 предупреждений)
./gradlew :app:lintDebug
```

Последний прогон: **21 unit-тест** и **38 UI-тестов** — все зелёные,
`lintDebug` — 0 ошибок и 0 предупреждений.

## Разрешения

| Разрешение | Зачем |
|------------|-------|
| `CAMERA` | съёмка фото замечания |
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | координаты пользователя |

Фото выбирается через системный photo picker — `READ_MEDIA_IMAGES` /
`READ_EXTERNAL_STORAGE` не нужны. Все разрешения запрашиваются на экране
`PermissionGate` при первом запуске; до выдачи основной UI не открывается.

## Статус по плану

Спринты 0–7 плана реализованы полностью (см. [main_mind.md](main_mind.md)),
спринт 8 (релиз) подготовлен: конфиг подписи и версионирование на месте —
осталось создать ключ и загрузить в Google Play.
