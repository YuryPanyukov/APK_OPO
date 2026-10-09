package code_sys.apkopo.util

import android.content.Context
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import code_sys.apkopo.domain.repository.PhotoFiles
import java.io.File
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/** Метаданные фото: путь + данные EXIF (координаты, время съёмки). */
data class PhotoMeta(
    val filePath: String,
    val lat: Double,
    val lng: Double,
    val time: Long
)

/**
 * Хранит фото во внутреннем хранилище приложения
 * (external files dir — разрешение на запись не требуется)
 * и извлекает из них EXIF-метаданные.
 */
class PhotoStorage(private val context: Context) : PhotoFiles {

    private val photosDir: File
        get() = File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES), "photos")
            .apply { mkdirs() }

    /**
     * Макс. длина одной стороны для хранимых фото.
     * При 2000px + JPEG q=85 считаем приемлимым для памяти на длинных осмотрах.
     */
    companion object {
        const val MAX_PHOTO_DIM = 2000
    }

    /**
     * Создаёт сжатую копию фото в памяти: макс. сторона [MAX_PHOTO_DIM] px,
     * JPEG quality 85. Возвращает null, если не удалось декодировать/сжать.
     */
    private fun resizeBitmap(source: File, maxDim: Int = MAX_PHOTO_DIM): Bitmap? {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(source.absolutePath, opts) ?: return null
        if (opts.outWidth <= 0 || opts.outHeight <= 0) return null

        var sample = 1
        while (opts.outWidth / sample > maxDim || opts.outHeight / sample > maxDim) sample *= 2

        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = BitmapFactory.decodeFile(source.absolutePath, decodeOpts) ?: return null
        if (bitmap.isRecycled) return null

        val scale = bitmap.width.toFloat() / maxDim.coerceAtLeast(1)
        val (w, h) = if (bitmap.width > bitmap.height) {
            bitmap.height * scale.toInt() to maxDim
        } else {
            maxDim to bitmap.width * scale.toInt()
        }

        val resized = Bitmap.createScaledBitmap(bitmap, w, h, true).also { recycled ->
            if (!recycled.isRecycled) recycled.recycle()
        }
        if (resized.isRecycled) return null
        return resized
    }

    /** Создаёт пустой файл для нового снимка (для камеры). */
    fun newCaptureFile(): File {
        photosDir.mkdirs()
        return File(photosDir, "img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg")
    }

    /** Копирует фото из галереи (Uri) в хранилище приложения и читает EXIF. */
    fun saveFromUri(uri: Uri, fallbackLat: Double, fallbackLng: Double): PhotoMeta? {
        val target = newCaptureFile()
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(target).use { output -> input.copyTo(output) }
            } ?: return null

            // Фото из галереи/камеры кладём сжатыми и в пределах устройства.
            val scaled = resizeBitmap(target) ?: return null
            val stream = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            scaled.recycle()
            target.writeBytes(stream.toByteArray())
            readMeta(target, fallbackLat, fallbackLng)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Удаляет файл фото. Удаляется только файл внутри каталога приложения —
     * случайная передача чужого пути ни к чему не приведёт.
     */
    override fun delete(path: String) {
        try {
            val target = File(path).canonicalFile
            val dir = photosDir.canonicalFile
            if (target.parentFile == dir) target.delete()
        } catch (_: Exception) {
            // нечего делать: отсутствующий файл или недоступный путь
        }
    }

    /** Читает EXIF уже сохранённого файла (снимок камеры). */
    fun readMeta(file: File, fallbackLat: Double, fallbackLng: Double): PhotoMeta {
        var lat = fallbackLat
        var lng = fallbackLng
        var time = file.lastModified()
        try {
            val exif = ExifInterface(file.absolutePath)
            val latLng = exif.getLatLong()
            if (latLng != null && latLng.size >= 2) {
                lat = latLng[0]
                lng = latLng[1]
            }
            val exifTime = parseExifTime(
                exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                    ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
            )
            if (exifTime != null) time = exifTime
        } catch (_: Exception) {
            // EXIF может отсутствовать — оставляем fallback-значения
        }
        return PhotoMeta(file.absolutePath, lat, lng, time)
    }

    private fun parseExifTime(value: String?): Long? {
        if (value.isNullOrBlank()) return null
        return try {
            SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US).parse(value)?.time
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        fun formatTime(millis: Long): String =
            SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date(millis))

        /** Uri для file-проваиера, через который камера пишет в наш файл. */
        fun uriFor(context: Context, file: File): android.net.Uri =
            androidx.core.content.FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                file
            )
    }
}
