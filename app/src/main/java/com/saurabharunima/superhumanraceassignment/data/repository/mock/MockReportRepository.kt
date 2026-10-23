package com.saurabharunima.superhumanraceassignment.data.repository.mock

import com.saurabharunima.superhumanraceassignment.core.NETWORK_DELAY_MS
import com.saurabharunima.superhumanraceassignment.core.suspendRunCatching
import com.saurabharunima.superhumanraceassignment.data.model.Report
import com.saurabharunima.superhumanraceassignment.data.model.ReportPeriod
import com.saurabharunima.superhumanraceassignment.data.remote.MockDataSource
import com.saurabharunima.superhumanraceassignment.data.remote.toDomain
import com.saurabharunima.superhumanraceassignment.data.repository.ReportRepository
import com.saurabharunima.superhumanraceassignment.domain.ReportBuilder
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class MockReportRepository @Inject constructor(
    private val source: MockDataSource,
    private val clock: Clock,
) : ReportRepository {

    override suspend fun getReport(period: ReportPeriod): Result<Report> = suspendRunCatching {
        delay(NETWORK_DELAY_MS)
        val today = LocalDate.now(clock)
        val logs = source.load().dailyLogs.map { it.toDomain(today) }
        ReportBuilder.build(logs, period, today)
    }
}
