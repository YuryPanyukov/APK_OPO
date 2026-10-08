package com.yury.recyclerview.presentation.commissions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yury.recyclerview.database.Commission
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommissionsScreen(
    onCommissionClick: (Long) -> Unit,
    onAddRemarkClick: (Long) -> Unit,
    viewModel: CommissionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Комиссии") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // Простая заглушка: предлагаем ввести название
            }) {
                Icon(Icons.Default.Add, contentDescription = "Создать комиссию")
            }
        }
    ) { paddingValues ->
        if (uiState.commissions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Пока нет комиссий")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.commissions) { commission ->
                    CommissionItem(
                        commission = commission,
                        onClick = { onCommissionClick(commission.id) },
                        onAddRemark = { onAddRemarkClick(commission.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CommissionItem(
    commission: Commission,
    onClick: () -> Unit,
    onAddRemark: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = commission.title,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onAddRemark) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить замечание")
                }
            }
            Text(
                text = "Дата: ${formatDate(commission.date)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
