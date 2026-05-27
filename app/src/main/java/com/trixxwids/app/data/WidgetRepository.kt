package com.trixxwids.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream

class WidgetRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.widgetDao()

    fun getAllWidgets(): Flow<List<WidgetEntity>> = dao.getAllWidgets()

    suspend fun getWidgetById(id: Long): WidgetEntity? = dao.getWidgetById(id)

    suspend fun saveWidget(
        name: String,
        configJson: String,
        sizeTag: String,
        thumbnail: Bitmap?,
        existingId: Long? = null
    ): Long {
        val thumbPath = thumbnail?.let { saveThumbnail(it) }
        if (existingId != null) {
            val existing = dao.getWidgetById(existingId)
            val entity = WidgetEntity(
                id = existingId,
                name = name,
                configJson = configJson,
                sizeTag = sizeTag,
                thumbnailPath = thumbPath ?: existing?.thumbnailPath,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            dao.updateWidget(entity)
            return existingId
        } else {
            val entity = WidgetEntity(
                name = name,
                configJson = configJson,
                sizeTag = sizeTag,
                thumbnailPath = thumbPath
            )
            return dao.insertWidget(entity)
        }
    }

    suspend fun deleteWidget(id: Long) {
        val widget = dao.getWidgetById(id)
        if (widget != null) {
            widget.thumbnailPath?.let { path ->
                val file = File(path)
                if (file.exists()) file.delete()
            }
            dao.deleteWidgetById(id)
        }
    }

    suspend fun duplicateWidget(id: Long): Long? {
        val original = dao.getWidgetById(id) ?: return null
        val entity = WidgetEntity(
            name = "${original.name} (Copy)",
            configJson = original.configJson,
            sizeTag = original.sizeTag,
            thumbnailPath = original.thumbnailPath
        )
        return dao.insertWidget(entity)
    }

    fun loadThumbnail(path: String?): Bitmap? {
        if (path == null) return null
        val file = File(path)
        if (!file.exists()) return null
        return BitmapFactory.decodeFile(path)
    }

    private fun saveThumbnail(bitmap: Bitmap): String {
        val dir = File(context.filesDir, "thumbnails")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "thumb_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        }
        return file.absolutePath
    }

    fun getThumbnailDir(): File {
        val dir = File(context.filesDir, "thumbnails")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }
}
