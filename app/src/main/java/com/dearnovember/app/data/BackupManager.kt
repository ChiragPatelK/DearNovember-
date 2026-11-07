package com.dearnovember.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class BackupException(message: String) : Exception(message)

data class BackupInfo(val createdAt: Long, val formatVersion: Int, val dbVersion: Int, val photoCount: Int, val appVersion: String)

/**
 * Backup ZIP layout (format 1):
 *   manifest.json            {formatVersion, createdAt, dbVersion, dbSha256, appVersion}
 *   database/dear_november.db  consistent snapshot of every table (journal, letters, scores, logs, settings...)
 *   photos/<file>            progress photos
 * Nothing leaves the device unless the user chooses an export destination.
 */
object BackupManager {
    const val FORMAT_VERSION = 1
    private const val DB_ENTRY = "database/dear_november.db"

    fun backupDir(ctx: Context): File = File(ctx.filesDir, "backups").apply { mkdirs() }
    private fun photosDir(ctx: Context): File = File(ctx.filesDir, "photos").apply { mkdirs() }
    private fun stamp() = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

    private fun sha256(input: InputStream): String {
        val md = MessageDigest.getInstance("SHA-256")
        val buf = ByteArray(8192)
        while (true) { val n = input.read(buf); if (n < 0) break; md.update(buf, 0, n) }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    fun writeZip(ctx: Context, out: OutputStream) {
        val db = AppDatabase.get(ctx)
        db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(TRUNCATE)").use { it.moveToFirst() }
        val dbFile = ctx.getDatabasePath(AppDatabase.NAME)
        val photos = photosDir(ctx).listFiles()?.filter { it.isFile } ?: emptyList()
        val manifest = JSONObject()
            .put("formatVersion", FORMAT_VERSION).put("createdAt", System.currentTimeMillis())
            .put("dbVersion", AppDatabase.VERSION).put("appVersion", "0.2.0")
            .put("dbSha256", dbFile.inputStream().use { sha256(it) }).put("photoCount", photos.size)
        ZipOutputStream(out).use { zip ->
            zip.putNextEntry(ZipEntry("manifest.json")); zip.write(manifest.toString(2).toByteArray()); zip.closeEntry()
            zip.putNextEntry(ZipEntry(DB_ENTRY)); dbFile.inputStream().use { it.copyTo(zip) }; zip.closeEntry()
            photos.forEach { f -> zip.putNextEntry(ZipEntry("photos/${f.name}")); f.inputStream().use { it.copyTo(zip) }; zip.closeEntry() }
        }
    }

    /** label: "auto" and "manual" are pruned by retention; "prerestore" and "archive" are never pruned. */
    suspend fun createLocalBackup(ctx: Context, label: String = "auto"): File {
        val repo = AppDatabase.get(ctx).moduleDao()
        val file = File(backupDir(ctx), "${label}_${stamp()}.zip")
        try {
            file.outputStream().use { writeZip(ctx, it) }
            repo.insertBackup(BackupHistory(createdAt = System.currentTimeMillis(), path = file.name, sizeBytes = file.length(), success = true))
            if (label == "auto" || label == "manual") prune(ctx, repo.settingsOnce()?.backupKeep ?: 7)
        } catch (e: Exception) {
            file.delete()
            repo.insertBackup(BackupHistory(createdAt = System.currentTimeMillis(), path = file.name, sizeBytes = 0, success = false, message = e.javaClass.simpleName))
            throw e
        }
        return file
    }

    private fun prune(ctx: Context, keep: Int) {
        val files = backupDir(ctx).listFiles { f -> f.name.startsWith("auto_") || f.name.startsWith("manual_") }
            ?.sortedByDescending { it.lastModified() } ?: return
        val limit = keep.coerceAtLeast(1)
        // Never delete the only remaining backup.
        if (files.size > limit) files.drop(limit).forEach { it.delete() }
    }

    fun inspect(ctx: Context, zipFile: File): BackupInfo {
        try {
            ZipFile(zipFile).use { zip ->
                val me = zip.getEntry("manifest.json") ?: throw BackupException("This is not a Dear November backup (no manifest).")
                val manifest = JSONObject(zip.getInputStream(me).bufferedReader().readText())
                val format = manifest.getInt("formatVersion")
                if (format > FORMAT_VERSION) throw BackupException("This backup was made by a newer version of the app.")
                val dbVersion = manifest.getInt("dbVersion")
                if (dbVersion > AppDatabase.VERSION) throw BackupException("This backup uses a newer database version.")
                val de = zip.getEntry(DB_ENTRY) ?: throw BackupException("The backup has no database.")
                if (zip.getInputStream(de).use { sha256(it) } != manifest.getString("dbSha256")) throw BackupException("The backup file is damaged (checksum mismatch).")
                val tmp = File(ctx.cacheDir, "inspect.db")
                zip.getInputStream(de).use { i -> tmp.outputStream().use { i.copyTo(it) } }
                try {
                    SQLiteDatabase.openDatabase(tmp.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
                        db.rawQuery("PRAGMA integrity_check", null).use { c -> if (!c.moveToFirst() || c.getString(0) != "ok") throw BackupException("The backup database failed its integrity check.") }
                    }
                } finally { tmp.delete() }
                return BackupInfo(manifest.getLong("createdAt"), format, dbVersion, manifest.optInt("photoCount"), manifest.optString("appVersion"))
            }
        } catch (e: BackupException) { throw e
        } catch (e: Exception) { throw BackupException("Could not read this file as a Dear November backup.") }
    }

    /** Call [inspect] first and take a "prerestore" backup. The caller must restart the app afterwards. */
    fun restore(ctx: Context, zipFile: File) {
        AppDatabase.close()
        val dbFile = ctx.getDatabasePath(AppDatabase.NAME)
        File(dbFile.path + "-wal").delete(); File(dbFile.path + "-shm").delete()
        ZipFile(zipFile).use { zip ->
            zip.getInputStream(zip.getEntry(DB_ENTRY)).use { i -> dbFile.outputStream().use { i.copyTo(it) } }
            photosDir(ctx).listFiles()?.forEach { it.delete() }
            zip.entries().asSequence().filter { it.name.startsWith("photos/") && !it.isDirectory }.forEach { e ->
                val safe = File(e.name).name // blocks path traversal
                zip.getInputStream(e).use { i -> File(photosDir(ctx), safe).outputStream().use { i.copyTo(it) } }
            }
        }
    }

    fun archiveDir(ctx: Context) = backupDir(ctx)
}
