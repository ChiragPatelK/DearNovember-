package com.dearnovember.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dearnovember.app.data.*
import com.dearnovember.app.ui.common.*
import kotlinx.coroutines.launch
import java.time.LocalDate

private val STATUSES = listOf("PENDING", "IN_PROGRESS", "DONE")
private fun statusLabel(s: String) = when (s) { "IN_PROGRESS" -> "In progress"; "DONE" -> "Done"; else -> "Pending" }

@Composable
fun AcademicsScreen(onBack: () -> Unit, onOpen: (String) -> Unit) {
    val repo = rememberRepo(); val scope = rememberCoroutineScope(); val today = rememberToday()
    val subjects by repo.m.subjects().collectAsStateWithLifecycle(emptyList())
    val topics by repo.m.allTopics().collectAsStateWithLifecycle(emptyList())
    val assignments by repo.m.assignments().collectAsStateWithLifecycle(emptyList())
    val sessions by repo.m.studySessions().collectAsStateWithLifecycle(emptyList())
    val settings by repo.m.settings().collectAsStateWithLifecycle(null)
    var editing by remember { mutableStateOf<Assignment?>(null) }
    val target = settings?.studyTargetMinutes ?: 210
    val todayMin = sessions.filter { it.date == today.toString() }.sumOf { it.minutes }

    Screen("Academic comeback", onBack) {
        SectionCard("Today's focus") {
            LabeledProgress("$todayMin of $target min studied", todayMin / target.toFloat(), "${todayMin / 60}h ${todayMin % 60}m")
            Text("Target is 3 to 4 hours of focused study. Edit it in Profile.", style = MaterialTheme.typography.labelMedium)
        }
        SectionCard("Assignments, all due 30 Nov") {
            assignments.forEach { a ->
                val subject = subjects.firstOrNull { it.id == a.subjectId }?.name ?: ""
                Row(Modifier.fillMaxWidth().clickable { editing = a }, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(a.title, style = MaterialTheme.typography.titleMedium)
                        Text("$subject  \u2022  target ${shortDate(a.targetStart)} to ${shortDate(a.targetEnd)}  \u2022  due ${shortDate(a.deadline)}", style = MaterialTheme.typography.labelMedium)
                    }
                    FilledTonalButton(onClick = {
                        val next = STATUSES[(STATUSES.indexOf(a.status) + 1) % STATUSES.size]
                        scope.launch { repo.m.updateAssignment(a.copy(status = next)) }
                    }) { Text(statusLabel(a.status)) }
                }
            }
            Text("Tap a status to move it forward. Tap a title to edit dates.", style = MaterialTheme.typography.labelMedium)
        }
        subjects.forEach { s ->
            val t = topics.filter { it.subjectId == s.id }
            val pct = if (t.isEmpty()) 0f else t.count { it.done } / t.size.toFloat()
            Card(Modifier.fillMaxWidth().clickable { onOpen("subject/${s.id}") }) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(s.name, style = MaterialTheme.typography.titleMedium)
                    Text(s.kind, style = MaterialTheme.typography.labelMedium)
                    LabeledProgress(if (t.isEmpty()) "No topics yet. Add them from the syllabus." else "${t.count { it.done }} of ${t.size} topics done", pct, "${(pct * 100).toInt()}%")
                }
            }
        }
    }
    editing?.let { a ->
        InputDialog("Edit assignment", listOf(Field("Title", a.title), Field("Target start (yyyy-mm-dd)", a.targetStart), Field("Target end (yyyy-mm-dd)", a.targetEnd), Field("Deadline (yyyy-mm-dd)", a.deadline)), onDismiss = { editing = null }) { v ->
            val ok = v.drop(1).all { runCatching { LocalDate.parse(it.trim()) }.isSuccess }
            if (ok && v[0].isNotBlank()) scope.launch { repo.m.updateAssignment(a.copy(title = v[0].trim(), targetStart = v[1].trim(), targetEnd = v[2].trim(), deadline = v[3].trim())) }
            editing = null
        }
    }
}

@Composable
fun SubjectScreen(id: Long, onBack: () -> Unit) {
    val repo = rememberRepo(); val scope = rememberCoroutineScope(); val today = rememberToday()
    val subject by repo.m.subject(id).collectAsStateWithLifecycle(null)
    val topics by repo.m.topics(id).collectAsStateWithLifecycle(emptyList())
    val sessions by repo.m.studySessions().collectAsStateWithLifecycle(emptyList())
    var addTopic by remember { mutableStateOf(false) }
    var log by remember { mutableStateOf(false) }
    val mine = sessions.filter { it.subjectId == id }

    Screen(subject?.name ?: "Subject", onBack) {
        SectionCard("Syllabus topics") {
            if (topics.isEmpty()) Text("Add topics from your syllabus. The app has no access to your college PDFs.")
            topics.forEach { t ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(t.done, onCheckedChange = { scope.launch { repo.m.updateTopic(t.copy(done = it)) } })
                    Text(t.title, Modifier.weight(1f))
                    FilterChip(selected = t.revised, onClick = { scope.launch { repo.m.updateTopic(t.copy(revised = !t.revised)) } }, label = { Text("Revised") })
                    TextButton(onClick = { scope.launch { repo.m.deleteTopic(t) } }) { Text("X") }
                }
            }
            Text("${topics.count { it.done }} done, ${topics.count { !it.done }} pending, ${topics.count { it.revised }} revised", style = MaterialTheme.typography.labelMedium)
            OutlinedButton(onClick = { addTopic = true }) { Text("Add topic") }
        }
        SectionCard("Study and practical sessions") {
            Button(onClick = { log = true }) { Text("Log a session") }
            Text("Total: ${mine.sumOf { it.minutes }} min", style = MaterialTheme.typography.labelMedium)
            mine.take(15).forEach { s ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("${shortDate(s.date)}  \u2022  ${s.minutes} min  \u2022  " + (if (s.isPractical) "practical" else "study") + (s.notes?.let { "  \u2022  $it" } ?: ""), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { scope.launch { repo.m.deleteStudy(s) } }) { Text("Remove") }
                }
            }
        }
    }
    if (addTopic) InputDialog("New topic", listOf(Field("Topic")), onDismiss = { addTopic = false }) { v ->
        if (v[0].isNotBlank()) scope.launch { repo.m.insertTopic(Topic(subjectId = id, title = v[0].trim())) }
        addTopic = false
    }
    if (log) InputDialog("Log session", listOf(Field("Minutes", "60", numeric = true), Field("Practical coding? (yes or no)", if (subject?.kind == "Practical") "yes" else "no"), Field("Notes (optional)")), onDismiss = { log = false }) { v ->
        val min = v[0].toDoubleOrNull()?.toInt()
        if (min != null && min > 0) scope.launch {
            repo.m.insertStudy(StudySession(subjectId = id, date = today.toString(), minutes = min, isPractical = v[1].trim().lowercase().startsWith("y"), notes = v[2].ifBlank { null }))
            val total = sessions.filter { it.date == today.toString() }.sumOf { it.minutes } + min
            if (total >= 60) repo.autoComplete(today, "study_1")
            if (total >= 120) repo.autoComplete(today, "study_2")
        }
        log = false
    }
}
