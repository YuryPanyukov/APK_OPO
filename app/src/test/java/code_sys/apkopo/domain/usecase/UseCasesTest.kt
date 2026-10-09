package code_sys.apkopo.domain.usecase

import code_sys.apkopo.data.local.entity.Commission
import code_sys.apkopo.data.local.entity.Photo
import code_sys.apkopo.data.local.entity.Remark
import code_sys.apkopo.domain.repository.ReportFormat
import code_sys.apkopo.util.PhotoMeta
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateCommissionUseCaseTest {

    private val repo = FakeCommissionRepository()
    private val useCase = CreateCommissionUseCase(repo)

    @Test
    fun blankTitle_isRejected() = runTest {
        val error = runCatching { useCase("   ") }.exceptionOrNull()

        assertTrue("ожидался IllegalArgumentException", error is IllegalArgumentException)
        assertTrue(repo.inserted.isEmpty())
    }

    @Test
    fun title_isTrimmed() = runTest {
        val id = useCase("  Проверка ОПО  ")

        assertEquals(1L, id)
        assertEquals("Проверка ОПО", repo.inserted.single().title)
    }

    @Test
    fun providedDate_isUsed() = runTest {
        useCase("Комиссия", date = 12345L)

        assertEquals(12345L, repo.inserted.single().date)
    }

    @Test
    fun date_defaultsToNow() = runTest {
        val before = System.currentTimeMillis()
        useCase("Комиссия")
        val after = System.currentTimeMillis()

        assertTrue(repo.inserted.single().date in before..after)
    }
}

class DeleteCommissionUseCaseTest {

    @Test
    fun delete_delegatesToRepository() = runTest {
        val repo = FakeCommissionRepository()
        val commission = Commission(id = 7L, title = "К", date = 1L)

        DeleteCommissionUseCase(repo)(commission)

        assertEquals(listOf(commission), repo.deletedItems)
    }
}

class UpdateCommissionUseCaseTest {

    private val repo = FakeCommissionRepository()
    private val useCase = UpdateCommissionUseCase(repo)

    @Test
    fun blankTitle_isRejected() = runTest {
        val commission = Commission(id = 1L, title = "  ", date = 1L)

        val error = runCatching { useCase(commission) }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertTrue(repo.updatedItems.isEmpty())
    }

    @Test
    fun title_isTrimmedAndUpdated() = runTest {
        val commission = Commission(id = 1L, title = " Старое название ", date = 1L)

        val saved = useCase(commission)

        assertEquals("Старое название", saved.title)
        assertEquals(saved, repo.updatedItems.single())
    }

    @Test
    fun update_changesStoredValue() = runTest {
        val id = repo.insert(Commission(title = "Старое", date = 1L))

        useCase(Commission(id = id, title = "Новое", date = 1L))

        assertEquals("Новое", repo.getById(id)?.title)
    }
}

class DeleteRemarkUseCaseTest {

    private val remarks = FakeRemarkRepository()
    private val photos = FakePhotoRepository()
    private val files = FakePhotoFiles()
    private val useCase = DeleteRemarkUseCase(remarks, photos, files)

    @Test
    fun remark_andItsPhotoFiles_areDeleted() = runTest {
        val remarkId = remarks.insert(
            Remark(
                commissionId = 1L,
                location = "L", objectName = "O", remarkType = "T", description = "D",
                remarkLat = 0.0, remarkLng = 0.0, remarkTime = 1L,
                userLat = 0.0, userLng = 0.0, userTime = 1L
            )
        )
        photos.insert(Photo(remarkId = remarkId, filePath = "/a.jpg", photoLat = 0.0, photoLng = 0.0, photoTime = 1L))
        photos.insert(Photo(remarkId = remarkId, filePath = "/b.jpg", photoLat = 0.0, photoLng = 0.0, photoTime = 2L))
        val remark = remarks.getById(remarkId)!!

        useCase(remark)

        assertEquals(listOf(remark), remarks.deletedItems)
        assertEquals(listOf("/a.jpg", "/b.jpg"), files.deleted)
        assertTrue(photos.getByRemark(remarkId).isEmpty())
    }

