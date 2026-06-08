package com.controlparental.app.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAdjusters

object TimeUtils {

    fun getCurrentWeekRange(): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sunday = monday.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        return monday to sunday
    }

    fun getWeekKey(): String {
        val today = LocalDate.now()
        return "${today.year}-W${"%02d".format(today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR))}"
    }

    fun getDaysUntilMidnight(): Long {
        val now = LocalTime.now()
        val midnight = LocalTime.MIDNIGHT
        return if (now == LocalTime.MIDNIGHT) {
            0L
        } else {
            java.time.Duration.between(now, midnight).toMillis()
        }
    }

    fun isNewDay(lastDate: String?): Boolean {
        return lastDate != LocalDate.now().toString()
    }

    fun isNewWeek(lastWeekKey: String?): Boolean {
        return lastWeekKey != getWeekKey()
    }

    fun formatDuration(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            hours > 0 -> "${hours}h ${mins}min"
            else -> "${mins} min"
        }
    }
}
