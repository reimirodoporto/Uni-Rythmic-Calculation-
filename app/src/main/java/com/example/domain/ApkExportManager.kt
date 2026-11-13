package com.example.domain

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StoredApkRecord(
    val id: String,
    val fileName: String,
    val versionLabel: String,
    val sizeBytes: Long,
    val timestampMs: Long,
    val sha256Short: String,
    val shareUri: Uri,
    val storageLocationLabel: String
)

sealed interface ApkDownloadResult {
    data class Success(
        val record: StoredApkRecord,
        val message: String
    ) : ApkDownloadResult

    data class Error(val message: String) : ApkDownloadResult
}

object ApkExportManager {

    private const val EXPORT_DIR_NAME = "apk_exports"
    private const val APK_MIME_TYPE = "application/vnd.android.package-archive"

    private fun exportsDir(context: Context): File {
        val dir = File(context.filesDir, EXPORT_DIR_NAME)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun authority(context: Context): String = "${context.packageName}.fileprovider"

    suspend fun listPreviousApkDownloads(context: Context): List<StoredApkRecord> = withContext(Dispatchers.IO) {
        val records = mutableListOf<StoredApkRecord>()
        val seenNames = mutableSetOf<String>()

        // 1. App-archived APK exports (supports FileProvider email attachment across all API levels)
        val localFiles = exportsDir(context)
            .listFiles { file -> file.isFile && file.extension.equals("apk", ignoreCase = true) }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()

        for (file in localFiles) {
            seenNames.add(file.name)
            val uri = FileProvider.getUriForFile(context, authority(context), file)
            records.add(
                StoredApkRecord(
                    id = "local_${file.name}_${file.lastModified()}",
                    fileName = file.name,
                    versionLabel = extractVersionLabel(file.name),
                    sizeBytes = file.length(),
                    timestampMs = file.lastModified(),
                    sha256Short = computeFileSha256Short(file),
                    shareUri = uri,
                    storageLocationLabel = "downloads and internal archive"
                )
            )
        }

        // 2. Also query MediaStore.Downloads on Android 10+ for any prior exported Uni-Rythmics APKs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching {
                val projection = arrayOf(
                    MediaStore.Downloads._ID,
                    MediaStore.Downloads.DISPLAY_NAME,
                    MediaStore.Downloads.SIZE,
                    MediaStore.Downloads.DATE_MODIFIED
                )
                val selection = "${MediaStore.Downloads.DISPLAY_NAME} LIKE ?"
                val selectionArgs = arrayOf("unirythmics%.apk")
                val sortOrder = "${MediaStore.Downloads.DATE_MODIFIED} DESC"

                context.contentResolver.query(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                    val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME)
                    val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Downloads.SIZE)
                    val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Downloads.DATE_MODIFIED)

