package code_sys.apkopo.ui.screens

import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import code_sys.apkopo.domain.repository.ReportFormat
import code_sys.apkopo.ui.theme.APKOPOTheme
import code_sys.apkopo.ui.viewmodel.ReportViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Compose UI-тесты экрана отчёта: выбор формата, генерация,
 * состояния Idle / Working / Error / Ready (PDF и HTML).
 */
@RunWith(AndroidJUnit4::class)
class ReportScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setContent(
        state: ReportViewModel.State = ReportViewModel.State.Idle,
        format: ReportFormat = ReportFormat.PDF,
        onFormatChange: (ReportFormat) -> Unit = {},
        onGenerate: () -> Unit = {},
        onBack: () -> Unit = {}
    ) {
        rule.setContent {
            APKOPOTheme {
                ReportScreen(
                    commissionTitle = "Комиссия №1",
                    state = state,
                    format = format,
                    onFormatChange = onFormatChange,
                    onGenerate = onGenerate,
                    onBack = onBack
                )
            }
        }
    }

    // ---------- Idle / генерация ----------

    @Test
    fun idle_showsGenerateButton_andInvokesCallback() {
        var generated = false
        setContent(onGenerate = { generated = true })

        rule.onNodeWithText("Сгенерировать отчёт").assertIsDisplayed().performClick()

        assertTrue(generated)
    }

    // ---------- Выбор формата ----------

    @Test
    fun format_defaultsToPdfSelected() {
        setContent(format = ReportFormat.PDF)

        rule.onNodeWithTag(formatTag(ReportFormat.PDF)).assertIsSelected()
        rule.onNodeWithTag(formatTag(ReportFormat.HTML)).assertIsNotSelected()
    }

    @Test
    fun format_htmlSelected_whenParameterIsHtml() {
        setContent(format = ReportFormat.HTML)

        rule.onNodeWithTag(formatTag(ReportFormat.HTML)).assertIsSelected()
        rule.onNodeWithTag(formatTag(ReportFormat.PDF)).assertIsNotSelected()
    }

    @Test
    fun format_clickHtml_invokesCallbackWithHtml() {
        var selected: ReportFormat? = null
        setContent(onFormatChange = { selected = it })

        rule.onNodeWithTag(formatTag(ReportFormat.HTML)).performClick()

        assertEquals(ReportFormat.HTML, selected)
    }

    @Test
    fun format_disabled_whileWorking() {
        setContent(state = ReportViewModel.State.Working)

        rule.onNodeWithTag(formatTag(ReportFormat.PDF)).assertIsNotEnabled()
        rule.onNodeWithTag(formatTag(ReportFormat.HTML)).assertIsNotEnabled()
    }

    // ---------- Working / Error ----------

    @Test
    fun working_showsProgressText() {
        setContent(state = ReportViewModel.State.Working)

        rule.onNodeWithText("Генерация отчёта…").assertIsDisplayed()
    }

    @Test
    fun error_showsRetry_andInvokesGenerate() {
        var generated = false
        setContent(
            state = ReportViewModel.State.Error,
            onGenerate = { generated = true }
        )

        rule.onNodeWithText("Повторить").assertIsDisplayed().performClick()

        assertTrue(generated)
    }

    // ---------- Ready: PDF ----------

    @Test
    fun readyPdf_showsPageCounter_andActionButtons() {
        val page = Bitmap.createBitmap(40, 60, Bitmap.Config.ARGB_8888)
        val state = ReportViewModel.State.Ready(
            file = File("report_1.pdf"),
            format = ReportFormat.PDF,
            pages = listOf(page)
        )
        setContent(state = state, format = ReportFormat.PDF)

        rule.onNodeWithText("Страница 1 из 1").assertIsDisplayed()
        rule.onNodeWithText("Отправить по e-mail").assertIsDisplayed()
        // Кнопка ниже в списке — проверяем наличие (может быть за нижней границей экрана).
        rule.onNodeWithText("Перегенерировать").assertExists()
    }

    @Test
    fun readyPdf_withoutPages_showsFallbackMessage() {
        val state = ReportViewModel.State.Ready(
            file = File("report_1.pdf"),
            format = ReportFormat.PDF,
            pages = emptyList()
        )
        setContent(state = state, format = ReportFormat.PDF)

        rule.onNodeWithText("Предпросмотр недоступен, но файл готов к отправке.")
            .assertIsDisplayed()
    }

    // ---------- Ready: HTML ----------

    @Test
    fun readyHtml_showsOpenInBrowser() {
        val state = ReportViewModel.State.Ready(
            file = File("report_1.html"),
            format = ReportFormat.HTML,
            pages = emptyList()
        )
        setContent(state = state, format = ReportFormat.HTML)

        rule.onNodeWithText("Открыть в браузере").assertIsDisplayed()
        rule.onNodeWithText("Отправить по e-mail").assertIsDisplayed()
    }

    // ---------- Навигация ----------

    @Test
    fun back_invokesCallback() {
        var back = false
        setContent(onBack = { back = true })

        rule.onNodeWithContentDescription("Назад").performClick()

        assertTrue(back)
    }
}
