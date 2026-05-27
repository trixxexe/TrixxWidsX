package com.trixxwids.app.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.trixxwids.app.data.ShapeType
import com.trixxwids.app.data.WidgetConfig
import com.trixxwids.app.data.WidgetElement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BitmapUtil {

    fun renderWidgetToBitmap(
        config: WidgetConfig,
        width: Int,
        height: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = adjustOpacity(config.backgroundColor, config.backgroundOpacity)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val sortedElements = config.elements.sortedBy { it.layer }
        for (element in sortedElements) {
            drawElement(canvas, element, width, height)
        }

        return bitmap
    }

    private fun drawElement(canvas: Canvas, element: WidgetElement, canvasW: Int, canvasH: Int) {
        val left = element.x * canvasW / 1000f
        val top = element.y * canvasH / 1000f
        val eleWidth = element.width * canvasW / 1000f
        val eleHeight = element.height * canvasH / 1000f
        val rect = RectF(left, top, left + eleWidth, top + eleHeight)

        val elementPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = adjustOpacity(element.backgroundColor, element.opacity)
            style = Paint.Style.FILL
        }

        if (element.type != com.trixxwids.app.data.ElementType.SHAPE) {
            val bgRect = RectF(rect)
            if (element.borderRadius > 0) {
                canvas.drawRoundRect(bgRect, element.borderRadius, element.borderRadius, elementPaint)
            } else {
                canvas.drawRect(bgRect, elementPaint)
            }

            if (element.borderWidth > 0) {
                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = adjustOpacity(element.borderColor, element.opacity)
                    style = Paint.Style.STROKE
                    strokeWidth = element.borderWidth
                }
                if (element.borderRadius > 0) {
                    canvas.drawRoundRect(bgRect, element.borderRadius, element.borderRadius, borderPaint)
                } else {
                    canvas.drawRect(bgRect, borderPaint)
                }
            }
        }

        when (element.type) {
            com.trixxwids.app.data.ElementType.TEXT -> {
                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = adjustOpacity(element.fontColor, element.opacity)
                    textSize = element.fontSize * canvasW / 1000f
                    textAlign = Paint.Align.CENTER
                    typeface = if (element.fontType == Typeface.BOLD) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                }
                val xCenter = left + eleWidth / 2f
                val yCenter = top + eleHeight / 2f
                val textHeight = textPaint.descent() - textPaint.ascent()
                val textY = yCenter + textHeight / 2f - textPaint.descent()
                canvas.drawText(element.text, xCenter, textY, textPaint)
            }

            com.trixxwids.app.data.ElementType.CLOCK -> {
                val now = System.currentTimeMillis()
                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(now))
                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = adjustOpacity(element.fontColor, element.opacity)
                    textSize = element.fontSize * canvasW / 1000f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }
                val xCenter = left + eleWidth / 2f
                val yCenter = top + eleHeight / 2f
                val textHeight = textPaint.descent() - textPaint.ascent()
                val textY = yCenter + textHeight / 2f - textPaint.descent()
                canvas.drawText(timeStr, xCenter, textY, textPaint)
            }

            com.trixxwids.app.data.ElementType.DATE -> {
                val now = System.currentTimeMillis()
                val dateStr = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(now))
                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = adjustOpacity(element.fontColor, element.opacity)
                    textSize = element.fontSize * canvasW / 1000f
                    textAlign = Paint.Align.CENTER
                }
                val xCenter = left + eleWidth / 2f
                val yCenter = top + eleHeight / 2f
                val textHeight = textPaint.descent() - textPaint.ascent()
                val textY = yCenter + textHeight / 2f - textPaint.descent()
                canvas.drawText(dateStr, xCenter, textY, textPaint)
            }

            com.trixxwids.app.data.ElementType.WEATHER -> {
                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = adjustOpacity(element.fontColor, element.opacity)
                    textSize = element.fontSize * canvasW / 1000f
                    textAlign = Paint.Align.CENTER
                }
                val xCenter = left + eleWidth / 2f
                val yCenter = top + eleHeight / 2f
                val textHeight = textPaint.descent() - textPaint.ascent()
                val textY = yCenter + textHeight / 2f - textPaint.descent()
                canvas.drawText("☀️ 72°F", xCenter, textY, textPaint)
            }

            com.trixxwids.app.data.ElementType.SHAPE -> {
                when (element.shapeType) {
                    ShapeType.RECTANGLE -> {
                        if (element.borderRadius > 0) {
                            canvas.drawRoundRect(rect, element.borderRadius, element.borderRadius, elementPaint)
                        } else {
                            canvas.drawRect(rect, elementPaint)
                        }
                        if (element.borderWidth > 0) {
                            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                color = adjustOpacity(element.borderColor, element.opacity)
                                style = Paint.Style.STROKE
                                strokeWidth = element.borderWidth
                            }
                            if (element.borderRadius > 0) {
                                canvas.drawRoundRect(rect, element.borderRadius, element.borderRadius, borderPaint)
                            } else {
                                canvas.drawRect(rect, borderPaint)
                            }
                        }
                    }

                    ShapeType.CIRCLE -> {
                        val cx = left + eleWidth / 2f
                        val cy = top + eleHeight / 2f
                        val radius = Math.min(eleWidth, eleHeight) / 2f
                        canvas.drawCircle(cx, cy, radius, elementPaint)
                        if (element.borderWidth > 0) {
                            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                color = adjustOpacity(element.borderColor, element.opacity)
                                style = Paint.Style.STROKE
                                strokeWidth = element.borderWidth
                            }
                            canvas.drawCircle(cx, cy, radius - element.borderWidth / 2f, borderPaint)
                        }
                    }
                }
            }

            com.trixxwids.app.data.ElementType.IMAGE -> {
                element.imageUri?.let { uriStr ->
                    try {
                        val bm = BitmapFactory.decodeFile(uriStr)
                        if (bm != null) {
                            val srcRect = RectF(0f, 0f, bm.width.toFloat(), bm.height.toFloat())
                            canvas.drawBitmap(bm, null, rect, null)
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    private fun adjustOpacity(color: Int, opacity: Int): Int {
        val alpha = (Color.alpha(color) * opacity / 255).coerceIn(0, 255)
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }
}
