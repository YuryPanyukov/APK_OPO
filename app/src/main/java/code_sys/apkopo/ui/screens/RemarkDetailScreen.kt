package code_sys.apkopo.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import code_sys.apkopo.ui.viewmodel.RemarkDetailViewModel
import code_sys.apkopo.util.PhotoStorage
import java.io.File
import java.util.Locale

/**
 * Экран просмотра замечания: слайдер фото (свайп, счётчик, зум по тапу),
 * подпись с EXIF-метаданными, информация о замечании и кнопка
 * «Показать на карте» (открывает внешнее картографическое приложение).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemarkDetailScreen(
    viewModel: RemarkDetailViewModel,
    onEdit: () -> Unit,
    onBack: () -> Unit
) {
    val remark by viewModel.remark.collectAsStateWithLifecycle()
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Замечание") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                    }
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить")
                    }
                }
            )
        }
    ) { padding ->
        val r = remark
        if (r == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { Text("Загрузка…") }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ---------- Слайдер фото ----------
            if (photos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Фото не прикреплены",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val pagerState = rememberPagerState(pageCount = { photos.size })
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .background(Color.Black)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        ZoomableImage(
                            model = File(photos[page].filePath),
                            contentDescription = "Фото ${page + 1}"
                        )
                    }

                    // Счётчик (например, 2 / 5)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(
                                Color.Black.copy(alpha = 0.55f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1} / ${photos.size}",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }

                    // Подпись текущего фото (EXIF: время + GPS)
                    val current = photos.getOrNull(pagerState.currentPage)
                    if (current != null) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.55f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Съёмка: ${PhotoStorage.formatTime(current.photoTime)}",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (current.photoLat == 0.0 && current.photoLng == 0.0) {
                                    "GPS: нет данных"
                                } else {
                                    "GPS: " + "%.6f, %.6f".format(
                                        Locale.US, current.photoLat, current.photoLng
                                    )
                                },
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // ---------- Информация о замечании ----------
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = r.objectName,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = r.remarkType,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(12.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow("Место", r.location)
                        DetailRow("Описание", r.description)
                        DetailRow(
                            "GPS замечания",
                            "${formatCoord(r.remarkLat, r.remarkLng)} • " +
                                PhotoStorage.formatTime(r.remarkTime)
                        )
                        DetailRow(
                            "GPS пользователя",
                            "${formatCoord(r.userLat, r.userLng)} • " +
                                PhotoStorage.formatTime(r.userTime)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                val hasCoords = r.remarkLat != 0.0 || r.remarkLng != 0.0
                FilledTonalButton(
                    onClick = {
                        if (!hasCoords) {
                            android.widget.Toast
                                .makeText(context, "Координаты неизвестны", android.widget.Toast.LENGTH_SHORT)
                                .show()
                            return@FilledTonalButton
                        }
                        openInExternalMap(context, r.remarkLat, r.remarkLng)
                    },
                    enabled = hasCoords,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Показать на карте")
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Удалить замечание?") },
            text = { Text("Замечание и все прикреплённые фото будут удалены.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete(onDone = onBack)
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Отмена") }
            }
        )
    }
}

/** Строка «label: value». */
@Composable
private fun DetailRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(140.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun formatCoord(lat: Double, lng: Double): String =
    "%.6f, %.6f".format(Locale.US, lat, lng)

/** Открывает точку во внешнем картографическом приложении (geo:-схема). */
private fun openInExternalMap(context: android.content.Context, lat: Double, lng: Double) {
    val label = "Замечание"
    val geoUri = "geo:$lat,$lng?q=$lat,$lng($label)".toUri()
    val geoIntent = Intent(Intent.ACTION_VIEW, geoUri)
    val webIntent = Intent(
        Intent.ACTION_VIEW,
        "https://www.google.com/maps/search/?api=1&query=$lat,$lng".toUri()
    )
    try {
        context.startActivity(geoIntent)
    } catch (e: ActivityNotFoundException) {
        try {
            context.startActivity(webIntent)
        } catch (e2: ActivityNotFoundException) {
            android.widget.Toast
                .makeText(context, "Нет приложения для карт", android.widget.Toast.LENGTH_SHORT)
                .show()
        }
    }
}

/**
 * Изображение с зумом: тап — 1x/2.5x, щипок — плавный зум,
 * перетаскивание — при зуме > 1.
 */
@Composable
private fun ZoomableImage(model: File, contentDescription: String?) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(1f, 6f)
                    scale = newScale
                    offset = if (newScale <= 1f) {
                        Offset.Zero
                    } else {
                        Offset(offset.x + pan.x, offset.y + pan.y)
                            .coerceIn(-1000f, 1000f)
                    }
                }
            }
            .clickable {
                if (scale > 1f) {
                    scale = 1f
                    offset = Offset.Zero
                } else {
                    scale = 2.5f
                }
            }
    )
}

private fun Offset.coerceIn(min: Float, max: Float): Offset =
    Offset(x.coerceIn(min, max), y.coerceIn(min, max))
