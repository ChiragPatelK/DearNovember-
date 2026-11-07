package com.dearnovember.app

import android.app.Application
import com.dearnovember.app.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DearNovemberApp : Application() {
    val repository: Repository by lazy { Repository(AppDatabase.get(this)) }
    override fun onCreate() {
        super.onCreate()
        Reminders.ensureChannel(this)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            repository.seedIfNeeded()
            val s = repository.m.settingsOnce()
            BackupScheduler.apply(this@DearNovemberApp, s?.backupEnabled ?: true)
            Reminders.scheduleAll(this@DearNovemberApp, repository.m.remindersOnce())
        }
    }
}
