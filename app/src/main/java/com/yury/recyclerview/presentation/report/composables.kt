package com.yury.recyclerview.presentation.report

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import android.content.Intent
import com.yury.recyclerview.domain.usecase.GenerateReportArgs
import com.yury.recyclerview.domain.usecase.GenerateReportUseCase
import com.yury.recyclerview.domain.usecase.ReportFormat
import dagger.hilt.android.entryPoint
import java.io.File
import kotlinx.coroutines.launch
import kotlinx.coroutines.MainScope

@Composable
fun ReportScreen(
    commissionId: Long,
    generateReportUseCase: GenerateReportUseCase = hiltEntryPoint.reportScreenUseCase()
) {
    val context = LocalContext.current
    var reportFile by remember { mutableStateOf<File?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Отчёт по комиссии $commissionId", fontSize = 20.sp)

        Button(onClick = {
            loading = true
            MainScope().launch {
                try {
                    reportFile = generateReportUseCase(
                        GenerateReportArgs(
                            commissionId = commissionId,
                            format = ReportFormat.PDF
                        )
                    )
                } finally {
                    loading = false
                }
            }
        }) {
            Text("Сгенерировать PDF")
        }

        if (reportFile != null) {
            Button(onClick = {
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    reportFile!!
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Отправить отчёт"))
            }) {
                Text("Отправить по email")
            }
        }
    }
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface ReportScreenEntryPoint {
    fun reportScreenUseCase(): GenerateReportUseCase
}

fun hiltEntryPoint(): ReportScreenEntryPoint =
    dagger.hilt.android.EntryPointAccessors.fromApplication(
        LocalContext.current.applicationContext,
        ReportScreenEntryPoint::class.java
    )