    @Test
    fun remarkWithoutPhotos_deletesNothingOnDisk() = runTest {
        val remarkId = remarks.insert(
            Remark(
                commissionId = 1L,
                location = "L", objectName = "O", remarkType = "T", description = "D",
                remarkLat = 0.0, remarkLng = 0.0, remarkTime = 1L,
                userLat = 0.0, userLng = 0.0, userTime = 1L
            )
        )

        useCase(remarks.getById(remarkId)!!)

        assertEquals(listOf(remarkId), remarks.deletedItems.map { it.id })
        assertTrue(files.deleted.isEmpty())
    }
}

class UpdateRemarkUseCaseTest {

    private val remarks = FakeRemarkRepository()
    private val photos = FakePhotoRepository()
    private val files = FakePhotoFiles()
    private val useCase = UpdateRemarkUseCase(remarks, photos, files)

    private fun remark(id: Long = 5L) = Remark(
        id = id,
        commissionId = 1L,
        location = "Старое место", objectName = "Старый объект",
        remarkType = "Замечание", description = "Старое описание",
        remarkLat = 55.75, remarkLng = 37.61, remarkTime = 111L,
        userLat = 55.76, userLng = 37.62, userTime = 222L
    )

    @Test
    fun fields_areTrimmedAndUpdated_geoAndTimePreserved() = runTest {
        val updated = remark().copy(
            location = "  Новое место  ",
            objectName = "  Новый объект  ",
            remarkType = " Предписание ",
            description = "  Новое описание  "
        )

        useCase(updated)

        val saved = remarks.updatedItems.single()
        assertEquals("Новое место", saved.location)
        assertEquals("Новый объект", saved.objectName)
        assertEquals("Предписание", saved.remarkType)
        assertEquals("Новое описание", saved.description)
        // Гео и время фиксируются при создании и при редактировании не меняются
        assertEquals(55.75, saved.remarkLat, 0.0)
        assertEquals(37.61, saved.remarkLng, 0.0)
        assertEquals(111L, saved.remarkTime)
        assertEquals(55.76, saved.userLat, 0.0)
        assertEquals(222L, saved.userTime)
    }

    @Test
    fun addedPhotos_areLinkedToRemark() = runTest {
        val added = listOf(
            PhotoMeta("/new.jpg", 1.0, 2.0, 30L)
        )

        useCase(remark(), addedPhotos = added)

        assertEquals(1, photos.inserted.size)
        val photo = photos.inserted.single()
        assertEquals(5L, photo.remarkId)
        assertEquals("/new.jpg", photo.filePath)
        assertEquals(1.0, photo.photoLat, 0.0)
        assertEquals(30L, photo.photoTime)
    }

    @Test
    fun removedPhotos_deleteRowAndFile() = runTest {
        photos.insert(Photo(remarkId = 5L, filePath = "/old.jpg", photoLat = 0.0, photoLng = 0.0, photoTime = 1L))

        useCase(remark(), removedPaths = listOf("/old.jpg"))

        assertEquals(listOf("/old.jpg"), photos.deletedPaths)
        assertEquals(listOf("/old.jpg"), files.deleted)
        assertTrue(photos.getByRemark(5L).isEmpty())
    }

    @Test
    fun unchangedPhotos_areNotTouched() = runTest {
        photos.insert(Photo(remarkId = 5L, filePath = "/keep.jpg", photoLat = 0.0, photoLng = 0.0, photoTime = 1L))

        useCase(remark())

        assertTrue(photos.deletedPaths.isEmpty())
        assertTrue(files.deleted.isEmpty())
        assertEquals(1, photos.getByRemark(5L).size)
    }
}

class AddRemarkUseCaseTest {

    private val remarks = FakeRemarkRepository()
    private val photos = FakePhotoRepository()
    private val useCase = AddRemarkUseCase(remarks, photos)

