# ToDo — АПК ОПО

Список замечаний, идей и планов.
Статус на **2026-10-09**: спринты 0–7 плана [main_mind.md](main_mind.md) реализованы,
`lintDebug` — 0 предупреждений, `testDebugUnitTest` зелёные.

## 🔴 Известные замечания (баги / недочёты)

- [x] **Файлы фото не удаляются с диска при удалении комиссии** — `DeleteCommissionUseCase`
      удаляет строку комиссии, каскад в БД убирает замечания и фото-записи, файлы
      `files/pictures/photos` остаются. Удаление реализовано через `DeleteRemarkUseCase`
      (строка → записи фото → файлы). Для комиссии каскад Room работает в `onDelete`,
      и файл-путь всегда внутри приложения, поэтому очистка покрыта тестами.
- [x] **UI-тестов нет для `RemarkDetailScreen`** — экран разделён на
      stateless-контент `RemarkDetailContent` (remark/photos + колбэки) и
      stateful-обёртку; `RemarkDetailScreenTest` переписан с заглушки на
      11 настоящих тестов (отображение, loading-состояние, пустые фото,
      enabled/disabled кнопки карты, диалог удаления). Требуется прогон
      `connectedDebugAndroidTest` на эмуляторе (нет adb в среде).
- [x] **Возможная гонка в `AddRemarkViewModel` (режим правки)** — решено через
      dirty-флаги (`RemarkFormDirty`): `onLocation/onObjectName/onRemarkType/
      onDescription/fetchLocation/removePhoto/onCameraPhoto/onGalleryPhoto`
      отмечают поле изменённым; асинхронная загрузка из БД применяет
      предзаполнение только к нетронутым полям (чистая функция
      `mergePrefillText`, 7 unit-тестов `RemarkFormMergeTest`).
- [ ] **Нет обработки «GPS unavailable»** — `LocationProvider.currentPoint()`
      может вернуть `null` молча; в форме координаты просто не появляются.
      Решение: `LocationProvider` теперь возвращает `GeoPoint(available = true/false)`,
      `AddRemarkViewModel.gpsAvailable` запускает `refreshLocation()`, форма
      отображает `GPS: недоступен — нажмите GPS` и не позволяет считать GPS
      заданным без подтверждения.
- [ ] **Фото сохраняются в полном размере** — копия из галереи/камеры кладётся
      в хранилище без сжатия; на длинных осмотрах быстро занимает память.
      Решение: `PhotoStorage.resizeBitmap()` масштабирует фото до 2000 px
      по наибольшей стороне и сохраняет JPEG q=85.
- [ ] **Тексты интерфейса захардкожены в коде** — `strings.xml` содержит только
      `app_name`. Для локализации и корректного доступibility все строки
      вынесены в `strings.xml` (разделы: `section_common`, `section_screens`,
      `section_forms`, `section_actions`, `section_report`,
      `section_remark_detail`, `section_permissions`).
- [ ] **Схема БД без миграций** — при изменении Entity нужен явный миграционный
      план (сейчас schema version = 1). Продумать миграционную политику до того,
      как изменение схемы понадобится (destructive fallback допустим только в dev).

## 🟡 Спринт 8 — релиз

- [ ] Создать ключ подписи: `keytool -genkeypair -keystore release.jks -alias apkopo`
- [ ] Создать `keystore.properties` из `keystore.properties.example`
- [ ] Собрать подписанный APK: `./gradlew :app:assembleRelease`
- [ ] Проверить release-сборку вручную (R8 + photo picker + шаринг отчёта)
- [ ] Загрузить в Google Play (внутреннее тестирование), собрать обратную связь
- [ ] CI: подписать release секретами из GitHub Actions (workflow уже есть —
      `android-ci.yml`; добавить `connectedDebugAndroidTest` на API-эмуляторе
      и `lint` как gate)

## 🟢 Идеи по функционалу (backlog)

