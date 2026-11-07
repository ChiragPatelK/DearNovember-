package com.dearnovember.app.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dearnovember.app.AppLock
import com.dearnovember.app.data.*
import com.dearnovember.app.ui.common.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun fmt(ms: Long) = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(Date(ms))

private fun restartApp(ctx: Context) {
    val intent = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) }
    if (intent != null) ctx.startActivity(intent)
    android.os.Process.killProcess(android.os.Process.myPid())
}

@Composable
fun ProfileScreen() {
    val ctx = LocalContext.current
    val repo = rememberRepo(); val scope = rememberCoroutineScope()
    val settings by repo.m.settings().collectAsStateWithLifecycle(null)
    val reminders by repo.m.reminders().collectAsStateWithLifecycle(emptyList())
    val backups by repo.m.recentBackups().collectAsStateWithLifecycle(emptyList())
    var msg by remember { mutableStateOf<String?>(null) }
    var pending by remember { mutableStateOf<Pair<File, BackupInfo>?>(null) }
    var protein by remember { mutableStateOf(false) }
    var study by remember { mutableStateOf(false) }
    var resetAsk by remember { mutableStateOf(false) }
    val s = settings ?: return Screen("Profile") { Text("Loading...") }

    fun update(n: AppSettings) { scope.launch { repo.m.upsertSettings(n) } }

    val notifPerm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) msg = "Notifications are blocked in system settings, so reminders will stay silent."
    }
    val exportPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) scope.launch {
            msg = try {
                withContext(Dispatchers.IO) { ctx.contentResolver.openOutputStream(uri)?.use { BackupManager.writeZip(ctx, it) } ?: throw IllegalStateException() }
                "Backup exported."
            } catch (e: Exception) { "Export failed. Pick another location and try again." }
        }
    }
    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            msg = try {
                val tmp = File(ctx.cacheDir, "import.zip")
                withContext(Dispatchers.IO) { ctx.contentResolver.openInputStream(uri)?.use { i -> tmp.outputStream().use { i.copyTo(it) } } ?: throw BackupException("Could not open that file.") }
                val info = withContext(Dispatchers.IO) { BackupManager.inspect(ctx, tmp) }
                pending = tmp to info
                null
            } catch (e: BackupException) { e.message } catch (e: Exception) { "Could not read that file." }
        }
    }

    Screen("Profile and settings") {
        Text("Dear November: a promise to become someone I'm proud of.", style = MaterialTheme.typography.bodyLarge)
        msg?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

        SectionCard("Appearance") {
            Text("Style", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ROMANTIC" to "Romantic anime", "CINEMATIC" to "Cinematic").forEach { (k, l) -> FilterChip(selected = s.style == k, onClick = { update(s.copy(style = k)) }, label = { Text(l) }) }
            }
            Text("Mode", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("SYSTEM" to "System", "LIGHT" to "Light", "DARK" to "Night").forEach { (k, l) -> FilterChip(selected = s.mode == k, onClick = { update(s.copy(mode = k)) }, label = { Text(l) }) }
            }
        }
        SectionCard("Daily targets") {
            Text("Protein: ${s.proteinTargetGrams} g (estimate)"); OutlinedButton(onClick = { protein = true }) { Text("Change protein target") }
            Text("Study: ${s.studyTargetMinutes} min"); OutlinedButton(onClick = { study = true }) { Text("Change study target") }
            Text("Scoring is fixed at 100 points across six categories so every day stays comparable.", style = MaterialTheme.typography.labelMedium)
        }
        SectionCard("Reminders") {
            Text("Off by default. Reminders only run during 7 to 30 Nov.", style = MaterialTheme.typography.labelMedium)
            reminders.forEach { r ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(r.label, style = MaterialTheme.typography.bodyMedium)
                        if (r.key != "wake") TextButton(onClick = {
                            TimePickerDialog(ctx, { _, h, m ->
                                val n = r.copy(hour = h, minute = m)
                                scope.launch { repo.m.upsertReminder(n); Reminders.schedule(ctx, n) }
                            }, r.hour, r.minute, true).show()
                        }) { Text("%02d:%02d".format(r.hour, r.minute)) }
                    }
                    Switch(checked = r.enabled, onCheckedChange = { on ->
                        if (on && Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) notifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
                        val n = r.copy(enabled = on)
                        scope.launch { repo.m.upsertReminder(n); Reminders.schedule(ctx, n) }
                    })
                }
            }
        }
        SectionCard("Privacy and app lock") {
            Text("Everything stays on this phone. No account, no cloud, no analytics, no ads.", style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Lock with fingerprint, face or screen lock", Modifier.weight(1f))
                Switch(checked = s.appLock, onCheckedChange = { on ->
                    val act = ctx as? FragmentActivity
                    if (!on) update(s.copy(appLock = false))
                    else if (act != null && AppLock.available(act)) AppLock.prompt(act) { update(s.copy(appLock = true)) }
                    else msg = "Set up a screen lock or biometrics in system settings first."
                })
            }
        }
        SectionCard("Backup and restore") {
            val last = backups.firstOrNull { it.success }
            Text("Last successful backup: ${last?.let { fmt(it.createdAt) } ?: "none yet"}", style = MaterialTheme.typography.bodyMedium)
            backups.firstOrNull()?.let { if (!it.success) Text("Latest attempt failed (${it.message ?: "unknown"}).", color = MaterialTheme.colorScheme.error) }
            Text("Automatic backups go to the app's private folder once a day. Exports go wherever you choose.", style = MaterialTheme.typography.labelMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Automatic daily backup", Modifier.weight(1f))
                Switch(checked = s.backupEnabled, onCheckedChange = { on -> update(s.copy(backupEnabled = on)); BackupScheduler.apply(ctx, on) })
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Keep ${s.backupKeep} recent backups", Modifier.weight(1f))
                OutlinedButton(onClick = { update(s.copy(backupKeep = (s.backupKeep - 1).coerceAtLeast(1))) }) { Text("-") }
                OutlinedButton(onClick = { update(s.copy(backupKeep = (s.backupKeep + 1).coerceAtMost(30))) }) { Text("+") }
            }
            Button(onClick = { scope.launch { msg = try { withContext(Dispatchers.IO) { BackupManager.createLocalBackup(ctx, "manual") }; "Backup saved on this phone." } catch (e: Exception) { "Backup failed." } } }) { Text("Back up now") }
            OutlinedButton(onClick = { exportPicker.launch("DearNovember_backup_${SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())}.zip") }) { Text("Export backup (choose location)") }
            OutlinedButton(onClick = { importPicker.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream")) }) { Text("Restore from backup") }
        }
        SectionCard("Archive and restart") {
            Text("Saves a permanent archive backup first, then clears this run's scores and logs. Your journal and letters are kept.", style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = { resetAsk = true }) { Text("Archive and start fresh") }
        }
        SectionCard("About") {
            Text("Dear November 0.2.0", style = MaterialTheme.typography.titleMedium)
            Text("A 24-day journey, 7 to 30 November 2026. Not a medical or mental-health tool. Nutrition values are estimates.", style = MaterialTheme.typography.bodyMedium)
        }
    }

    if (protein) InputDialog("Protein target (g)", listOf(Field("Grams", s.proteinTargetGrams.toString(), numeric = true)), onDismiss = { protein = false }) { v ->
        v[0].toDoubleOrNull()?.toInt()?.takeIf { it in 20..250 }?.let { update(s.copy(proteinTargetGrams = it)) }; protein = false
    }
    if (study) InputDialog("Study target (minutes)", listOf(Field("Minutes", s.studyTargetMinutes.toString(), numeric = true)), onDismiss = { study = false }) { v ->
        v[0].toDoubleOrNull()?.toInt()?.takeIf { it in 30..720 }?.let { update(s.copy(studyTargetMinutes = it)) }; study = false
    }
    pending?.let { (file, info) ->
        ConfirmDialog("Restore this backup?",
            "Backup from ${fmt(info.createdAt)} (format ${info.formatVersion}, database ${info.dbVersion}, ${info.photoCount} photos). It passed the integrity check.\n\nRestoring replaces ALL current data. A safety copy of your current data is saved first, and the app will restart.",
            "Restore", onDismiss = { pending = null }) {
            pending = null
            scope.launch {
                msg = try {
                    withContext(Dispatchers.IO) { BackupManager.createLocalBackup(ctx, "prerestore"); BackupManager.restore(ctx, file) }
                    restartApp(ctx); null
                } catch (e: Exception) { "Restore failed. Your safety copy is in the backups folder." }
            }
        }
    }
    if (resetAsk) InputDialog("Archive and start fresh", listOf(Field("Type RESET to confirm")), onDismiss = { resetAsk = false }, confirmLabel = "Archive and reset") { v ->
        resetAsk = false
        if (v[0].trim() == "RESET") scope.launch {
            msg = try {
                withContext(Dispatchers.IO) { BackupManager.createLocalBackup(ctx, "archive") }
                repo.clearChallengeData(ctx); "Archived. A fresh run is ready."
            } catch (e: Exception) { "Archive failed, so nothing was cleared." }
        } else msg = "Reset cancelled."
    }
}
