package com.dearnovember.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.dearnovember.app.domain.Challenge
import com.dearnovember.app.domain.Chapters
import com.dearnovember.app.ui.common.*
import java.time.LocalDate

@Composable
fun JourneyScreen(onOpen: (String) -> Unit) {
    val today = remember { LocalDate.now() }
    val todayDay = Challenge.dayNumber(Challenge.displayDate(today)) ?: 1
    var expanded by remember { mutableIntStateOf(todayDay) }
    Screen("Your journey") {
        Text("A promise to become someone I'm proud of.", style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic)
        val pillars = listOf(
            Triple("Physical transformation", "Workouts, vegetarian nutrition, progress photos", "physical"),
            Triple("Academic comeback", "Six subjects, topics, assignments by 30 Nov", "academics"),
            Triple("Mental and emotional growth", "Mood, check-ins, meditation, missions", "mental"),
            Triple("Communication and personality", "Speaking practice, vocabulary, topics", "speaking"),
            Triple("Discipline and productivity", "Sleep, wake-up, Instagram, focus timer", "discipline"),
            Triple("Intellectual development", "Chess and Rubik's Cube", "intellect"),
        )
        pillars.forEach { (title, sub, route) ->
            Card(Modifier.fillMaxWidth().clickable { onOpen(route) }) {
                Column(Modifier.padding(16.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(sub, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Text("24 chapters", style = MaterialTheme.typography.headlineSmall)
        Chapters.all.forEach { c ->
            val isToday = c.day == todayDay
            Card(
                Modifier.fillMaxWidth().clickable { expanded = if (expanded == c.day) -1 else c.day },
                colors = CardDefaults.cardColors(containerColor = if (isToday) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Day ${c.day}  \u2022  ${Challenge.dateOf(c.day).dayOfMonth} Nov" + if (isToday) "  \u2022  today" else "", style = MaterialTheme.typography.labelMedium)
                    Text(c.title, style = MaterialTheme.typography.titleMedium)
                    if (expanded == c.day) {
                        Text("\u201C${c.quote}\u201D", fontStyle = FontStyle.Italic, style = MaterialTheme.typography.bodyLarge)
                        Text("Mission: ${c.mission}", style = MaterialTheme.typography.bodyMedium)
                        Text("Reflect: ${c.question}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
