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
import com.dearnovember.app.domain.Prompts
import com.dearnovember.app.ui.common.*
import kotlinx.coroutines.launch

@Composable
fun SpeakingScreen(onBack: () -> Unit) {
    val repo = rememberRepo(); val scope = rememberCoroutineScope(); val today = rememberToday(); val d = today.toString()
    val session by repo.m.speaking(d).collectAsStateWithLifecycle(null)
    val history by repo.m.speakingAll().collectAsStateWithLifecycle(emptyList())
    val words by repo.m.words().collectAsStateWithLifecycle(emptyList())
    var topic by remember { mutableStateOf(Prompts.topics.random()) }
    var addWord by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf(false) }
    val day = Challenge.dayNumber(today) ?: 1
    val wod = Prompts.word(day)
    val cur = session ?: SpeakingSession(date = d, warmup = false, reading = false, shadowing = false, topic = false)

    fun save(s: SpeakingSession) = scope.launch {
        val existing = repo.m.speakingOnce(d)
        repo.m.upsertSpeaking(s.copy(id = existing?.id ?: 0))
        if (s.warmup && s.reading && s.shadowing && s.topic) repo.autoComplete(today, "speaking")
    }

    Screen("Communication and personality", onBack) {
        Text("The goal is clear, calm, confident speech in your own natural voice. No accent is better or worse.", style = MaterialTheme.typography.labelMedium)
        SectionCard("Today's practice (15 minutes)") {
            listOf(
                Triple("Breathing and relaxed warm-up (3 min)", cur.warmup, { c: Boolean -> save(cur.copy(warmup = c)) }),
                Triple("Reading aloud (5 min)", cur.reading, { c: Boolean -> save(cur.copy(reading = c)) }),
                Triple("Pronunciation and shadowing (5 min)", cur.shadowing, { c: Boolean -> save(cur.copy(shadowing = c)) }),
                Triple("Random topic speaking (2 min)", cur.topic, { c: Boolean -> save(cur.copy(topic = c)) }),
            ).forEach { (label, checked, onChange) ->
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked, onCheckedChange = { onChange(it) }); Text(label) }
            }
            OutlinedButton(onClick = { note = true }) { Text("Self-assessment note") }
            cur.selfNote?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            Text("Audio recording is not part of this version.", style = MaterialTheme.typography.labelMedium)
        }
        SectionCard("Random topic") {
            Text(topic, style = MaterialTheme.typography.bodyLarge)
            OutlinedButton(onClick = { topic = Prompts.topics.random() }) { Text("Another topic") }
        }
        SectionCard("Word of the day") {
            Text(wod.word, style = MaterialTheme.typography.headlineSmall)
            Text(wod.meaning); Text("\u201C${wod.example}\u201D", style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = { scope.launch { repo.m.insertWord(VocabularyWord(word = wod.word, meaning = wod.meaning, example = wod.example, dateAdded = d)) } }) { Text("Add to my notebook") }
        }
        SectionCard("Vocabulary notebook") {
            Button(onClick = { addWord = true }) { Text("Add a word") }
            if (words.isEmpty()) Text("Your words will collect here.")
            words.forEach { w ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text(w.word, style = MaterialTheme.typography.titleMedium); Text(w.meaning + (w.example?.let { " \u2022 $it" } ?: ""), style = MaterialTheme.typography.bodyMedium) }
                    TextButton(onClick = { scope.launch { repo.m.deleteWord(w) } }) { Text("Remove") }
                }
            }
        }
        SectionCard("Practice history") {
            if (history.isEmpty()) Text("No sessions yet.")
            history.take(10).forEach { h ->
                val n = listOf(h.warmup, h.reading, h.shadowing, h.topic).count { it }
                Text("${shortDate(h.date)}  \u2022  $n of 4 steps", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    if (addWord) InputDialog("New word", listOf(Field("Word"), Field("Meaning"), Field("Example sentence")), onDismiss = { addWord = false }) { v ->
        if (v[0].isNotBlank()) scope.launch { repo.m.insertWord(VocabularyWord(word = v[0].trim(), meaning = v[1], example = v[2].ifBlank { null }, dateAdded = d)) }
        addWord = false
    }
    if (note) InputDialog("How did it go?", listOf(Field("Note", cur.selfNote ?: "", multiline = true)), onDismiss = { note = false }) { v -> save(cur.copy(selfNote = v[0].ifBlank { null })); note = false }
}
