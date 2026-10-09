package code_sys.apkopo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import code_sys.apkopo.data.local.entity.Commission
import code_sys.apkopo.data.local.entity.Remark
import code_sys.apkopo.util.PhotoStorage

/** Детали комиссии: карточка + список замечаний + действия. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommissionDetailScreen(
    commission: Commission?,
    remarks: List<Remark>,
    onBack: () -> Unit,
    onAddRemark: () -> Unit,
    onReport: () -> Unit,
    onOpenRemark: (Remark) -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }
    var confirmRename by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(commission?.title ?: "Комиссия") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { confirmRename = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Переименовать")
                    }
                    IconButton(onClick = onReport) {
                        Icon(Icons.Default.Share, contentDescription = "Отчёт")
                    }
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddRemark,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Замечание") }
            )
        }
    ) { padding ->
        if (commission == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { Text("Загрузка…") }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Дата создания", style = MaterialTheme.typography.labelMedium)
                        Text(
                            PhotoStorage.formatTime(commission.date),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "Замечаний: ${remarks.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            if (remarks.isEmpty()) {
                item {
                    Text(
                        "Замечаний пока нет. Нажмите «Замечание», чтобы добавить.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            } else {
                itemsIndexed(remarks, key = { _, r -> r.id }) { index, remark ->
                    RemarkCard(
                        index = index,
                        remark = remark,
                        onClick = { onOpenRemark(remark) }
                    )
                }
                item { HorizontalDivider() }
                item {
                    Text(
                        "Для просмотра отчёта нажмите кнопку отправки в панели сверху.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (confirmRename) {
        RenameCommissionDialog(
            initialTitle = commission?.title.orEmpty(),
            onDismiss = { confirmRename = false },
            onConfirm = { title ->
                confirmRename = false
                onRename(title)
            }
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Удалить комиссию?") },
            text = { Text("Все замечания и фото будут удалены.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun RenameCommissionDialog(
    initialTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Переименовать комиссию") },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (title.isNotBlank()) onConfirm(title) },
                enabled = title.isNotBlank()
            ) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
private fun RemarkCard(index: Int, remark: Remark, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "${index + 1}. ${remark.objectName}",
                style = MaterialTheme.typography.titleSmall
            )
            InfoRow("Место", remark.location)
            InfoRow("Тип", remark.remarkType)
            InfoRow("Описание", remark.description)
            InfoRow(
                "GPS",
                "%.6f, %.6f".format(java.util.Locale.US, remark.remarkLat, remark.remarkLng)
            )
            InfoRow("Время", PhotoStorage.formatTime(remark.remarkTime))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(modifier = Modifier.padding(top = 4.dp)) {
        Text(
            "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}
