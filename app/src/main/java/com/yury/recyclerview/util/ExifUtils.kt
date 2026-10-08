package com.yury.recyclerview.util

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Утилита для работы с фото:
 * - копирование Uri во внутреннее хранилище приложения,
 * - чтение GPS-координат и времени съёмки из EXIF (если доступно).
 */
object ExifUtils {

    data class PhotoExif(
        val path: String,
        val lat: Double,
        val lng: Double,
        val time: Long
    )

    fun extractExifInfo(context: Context, uri: Uri): PhotoExif {
        val destFile = copyPhotoToInternalStorage(context, uri)
        val lat = readExifLatitude(destFile)
        val lng = readExifLongitude(destFile)
        val time = readExifDateTime(destFile)
        return PhotoExif(
            path = destFile.absolutePath,
            lat = lat,
            lng = lng,
            time = time
        )
    }

    private fun copyPhotoToInternalStorage(context: Context, uri: Uri): File {
        val destDir = File(context.filesDir, "photos")
        if (!destDir.exists()) destDir.mkdirs()
        val destFile = File(destDir, "photo_${System.currentTimeMillis()}.jpg")

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }

        return destFile
    }

    private fun readExifLatitude(file: File): Double {
        return try {
            val exif = ExifInterface(file.absolutePath)
            convertRationalToDecimal(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE), exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF))
        } catch (e: Exception) {
            0.0
        }
    }

    private fun readExifLongitude(file: File): Double {
        return try {
            val exif = ExifInterface(file.absolutePath)
            convertRationalToDecimal(exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE), exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF))
        } catch (e: Exception) {
            0.0
        }
    }

    private fun readExifDateTime(file: File): Long {
        return try {
            val exif = ExifInterface(file.absolutePath)
            val dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME) ?: return System.currentTimeMillis()
            // EXIF дата обычно в виде "YYYY:MM:DD HH:MM:SS"
            val parsed = java.text.SimpleDateFormat("yyyy:MM:dd HH:mm:ss", java.util.Locale.US).parse(dateTime)
            parsed?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun convertRationalToDecimal(geo: String?, ref: String?): Double {
        if (geo == null || ref == null) return 0.0
        val parts = geo.split(",")
        if (parts.size != 3) return 0.0
        val degrees = parseRational(parts[0])
        val minutes = parseRational(parts[1])
        val seconds = parseRational(parts[2])
        var result = degrees + minutes / 60.0 + seconds / 3600.0
        if (ref == "S" || ref == "W") result = -result
        return result
    }

    private fun parseRational(rational: String): Double {
        val trimmed = rational.trim()
        return if (trimmed.contains("/")) {
            val parts = trimmed.split("/")
            if (parts.size != 2) 0.0 else parts[0].toDoubleOrNull()?.div(parts[1].toDoubleOrNull() ?: 1.0) ?: 0.0
        } else {
            trimmed.toDoubleOrNull() ?: 0.0
        }
    }
}
