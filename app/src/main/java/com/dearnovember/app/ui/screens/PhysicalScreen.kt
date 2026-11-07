package com.dearnovember.app.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dearnovember.app.data.*
import com.dearnovember.app.domain.WorkoutPlan
import com.dearnovember.app.ui.common.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PhysicalScreen(onBack: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    Screen("Physical transformation", onBack) {
        TabsRow(listOf("Workout", "Nutrition", "Body"), tab) { tab = it }
        when (tab) { 0 -> WorkoutTab(); 1 -> NutritionTab(); else -> BodyTab() }
    }
}

@Composable
private fun WorkoutTab() {
    val repo = rememberRepo(); val scope = rememberCoroutineScope(); val today = rememberToday(); val d = today.toString()
    val plan = remember { WorkoutPlan.forDay(today.dayOfWeek) }
    val exercises by repo.m.exercises().collectAsStateWithLifecycle(emptyList())
    val workout by repo.m.workout(d).collectAsStateWithLifecycle(null)
    val allSets by repo.m.allSets().collectAsStateWithLifecycle(emptyList())
    val workouts by repo.m.workouts().collectAsStateWithLifecycle(emptyList())
    var logFor by remember { mutableStateOf<Exercise?>(null) }
    var editing by remember { mutableStateOf<Exercise?>(null) }
    var adding by remember { mutableStateOf(false) }
    val byName = exercises.associateBy { it.name }

    SectionCard(plan.title) {
        Text(plan.note, style = MaterialTheme.typography.bodyMedium)
        Text("Never train through pain. Skip or use the easier variation.", style = MaterialTheme.typography.labelMedium)
        plan.exercises.mapNotNull { byName[it] }.forEach { ex ->
            val done = allSets.count { it.workoutId == workout?.id && it.exerciseId == ex.id }
            val best = allSets.filter { it.exerciseId == ex.id }.maxByOrNull { it.reps }
            Column(Modifier.padding(vertical = 6.dp)) {
                Text(ex.name, style = MaterialTheme.typography.titleMedium)
                Text("${ex.defaultSets} x ${ex.defaultReps}  \u2022  rest ${ex.restSeconds}s  \u2022  logged today: $done", style = MaterialTheme.typography.bodyMedium)
                Text(ex.instructions, style = MaterialTheme.typography.bodyMedium)
                ex.easierVariation?.let { Text("Easier: $it", style = MaterialTheme.typography.labelMedium) }
                if (best != null) Text("Best so far: ${best.reps}" + (best.weightKg?.let { " at $it kg" } ?: "") + ". Add a rep or a set when every set feels easy.", style = MaterialTheme.typography.labelMedium)
                Row { TextButton(onClick = { logFor = ex }) { Text("Log set") }; TextButton(onClick = { editing = ex }) { Text("Edit") } }
            }
        }
        val completed = workout?.completed == true
        Button(onClick = {
            scope.launch {
                val w = repo.ensureWorkout(today, plan.title)
                repo.m.updateWorkout(w.copy(completed = !completed))
                repo.setTaskDone(today, "workout", !completed)
            }
        }) { Text(if (completed) "Workout complete (tap to undo)" else "Mark workout complete") }
    }

    SectionCard("Personal records") {
        val groups = allSets.groupBy { it.exerciseId }
        if (groups.isEmpty()) Text("Log a set to start your records.")
        exercises.filter { it.id in groups }.forEach { ex ->
            val sets = groups[ex.id].orEmpty()
            val maxReps = sets.maxOf { it.reps }
            val maxW = sets.mapNotNull { it.weightKg }.maxOrNull()
            Text("${ex.name}: $maxReps reps" + (maxW?.let { ", heaviest $it kg" } ?: ""), style = MaterialTheme.typography.bodyMedium)
        }
    }
    SectionCard("Workout history") {
        if (workouts.isEmpty()) Text("No workouts yet.")
        workouts.take(10).forEach { w ->
            Text("${shortDate(w.date)}  \u2022  ${w.type}  \u2022  ${allSets.count { it.workoutId == w.id }} sets  \u2022  " + if (w.completed) "complete" else "partial", style = MaterialTheme.typography.bodyMedium)
        }
    }
    SectionCard("Exercise library") {
        exercises.forEach { ex -> Row(Modifier.fillMaxWidth().clickable { editing = ex }, horizontalArrangement = Arrangement.SpaceBetween) { Text(ex.name); Text("${ex.defaultSets} x ${ex.defaultReps}") } }
        OutlinedButton(onClick = { adding = true }) { Text("Add exercise") }
    }

    logFor?.let { ex ->
        InputDialog("Log ${ex.name}", listOf(Field("Reps (or minutes / seconds)", ex.defaultReps.toString(), numeric = true), Field("Weight in kg (optional)", numeric = true)),
            onDismiss = { logFor = null }) { v ->
            val reps = v[0].toDoubleOrNull()?.toInt()
            if (reps != null && reps > 0) scope.launch {
                val w = repo.ensureWorkout(today, plan.title)
                val n = repo.m.setCount(w.id, ex.id) + 1
                repo.m.insertSet(ExerciseSet(workoutId = w.id, exerciseId = ex.id, setNumber = n, reps = reps, weightKg = v[1].toDoubleOrNull()))
            }
            logFor = null
        }
    }
    if (adding || editing != null) {
        val ex = editing
        InputDialog(if (ex == null) "New exercise" else "Edit ${ex.name}",
            listOf(Field("Name", ex?.name ?: ""), Field("Instructions", ex?.instructions ?: "", multiline = true),
                Field("Sets", (ex?.defaultSets ?: 3).toString(), numeric = true), Field("Reps", (ex?.defaultReps ?: 10).toString(), numeric = true),
                Field("Rest seconds", (ex?.restSeconds ?: 60).toString(), numeric = true), Field("Easier variation", ex?.easierVariation ?: "")),
            onDismiss = { adding = false; editing = null }) { v ->
            if (v[0].isNotBlank()) scope.launch {
                val sets = v[2].toDoubleOrNull()?.toInt() ?: 3; val reps = v[3].toDoubleOrNull()?.toInt() ?: 10; val rest = v[4].toDoubleOrNull()?.toInt() ?: 60
                if (ex == null) repo.m.insertExercise(Exercise(name = v[0].trim(), instructions = v[1], defaultSets = sets, defaultReps = reps, restSeconds = rest, easierVariation = v[5].ifBlank { null }))
                else repo.m.updateExercise(ex.copy(name = v[0].trim(), instructions = v[1], defaultSets = sets, defaultReps = reps, restSeconds = rest, easierVariation = v[5].ifBlank { null }))
            }
            adding = false; editing = null
        }
    }
}

