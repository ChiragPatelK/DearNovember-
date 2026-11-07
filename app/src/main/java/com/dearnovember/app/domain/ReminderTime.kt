package com.dearnovember.app.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object ReminderTime {
    /** Next trigger strictly after [now], only on challenge dates. Wake-up follows the shifting schedule. */
    fun next(key: String, hour: Int, minute: Int, now: LocalDateTime): LocalDateTime? {
        var d: LocalDate = maxOf(now.toLocalDate(), Challenge.START)
        while (!d.isAfter(Challenge.END)) {
            val t: LocalTime = if (key == "wake") Challenge.wakeUpTarget(d) ?: LocalTime.of(hour, minute) else LocalTime.of(hour, minute)
            val candidate = LocalDateTime.of(d, t)
            if (candidate.isAfter(now)) return candidate
            d = d.plusDays(1)
        }
        return null
    }
}
