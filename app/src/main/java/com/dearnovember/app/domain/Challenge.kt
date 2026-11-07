package com.dearnovember.app.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/** Fixed challenge calendar: Day 1 = 7 Nov 2026, Day 24 = 30 Nov 2026. */
object Challenge {
    val START: LocalDate = LocalDate.of(2026, 11, 7)
    val END: LocalDate = LocalDate.of(2026, 11, 30)
    const val TOTAL_DAYS = 24

    fun dayNumber(date: LocalDate): Int? =
        if (date.isBefore(START) || date.isAfter(END)) null
        else (ChronoUnit.DAYS.between(START, date) + 1).toInt()

    fun dateOf(day: Int): LocalDate = START.plusDays((day - 1).toLong())

    /** Date the app shows as "today": clamped into the challenge window. */
    fun displayDate(today: LocalDate): LocalDate = when {
        today.isBefore(START) -> START
        today.isAfter(END) -> END
        else -> today
    }

    fun isRecoveryDay(date: LocalDate) = date.dayOfWeek == DayOfWeek.SUNDAY

    fun wakeUpTarget(date: LocalDate): LocalTime? {
        if (dayNumber(date) == null) return null
        return when (date.dayOfMonth) {
            7, 8 -> LocalTime.of(11, 30)
            9, 10 -> LocalTime.of(11, 0)
            11, 12 -> LocalTime.of(10, 30)
            13, 14 -> LocalTime.of(10, 0)
            15, 16 -> LocalTime.of(9, 30)
            17, 18 -> LocalTime.of(9, 0)
            19, 20 -> LocalTime.of(8, 30)
            else -> LocalTime.of(8, 0)
        }
    }
}
