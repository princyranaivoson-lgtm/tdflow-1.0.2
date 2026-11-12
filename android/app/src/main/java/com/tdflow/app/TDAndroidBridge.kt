package com.tdflow.app

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.WindowManager
import android.webkit.JavascriptInterface
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.atomic.AtomicInteger

/** Pont minimal demandé par l'HTML historique : bibliothèque locale + sauvegarde/partage MP4. */
class TDAndroidBridge(private val activity: Activity) {
    private val resolver = activity.contentResolver
    private var output: java.io.OutputStream? = null
    private var pendingUri: Uri? = null
    private var pendingName: String = "tdflow-video.mp4"
    private var lastSaved: Uri? = null
    private val requestCode = AtomicInteger(4781)
    private val musicDir: File by lazy { File(activity.filesDir, "tdflow/music").apply { mkdirs() } }

    private fun audioPermission(): String? = when {
        Build.VERSION.SDK_INT >= 33 -> Manifest.permission.READ_MEDIA_AUDIO
        Build.VERSION.SDK_INT >= 23 -> Manifest.permission.READ_EXTERNAL_STORAGE
        else -> null
    }

    private fun hasAudioPermission(): Boolean {
        val p = audioPermission() ?: return true
        return ContextCompat.checkSelfPermission(activity, p) == PackageManager.PERMISSION_GRANTED
    }

    @JavascriptInterface
    fun requestMusicPermission(): String {
        val p = audioPermission() ?: return "granted"
        if (hasAudioPermission()) return "granted"
        activity.runOnUiThread { ActivityCompat.requestPermissions(activity, arrayOf(p), requestCode.get()) }
        return "requested"
    }

    @JavascriptInterface
    fun listMusic(): String {
        if (!hasAudioPermission()) return JSONObject().put("permissionRequired", true).toString()
        val items = JSONArray()
        val collection = if (Build.VERSION.SDK_INT >= 29) MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL) else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.MIME_TYPE)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        try {
            resolver.query(collection, projection, selection, null, "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC")?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val mimeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val uri = Uri.withAppendedPath(collection, id.toString())
                    items.put(JSONObject().put("id", id.toString()).put("title", c.getString(titleCol) ?: "Sans titre")
                        .put("artist", c.getString(artistCol) ?: "Artiste inconnu").put("album", c.getString(albumCol) ?: "Album inconnu")
                        .put("duration", c.getLong(durCol)).put("albumId", c.getLong(albumIdCol)).put("mime", c.getString(mimeCol) ?: "audio/*").put("uri", uri.toString()))
                }
            }
            return JSONObject().put("permissionRequired", false).put("tracks", items).toString()
        } catch (e: Exception) {
            return JSONObject().put("permissionRequired", false).put("error", e.message ?: "Lecture MediaStore impossible").put("tracks", items).toString()
        }
    }

    /** Copie le morceau autorisé dans l'espace privé persistant de l'application, sans éditer l'original. */
    @JavascriptInterface
    fun importMusic(uriText: String): String {
        if (!hasAudioPermission()) return JSONObject().put("error", "permission").toString()
        return try {
            val uri = Uri.parse(uriText)
            val name = "track_${System.currentTimeMillis()}_${uri.lastPathSegment ?: "audio"}.bin"
            val outFile = File(musicDir, name)
            val input = resolver.openInputStream(uri) ?: error("Fichier audio indisponible ou accès révoqué")
            input.use { source -> outFile.outputStream().use { destination -> source.copyTo(destination) } }
            JSONObject().put("key", name).put("size", outFile.length()).toString()
        } catch (e: Exception) { JSONObject().put("error", e.message ?: "Import audio impossible").toString() }
    }

    /** Lecture découpée pour éviter de sérialiser un gros fichier audio en un seul appel WebView. */
    @JavascriptInterface
    fun readMusicChunk(key: String, offset: Long, length: Int): String {
        return try {
            val f = File(musicDir, File(key).name)
            require(f.isFile && f.canonicalPath.startsWith(musicDir.canonicalPath + File.separator)) { "Fichier importé invalide" }
            val count = length.coerceIn(1, 262144).coerceAtMost((f.length() - offset).coerceAtLeast(0).toInt())
            val bytes = ByteArray(count)
            RandomAccessFile(f, "r").use { raf -> raf.seek(offset.coerceAtLeast(0)); raf.readFully(bytes) }
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (_: Exception) { "" }
    }

    @JavascriptInterface fun keepOn(enabled: Boolean) {
        activity.runOnUiThread { if (enabled) activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) else activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    @JavascriptInterface
    fun beginSave(name: String) {
        try {
            output?.close(); output = null
            pendingName = (File(name).name.ifBlank { "tdflow-video.mp4" }).take(120)
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, pendingName)
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT >= 29) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/TDFlow")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }
            val collection = if (Build.VERSION.SDK_INT >= 29) MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) else MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            pendingUri = resolver.insert(collection, values)
            output = pendingUri?.let { resolver.openOutputStream(it, "w") }
        } catch (_: Exception) { pendingUri = null; output = null }
    }

    @JavascriptInterface
    fun chunk(base64: String) {
        try { output?.write(Base64.decode(base64, Base64.DEFAULT)) } catch (_: Exception) { }
    }

    @JavascriptInterface
    fun endSave(): String {
        return try {
            output?.flush(); output?.close(); output = null
            val uri = pendingUri ?: return "error"
            if (Build.VERSION.SDK_INT >= 29) resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
            lastSaved = uri; pendingUri = null; "ok"
        } catch (_: Exception) { try { output?.close() } catch (_: Exception) {}; output = null; "error" }
    }

    @JavascriptInterface
    fun shareLast() {
        val uri = lastSaved ?: return
        activity.runOnUiThread {
            val intent = Intent(Intent.ACTION_SEND).apply { type = "video/mp4"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            activity.startActivity(Intent.createChooser(intent, "Partager la vidéo TDFlow"))
        }
    }
}
