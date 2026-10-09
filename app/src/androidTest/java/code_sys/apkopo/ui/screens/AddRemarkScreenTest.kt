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
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import code_sys.apkopo.ui.theme.APKOPOTheme
import code_sys.apkopo.util.GeoPoint
import code_sys.apkopo.util.PhotoMeta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Compose UI-тесты формы добавления замечания.
 * Основное внимание — валидации полей «Место» и «Объект».
 */
@RunWith(AndroidJUnit4::class)
class AddRemarkScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val hintText = "Заполните «Место» и «Объект», чтобы сохранить."

    private fun setContent(
        title: String = "Новое замечание",
        saveLabel: String = "Сохранить замечание",
        location: String = "",
        objectName: String = "",
        remarkType: String = "Замечание",
        description: String = "",
        photos: List<PhotoMeta> = emptyList(),
        geo: GeoPoint? = null,
        saving: Boolean = false,
        onLocation: (String) -> Unit = {},
        onObjectName: (String) -> Unit = {},
        onFetchLocation: () -> Unit = {},
        onSave: () -> Unit = {},
        onBack: () -> Unit = {}
    ) {
        rule.setContent {
            APKOPOTheme {
                AddRemarkContent(
                    title = title,
                    saveLabel = saveLabel,
                    location = location,
                    objectName = objectName,
                    remarkType = remarkType,
                    description = description,
                    photos = photos,
                    geo = geo,
                    saving = saving,
                    onLocation = onLocation,
                    onObjectName = onObjectName,
                    onRemarkType = {},
                    onDescription = {},
                    onRemovePhoto = {},
                    onFetchLocation = onFetchLocation,
                    onNewCameraFile = { File("test.jpg") },
                    onCameraPhoto = {},
                    onGalleryPhoto = {},
                    onSave = onSave,
                    onBack = onBack
                )
            }
        }
    }

    // ---------- Валидация ----------

    @Test
    fun emptyFields_saveDisabled_andHintShown() {
        setContent()

        rule.onNodeWithTag(AddRemarkTags.SAVE).assertIsNotEnabled()
        rule.onNodeWithText(hintText).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun onlyLocation_saveDisabled_andHintShown() {
        setContent(location = "Котельная")

        rule.onNodeWithTag(AddRemarkTags.SAVE).assertIsNotEnabled()
        rule.onNodeWithText(hintText).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun onlyObject_saveDisabled_andHintShown() {
        setContent(objectName = "Котёл №1")

        rule.onNodeWithTag(AddRemarkTags.SAVE).assertIsNotEnabled()
        rule.onNodeWithText(hintText).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun whitespaceOnlyFields_saveDisabled() {
        setContent(location = "   ", objectName = "\t ")

        rule.onNodeWithTag(AddRemarkTags.SAVE).assertIsNotEnabled()
    }

    @Test
    fun bothFieldsFilled_saveEnabled_andHintHidden() {
        setContent(location = "Котельная", objectName = "Котёл №1")

        rule.onNodeWithTag(AddRemarkTags.SAVE).performScrollTo().assertIsEnabled()
        rule.onNodeWithText(hintText).assertDoesNotExist()
    }

    @Test
    fun whileSaving_saveDisabled() {
        setContent(location = "Котельная", objectName = "Котёл №1", saving = true)

        rule.onNodeWithTag(AddRemarkTags.SAVE).assertIsNotEnabled()
    }

    @Test
    fun clickingSave_whenValid_invokesCallback() {
        var saved = false
        setContent(
            location = "Котельная",
            objectName = "Котёл №1",
            onSave = { saved = true }
        )

        rule.onNodeWithTag(AddRemarkTags.SAVE).performScrollTo().performClick()

        assertTrue(saved)
    }

    // ---------- Ввод ----------

    @Test
    fun typingIntoLocation_invokesCallback() {
        var value: String? = null
        setContent(onLocation = { value = it })

        rule.onNodeWithTag(AddRemarkTags.LOCATION).performTextInput("Цех 3")

        assertEquals("Цех 3", value)
    }

    @Test
    fun typingIntoObject_invokesCallback() {
        var value: String? = null
        setContent(onObjectName = { value = it })

        rule.onNodeWithTag(AddRemarkTags.OBJECT).performTextInput("Насос")

        assertEquals("Насос", value)
    }

    // ---------- Прочее ----------

    @Test
    fun gpsButton_invokesFetchLocation() {
        var fetched = false
        setContent(onFetchLocation = { fetched = true })

        rule.onNodeWithText("GPS").performScrollTo().performClick()

        assertTrue(fetched)
    }

    @Test
    fun photoButtons_arePresent() {
        setContent()

        rule.onNodeWithText("Камера").assertExists()
        rule.onNodeWithText("Галерея").assertExists()
    }

    @Test
    fun back_invokesCallback() {
        var back = false
        setContent(onBack = { back = true })

        rule.onNodeWithContentDescription("Назад").performClick()

        assertTrue(back)
    }

    // ---------- Режим редактирования ----------

    @Test
    fun editMode_customTitleAndSaveLabel_areShown() {
        setContent(
            title = "Редактирование замечания",
            saveLabel = "Сохранить изменения",
            location = "Котельная",
            objectName = "Котёл №1"
        )

        rule.onNodeWithText("Редактирование замечания").assertIsDisplayed()
        rule.onNodeWithText("Сохранить изменения").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun createMode_defaultTitleAndSaveLabel_areShown() {
        setContent(location = "Котельная", objectName = "Котёл №1")

        rule.onNodeWithText("Новое замечание").assertIsDisplayed()
        rule.onNodeWithText("Сохранить замечание").performScrollTo().assertIsDisplayed()
    }
}