                    while (cursor.moveToNext()) {
                        val rowId = cursor.getLong(idCol)
                        val name = cursor.getString(nameCol) ?: continue
                        if (seenNames.contains(name)) continue
                        seenNames.add(name)
                        val size = cursor.getLong(sizeCol)
                        val modifiedMs = cursor.getLong(dateCol) * 1000L
                        val contentUri = Uri.withAppendedPath(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                            rowId.toString()
                        )
                        records.add(
                            StoredApkRecord(
                                id = "ms_$rowId",
                                fileName = name,
                                versionLabel = extractVersionLabel(name),
                                sizeBytes = size,
                                timestampMs = modifiedMs,
                                sha256Short = "mediastore",
                                shareUri = contentUri,
                                storageLocationLabel = "device downloads"
                            )
                        )
                    }
                }
            }
        }

        records.sortedByDescending { it.timestampMs }
    }

    suspend fun downloadLatestApkToStorage(context: Context): ApkDownloadResult = withContext(Dispatchers.IO) {
        runCatching {
            val sourcePath = context.applicationInfo.publicSourceDir
                ?: context.applicationInfo.sourceDir
            if (sourcePath.isNullOrBlank()) {
                return@withContext ApkDownloadResult.Error("unable to locate compiled apk package on device")
            }
            val sourceApk = File(sourcePath)
            if (!sourceApk.exists() || !sourceApk.canRead()) {
                return@withContext ApkDownloadResult.Error("apk source file is not readable in current environment")
            }

            val now = System.currentTimeMillis()
            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(now))
            val safeVer = BuildConfig.VERSION_NAME.replace(".", "_")
            val fileName = "unirythmics_v${safeVer}_b${BuildConfig.VERSION_CODE}_$stamp.apk"

            // Archive copy in app files/apk_exports for FileProvider email attachments & version history
            val archiveFile = File(exportsDir(context), fileName)
            FileInputStream(sourceApk).use { input ->
                FileOutputStream(archiveFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Write directly to public Downloads via zero-permission MediaStore (API 29+) or app Downloads (API 26-28)
            val storageLabel: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, APK_MIME_TYPE)
                    put(
                        MediaStore.Downloads.RELATIVE_PATH,
                        "${Environment.DIRECTORY_DOWNLOADS}/UniRythmics"
                    )
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val itemUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: error("failed to create mediastore downloads entry")
                resolver.openOutputStream(itemUri)?.use { out ->
                    FileInputStream(archiveFile).use { input ->
                        input.copyTo(out)
                    }
                } ?: error("failed to open downloads output stream")

                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(itemUri, values, null, null)
                "downloads/unirythmics/$fileName"
            } else {
                val extDownloads = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: exportsDir(context)
                if (!extDownloads.exists()) extDownloads.mkdirs()
                val dest = File(extDownloads, fileName)
                FileInputStream(archiveFile).use { input ->
                    FileOutputStream(dest).use { out ->
                        input.copyTo(out)
                    }
                }
                dest.absolutePath
            }

            val shaShort = computeFileSha256Short(archiveFile)
            val shareUri = FileProvider.getUriForFile(context, authority(context), archiveFile)
            val record = StoredApkRecord(
                id = "local_${archiveFile.name}_$now",
                fileName = fileName,
                versionLabel = "v${BuildConfig.VERSION_NAME} b${BuildConfig.VERSION_CODE}",
                sizeBytes = archiveFile.length(),
                timestampMs = now,
                sha256Short = shaShort,
                shareUri = shareUri,
                storageLocationLabel = storageLabel.lowercase()
            )
            ApkDownloadResult.Success(
                record = record,
                message = "saved ${formatBytes(record.sizeBytes)} to $storageLabel"
            )
        }.getOrElse { err ->
            ApkDownloadResult.Error(
                (err.localizedMessage ?: "apk download failed")
                    .replace(Regex("[.#*()\\[\\]{}_\"-]"), " ")
                    .lowercase()
            )
        }
    }

    fun buildEmailApkIntent(
        context: Context,
        record: StoredApkRecord,
        recipientEmail: String
    ): Intent {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = APK_MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, record.shareUri)
            putExtra(Intent.EXTRA_SUBJECT, "UNIRYTHMICS APK ${record.versionLabel.uppercase()}")
            putExtra(
                Intent.EXTRA_TEXT,
                "attached apk ${record.fileName} size ${formatBytes(record.sizeBytes)} sha256 ${record.sha256Short}"
            )
            val cleanRecipient = recipientEmail.trim()
            if (cleanRecipient.isNotEmpty()) {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(cleanRecipient))
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(sendIntent, "SEND APK TO EMAIL")
    }

    fun formatBytes(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb >= 1.0) {
            String.format(Locale.US, "%.2f MB", mb)
        } else {
            val kb = bytes / 1024.0
            String.format(Locale.US, "%.1f KB", kb)
        }
    }

    fun formatTimestamp(ms: Long): String {
        if (ms <= 0L) return "unknown"
        return SimpleDateFormat("yyyy MM dd HH:mm", Locale.US).format(Date(ms))
    }

    private fun extractVersionLabel(fileName: String): String {
        val match = Regex("v([0-9_]+)_b([0-9]+)").find(fileName)
        return if (match != null) {
            val ver = match.groupValues[1].replace("_", ".")
            val build = match.groupValues[2]
            "v$ver b$build"
        } else {
            "v${BuildConfig.VERSION_NAME} b${BuildConfig.VERSION_CODE}"
        }
    }

    private fun computeFileSha256Short(file: File): String {
        return runCatching {
            val digest = MessageDigest.getInstance("SHA-256")
            FileInputStream(file).use { input ->
                val buf = ByteArray(8192)
                var read = input.read(buf)
                while (read != -1) {
                    digest.update(buf, 0, read)
                    read = input.read(buf)
                }
            }
            digest.digest().take(6).joinToString("") { "%02x".format(it) }
        }.getOrDefault("sha256")
    }
}