private val SLOTS = listOf("Breakfast", "Lunch", "Snack", "Dinner")

@Composable
private fun NutritionTab() {
    val repo = rememberRepo(); val scope = rememberCoroutineScope(); val today = rememberToday(); val d = today.toString()
    val foods by repo.m.foods().collectAsStateWithLifecycle(emptyList())
    val meals by repo.m.meals(d).collectAsStateWithLifecycle(emptyList())
    val water by repo.m.water(d).collectAsStateWithLifecycle(null)
    val settings by repo.m.settings().collectAsStateWithLifecycle(null)
    var slot by remember { mutableStateOf(SLOTS[0]) }
    var custom by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf(false) }
    val target = settings?.proteinTargetGrams ?: 80
    val protein = meals.sumOf { (it.proteinGramsEstimate ?: 0.0) * it.servings }
    val glasses = water?.glasses ?: 0

    SectionCard("Protein today (estimate)") {
        LabeledProgress("About ${protein.toInt()} g of $target g", (protein / target).toFloat(), "${((protein / target) * 100).toInt()}%")
        Text("Values are rough estimates and vary by brand and portion. No calorie counting needed.", style = MaterialTheme.typography.labelMedium)
        Text("Meals logged: ${meals.map { it.slot }.toSet().size} of ${SLOTS.size} slots", style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = { editTarget = true }) { Text("Change protein target") }
    }
    SectionCard("Water") {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { scope.launch { repo.m.upsertWater(WaterLog(d, (glasses - 1).coerceAtLeast(0))) } }) { Text("-") }
            Text("$glasses glasses", style = MaterialTheme.typography.titleMedium)
            OutlinedButton(onClick = { scope.launch { repo.m.upsertWater(WaterLog(d, glasses + 1)) } }) { Text("+") }
        }
    }
    SectionCard("Today's meals") {
        if (meals.isEmpty()) Text("Nothing logged yet.")
        meals.forEach { m ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${m.slot}: ${m.label}" + (m.proteinGramsEstimate?.let { " (~${(it * m.servings).toInt()} g)" } ?: " (protein unknown)"), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { scope.launch { repo.m.deleteMeal(m) } }) { Text("Remove") }
            }
        }
    }
    SectionCard("Add food") {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { SLOTS.forEach { s -> FilterChip(selected = slot == s, onClick = { slot = s }, label = { Text(s) }) } }
        foods.forEach { f ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(f.name); Text(f.servingLabel + (f.proteinGramsEstimate?.let { "  \u2022  ~${it.toInt()} g protein" } ?: "  \u2022  protein not set"), style = MaterialTheme.typography.labelMedium)
                }
                TextButton(onClick = { scope.launch { repo.m.insertMeal(Meal(date = d, slot = slot, foodId = f.id, label = f.name, servings = 1.0, proteinGramsEstimate = f.proteinGramsEstimate)) } }) { Text("Add") }
            }
        }
        OutlinedButton(onClick = { custom = true }) { Text("Add custom food") }
    }
    if (custom) InputDialog("Custom food", listOf(Field("Name"), Field("Serving (e.g. 1 bowl)"), Field("Protein g per serving (leave empty if unknown)", numeric = true)), onDismiss = { custom = false }) { v ->
        if (v[0].isNotBlank()) scope.launch { repo.m.insertFood(Food(name = v[0].trim(), servingLabel = v[1].ifBlank { "1 serving" }, proteinGramsEstimate = v[2].toDoubleOrNull(), isCustom = true)) }
        custom = false
    }
    if (editTarget) InputDialog("Daily protein target (g)", listOf(Field("Grams", target.toString(), numeric = true)), onDismiss = { editTarget = false }) { v ->
        val g = v[0].toDoubleOrNull()?.toInt()
        val s = settings
        if (g != null && g in 20..250 && s != null) scope.launch { repo.m.upsertSettings(s.copy(proteinTargetGrams = g)) }
        editTarget = false
    }
}

