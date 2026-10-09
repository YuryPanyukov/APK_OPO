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
import code_sys.apkopo.ui.viewmodel.RemarkDetailViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Compose UI-тесты экрана просмотра замечания: прелоуд, карточка,
 *  слайдер, карта, редактирование/удаление, состояния.
 */
@RunWith(AndroidJUnit4::class)
class RemarkDetailScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setContent(
        remark: code_sys.apkopo.data.local.entity.Remark = emptyRemark(),
        photos: List<code_sys.apkopo.util.PhotoMeta> = emptyList(),
        state: RemarkDetailViewModel.State = RemarkDetailViewModel.State.Ready,
        onEdit: () -> Unit = {},
        onBack: () -> Unit = {},
        onDelete: () -> Unit = {}
    ) {
        rule.setContent {
            APKOPOTheme {
                RemarkDetailScreen(
                    viewModel = viewModel(remark, photos),
                    onEdit = onEdit,
                    onBack = onBack
                )
            }
        }
    }

    private fun viewModel(remark: code_sys.apkopo.data.local.entity.Remark, photos: List<code_sys.apkopo.util.PhotoMeta>): RemarkDetailViewModel {
        val container = DummyContainer(remark, photos)
        return RemarkDetailViewModel(container, remark.id)
    }

    private fun emptyRemark(id: Long = 42L): code_sys.apkopo.data.local.entity.Remark = code_sys.apkopo.data.local.entity.Remark(
        id = id,
        commissionId = 1L,
        location = "Цех 3",
        objectName = "Труба",
        remarkType = "Нарушение",
        description = "Пlossаждения в шкале манометра",
        remarkLat = 55.7522,
        remarkLng = 37.6155,
        remarkTime = 1_700_000_000_000L,
        userLat = 55.7530,
        userLng = 37.6160,
        userTime = 1_700_000_000_100L
    )

    private fun emptyPhoto(meta: code_sys.apkopo.util.PhotoMeta = PhotoMeta("file.jpg", 0.0, 0.0, 1L)) = meta

    private fun DummyContainer(remark: code_sys.apkopo.data.local.entity.Remark, photos: List<code_sys.apkopo.util.PhotoMeta>): DummyContainer {
        return object : DummyContainer {
            override val remark = remark
            override val photos = photos
        }
    }

    private abstract class DummyContainer {
        abstract val remark: code_sys.apkopo.data.local.entity.Remark
        abstract val photos: List<code_sys.apkopo.util.PhotoMeta>
    }

    @Test
    fun loaded_showsObjectAndType() {
        setContent(remark = emptyRemark().copy(objectName = "Котёл №1"))

        rule.onNodeWithText("Котёл №1").assertIsDisplayed()
        rule.onNodeWithText("Нарушение").assertIsDisplayed()
        rule.onNodeWithText("Цех 3").assertIsDisplayed()
    }

    @Test
    fun photosSlider_showsCount() {
        val photos = listOf(
            emptyPhoto(PhotoMeta("a.jpg", 1.0, 2.0, 1_000L)),
            emptyPhoto(PhotoMeta("b.jpg", 3.0, 4.0, 2_000L))
        )

        setContent(photos = photos)

        rule.onNodeWithText("1 / 2").assertIsDisplayed()
        rule.onNodeWithText("2 / 2").assertIsDisplayed()
    }

    @Test
    fun emptyPhotos_showsFallback() {
        setContent(photos = emptyList())

        rule.onNodeWithText("Фото не прикреплены").assertIsDisplayed()
    }

    @Test
    fun showMapButton_enabled_whenCoords() {
        setContent(remark = emptyRemark().copy(remarkLat = 55.75, remarkLng = 37.61))

        rule.onNodeWithText("Показать на карте").assertIsDisplayed()
        rule.onNodeWithText("Показать на карте").performClick()
    }

    @Test
    fun showMapButton_disabled_whenNoCoords() {
        setContent(remark = emptyRemark().copy(remarkLat = 0.0, remarkLng = 0.0))

        rule.onNodeWithText("Показать на карте").assertIsDisplayed()
        rule.onNodeWithText("Показать на карте").assertIsNotEnabled()
    }

    @Test
    fun editButton_invokesOnEdit() {
        var edited = false
        setContent(onEdit = { edited = true })

        rule.onNodeWithContentDescription("Редактировать").performClick()

        assertTrue(edited)
    }

    @Test
    fun back_invokesOnBack() {
        var back = false
        setContent(onBack = { back = true })

        rule.onNodeWithContentDescription("Назад").performClick()

        assertTrue(back)
    }

    @Test
    fun deleteOpensDialog_andDeletes() {
        var deleted = false
        setContent(onDelete = { deleted = true })

        rule.onNodeWithContentDescription("Удалить").performClick()
        rule.onNodeWithText("Удалить замечание?").assertIsDisplayed()
        rule.onNodeWithText("Удалить", substring = true).performClick()

        assertTrue(deleted)
    }

    @Test
    fun photoMetadata_showsExif() {
        setContent(
            photos = listOf(
                emptyPhoto(PhotoMeta("p.jpg", 1.0, 2.0, 1_700_000_001_000L))
            )
        )

        rule.onNodeWithText("Съёмка: 28.09.2023 12:13:21").assertIsDisplayed()
        rule.onNodeWithText("GPS: 1.000000, 2.000000").assertIsDisplayed()
    }
}
