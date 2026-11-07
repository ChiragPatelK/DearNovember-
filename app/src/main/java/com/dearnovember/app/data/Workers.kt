package com.dearnovember.app.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.dearnovember.app.MainActivity
import com.dearnovember.app.R
import com.dearnovember.app.domain.ReminderTime
import com.dearnovember.app.domain.Seeds
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class BackupWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result = try {
        BackupManager.createLocalBackup(applicationContext, "auto"); Result.success()
    } catch (e: Exception) { Result.retry() }
}

object BackupScheduler {
    private const val NAME = "auto_backup"
    fun apply(ctx: Context, enabled: Boolean) {
        val wm = WorkManager.getInstance(ctx)
        if (!enabled) { wm.cancelUniqueWork(NAME); return }
        val req = PeriodicWorkRequestBuilder<BackupWorker>(24, TimeUnit.HOURS).build()
        wm.enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.KEEP, req)
    }
}

class ReminderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val key = inputData.getString("key") ?: return Result.success()
        val r = AppDatabase.get(applicationContext).moduleDao().reminder(key) ?: return Result.success()
        if (!r.enabled) return Result.success()
        Reminders.notify(applicationContext, r)
        Reminders.schedule(applicationContext, r)
        return Result.success()
    }
}

object Reminders {
    private const val CHANNEL = "reminders"

    fun ensureChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = ctx.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(NotificationChannel(CHANNEL, "Reminders", NotificationManager.IMPORTANCE_DEFAULT))
        }
    }

    fun schedule(ctx: Context, r: Reminder) {
        val wm = WorkManager.getInstance(ctx)
        val name = "reminder_${r.key}"
        if (!r.enabled) { wm.cancelUniqueWork(name); return }
        val next = ReminderTime.next(r.key, r.hour, r.minute, LocalDateTime.now()) ?: run { wm.cancelUniqueWork(name); return }
        val delay = Duration.between(LocalDateTime.now(), next).toMillis().coerceAtLeast(1_000)
        val req = OneTimeWorkRequestBuilder<ReminderWorker>().setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("key" to r.key)).build()
        wm.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, req)
    }

    fun scheduleAll(ctx: Context, list: List<Reminder>) = list.forEach { schedule(ctx, it) }

    fun notify(ctx: Context, r: Reminder) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        ensureChannel(ctx)
        val pi = PendingIntent.getActivity(ctx, r.key.hashCode(), Intent(ctx, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(ctx, CHANNEL).setSmallIcon(R.drawable.ic_fg)
            .setContentTitle("Dear November").setContentText(Seeds.reminderText[r.key] ?: r.label)
            .setContentIntent(pi).setAutoCancel(true).build()
        ctx.getSystemService(NotificationManager::class.java).notify(r.key.hashCode(), n)
    }
}
