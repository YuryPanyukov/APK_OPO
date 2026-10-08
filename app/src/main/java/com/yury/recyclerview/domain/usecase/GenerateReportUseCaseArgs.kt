package com.yury.recyclerview.domain.usecase

data class GenerateReportArgs(
    val commissionId: Long,
    val format: ReportFormat = ReportFormat.PDF
)

enum class ReportFormat {
    PDF,
    HTML
}
