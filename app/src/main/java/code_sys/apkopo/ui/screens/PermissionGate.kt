package code_sys.apkopo.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

/** Все разрешения, необходимые приложению. */
fun requiredPermissions(): Array<String> = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

/**
 * Экран запроса разрешений: пока не выданы все — основной UI не открывается.
 * При отказе показываем пояснение и кнопку повторного запроса.
 */
@Composable
fun PermissionGate(onAllGranted: () -> Unit) {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            requiredPermissions().all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        granted = result.values.all { it }
        // переход выполняется в LaunchedEffect(granted)
    }

    LaunchedEffect(granted) {
        if (granted) onAllGranted()
    }

    DisposableEffect(Unit) {
        if (!granted) launcher.launch(requiredPermissions())
        onDispose { }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "АПК ОПО",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (granted) {
                "Все разрешения получены"
            } else {
                "Для работы приложения нужны разрешения: камера и геолокация. Фото выбираются через системный photo picker — отдельное разрешение не нужно."
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        if (granted) {
            // автопереход выполняется в LaunchedEffect(granted)
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
        } else {
            Button(onClick = {
                launcher.launch(requiredPermissions())
            }) {
                Text("Предоставить разрешения")
            }
        }
    }
}
