package code_sys.apkopo.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import code_sys.apkopo.R
import code_sys.apkopo.data.local.entity.Photo
import code_sys.apkopo.data.local.entity.Remark
import code_sys.apkopo.ui.theme.APKOPOTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI-тесты stateless-экрана просмотра замечания.
 * Покрывают: отображение данных, пустые состояния, кнопку «Показать на карте»
 * (включая disabled без координат) и диалог удаления из контента.
 */
@RunWith(AndroidJUnit4::class)
class RemarkDetailScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val remark = Remark(
        id = 1,
        commissionId = 10,
        location = "Котельная №2",
        objectName = "Насос Н-3",
        remarkType = "Неисправность",
        description = "Течь по фланцу",
        remarkLat = 55.751,
        remarkLng = 37.618,
        remarkTime = 1_700_000_000_000L,
        userLat = 55.752,
        userLng = 37.619,
        userTime = 1_700_000_100_000L
    )

    private val photo = Photo(
        id = 1,
        remarkId = 1,
        filePath = "/tmp/img.jpg",
        photoLat = 55.751,
        photoLng = 37.618,
        photoTime = 1_700_000_050_000L
    )

    private fun setContent(
        remark: Remark? = this.remark,
        photos: List<Photo> = listOf(photo),
        showDeleteDialog: Boolean = false,
        onEdit: () -> Unit = {},
        onBack: () -> Unit = {},
        onRequestDelete: () -> Unit = {},
        onConfirmDelete: () -> Unit = {},
        onCancelDelete: () -> Unit = {}
    ) {
        rule.setContent {
            APKOPOTheme {
                RemarkDetailContent(
                    remark = remark,
                    photos = photos,
                    // В тестах фото не реально загружается (нет файла на диске):
                    // подходит синглтон-загрузчик Coil без сети.
                    imageLoader = coil3.SingletonImageLoader.get(androidx.compose.ui.platform.LocalContext.current),
                    onEdit = onEdit,
                    onBack = onBack,
                    showDeleteDialog = showDeleteDialog,
                    onRequestDelete = onRequestDelete,
                    onConfirmDelete = onConfirmDelete,
                    onCancelDelete = onCancelDelete
                )
            }
        }
    }

    // ---------- Отображение ----------

    @Test
    fun displaysRemarkInfo() {
        setContent()

        // Заголовок экрана и объект замечания
        rule.onNodeWithText("Замечание").assertIsDisplayed()
        rule.onNodeWithText("Насос Н-3").assertIsDisplayed()
        rule.onNodeWithText("Неисправность").assertIsDisplayed()

        // Карточка с деталями — прокрутка до «Место: Котельная №2»
        rule.onNodeWithText("Место: ", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
        rule.onNodeWithText("Котельная №2", substring = true).assertIsDisplayed()
        rule.onNodeWithText("Течь по фланцу", substring = true).assertIsDisplayed()
    }

    @Test
    fun nullRemark_showsLoading() {
        setContent(remark = null)

        rule.onNodeWithText("Загрузка…").assertIsDisplayed()
        rule.onNodeWithText("Насос Н-3").assertDoesNotExist()
    }

    @Test
    fun noPhotos_showsEmptyState() {
        setContent(photos = emptyList())

        rule.onNodeWithText("Фото не прикреплены").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun withPhotos_photoDescriptionsExist() {
        setContent()

        rule.onNodeWithContentDescription("Фото 1").assertExists()
    }

    // ---------- Кнопка «Показать на карте» ----------

    @Test
    fun mapButton_enabled_withCoords() {
        setContent()

        rule.onNodeWithTag(RemarkDetailTags.SHOW_MAP)
            .performScrollTo()
            .assertIsEnabled()
    }

    @Test
    fun mapButton_disabled_withoutCoords() {
        setContent(
            remark = remark.copy(remarkLat = 0.0, remarkLng = 0.0)
        )

        rule.onNodeWithTag(RemarkDetailTags.SHOW_MAP)
            .performScrollTo()
            .assertIsNotEnabled()
    }

    // ---------- Верхняя панель ----------

    @Test
    fun editButton_invokesCallback() {
        var edited = false
        setContent(onEdit = { edited = true })

        rule.onNodeWithTag(RemarkDetailTags.EDIT).performClick()

        assertTrue(edited)
    }

    @Test
    fun backButton_invokesCallback() {
        var backed = false
        setContent(onBack = { backed = true })

        rule.onNodeWithContentDescription("Назад").performClick()

        assertTrue(backed)
    }

    // ---------- Диалог удаления ----------

    @Test
    fun deleteButton_showsDialog_viaCallbackDriven() {
        // Контент stateless: кнопка DELETE только зовёт колбэк;
        // сам диалог включается параметром showDeleteDialog.
        var requested = false
        setContent(onRequestDelete = { requested = true })

        rule.onNodeWithTag(RemarkDetailTags.DELETE).performClick()

        assertTrue(requested)
    }

    @Test
    fun deleteDialog_visible_showsTitleAndButtons() {
        setContent(showDeleteDialog = true)

        rule.onNodeWithText("Удалить замечание?").assertIsDisplayed()
        rule.onNodeWithText("Замечание и все прикреплённые фото будут удалены.")
            .assertIsDisplayed()
    }

    @Test
    fun deleteDialog_confirm_invokesCallback() {
        var confirmed = false
        setContent(showDeleteDialog = true, onConfirmDelete = { confirmed = true })

        rule.onNodeWithTag(RemarkDetailTags.CONFIRM_DELETE).performClick()

        assertTrue(confirmed)
    }

    @Test
    fun deleteDialog_cancel_invokesCallback() {
        var cancelled = false
        setContent(showDeleteDialog = true, onCancelDelete = { cancelled = true })

        rule.onNodeWithTag(RemarkDetailTags.CANCEL_DELETE).performClick()

        assertTrue(cancelled)
    }
}
