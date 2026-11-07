package com.dearnovember.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dearnovember.app.domain.*
import com.dearnovember.app.ui.common.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun ProgressScreen() {
    val repo = rememberRepo(); val today = LocalDate.now()
    val progress by repo.observeAllProgress().collectAsStateWithLifecycle(emptyList())
    val done by repo.observeAllDone().collectAsStateWithLifecycle(emptyList())
    val workouts by repo.m.workouts().collectAsStateWithLifecycle(emptyList())
    val meds by repo.m.meditations().collectAsStateWithLifecycle(emptyList())
    val speaking by repo.m.speakingAll().collectAsStateWithLifecycle(emptyList())
    val sleepAll by repo.m.sleepAll().collectAsStateWithLifecycle(emptyList())
    val topics by repo.m.allTopics().collectAsStateWithLifecycle(emptyList())
    val assignments by repo.m.assignments().collectAsStateWithLifecycle(emptyList())
    val settings by repo.m.settings().collectAsStateWithLifecycle(null)
    val photos by repo.m.photos().collectAsStateWithLifecycle(emptyList())
    var openDay by remember { mutableStateOf<LocalDate?>(null) }

    val scores = progress.associate { LocalDate.parse(it.date) to it.score }
    val elapsed = (Challenge.dayNumber(Challenge.displayDate(today)) ?: 1).let { if (today.isBefore(Challenge.START)) 0 else it }
    val denom = elapsed.coerceAtLeast(1)
    val total = scores.values.sum()
    val best = scores.maxByOrNull { it.value }
    val streak = Streaks.compute(scores, today)

    Screen("Progress") {
        SectionCard("Challenge") {
            LabeledProgress("Day $elapsed of ${Challenge.TOTAL_DAYS}", elapsed / Challenge.TOTAL_DAYS.toFloat(), "${(elapsed * 100 / Challenge.TOTAL_DAYS)}%")
            Text("Points earned: $total of ${Challenge.TOTAL_DAYS * 100}", style = MaterialTheme.typography.bodyMedium)
            Text("Average daily score: ${total / denom}", style = MaterialTheme.typography.bodyMedium)
            Text("Best day: " + (best?.let { "Day ${Challenge.dayNumber(it.key)} with ${it.value}" } ?: "none yet"), style = MaterialTheme.typography.bodyMedium)
            Text("Streak: ${streak.current}  \u2022  Longest: ${streak.longest}", style = MaterialTheme.typography.bodyMedium)
            Text("A day counts at 60 or more. One missed day pauses the streak; two in a row reset it.", style = MaterialTheme.typography.labelMedium)
        }
        SectionCard("Daily scores") { ScoreChart(scores) }
        SectionCard("Calendar (tap a day to view or fix it)") {
            (1..Challenge.TOTAL_DAYS).chunked(6).forEach { week ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    week.forEach { day ->
                        val date = Challenge.dateOf(day); val sc = scores[date]
                        val alpha = if (sc == null) 0.08f else (0.25f + sc / 140f).coerceAtMost(1f)
                        Box(
                            Modifier.weight(1f).aspectRatio(1f).background(MaterialTheme.colorScheme.primary.copy(alpha = alpha), RoundedCornerShape(10.dp))
                                .then(if (date == Challenge.displayDate(today)) Modifier.border(2.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(10.dp)) else Modifier)
                                .clickable(enabled = !date.isAfter(today)) { openDay = date },
                            contentAlignment = Alignment.Center,
                        ) { Text("$day", style = MaterialTheme.typography.labelMedium) }
                    }
                }
            }
        }
        SectionCard("By category (average share of available points)") {
            Category.entries.forEach { c ->
                val pts = done.filter { DefaultTasks.find(it.taskKey)?.category == c }.sumOf { DefaultTasks.find(it.taskKey)?.points ?: 0 }
                val v = pts / (c.maxPoints * denom).toFloat()
                LabeledProgress(c.label, v, "${(v * 100).toInt().coerceAtMost(100)}%")
            }
        }
        SectionCard("Consistency") {
            val topicPct = if (topics.isEmpty()) 0f else topics.count { it.done } / topics.size.toFloat()
            LabeledProgress("Academic topics", topicPct, "${(topicPct * 100).toInt()}%")
            LabeledProgress("Assignments done", assignments.count { it.status == "DONE" } / assignments.size.coerceAtLeast(1).toFloat(), "${assignments.count { it.status == "DONE" }} of ${assignments.size}")
            val w = workouts.count { it.completed }
            LabeledProgress("Workout days", w / denom.toFloat(), "$w of $denom")
            val m = meds.map { it.date }.toSet().size
            LabeledProgress("Meditation days", m / denom.toFloat(), "$m of $denom")
            val sp = speaking.count { it.warmup && it.reading && it.shadowing && it.topic }
            LabeledProgress("Speaking practice days", sp / denom.toFloat(), "$sp of $denom")
            LabeledProgress("Nights logged", sleepAll.size / denom.toFloat(), "${sleepAll.size} of $denom")
        }
        SectionCard("Day 1 and Day 24") {
            Text("Push-ups: ${settings?.pushupBaseline ?: "--"} then ${settings?.pushupFinal ?: "--"}", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PhotoBox(photos.firstOrNull { it.slot == "DAY1" }, "Day 1")
                PhotoBox(photos.firstOrNull { it.slot == "DAY24" }, "Day 24")
            }
            Text("Add photos from Journey, then Physical, then Body.", style = MaterialTheme.typography.labelMedium)
        }
    }
    openDay?.let { DayEditorDialog(it) { openDay = null } }
}

@Composable
private fun ScoreChart(scores: Map<LocalDate, Int>) {
    val bar = MaterialTheme.colorScheme.primary
    val empty = MaterialTheme.colorScheme.surfaceVariant
    Canvas(Modifier.fillMaxWidth().height(120.dp)) {
        val n = Challenge.TOTAL_DAYS
        val gap = 4.dp.toPx()
        val w = (size.width - gap * (n - 1)) / n
        for (i in 0 until n) {
            val s = scores[Challenge.dateOf(i + 1)]
            val h = if (s == null) 4.dp.toPx() else (size.height * s / 100f).coerceAtLeast(4.dp.toPx())
            drawRoundRect(if (s == null) empty else bar, Offset(i * (w + gap), size.height - h), Size(w, h), CornerRadius(w / 3))
        }
    }
}

/** View and correct any past day. Edits go through the same transactional path as Home. */
@Composable
fun DayEditorDialog(date: LocalDate, onClose: () -> Unit) {
    val repo = rememberRepo(); val scope = rememberCoroutineScope()
    val comps by repo.observeCompletions(date).collectAsStateWithLifecycle(emptyList())
    val doneKeys = comps.filter { it.completed }.map { it.taskKey }.toSet()
    val score = Scoring.score(doneKeys)
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Day ${Challenge.dayNumber(date)}: $score / 100  (${Scoring.band(score).label})") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                DefaultTasks.all.forEach { t ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(t.key in doneKeys, onCheckedChange = { c -> scope.launch { repo.setTaskDone(date, t.key, c) } })
                        Text("${t.name} (${t.points})", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("Close") } },
    )
}
