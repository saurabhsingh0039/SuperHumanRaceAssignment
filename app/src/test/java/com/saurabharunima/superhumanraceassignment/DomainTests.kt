package com.saurabharunima.superhumanraceassignment.domain

import com.saurabharunima.superhumanraceassignment.data.model.DailyLog
import com.saurabharunima.superhumanraceassignment.data.model.DistanceUnit
import com.saurabharunima.superhumanraceassignment.data.model.ReportPeriod
import com.saurabharunima.superhumanraceassignment.data.model.TransportMode
import java.time.LocalDate
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KeywordResponderTest {
    private val responder = KeywordResponder(
        rules = listOf(
            listOf("hi", "hello") to "greet",
            listOf("carbon footprint") to "co2",
            listOf("transport") to "transport",
        ),
        fallback = "fallback",
    )

    @Test fun `single word keyword matches a whole token case-insensitively`() {
        assertEquals("greet", responder.respond("HELLO there"))
    }

    @Test fun `single word keyword does not match inside another word`() {
        assertEquals("fallback", responder.respond("this is nice"))
    }

    @Test fun `multi word keyword matches as a substring`() {
        assertEquals("co2", responder.respond("what is my carbon footprint?"))
    }

    @Test fun `first matching rule wins and unmatched input falls back`() {
        assertEquals("greet", responder.respond("hi, tell me about transport"))
        assertEquals("fallback", responder.respond("asdf"))
    }
}

class MarkdownStripperTest {
    @Test fun `removes bold, code and heading markers`() {
        assertEquals("CARBON x", MarkdownStripper.plain("**CARBON** `x`"))
        assertEquals("Title\nbody", MarkdownStripper.plain("## Title\nbody"))
    }

    @Test fun `leaves plain text unchanged`() {
        assertEquals("Walk more, drive less.", MarkdownStripper.plain("Walk more, drive less."))
    }
}

class DistanceFormatterTest {
    @Before fun fixLocale() = Locale.setDefault(Locale.US)

    @Test fun `km stays km`() {
        assertEquals("10.0 km", DistanceFormatter.short(10.0, DistanceUnit.KM))
    }

    @Test fun `km converts to miles`() {
        assertEquals("6.2 mi", DistanceFormatter.short(10.0, DistanceUnit.MILES))
        assertEquals(6.21371, DistanceFormatter.convert(10.0, DistanceUnit.MILES), 1e-9)
    }

    @Test fun `spoken form uses full unit names`() {
        assertEquals("1.0 kilometres", DistanceFormatter.spoken(1.0, DistanceUnit.KM))
        assertEquals("0.6 miles", DistanceFormatter.spoken(1.0, DistanceUnit.MILES))
    }
}

class AuthValidatorTest {
    @Test fun `email accepts valid and trims whitespace`() {
        assertNull(AuthValidator.email("a@b.co"))
        assertNull(AuthValidator.email("  a@b.co "))
    }

    @Test fun `email rejects malformed input`() {
        assertNotNull(AuthValidator.email("abc"))
        assertNotNull(AuthValidator.email("a@b"))
        assertNotNull(AuthValidator.email(""))
    }

    @Test fun `password needs at least 8 characters`() {
        assertNotNull(AuthValidator.password("1234567"))
        assertNull(AuthValidator.password("12345678"))
    }

    @Test fun `name needs at least 2 non-blank characters`() {
        assertNotNull(AuthValidator.name(" A "))
        assertNull(AuthValidator.name("Al"))
    }
}

class Co2CalculatorTest {
    @Test fun `walking saves the full car baseline`() {
        assertEquals(1.71, Co2Calculator.savedKg(TransportMode.WALK, 10.0), 1e-9)
        assertEquals(0.0, Co2Calculator.emissionsKg(TransportMode.WALK, 10.0), 1e-9)
    }

    @Test fun `driving saves nothing and bus emits less than car`() {
        assertEquals(0.0, Co2Calculator.savedKg(TransportMode.CAR, 10.0), 1e-9)
        assertTrue(Co2Calculator.emissionsKg(TransportMode.BUS, 100.0) < Co2Calculator.emissionsKg(TransportMode.CAR, 100.0))
    }
}

class TimeAgoTest {
    private val now = 10L * 24 * 60 * 60_000

    @Test fun `buckets minutes, hours and days`() {
        assertEquals("Just now", TimeAgo.format(now, now))
        assertEquals("5m ago", TimeAgo.format(now, now - 5 * 60_000))
        assertEquals("3h ago", TimeAgo.format(now, now - 3 * 60 * 60_000))
        assertEquals("2d ago", TimeAgo.format(now, now - 2L * 24 * 60 * 60_000))
    }

    @Test fun `a timestamp in the future reads as just now`() {
        assertEquals("Just now", TimeAgo.format(now, now + 60_000))
    }
}

class GeoMathTest {
    @Test fun `same point is zero distance`() {
        assertEquals(0.0, GeoMath.distanceKm(12.9, 77.6, 12.9, 77.6), 1e-9)
    }

    @Test fun `one degree of longitude on the equator is about 111 km and symmetric`() {
        val ab = GeoMath.distanceKm(0.0, 0.0, 0.0, 1.0)
        val ba = GeoMath.distanceKm(0.0, 1.0, 0.0, 0.0)
        assertEquals(111.19, ab, 0.05)
        assertEquals(ab, ba, 1e-9)
    }
}

class ReportBuilderTest {
    private val today = LocalDate.of(2026, 10, 23)
    private val logs = listOf(
        DailyLog(today, TransportMode.WALK, 10.0),
        DailyLog(today.minusDays(1), TransportMode.CAR, 5.0),
        DailyLog(today.minusDays(20), TransportMode.BUS, 3.0),
    )

    @Test fun `weekly report spans 7 days ending today and ignores older logs`() {
        val report = ReportBuilder.build(logs, ReportPeriod.WEEKLY, today)
        assertEquals(7, report.daily.size)
        assertEquals(today.minusDays(6), report.daily.first().date)
        assertEquals(today, report.daily.last().date)
        assertEquals(setOf(TransportMode.WALK, TransportMode.CAR), report.modeUsage.map { it.mode }.toSet())
    }

    @Test fun `totals and shares are computed from the logs`() {
        val report = ReportBuilder.build(logs, ReportPeriod.WEEKLY, today)
        assertEquals(1.71, report.totalSavedKg, 1e-9)
        assertEquals(0.855, report.totalEmittedKg, 1e-9)
        assertEquals(10.0 / 15.0, report.modeUsage.first { it.mode == TransportMode.WALK }.share, 1e-9)
    }

    @Test fun `monthly report includes the older log and empty input yields zeroes`() {
        assertEquals(3, ReportBuilder.build(logs, ReportPeriod.MONTHLY, today).modeUsage.size)
        val empty = ReportBuilder.build(emptyList(), ReportPeriod.WEEKLY, today)
        assertEquals(7, empty.daily.size)
        assertTrue(empty.modeUsage.isEmpty())
        assertEquals(0.0, empty.totalSavedKg, 0.0)
    }
}
