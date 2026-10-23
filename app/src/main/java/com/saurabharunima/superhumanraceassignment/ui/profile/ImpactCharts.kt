package com.saurabharunima.superhumanraceassignment.ui.profile

import com.saurabharunima.superhumanraceassignment.domain.DistanceFormatter
import com.saurabharunima.superhumanraceassignment.ui.theme.LocalDistanceUnit
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.saurabharunima.superhumanraceassignment.data.model.DailyCo2
import com.saurabharunima.superhumanraceassignment.data.model.ModeUsage
import com.saurabharunima.superhumanraceassignment.data.model.TransportMode
import com.saurabharunima.superhumanraceassignment.ui.theme.AppTheme
import com.saurabharunima.superhumanraceassignment.ui.theme.Blue400
import com.saurabharunima.superhumanraceassignment.ui.theme.Blue700
import java.time.format.DateTimeFormatter

private val AxisDate: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM")

/**
 * Plain-Compose bar chart (no chart library). The visual is hidden from TalkBack and replaced
 * with one text summary, since 30 unlabeled bars are not useful read aloud.
 */
@Composable
fun DailyCo2BarChart(daily: List<DailyCo2>, modifier: Modifier = Modifier) {
    val maxValue = daily.maxOfOrNull { it.savedKg } ?: 0.0
    val best = daily.maxByOrNull { it.savedKg }
    val total = daily.sumOf { it.savedKg }
    val summary = buildString {
        append("Bar chart of CO₂e saved per day over the last ${daily.size} days. ")
        append("Total %.1f kilograms.".format(total))
        if (best != null && best.savedKg > 0) {
            append(" Best day ${best.date.format(AxisDate)}, %.1f kilograms.".format(best.savedKg))
        }
    }

    Column(modifier.fillMaxWidth().clearAndSetSemantics { contentDescription = summary }) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("kg CO₂e saved", style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.muted)
            Text("max %.1f".format(maxValue), style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.muted)
        }
        Row(
            Modifier.fillMaxWidth().height(140.dp).padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(if (daily.size > 14) 2.dp else 8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            daily.forEach { day ->
                val fraction = if (maxValue > 0) (day.savedKg / maxValue).toFloat() else 0f
                Box(
                    Modifier
                        .weight(1f)
                        // A 2% stub keeps zero days visible as a baseline instead of a gap.
                        .fillMaxHeight(fraction.coerceIn(0.02f, 1f))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(if (day.savedKg > 0) Blue700 else AppTheme.colors.cardBorder),
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(daily.firstOrNull()?.date?.format(AxisDate).orEmpty(), style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.muted)
            Text(daily.lastOrNull()?.date?.format(AxisDate).orEmpty(), style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.muted)
        }
    }
}

/** Horizontal share bars. Every row carries its own text label, so colour is never the only cue. */
@Composable
fun TransportUsageChart(usage: List<ModeUsage>, modifier: Modifier = Modifier) {
    val unit = LocalDistanceUnit.current
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        usage.sortedByDescending { it.km }.forEach { item ->
            val percent = (item.share * 100).toInt()
            val description = "${item.mode.label}, $percent percent, ${DistanceFormatter.spoken(item.km, unit)}"
            Column(
                Modifier.semantics(mergeDescendants = true) { contentDescription = description },
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.mode.label, style = MaterialTheme.typography.labelLarge)
                    Text("$percent%% · ${DistanceFormatter.short(item.km, unit)}".format(), style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.muted)
                }
                Box(
                    Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(AppTheme.colors.cardBorder),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(item.share.toFloat().coerceIn(0.02f, 1f))
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(item.mode.color()),
                    )
                }
            }
        }
    }
}

@Composable
private fun TransportMode.color(): Color = when (this) {
    TransportMode.WALK -> AppTheme.colors.leaf
    TransportMode.CYCLE -> Blue400
    TransportMode.BUS -> AppTheme.colors.gold
    TransportMode.TRAIN -> AppTheme.colors.gem
    TransportMode.CAR -> AppTheme.colors.muted
}
