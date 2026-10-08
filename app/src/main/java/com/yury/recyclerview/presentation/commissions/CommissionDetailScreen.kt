package com.yury.recyclerview.presentation.commissions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommissionDetailScreen(
    commissionId: Long,
    onBack: () -> Unit,
    onAddRemark: () -> Unit,
    onReport: () -> Unit,
    viewModel: CommissionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(commissionId) {
        viewModel.loadCommission(commissionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.commission?.title ?: "Комиссия") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onReport) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Отчёт")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRemark) {
                Icon(Icons.Default.Add, contentDescription = "Добавить замечание")
            }
        }
    ) { paddingValues ->
        if (uiState.remarks.isEmpty() && !uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Пока нет замечаний")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.remarks) { remark ->
                    RemarkItem(remark = remark)
                }
            }
        }
    }
}

@Composable
fun RemarkItem(remark: Remark) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Замечание #${remark.id}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Место: ${remark.location}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Объект: ${remark.objectName}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Тип: ${remark.remarkType}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Описание: ${remark.description}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "GPS замечания: (${remark.remarkLat}, ${remark.remarkLng})",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "GPS пользователя: (${remark.userLat}, ${remark.userLng})",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Время: ${formatDate(remark.remarkTime)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