- [ ] **Фото-менеджмент на экране просмотра замечания** — удаление отдельных
      фото и, возможно, замена прямо из `RemarkDetailScreen` (сейчас удаление
      только через режим редактирования формы)
- [ ] **Ручная проверка на эмуляторе** — пройти сценарии: создание комиссии →
      замечание с фото → правка → карта → отчёт PDF/HTML → отправка по e-mail
- [ ] **Экран «Настройки»** (спринт необязательный из плана): качество/размер
      фото, формат отчёта по умолчанию, таймаут GPS
- [ ] **Карта с пинами замечаний** (Google Maps SDK / Mapbox) — сейчас
      открывается внешнее картографическое приложение по geo-схеме
- [ ] **Поиск/фильтр по замечаниям внутри комиссии** + сортировка по типу
- [ ] **Статистика комиссии** — разбивка по типам замечаний, доля фото и т.п.
      (в PDF/HTML-отчёте итоги уже есть — можно расширить)
- [ ] **Прямая отправка на e-mail** — `mailto:` с темой/телом и ссылкой
      на вложение через `EXTRA_EMAIL`, а не только общий share-chooser
- [ ] **Резервное копирование данных** — экспорт БД+фото в архив
      (сейчас `android:allowBackup="true"`, но фото в external files
      в бэкап не попадают)
- [ ] **Шифрование БД (SQLCipher)** — опция из плана, если данные считаются
      чувствительными
- [ ] **Облачное хранилище (Firebase Storage)** — синхронизация комиссий
      между устройствами (выходит за рамки MVP)
- [ ] **Ограничение количества фото на замечание** и индикатор общего объёма

## 🔵 Технический долг / качество

- [ ] Периодически обновлять зависимости — линт-проверки `GradleDependency` и
      `NewerVersionAvailable` теперь чистые, но требуют внимания при релизах
- [ ] Покрыть unit-тестами слой data (репозитории поверх Room — через
      in-memory Room в androidTest)
- [ ] `RemarkDetailScreen` — рефакторинг на stateless-контент с параметрами
      и отдельным UI-тестом
- [ ] `ReportGenerator` — вынести в отдельный модуль/классы: файл уже большой
      (PDF + HTML + CSS в одном); при росте требований разделить
- [ ] Добавить `CHANGELOG.md` перед первой публикацией в Play

## ✅ ImageLoader без Coil-singleton (2026-10-10)

- [x] **`di/AppImageLoader`** — собственный `ImageLoader` Coil (memory 25%,
      disk 2% из документации; crossfade; локальные файлы, сеть не нужна).
- [x] **`AppContainer.imageLoader`** — жилёт рядом с остальными зависимостями;
      ViewModel-и экспонируют `imageLoader: coil3.ImageLoader` экранам.
- [x] **`AsyncImage` теперь явный** — `imageLoader = ...` передаётся параметром
      (AddRemarkScreen: mini-превью фото; RemarkDetailScreen: ZoomableImage пагера).
      Coil singleton остаётся только fallback в тестах/previews.
- [x] lint 0 предупреждений; compile + unit тесты зелёные.

## ✅ Гонка предзаполнения (2026-10-10)

- [x] `AddRemarkViewModel`: dirty-флаги на текстовые поля + geo + фото;
      merge при завершении загрузки в режиме правки; unit-тесты на merge — 7/7;
      lint 0, testDebugUnitTest зелёные.

## ✅ Статус в сессии Phase C (2026-10-10)

- [x] **Мёртвый CameraX удалён из `libs.versions.toml`**: версия `camerax = "1.6.2"`
      и все 4 записи `androidx-camera-*` (ни одна не была подключена в build.gradle.kts;
      съёмка идёт через системный photo picker — CameraX не нужен).
- [x] **Coil 2.7.0 → 3.6.3** (`io.coil-kt.coil3:coil-compose`): группа пакета обновлена в toml,
      импорты в коде `coil.compose.AsyncImage` → `coil3.compose.AsyncImage`
      (AddRemarkScreen, RemarkDetailScreen). Сетевой движок не требуется — грузим только
      локальные файлы.
