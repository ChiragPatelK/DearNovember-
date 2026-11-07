package com.dearnovember.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dearnovember.app.data.JournalEntry
import com.dearnovember.app.data.Letter
import com.dearnovember.app.ui.common.*
import kotlinx.coroutines.launch

/** Everything here stays on the device and is included in backups. Nothing is logged or sent. */
@Composable
fun JournalScreen() {
    val repo = rememberRepo(); val scope = rememberCoroutineScope(); val today = rememberToday()
    val entries by repo.journal.observeEntries().collectAsStateWithLifecycle(emptyList())
    val letters by repo.journal.observeLetters().collectAsStateWithLifecycle(emptyList())
    var tab by remember { mutableIntStateOf(0) }
    var editEntry by remember { mutableStateOf<JournalEntry?>(null) }
    var newEntry by remember { mutableStateOf(false) }
    var editLetter by remember { mutableStateOf<Letter?>(null) }
    var newLetter by remember { mutableStateOf(false) }
    var deleteEntry by remember { mutableStateOf<JournalEntry?>(null) }
    var deleteLetter by remember { mutableStateOf<Letter?>(null) }

    Screen("Journal") {
        TabsRow(listOf("Personal journal", "Letters to Her"), tab) { tab = it }
        if (tab == 0) {
            Text("Private thoughts, wins, failures and lessons. Only on this phone.", style = MaterialTheme.typography.bodyMedium)
            Button(onClick = { newEntry = true }) { Text("Write an entry") }
            if (entries.isEmpty()) Text("Nothing here yet. One honest sentence is enough to start.")
            entries.forEach { e ->
                Card(Modifier.fillMaxWidth().clickable { editEntry = e }) {
                    Column(Modifier.padding(16.dp)) {
                        Text(shortDate(e.date), style = MaterialTheme.typography.labelMedium)
                        Text(e.title.ifBlank { "Untitled" }, style = MaterialTheme.typography.titleMedium)
                        Text(e.body, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            Text("Unsent letters. They are never shared or sent anywhere.", style = MaterialTheme.typography.bodyMedium)
            Button(onClick = { newLetter = true }) { Text("Write a letter") }
            if (letters.isEmpty()) Text("No letters yet. Say what you need to say. It stays here.")
            letters.forEach { l ->
                Card(Modifier.fillMaxWidth().clickable { editLetter = l }) {
                    Column(Modifier.padding(16.dp)) {
                        Text(shortDate(l.date) + (l.mood?.let { "  \u2022  mood $it/5" } ?: ""), style = MaterialTheme.typography.labelMedium)
                        Text(l.title.ifBlank { "Untitled" }, style = MaterialTheme.typography.titleMedium)
                        Text(l.body, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    if (newEntry || editEntry != null) {
        val e = editEntry
        InputDialog(if (e == null) "New entry" else "Edit entry",
            listOf(Field("Title", e?.title ?: ""), Field("What's on your mind?", e?.body ?: "", multiline = true)),
            onDismiss = { newEntry = false; editEntry = null },
            onDelete = if (e != null) ({ deleteEntry = e; editEntry = null }) else null) { v ->
            val now = System.currentTimeMillis()
            scope.launch {
                if (e == null) repo.journal.insert(JournalEntry(date = today.toString(), title = v[0].trim(), body = v[1], createdAt = now, updatedAt = now))
                else repo.journal.update(e.copy(title = v[0].trim(), body = v[1], updatedAt = now))
            }
            newEntry = false; editEntry = null
        }
    }
    if (newLetter || editLetter != null) {
        val l = editLetter
        InputDialog(if (l == null) "New letter" else "Edit letter",
            listOf(Field("Title", l?.title ?: ""), Field("Dear...", l?.body ?: "", multiline = true), Field("Mood 1 to 5 (optional)", l?.mood?.toString() ?: "", numeric = true)),
            onDismiss = { newLetter = false; editLetter = null },
            onDelete = if (l != null) ({ deleteLetter = l; editLetter = null }) else null) { v ->
            val now = System.currentTimeMillis()
            val mood = v[2].toDoubleOrNull()?.toInt()?.coerceIn(1, 5)
            scope.launch {
                if (l == null) repo.journal.insertLetter(Letter(date = today.toString(), title = v[0].trim(), body = v[1], mood = mood, createdAt = now, updatedAt = now))
                else repo.journal.updateLetter(l.copy(title = v[0].trim(), body = v[1], mood = mood, updatedAt = now))
            }
            newLetter = false; editLetter = null
        }
    }
    deleteEntry?.let { e ->
        ConfirmDialog("Delete entry?", "This cannot be undone.", "Delete", { deleteEntry = null }) { scope.launch { repo.journal.delete(e) }; deleteEntry = null }
    }
    deleteLetter?.let { l ->
        ConfirmDialog("Delete letter?", "This cannot be undone.", "Delete", { deleteLetter = null }) { scope.launch { repo.journal.deleteLetter(l) }; deleteLetter = null }
    }
}
