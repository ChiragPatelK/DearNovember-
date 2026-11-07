package com.dearnovember.app

import com.dearnovember.app.domain.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ScoringTest {
    @Test fun taskPointsTotal100() = assertEquals(100, DefaultTasks.all.sumOf { it.points })
    @Test fun categoryPointsMatchSpec() = Category.entries.forEach { c ->
        assertEquals(c.name, c.maxPoints, DefaultTasks.all.filter { it.category == c }.sumOf { it.points })
    }
    @Test fun noCompletionsScoresZero() = assertEquals(0, Scoring.score(emptyList()))
    @Test fun allCompletionsScores100() = assertEquals(100, Scoring.score(DefaultTasks.all.map { it.key }))
    @Test fun duplicateKeysDoNotDoubleScore() = assertEquals(10, Scoring.score(listOf("study_1", "study_1", "study_1")))
    @Test fun unknownKeysIgnored() = assertEquals(0, Scoring.score(listOf("nope")))
    @Test fun bands() {
        assertEquals(ScoreBand.EXCELLENT, Scoring.band(90)); assertEquals(ScoreBand.STRONG, Scoring.band(89))
        assertEquals(ScoreBand.STRONG, Scoring.band(75)); assertEquals(ScoreBand.DECENT, Scoring.band(74))
        assertEquals(ScoreBand.DECENT, Scoring.band(60)); assertEquals(ScoreBand.NEEDS_IMPROVEMENT, Scoring.band(59))
    }
}

class ChallengeTest {
    @Test fun day1IsNov7() = assertEquals(1, Challenge.dayNumber(LocalDate.of(2026, 11, 7)))
    @Test fun day24IsNov30() = assertEquals(24, Challenge.dayNumber(LocalDate.of(2026, 11, 30)))
    @Test fun outsideWindowIsNull() {
        assertNull(Challenge.dayNumber(LocalDate.of(2026, 11, 6))); assertNull(Challenge.dayNumber(LocalDate.of(2026, 12, 1)))
    }
    @Test fun wakeSchedule() {
        assertEquals("11:30", Challenge.wakeUpTarget(LocalDate.of(2026, 11, 8)).toString())
        assertEquals("09:00", Challenge.wakeUpTarget(LocalDate.of(2026, 11, 17)).toString())
        assertEquals("08:00", Challenge.wakeUpTarget(LocalDate.of(2026, 11, 30)).toString())
    }
    @Test fun twentyFourChapters() = assertEquals(24, Chapters.all.size)
}

class StreakTest {
    private fun d(n: Int) = LocalDate.of(2026, 11, n)
    @Test fun consecutiveGoodDays() {
        val r = Streaks.compute(mapOf(d(7) to 80, d(8) to 70, d(9) to 60), d(9)); assertEquals(3, r.current); assertEquals(3, r.longest)
    }
    @Test fun oneMissDoesNotReset() {
        val r = Streaks.compute(mapOf(d(7) to 80, d(8) to 10, d(9) to 70), d(9)); assertEquals(2, r.current)
    }
    @Test fun twoMissesReset() {
        val r = Streaks.compute(mapOf(d(7) to 80, d(8) to 10, d(9) to 10, d(10) to 70), d(10)); assertEquals(1, r.current); assertEquals(1, r.longest)
    }
    @Test fun unfinishedTodayDoesNotBreak() {
        val r = Streaks.compute(mapOf(d(7) to 80, d(8) to 80, d(9) to 20), d(9)); assertEquals(2, r.current)
    }
    @Test fun beforeStartIsZero() = assertEquals(0, Streaks.compute(emptyMap(), LocalDate.of(2026, 11, 1)).current)
}

class ReminderTimeTest {
    private fun t(y: Int, m: Int, d: Int, h: Int, min: Int) = java.time.LocalDateTime.of(y, m, d, h, min)
    @Test fun beforeStartFiresOnDay1() = assertEquals(t(2026, 11, 7, 18, 0), ReminderTime.next("workout", 18, 0, t(2026, 11, 1, 9, 0)))
    @Test fun afterTodaysTimeRollsToTomorrow() = assertEquals(t(2026, 11, 9, 18, 0), ReminderTime.next("workout", 18, 0, t(2026, 11, 8, 19, 0)))
    @Test fun wakeFollowsSchedule() = assertEquals(t(2026, 11, 9, 11, 0), ReminderTime.next("wake", 8, 0, t(2026, 11, 8, 23, 0)))
    @Test fun noRemindersAfterChallenge() = assertNull(ReminderTime.next("workout", 18, 0, t(2026, 11, 30, 23, 0)))
}
