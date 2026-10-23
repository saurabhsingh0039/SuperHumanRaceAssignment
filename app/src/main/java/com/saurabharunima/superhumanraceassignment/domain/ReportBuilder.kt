package com.saurabharunima.superhumanraceassignment.domain

import com.saurabharunima.superhumanraceassignment.data.model.DailyCo2
import com.saurabharunima.superhumanraceassignment.data.model.DailyLog
import com.saurabharunima.superhumanraceassignment.data.model.ModeUsage
import com.saurabharunima.superhumanraceassignment.data.model.Report
import com.saurabharunima.superhumanraceassignment.data.model.ReportPeriod
import com.saurabharunima.superhumanraceassignment.data.model.TransportMode
import java.time.LocalDate

/** Pure function of its inputs, so it is trivially unit-testable. */
object ReportBuilder {
    fun build(logs: List<DailyLog>, period: ReportPeriod, today: LocalDate): Report {
        val start = today.minusDays(period.days - 1L)
        val inRange = logs.filter { !it.date.isBefore(start) && !it.date.isAfter(today) }

        val daily = (0 until period.days).map { i ->
            val date = start.plusDays(i.toLong())
            val day = inRange.filter { it.date == date }
            DailyCo2(
                date = date,
                savedKg = day.sumOf { Co2Calculator.savedKg(it.mode, it.km) },
                emittedKg = day.sumOf { Co2Calculator.emissionsKg(it.mode, it.km) },
            )
        }

        val totalKm = inRange.sumOf { it.km }
        val usage = TransportMode.entries
            .map { mode ->
                val km = inRange.filter { it.mode == mode }.sumOf { it.km }
                ModeUsage(mode, km, if (totalKm > 0) km / totalKm else 0.0)
            }
            .filter { it.km > 0 }

        return Report(
            period = period,
            totalSavedKg = daily.sumOf { it.savedKg },
            totalEmittedKg = daily.sumOf { it.emittedKg },
            daily = daily,
            modeUsage = usage,
        )
    }
}
