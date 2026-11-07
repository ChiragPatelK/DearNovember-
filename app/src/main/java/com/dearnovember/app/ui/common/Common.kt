package com.dearnovember.app.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dearnovember.app.DearNovemberApp
import com.dearnovember.app.data.Repository
import com.dearnovember.app.domain.Challenge
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun rememberRepo(): Repository {
    val app = LocalContext.current.applicationContext as DearNovemberApp
    return remember { app.repository }
}

@Composable
fun rememberToday(): LocalDate = remember { Challenge.displayDate(LocalDate.now()) }

fun shortDate(iso: String): String = try { LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("d MMM")) } catch (e: Exception) { iso }

@Composable
fun Screen(title: String, onBack: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text(title, style = MaterialTheme.typography.headlineSmall)
        }
        content()
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun SectionCard(title: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (title != null) Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

data class Field(val label: String, val initial: String = "", val multiline: Boolean = false, val numeric: Boolean = false)

@Composable
fun InputDialog(title: String, fields: List<Field>, onDismiss: () -> Unit, onDelete: (() -> Unit)? = null, confirmLabel: String = "Save", onConfirm: (List<String>) -> Unit) {
    val values = remember { mutableStateListOf<String>().also { l -> fields.forEach { l.add(it.initial) } } }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                fields.forEachIndexed { i, f ->
                    OutlinedTextField(
                        value = values[i], onValueChange = { values[i] = it }, label = { Text(f.label) },
                        minLines = if (f.multiline) 5 else 1, singleLine = !f.multiline,
                        keyboardOptions = if (f.numeric) KeyboardOptions(keyboardType = KeyboardType.Decimal) else KeyboardOptions.Default,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (onDelete != null) TextButton(onClick = onDelete) { Text("Delete") }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(values.toList()) }) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
fun ConfirmDialog(title: String, text: String, confirmLabel: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text(title) }, text = { Text(text) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
fun TabsRow(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    TabRow(selectedTabIndex = selected, containerColor = androidx.compose.ui.graphics.Color.Transparent) {
        labels.forEachIndexed { i, l -> Tab(selected = selected == i, onClick = { onSelect(i) }, text = { Text(l) }) }
    }
}

@Composable
fun LabeledProgress(label: String, value: Float, trailing: String) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium); Text(trailing, style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(progress = { value.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(8.dp))
    }
}
