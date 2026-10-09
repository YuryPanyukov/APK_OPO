package code_sys.apkopo.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

data class GeoPoint(val lat: Double, val lng: Double, val time: Long, val available: Boolean = false)

/**
 * Обёртка над FusedLocationProviderClient: получение текущего
 * геоположения в suspend-функции с таймаутом.
 */
class LocationProvider(context: Context) {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission") // разрешение запрашивается на экране до вызова
    suspend fun currentPoint(timeoutMs: Long = 10_000L): GeoPoint? {
        val fresh = withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine<GeoPoint?> { cont ->
                client.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                ).addOnSuccessListener { location ->
                    if (cont.isActive) cont.resume(location?.toPoint(true))
                }.addOnFailureListener {
                    if (cont.isActive) cont.resume(null)
                }
            }
        }
        if (fresh != null) return fresh

        // Фолбэк: последнее известное положение
        return suspendCancellableCoroutine<GeoPoint?> { cont ->
            try {
                client.lastLocation.addOnSuccessListener { location ->
                    if (cont.isActive) cont.resume(location?.toPoint(true))
                }.addOnFailureListener {
                    if (cont.isActive) cont.resume(null)
                }
            } catch (e: SecurityException) {
                cont.resume(null)
            }
        }
    }

    private fun Location.toPoint(available: Boolean = false): GeoPoint = GeoPoint(latitude, longitude, time, available)
}