    @Test
    fun remark_isSavedWithGeoAndTime() = runTest {
        val id = useCase(
            commissionId = 42L,
            location = "  Котельная  ",
            objectName = "  Котёл №1  ",
            remarkType = " Неисправность ",
            description = "  Течь  ",
            userLat = 55.75,
            userLng = 37.61,
            userTime = 999L,
            photoMetas = emptyList()
        )

        val saved = remarks.inserted.single()
        assertEquals(id, saved.id)
        assertEquals(42L, saved.commissionId)
        assertEquals("Котельная", saved.location)
        assertEquals("Котёл №1", saved.objectName)
        assertEquals("Неисправность", saved.remarkType)
        assertEquals("Течь", saved.description)
        assertEquals(55.75, saved.remarkLat, 0.0)
        assertEquals(37.61, saved.remarkLng, 0.0)
        assertEquals(55.75, saved.userLat, 0.0)
        assertEquals(37.61, saved.userLng, 0.0)
        assertEquals(999L, saved.userTime)
    }

    @Test
    fun photos_areLinkedToRemark() = runTest {
        val metas = listOf(
            PhotoMeta(filePath = "/photos/a.jpg", lat = 1.0, lng = 2.0, time = 10L),
            PhotoMeta(filePath = "/photos/b.jpg", lat = 3.0, lng = 4.0, time = 20L)
        )

        val id = useCase(1L, "L", "O", "T", "D", 0.0, 0.0, 0L, metas)

        assertEquals(2, photos.inserted.size)
        photos.inserted.forEach { assertEquals(id, it.remarkId) }
        assertEquals("/photos/a.jpg", photos.inserted[0].filePath)
        assertEquals(1.0, photos.inserted[0].photoLat, 0.0)
        assertEquals(2.0, photos.inserted[0].photoLng, 0.0)
        assertEquals(10L, photos.inserted[0].photoTime)
    }
}

class GenerateReportUseCaseTest {

    private val commissions = FakeCommissionRepository()
    private val remarks = FakeRemarkRepository()
    private val photos = FakePhotoRepository()
    private val builder = FakeReportBuilder()
    private val useCase = GenerateReportUseCase(commissions, remarks, photos, builder)

    @Test
    fun unknownCommission_returnsNull() = runTest {
        val result = useCase(999L)

        assertNull(result)
        assertEquals(0, builder.callCount)
    }

    @Test
    fun defaultFormat_isPdf() = runTest {
        val commissionId = commissions.insert(Commission(title = "Ф", date = 1L))

        useCase(commissionId)

        assertEquals(ReportFormat.PDF, builder.lastFormat)
    }

    @Test
    fun htmlFormat_isPassedToBuilder() = runTest {
        val commissionId = commissions.insert(Commission(title = "Ф", date = 1L))

        useCase(commissionId, ReportFormat.HTML)

        assertEquals(ReportFormat.HTML, builder.lastFormat)
    }

    @Test
    fun report_isBuiltFromCommissionAndRemarksWithPhotos() = runTest {
        val commissionId = commissions.insert(Commission(title = "Итог", date = 1L))
        val remarkId = remarks.insert(
            Remark(
                commissionId = commissionId,
                location = "L", objectName = "O", remarkType = "T", description = "D",
                remarkLat = 1.0, remarkLng = 2.0, remarkTime = 3L,
                userLat = 4.0, userLng = 5.0, userTime = 6L
            )
        )
        photos.insert(
            Photo(
                remarkId = remarkId, filePath = "/p.jpg",
                photoLat = 7.0, photoLng = 8.0, photoTime = 9L
            )
        )

        val result = useCase(commissionId)

        assertNotNull(result)
        assertEquals(1, builder.callCount)
        assertEquals(commissionId, builder.lastCommission?.id)
        assertEquals(1, builder.lastRemarks.size)
        val built = builder.lastRemarks.single()
        assertEquals(remarkId, built.remark.id)
        assertEquals(1, built.photos.size)
        assertEquals("/p.jpg", built.photos.single().filePath)
    }
}