@Composable
private fun BodyTab() {
    val repo = rememberRepo(); val scope = rememberCoroutineScope(); val ctx = LocalContext.current
    val settings by repo.m.settings().collectAsStateWithLifecycle(null)
    val photos by repo.m.photos().collectAsStateWithLifecycle(emptyList())
    var pickSlot by remember { mutableStateOf("DAY1") }
    var editPushups by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) scope.launch {
            val slot = pickSlot
            val path = withContext(Dispatchers.IO) {
                val dir = File(ctx.filesDir, "photos").apply { mkdirs() }
                dir.listFiles()?.filter { it.name.startsWith(slot) }?.forEach { it.delete() }
                val f = File(dir, "${slot}_${System.currentTimeMillis()}.jpg")
                ctx.contentResolver.openInputStream(uri)?.use { i -> f.outputStream().use { i.copyTo(it) } }
                f.path
            }
            repo.m.upsertPhoto(ProgressPhoto(slot, path, System.currentTimeMillis()))
        }
    }
    SectionCard("Push-ups (continuous, good form)") {
        Text("Day 1 baseline: ${settings?.pushupBaseline?.toString() ?: "not set"}")
        Text("Day 24: ${settings?.pushupFinal?.toString() ?: "not set"}")
        Row { TextButton(onClick = { editPushups = "baseline" }) { Text("Set Day 1") }; TextButton(onClick = { editPushups = "final" }) { Text("Set Day 24") } }
    }
    SectionCard("Progress photos") {
        Text("Stored only on this phone and included in your backups.", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("DAY1" to "Day 1", "DAY24" to "Day 24").forEach { (slot, label) ->
                PhotoBox(photos.firstOrNull { it.slot == slot }, label) { pickSlot = slot; picker.launch("image/*") }
            }
        }
    }
    editPushups?.let { which ->
        InputDialog(if (which == "baseline") "Day 1 push-ups" else "Day 24 push-ups", listOf(Field("Continuous push-ups", numeric = true)), onDismiss = { editPushups = null }) { v ->
            val n = v[0].toDoubleOrNull()?.toInt(); val s = settings
            if (n != null && n >= 0 && s != null) scope.launch { repo.m.upsertSettings(if (which == "baseline") s.copy(pushupBaseline = n) else s.copy(pushupFinal = n)) }
            editPushups = null
        }
    }
}

@Composable
fun PhotoBox(photo: ProgressPhoto?, label: String, onClick: (() -> Unit)? = null) {
    val bmp = remember(photo?.path, photo?.takenAt) {
        photo?.let { p -> try { BitmapFactory.decodeFile(p.path, BitmapFactory.Options().apply { inSampleSize = 4 })?.asImageBitmap() } catch (e: Exception) { null } }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(150.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            if (bmp != null) Image(bmp, label, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else Text(if (onClick != null) "Add photo" else "No photo")
        }
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
