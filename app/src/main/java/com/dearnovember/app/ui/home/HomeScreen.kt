package com.dearnovember.app.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dearnovember.app.domain.*
import com.dearnovember.app.ui.theme.LocalSky
import androidx.compose.foundation.clickable
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as DateTextStyle
import java.util.Locale

@Composable
fun HomeScreen(onOpen: (String) -> Unit = {}, vm: HomeViewModel = viewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Column(
                Modifier.fillMaxWidth()
                    .background(LocalSky.current)
                    .statusBarsPadding().padding(24.dp)
            ) {
                Text("Dear November", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(12.dp))
                val prefix = if (s.beforeStart) "Begins ${Challenge.START.format(DateTimeFormatter.ofPattern("d MMM"))}" else "Day ${s.day} of ${Challenge.TOTAL_DAYS}"
                Text(prefix, style = MaterialTheme.typography.bodyLarge)
                Text(s.chapter.title, style = MaterialTheme.typography.displaySmall)
                Text(
                    s.date.dayOfWeek.getDisplayName(DateTextStyle.FULL, Locale.getDefault()) + ", " +
                        s.date.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))
                Text("\u201C${s.chapter.quote}\u201D", style = MaterialTheme.typography.headlineSmall, fontStyle = FontStyle.Italic)
            }
        }
        item {
            Card(Modifier.padding(16.dp).fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    ScoreRing(s.score)
                    Spacer(Modifier.width(20.dp))
                    Column {
                        Text(Scoring.band(s.score).label, style = MaterialTheme.typography.headlineSmall)
                        Text("${s.rows.count { it.done }} of ${s.rows.size} tasks done", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("Streak: ${s.streak.current} (best ${s.streak.longest})", style = MaterialTheme.typography.bodyMedium)
                        Text("Challenge: ${(s.overallCompletion * 100).toInt()}% of points earned", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        item {
            Card(
                Modifier.padding(horizontal = 16.dp).fillMaxWidth(), shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Today's mission", style = MaterialTheme.typography.titleMedium)
                    Text(s.chapter.mission, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(10.dp))
                    Text(s.chapter.question, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
                }
            }
        }
        item {
            Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
                Text("Six pillars", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                val pillars = listOf("Body" to "physical", "Study" to "academics", "Mind" to "mental", "Voice" to "speaking", "Discipline" to "discipline", "Intellect" to "intellect")
                pillars.chunked(3).forEach { rowItems ->
                    Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowItems.forEach { (label, route) ->
                            Card(Modifier.weight(1f).clickable { onOpen(route) }, shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                                    Text(label, style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
        Category.entries.forEach { cat ->
            val rows = s.rows.filter { it.template.category == cat }
            item(key = "h_${cat.name}") {
                Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(cat.label, style = MaterialTheme.typography.titleMedium)
                    Text("${s.categoryScores[cat] ?: 0} / ${cat.maxPoints}", style = MaterialTheme.typography.labelMedium)
                }
            }
            items(rows, key = { it.template.key }) { row ->
                val name = if (row.recovery) row.template.recoveryName!! else row.template.name
                val desc = if (row.recovery) row.template.recoveryDescription!! else row.template.description
                ListItem(
                    headlineContent = { Text(name) },
                    supportingContent = { Text("$desc  \u2022  ${row.template.points} pts") },
                    leadingContent = { Checkbox(row.done, onCheckedChange = { vm.toggle(row.template.key, it) }) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
        }
    }
}

@Composable
private fun ScoreRing(score: Int) {
    val sweep by animateFloatAsState(score / 100f, tween(700), label = "score")
    val track = MaterialTheme.colorScheme.surfaceVariant
    val arc = MaterialTheme.colorScheme.primary
    Box(Modifier.size(104.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            val inset = 6.dp.toPx()
            val size = Size(this.size.width - 2 * inset, this.size.height - 2 * inset)
            drawArc(track, 0f, 360f, false, Offset(inset, inset), size, style = stroke)
            drawArc(arc, -90f, 360f * sweep, false, Offset(inset, inset), size, style = stroke)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$score", style = MaterialTheme.typography.headlineSmall)
            Text("of 100", style = MaterialTheme.typography.labelMedium)
        }
    }
}
