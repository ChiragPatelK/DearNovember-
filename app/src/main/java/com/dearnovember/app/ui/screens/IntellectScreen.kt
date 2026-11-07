package com.dearnovember.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dearnovember.app.data.*
import com.dearnovember.app.domain.Prompts
import com.dearnovember.app.ui.common.*
import kotlinx.coroutines.launch

@Composable
fun IntellectScreen(onBack: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    Screen("Intellectual development", onBack) {
        TabsRow(listOf("Chess", "Rubik's Cube"), tab) { tab = it }
        if (tab == 0) ChessTab() else CubeTab()
    }
}

@Composable
private fun ChessTab() {
    val repo = rememberRepo(); val scope = rememberCoroutineScope(); val today = rememberToday(); val d = today.toString()
    val sessions by repo.m.chess().collectAsStateWithLifecycle(emptyList())
    var log by remember { mutableStateOf(false) }
    val todayMin = sessions.filter { it.date == d }.sumOf { it.minutes }
    val rating = sessions.firstOrNull { it.rating != null }?.rating
    SectionCard("Today") {
        LabeledProgress("$todayMin of 20 min", todayMin / 20f, if (todayMin >= 20) "done" else "daily target")
        Text("Puzzles: ${sessions.sumOf { it.puzzles }}  \u2022  W ${sessions.sumOf { it.wins }} / L ${sessions.sumOf { it.losses }} / D ${sessions.sumOf { it.draws }}", style = MaterialTheme.typography.bodyMedium)
        Text("Rating (entered by you): ${rating ?: "not set"}", style = MaterialTheme.typography.bodyMedium)
        Button(onClick = { log = true }) { Text("Log chess practice") }
    }
    SectionCard("History and mistakes") {
        if (sessions.isEmpty()) Text("No sessions yet.")
        sessions.take(15).forEach { s ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("${shortDate(s.date)}  \u2022  ${s.minutes} min  \u2022  ${s.puzzles} puzzles  \u2022  ${s.wins}-${s.losses}-${s.draws}" + (s.mistakes?.let { "\nMistake: $it" } ?: ""), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { scope.launch { repo.m.deleteChess(s) } }) { Text("Remove") }
            }
        }
    }
    if (log) InputDialog("Log chess", listOf(Field("Minutes", "20", numeric = true), Field("Puzzles solved", "0", numeric = true), Field("Wins", "0", numeric = true), Field("Losses", "0", numeric = true), Field("Draws", "0", numeric = true), Field("Rating (optional)", numeric = true), Field("One mistake to remember")), onDismiss = { log = false }) { v ->
        fun n(i: Int) = v[i].toDoubleOrNull()?.toInt() ?: 0
        val min = n(0)
        if (min > 0) scope.launch {
            repo.m.insertChess(ChessSession(date = d, minutes = min, puzzles = n(1), wins = n(2), losses = n(3), draws = n(4), rating = v[5].toDoubleOrNull()?.toInt(), mistakes = v[6].ifBlank { null }))
            if (todayMin + min >= 20) repo.autoComplete(today, "chess")
        }
        log = false
    }
}

@Composable
private fun CubeTab() {
    val repo = rememberRepo(); val scope = rememberCoroutineScope(); val today = rememberToday(); val d = today.toString()
    val stages by repo.m.stages().collectAsStateWithLifecycle(emptyList())
    val solves by repo.m.solves().collectAsStateWithLifecycle(emptyList())
    var log by remember { mutableStateOf(false) }
    val best = solves.filter { it.solved }.mapNotNull { it.seconds }.minOrNull()
    SectionCard("Learning path (beginner method)") {
        Prompts.cubeStages.forEachIndexed { i, title ->
            val st = stages.firstOrNull { it.stage == i + 1 }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(st?.done == true, onCheckedChange = { c -> scope.launch { repo.m.upsertStage(CubeStage(i + 1, c, if (c) d else null)); if (c) repo.autoComplete(today, "cube") } })
                Text("${i + 1}. $title")
            }
        }
        Text("${stages.count { it.done }} of ${Prompts.cubeStages.size} stages complete", style = MaterialTheme.typography.labelMedium)
    }
    SectionCard("Solves") {
        Text("Personal best: ${best?.let { "$it s" } ?: "no timed solve yet"}", style = MaterialTheme.typography.bodyMedium)
        Button(onClick = { log = true }) { Text("Log a solve or practice") }
        solves.take(15).forEach { s ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("${shortDate(s.date)}  \u2022  stage ${s.stage}  \u2022  " + (if (s.solved) "solved" + (s.seconds?.let { " in $it s" } ?: "") else "practice") + (s.notes?.let { "  \u2022  $it" } ?: ""), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { scope.launch { repo.m.deleteSolve(s) } }) { Text("Remove") }
            }
        }
    }
    if (log) InputDialog("Log cube practice", listOf(Field("Stage (1 to 7)", (stages.firstOrNull { !it.done }?.stage ?: 1).toString(), numeric = true), Field("Full solve? (yes or no)", "no"), Field("Time in seconds (optional)", numeric = true), Field("Notes")), onDismiss = { log = false }) { v ->
        val stage = (v[0].toDoubleOrNull()?.toInt() ?: 1).coerceIn(1, 7)
        scope.launch {
            repo.m.insertSolve(CubeSolve(date = d, stage = stage, solved = v[1].trim().lowercase().startsWith("y"), seconds = v[2].toDoubleOrNull()?.toInt(), notes = v[3].ifBlank { null }))
            repo.autoComplete(today, "cube")
        }
        log = false
    }
}
