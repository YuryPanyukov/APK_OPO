package code_sys.apkopo.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import code_sys.apkopo.data.local.entity.Commission
import code_sys.apkopo.ui.theme.APKOPOTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI-тесты экрана списка комиссий: пустое состояние, список,
 * FAB и диалог создания, подтверждение удаления.
 */
@RunWith(AndroidJUnit4::class)
class CommissionListScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setContent(
        commissions: List<Commission> = emptyList(),
        onCreate: (String) -> Unit = {},
        onDelete: (Commission) -> Unit = {},
        onClick: (Commission) -> Unit = {}
    ) {
        rule.setContent {
            APKOPOTheme {
                CommissionListScreen(
                    commissions = commissions,
                    onCreate = onCreate,
                    onDelete = onDelete,
                    onClick = onClick
                )
            }
        }
    }

    private fun commission(id: Long, title: String) =
        Commission(id = id, title = title, date = id * 1_000L)

    private fun openCreateDialog() =
        rule.onNodeWithContentDescription("Создать комиссию").performClick()

    // ---------- Пустое состояние ----------

    @Test
    fun empty_showsPlaceholder() {
        setContent(commissions = emptyList())

        rule.onNodeWithText("Нет комиссий", substring = true).assertIsDisplayed()
    }

    // ---------- Диалог создания ----------

    @Test
    fun fab_opensCreateDialog() {
        setContent()

        openCreateDialog()

        rule.onNodeWithText("Новая комиссия").assertIsDisplayed()
    }

    @Test
    fun createDialog_confirmDisabled_whileTitleBlank() {
        setContent()
        openCreateDialog()

        rule.onNodeWithText("Создать").assertIsNotEnabled()
    }

    @Test
    fun createDialog_confirmEnabledAfterInput_andInvokesOnCreate() {
        var created: String? = null
        setContent(onCreate = { created = it })
        openCreateDialog()

        rule.onNode(hasSetTextAction()).performTextInput("Комиссия А")
        rule.onNodeWithText("Создать").assertIsEnabled().performClick()

        assertEquals("Комиссия А", created)
        rule.onNodeWithText("Новая комиссия").assertDoesNotExist()
    }

    @Test
    fun createDialog_cancel_doesNotInvokeOnCreate() {
        var called = false
        setContent(onCreate = { called = true })
        openCreateDialog()
        rule.onNode(hasSetTextAction()).performTextInput("Комиссия А")

        rule.onNodeWithText("Отмена").performClick()

        assertFalse(called)
        rule.onNodeWithText("Новая комиссия").assertDoesNotExist()
    }

    // ---------- Список ----------

    @Test
    fun list_showsTitles() {
        setContent(
            commissions = listOf(commission(1, "Первая"), commission(2, "Вторая"))
        )

        rule.onNodeWithText("Первая").assertIsDisplayed()
        rule.onNodeWithText("Вторая").assertIsDisplayed()
    }

    @Test
    fun itemClick_invokesOnClickWithCommission() {
        val item = commission(1, "Первая")
        var clicked: Commission? = null
        setContent(commissions = listOf(item), onClick = { clicked = it })

        rule.onNodeWithText("Первая").performClick()

        assertEquals(item, clicked)
    }

    // ---------- Поиск ----------

    @Test
    fun search_filtersListByTitle() {
        setContent(
            commissions = listOf(commission(1, "Первая"), commission(2, "Вторая"))
        )

        rule.onNodeWithTag(CommissionListTags.SEARCH).performTextInput("Втора")

        rule.onNodeWithText("Вторая").assertIsDisplayed()
        rule.onNodeWithText("Первая").assertDoesNotExist()
    }

    @Test
    fun search_noMatch_showsEmptyMessage_andHidesList() {
        setContent(commissions = listOf(commission(1, "Первая")))

        rule.onNodeWithTag(CommissionListTags.SEARCH).performTextInput("Несуществующая")

        rule.onNodeWithText("Ничего не найдено", substring = true).assertIsDisplayed()
        rule.onNodeWithText("Первая").assertDoesNotExist()
    }

    @Test
    fun search_emptyQuery_showsAllItems() {
        setContent(
            commissions = listOf(commission(1, "Первая"), commission(2, "Вторая"))
        )

        rule.onNodeWithText("Первая").assertIsDisplayed()
        rule.onNodeWithText("Вторая").assertIsDisplayed()
    }

    // ---------- Удаление ----------

    @Test
    fun delete_opensConfirmDialog_andInvokesOnDelete() {
        val item = commission(1, "Первая")
        var deleted: Commission? = null
        setContent(commissions = listOf(item), onDelete = { deleted = it })

        rule.onNodeWithContentDescription("Удалить").performClick()
        rule.onNodeWithText("Удалить комиссию?").assertIsDisplayed()
        rule.onNodeWithText("Удалить").performClick()

        assertEquals(item, deleted)
    }

    @Test
    fun delete_cancel_doesNotInvokeOnDelete() {
        val item = commission(1, "Первая")
        var deleted: Commission? = null
        setContent(commissions = listOf(item), onDelete = { deleted = it })

        rule.onNodeWithContentDescription("Удалить").performClick()
        rule.onNodeWithText("Отмена").performClick()

        assertNull(deleted)
        rule.onNodeWithText("Удалить комиссию?").assertDoesNotExist()
    }
}
