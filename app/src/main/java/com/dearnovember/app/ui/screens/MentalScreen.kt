package com.dearnovember.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dearnovember.app.data.*
import com.dearnovember.app.domain.Challenge
import com.dearnovember.app.domain.Prompts
import com.dearnovember.app.ui.common.*
import kotlinx.coroutines.launch

private val MOODS = listOf(1 to "\uD83D\uDE1E", 2 to "\uD83D\uDE15", 3 to "\uD83D\uDE10", 4 to "\uD83D\uDE42", 5 to "\uD83D\uDE0A")

@Composable
fun MentalScreen(onBack: () -> Unit, onOpen: (String) -> Unit) {
    val repo = rememberRepo(); val scope = rememberCoroutineScope(); val today = rememberToday(); val d = today.toString()
    val moods by repo.m.moods().collectAsStateWithLifecycle(emptyList())
    val meds by repo.m.meditations().collectAsStateWithLifecycle(emptyList())
    var mood by remember { mutableIntStateOf(3) }
    var checkIn by remember { mutableStateOf(false) }
    var custom by remember { mutableStateOf(false) }
    val day = Challenge.dayNumber(today) ?: 1
    val medToday = meds.filter { it.date == d }.sumOf { it.minutes }

    Screen("Mental and emotional growth", onBack) {
        Text("A space for self-awareness. This is not a diagnostic or medical tool.", style = MaterialTheme.typography.labelMedium)
        SectionCard("How are you feeling?") {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MOODS.forEach { (n, face) -> FilterChip(selected = mood == n, onClick = { mood = n }, label = { Text(face) }) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { scope.launch { repo.m.insertMood(MoodEntry(date = d, mood = mood)); repo.autoComplete(today, "mood") } }) { Text("Save mood") }
                OutlinedButton(onClick = { checkIn = true }) { Text("Full check-in") }
            }
        }
        SectionCard("Today's mental mission") {
            Text(Prompts.mission(day), style = MaterialTheme.typography.bodyLarge)
            Text("Tick it on the Home checklist when done.", style = MaterialTheme.typography.labelMedium)
        }
        SectionCard("Meditation") {
            LabeledProgress("$medToday of 10 min today", medToday / 10f, if (medToday >= 5) "target met" else "aim for 5 to 10")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10).forEach { m -> FilledTonalButton(onClick = { scope.launch { repo.m.insertMeditation(MeditationSession(date = d, minutes = m)); if (medToday + m >= 5) repo.autoComplete(today, "meditation") } }) { Text("+$m min") } }
                OutlinedButton(onClick = { custom = true }) { Text("Other") }
            }
        }
        SectionCard("Reflection") {
            Text("Write privately in your journal. It never leaves your phone.", style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = { onOpen("journal") }) { Text("Open journal") }
        }
        SectionCard("Recent check-ins") {
            if (moods.isEmpty()) Text("No entries yet.")
            moods.take(8).forEach { m ->
                Text("${shortDate(m.date)}  ${MOODS.firstOrNull { it.first == m.mood }?.second ?: ""}" + (m.note?.let { "  $it" } ?: ""), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    if (checkIn) InputDialog("Emotional check-in",
        listOf(Field("What am I feeling today?", multiline = true), Field("What triggered this feeling?"), Field("Fact or assumption?"), Field("What is within my control?"), Field("What can I do differently?")),
        onDismiss = { checkIn = false }) { v ->
        scope.launch {
            repo.m.insertMood(MoodEntry(date = d, mood = mood, note = v[0].ifBlank { null }, trigger = v[1].ifBlank { null }, factsOrAssumptions = v[2].ifBlank { null }, withinControl = v[3].ifBlank { null }, doDifferently = v[4].ifBlank { null }))
            repo.autoComplete(today, "mood")
        }
        checkIn = false
    }
    if (custom) InputDialog("Meditation minutes", listOf(Field("Minutes", "7", numeric = true)), onDismiss = { custom = false }) { v ->
        val m = v[0].toDoubleOrNull()?.toInt()
        if (m != null && m > 0) scope.launch { repo.m.insertMeditation(MeditationSession(date = d, minutes = m)); if (medToday + m >= 5) repo.autoComplete(today, "meditation") }
        custom = false
    }
}
