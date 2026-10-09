package code_sys.apkopo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import code_sys.apkopo.data.local.entity.Commission
import code_sys.apkopo.util.PhotoStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Теги элементов списка комиссий (используются в UI-тестах). */
object CommissionListTags {
    const val SEARCH = "commission_search_field"
}

/** Список всех комиссий + создание/удаление/поиск. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommissionListScreen(
    commissions: List<Commission>,
    onCreate: (String) -> Unit,
    onDelete: (Commission) -> Unit,
    onClick: (Commission) -> Unit
) {
    var showCreate by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Commission?>(null) }
    var query by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Комиссии") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Default.Add, contentDescription = "Создать комиссию")
            }
        }
    ) { padding ->
        if (commissions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Нет комиссий.\nНажмите +, чтобы создать первую.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            val filtered = if (query.isBlank()) {
                commissions
            } else {
                commissions.filter { it.title.contains(query.trim(), ignoreCase = true) }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Поиск по названию") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag(CommissionListTags.SEARCH)
                )

                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ничего не найдено по запросу «$query»",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered, key = { it.id }) { commission ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onClick(commission) }
                            ) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier
                                        .padding(16.dp)
                                        .padding(end = 48.dp)
                                    ) {
                                        Text(
                                            text = commission.title,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = PhotoStorage.formatTime(commission.date),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { toDelete = commission },
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .padding(8.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Удалить")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        CreateCommissionDialog(
            onDismiss = { showCreate = false },
            onConfirm = { title ->
                onCreate(title)
                showCreate = false
            }
        )
    }

    toDelete?.let { commission ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Удалить комиссию?") },
            text = {
                Text("«${commission.title}» и все её замечания будут удалены.")
            },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(commission)
                    toDelete = null
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun CreateCommissionDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var title by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая комиссия") },
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
            ) { Text("Создать") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
