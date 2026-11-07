package com.dearnovember.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dearnovember.app.data.*
import com.dearnovember.app.domain.Challenge
import com.dearnovember.app.ui.common.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val HHMM = DateTimeFormatter.ofPattern("HH:mm")
private fun validTime(s: String) = s.isBlank() || runCatching { LocalTime.parse(s.trim(), HHMM) }.isSuccess

/** Consecutive days (ending at the latest day with data) that a task was completed. */
fun taskStreak(done: Set<LocalDate>, today: LocalDate): Int {
    var d = if (today in done) today else today.minusDays(1)
    var n = 0
    while (!d.isBefore(Challenge.START) && d in done) { n++; d = d.minusDays(1) }
    return n
}

@Composable
fun DisciplineScreen(onBack: () -> Unit) {
    val repo = rememberRepo(); val scope = rememberCoroutineScope(); val today = rememberToday(); val d = today.toString()
    val sleep by repo.m.sleep(d).collectAsStateWithLifecycle(null)
    val sleepAll by repo.m.sleepAll().collectAsStateWithLifecycle(emptyList())
    val insta by repo.m.insta(d).collectAsStateWithLifecycle(emptyList())
    val focus by repo.m.focusAll().collectAsStateWithLifecycle(emptyList())
    val done by repo.observeAllDone().collectAsStateWithLifecycle(emptyList())
    var logSleep by remember { mutableStateOf(false) }
    var customInsta by remember { mutableStateOf(false) }
    val instaTotal = insta.sumOf { it.minutes }
    val wake = Challenge.wakeUpTarget(today)
    val focusToday = focus.size // sessions overall; minutes below
    val focusMinutes = focus.sumOf { it.minutes }

    Screen("Discipline and productivity", onBack) {
        Text("Balanced, not extreme. Never miss twice. Music is fine in workouts and focus sessions.", style = MaterialTheme.typography.labelMedium)
        SectionCard("Sleep and wake-up") {
            Text("Today's wake-up target: ${wake?.format(HHMM) ?: "--"}  \u2022  Bedtime goal by the end: 23:30 to 08:00", style = MaterialTheme.typography.bodyMedium)
            Text("Logged today: bed ${sleep?.bedtime ?: "--"}, woke ${sleep?.wakeTime ?: "--"}", style = MaterialTheme.typography.bodyMedium)
            Text("${sleepAll.size} nights logged", style = MaterialTheme.typography.labelMedium)
            Button(onClick = { logSleep = true }) { Text("Log sleep") }
        }
        SectionCard("Instagram today (max 30 min)") {
            LabeledProgress("$instaTotal of 30 min", instaTotal / 30f, if (instaTotal <= 30) "within limit" else "over limit")
            Text("Manual log only. No usage-access permission is requested.", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 15).forEach { m -> FilledTonalButton(onClick = { scope.launch { repo.m.insertInsta(InstagramLog(date = d, minutes = m)) } }) { Text("+$m") } }
                OutlinedButton(onClick = { customInsta = true }) { Text("Other") }
            }
            insta.forEach { i -> Row(verticalAlignment = Alignment.CenterVertically) { Text("${i.minutes} min", Modifier.weight(1f)); TextButton(onClick = { scope.launch { repo.m.deleteInsta(i) } }) { Text("Remove") } } }
        }
        FocusTimer(onFinished = { minutes -> scope.launch { repo.m.insertFocus(FocusSession(date = d, minutes = minutes)) } })
        SectionCard("Focus so far") { Text("$focusToday sessions, $focusMinutes minutes in total") }
        SectionCard("Habit streaks (consecutive days)") {
            listOf("wake" to "Wake-up on time", "instagram" to "Instagram within limit", "bedtime" to "Bedtime on track", "workout" to "Workout", "study_1" to "Study block", "meditation" to "Meditation", "reflect" to "Reflection").forEach { (key, label) ->
                val days = done.filter { it.taskKey == key }.map { LocalDate.parse(it.date) }.toSet()
                Text("$label: ${taskStreak(days, today)}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    if (logSleep) InputDialog("Log sleep", listOf(Field("Bedtime last night (HH:mm)", sleep?.bedtime ?: ""), Field("Wake-up time (HH:mm)", sleep?.wakeTime ?: "")), onDismiss = { logSleep = false }) { v ->
        if (validTime(v[0]) && validTime(v[1])) scope.launch { repo.m.upsertSleep(SleepLog(d, v[0].trim().ifBlank { null }, v[1].trim().ifBlank { null })) }
        logSleep = false
    }
    if (customInsta) InputDialog("Instagram minutes", listOf(Field("Minutes", numeric = true)), onDismiss = { customInsta = false }) { v ->
        val m = v[0].toDoubleOrNull()?.toInt()
        if (m != null && m > 0) scope.launch { repo.m.insertInsta(InstagramLog(date = d, minutes = m)) }
        customInsta = false
    }
}

@Composable
private fun FocusTimer(onFinished: (Int) -> Unit) {
    var minutes by remember { mutableIntStateOf(25) }
    var endAt by remember { mutableStateOf<Long?>(null) }
    var startedAt by remember { mutableLongStateOf(0L) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(endAt) {
        val end = endAt ?: return@LaunchedEffect
        while (true) {
            now = System.currentTimeMillis()
            if (now >= end) { onFinished(minutes); endAt = null; break }
            delay(500)
        }
    }
    SectionCard("Focus timer") {
        val running = endAt != null
        if (running) {
            val left = ((endAt!! - now) / 1000).coerceAtLeast(0)
            Text("%02d:%02d".format(left / 60, left % 60), style = MaterialTheme.typography.displaySmall)
            Text("One task. No scrolling. Music is fine.", style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = {
                val elapsed = ((System.currentTimeMillis() - startedAt) / 60000).toInt()
                if (elapsed >= 1) onFinished(elapsed)
                endAt = null
            }) { Text("Stop") }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(15, 25, 45).forEach { m -> FilterChip(selected = minutes == m, onClick = { minutes = m }, label = { Text("$m min") }) } }
            Button(onClick = { startedAt = System.currentTimeMillis(); now = startedAt; endAt = startedAt + minutes * 60_000L }) { Text("Start focus session") }
            Text("Keep the app open while the timer runs.", style = MaterialTheme.typography.labelMedium)
        }
    }
}
