package code_sys.apkopo.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import code_sys.apkopo.R
import code_sys.apkopo.domain.repository.ReportFormat
import code_sys.apkopo.ui.viewmodel.ReportViewModel
import java.io.File

/** Экран генерации, предпросмотра и отправки отчёта (PDF или HTML). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    commissionTitle: String,
    state: ReportViewModel.State,
    format: ReportFormat,
    onFormatChange: (ReportFormat) -> Unit,
    onGenerate: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.report_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.button_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                stringResource(R.string.report_format_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
            )
            FormatSelector(
                selected = format,
                enabled = state !is ReportViewModel.State.Working,
                onSelect = onFormatChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when (val s = state) {
                is ReportViewModel.State.Ready -> ReportReadyContent(
                    commissionTitle = commissionTitle,
                    state = s,
                    onGenerate = onGenerate,
                    modifier = Modifier.fillMaxSize()
                )

                else -> CenteredStatus(
                    commissionTitle = commissionTitle,
                    state = s,
                    onGenerate = onGenerate,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                )
            }
        }
    }
}

/** Тег кнопки выбора формата отчёта (используется в UI-тестах). */
fun formatTag(format: ReportFormat): String = "report_format_${format.name}"

/** Переключатель формата PDF / HTML. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormatSelector(
    selected: ReportFormat,
    enabled: Boolean,
    onSelect: (ReportFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = ReportFormat.entries
    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth()
    ) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == selected,
                enabled = enabled,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                modifier = Modifier.testTag(formatTag(option)),
                label = { Text(option.name) }
            )
        }
    }
}

/** Предпросмотр страниц PDF (или пояснение для HTML) + кнопки действий. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportReadyContent(
    commissionTitle: String,
    state: ReportViewModel.State.Ready,
    onGenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val shareSubject = stringResource(R.string.report_commission_label, commissionTitle)
    val shareChooserTitle = stringResource(R.string.report_share_title)
    val noOpenAppText = stringResource(R.string.report_no_open_app)

    fun shareIntent(file: File): Intent {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = state.format.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, shareSubject)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun openIntent(file: File): Intent {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, state.format.mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(stringResource(R.string.report_commission_label, commissionTitle), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.report_format_info, state.format.name, state.file.name) +
                    if (state.format == ReportFormat.PDF) stringResource(R.string.report_pages_count, state.pages.size) else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (state.format == ReportFormat.HTML) {
            item {
                Text(
                    stringResource(R.string.report_html_ready),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else if (state.pages.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.report_no_preview),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            itemsIndexed(state.pages) { index, page ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Image(
                            bitmap = page.asImageBitmap(),
                            contentDescription = stringResource(R.string.content_page, index + 1),
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            stringResource(R.string.report_page_counter, index + 1, state.pages.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    context.startActivity(
                        Intent.createChooser(shareIntent(state.file), shareChooserTitle)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.button_send_report))
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    try {
                        context.startActivity(openIntent(state.file))
                    } catch (e: ActivityNotFoundException) {
                        android.widget.Toast
                            .makeText(context, noOpenAppText, android.widget.Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(
                    if (state.format == ReportFormat.HTML) stringResource(R.string.button_open_in_browser)
                    else stringResource(R.string.button_open_report)
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onGenerate, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.button_regenerate))
            }
        }
    }
}

/** Idle / Working / Error — по центру экрана. */
@Composable
private fun CenteredStatus(
    commissionTitle: String,
    state: ReportViewModel.State,
    onGenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.report_commission_label, commissionTitle),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(16.dp))

        when (state) {
            is ReportViewModel.State.Idle -> {
                Text(
                    stringResource(R.string.report_empty),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = onGenerate, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.report_generate))
                }
            }

            is ReportViewModel.State.Working -> {
                CircularProgressIndicator(modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.report_generating), style = MaterialTheme.typography.bodyMedium)
            }

            is ReportViewModel.State.Error -> {
                Text(
                    stringResource(R.string.report_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = onGenerate, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.button_retry))
                }
            }

            is ReportViewModel.State.Ready -> {
                // Сюда не попадаем: Ready обрабатывается в ReportReadyContent.
                Box(modifier = Modifier.size(0.dp))
            }
        }
    }
}