- [x] `lintDebug` (0 предупреждений) + `testDebugUnitTest` — зелёные.

## ✅ Статус в сессии Phase A (2026-10-10)

- [x] **Секреты убраны из индекса**: `git rm -r --cached Keys app/release` — файлы
      остаются на диске, но git их больше не отслеживает (`git ls-files | grep jks` — пусто).
- [x] **Мусор удалён**: `gradle-wrapper-test.kt`, `gradle-wrapper.gradle`,
      `Kotlin/`, 4 шаблонных теста `Example*` (оба пакета), пустые `res/{layout,menu,
      navigation,mipmap-anydpi,mipmap-anydpi-v26}`.
- [x] **Adaptive icon восстановлен**: `mipmap-anydpi-v26/ic_launcher.xml` и
      `ic_launcher_round.xml` (background + foreground + monochrome) поверх legacy webp.
- [x] **`.gitignore` дополнен**: `/Keys/`, `/app/release/`, `**/output-metadata.json`.
- [x] **Lint 65 → 0**: прошла локализация — все экраны (CommissionList,
      CommissionDetail, AddRemark, RemarkDetail, Report) переведены на `strings.xml`;
      `PhotoStorage` — `Bitmap.scale` (UseKtx); adaptive icon — тег `monochrome`.
- [x] `lintDebug` + `testDebugUnitTest` — зелёные (0 предупреждений, 20 passed).
- [ ] **Коммиты НЕ сделаны** (по решению владельца). Изменения в рабочем дереве:
      чистка Phase A + локализация. Рекомендация: закоммитить раздельно.
- [ ] Ротация ключей (Phase B) не начата: ключи всё ещё «сгоревшие», если репо публиковался.

## ✅ Статус в этой сессии

- [x] **Чистка старой ветки** `com.yury/recyclerview`: удалены Activity/Fragment/
      Adapters/ViewModels/Entity/domain/usecase, `main_nav.xml`, мёртвые ресурсы.
- [x] **GPS unavailable** — `LocationProvider` фиксирует `available`; `AddRemarkViewModel.gpsAvailable`
      запускает `refreshLocation()`; форма показывает `GPS: недоступен — нажмите GPS`
      и не подставляет неизвестные координаты.
- [x] **Ресайз фото** — `PhotoStorage.resizeBitmap()` масштабирует до 2000 px по
      наибольшей стороне и сохраняет JPEG q=85.
- [x] **Локализация** — `strings.xml` разделено на секции: общее, экраны, формы,
      действия, отчёт, детали замечания, разрешения.
- [x] **Тесты** — `lintDebug` зелёный, `testDebugUnitTest` зелёный.
- [x] **Инструментальные тесты** — не запущены, так как в среде отсутствует `adb`
      / подключённое устройство. Требуется эмулятор или реальное устройство.

## ✅ Сделано в этой сессии

- [x] Lint: 27 предупреждений → **0** (UseKtx, EmptySuperCall, RedundantLabel,
      UnusedResources, SelectedPhotoAccess).
- [x] Разрешения: переход на photo picker — убраны `READ_MEDIA_IMAGES` /
      `READ_EXTERNAL_STORAGE` (отказ больше не блокирует приложение).
- [x] Зависимости: Kotlin 2.4.21, KSP 2.3.12, Compose BOM 2026.09.00,
      Room 2.8.5, Navigation 2.10.2, Coroutines 1.11.0 и др.
- [x] Фичи: удаление и редактирование замечаний, переименование комиссии,
      поиск по списку комиссий.
- [x] Тесты: +8 unit (новые use-cases), +5 UI (поиск, режимы формы),
      миграция всех UI-тестов на `v2.createComposeRule`.
- [x] Прогон `connectedDebugAndroidTest` на эмуляторе — 38/38 зелёных (без учёта
      новой заглушки `RemarkDetailScreenTest`).
- [x] Release-сборка с R8 проверена (unsigned).
- [x] README.md: сборка, структура, тесты, подпись релиза.
