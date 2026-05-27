package com.trixxwids.app.ui.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.trixxwids.app.data.ElementType
import com.trixxwids.app.data.ShapeType
import com.trixxwids.app.data.WidgetConfig
import com.trixxwids.app.data.WidgetElement
import kotlin.math.abs
import kotlin.math.sqrt

class CanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var widgetConfig: WidgetConfig? = null
    var selectedElement: WidgetElement? = null
    var onElementSelected: ((WidgetElement?) -> Unit)? = null
    var onElementMoved: ((WidgetElement) -> Unit)? = null
    var onElementResized: ((WidgetElement) -> Unit)? = null

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    private val selectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#007AFF")
        style = Paint.Style.STROKE
        strokeWidth = 2f
        pathEffect = DashPathEffect(floatArrayOf(8f, 4f), 0f)
    }

    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#007AFF")
        style = Paint.Style.FILL
    }

    private var dragMode: DragMode = DragMode.NONE
    private var dragStartX = 0f
    private var dragStartY = 0f
    private var dragElementStartX = 0f
    private var dragElementStartY = 0f
    private var dragHandleIndex = -1
    private val handleSize = 24f
    private val minElementSize = 30f

    private enum class DragMode {
        NONE, DRAG, RESIZE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawGrid(canvas)
        drawElements(canvas)

        val selected = selectedElement
        if (selected != null) {
            drawSelection(canvas, selected)
        }
    }

    private fun drawGrid(canvas: Canvas) {
        val step = 40f
        var x = 0f
        while (x < width) {
            canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
            x += step
        }
        var y = 0f
        while (y < height) {
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
            y += step
        }
    }

    private fun drawElements(canvas: Canvas) {
        val config = widgetConfig ?: return
        val sorted = config.elements.sortedBy { it.layer }
        for (element in sorted) {
            drawElement(canvas, element)
        }
    }

    private fun drawElement(canvas: Canvas, element: WidgetElement) {
        val rect = getElementRect(element)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = adjustOpacity(element.backgroundColor, element.opacity)
            style = Paint.Style.FILL
        }

        when (element.type) {
            ElementType.SHAPE -> {
                when (element.shapeType) {
                    ShapeType.RECTANGLE -> {
                        if (element.borderRadius > 0) {
                            canvas.drawRoundRect(rect, element.borderRadius, element.borderRadius, bgPaint)
                        } else {
                            canvas.drawRect(rect, bgPaint)
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
                        val cx = rect.centerX()
                        val cy = rect.centerY()
                        val radius = minOf(rect.width(), rect.height()) / 2f
                        canvas.drawCircle(cx, cy, radius, bgPaint)
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

            else -> {
                if (element.backgroundColor != Color.TRANSPARENT) {
                    if (element.borderRadius > 0) {
                        canvas.drawRoundRect(rect, element.borderRadius, element.borderRadius, bgPaint)
                    } else {
                        canvas.drawRect(rect, bgPaint)
                    }
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
        }

        when (element.type) {
            ElementType.TEXT, ElementType.CLOCK, ElementType.DATE, ElementType.WEATHER -> {
                val text = when (element.type) {
                    ElementType.TEXT -> element.text
                    ElementType.CLOCK -> "12:30"
                    ElementType.DATE -> "Mon, Jan 1"
                    ElementType.WEATHER -> "72°F"
                    else -> element.text
                }
                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = adjustOpacity(element.fontColor, element.opacity)
                    textSize = element.fontSize
                    textAlign = Paint.Align.CENTER
                    typeface = if (element.fontType == Typeface.BOLD) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                }
                val xCenter = rect.centerX()
                val textHeight = textPaint.descent() - textPaint.ascent()
                val yCenter = rect.centerY() + textHeight / 2f - textPaint.descent()
                canvas.drawText(text, xCenter, yCenter, textPaint)
            }

            ElementType.IMAGE -> {
                element.imageUri?.let { uri ->
                    try {
                        val bm = BitmapFactory.decodeFile(uri)
                        if (bm != null) {
                            canvas.drawBitmap(bm, null, rect, null)
                        }
                    } catch (_: Exception) {}
                }
            }

            else -> {}
        }
    }

    private fun drawSelection(canvas: Canvas, element: WidgetElement) {
        val rect = getElementRect(element)
        canvas.drawRect(rect, selectionPaint)

        val handles = getHandles(rect)
        for (handle in handles) {
            canvas.drawCircle(handle.x, handle.y, handleSize / 2f, handlePaint)
            canvas.drawCircle(handle.x, handle.y, handleSize / 2f, Paint().apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 2f
            })
        }
    }

    private fun getElementRect(element: WidgetElement): RectF {
        val normX = element.x / 1000f
        val normY = element.y / 1000f
        val normW = element.width / 1000f
        val normH = element.height / 1000f
        return RectF(
            normX * width,
            normY * height,
            (normX + normW) * width,
            (normY + normH) * height
        )
    }

    private fun getHandles(rect: RectF): List<Handle> {
        return listOf(
            Handle(rect.left, rect.top),
            Handle(rect.right, rect.top),
            Handle(rect.left, rect.bottom),
            Handle(rect.right, rect.bottom),
            Handle(rect.centerX(), rect.top),
            Handle(rect.centerX(), rect.bottom),
            Handle(rect.left, rect.centerY()),
            Handle(rect.right, rect.centerY())
        )
    }

    private fun hitTestElement(x: Float, y: Float): WidgetElement? {
        val config = widgetConfig ?: return null
        return config.elements.lastOrNull { element ->
            val rect = getElementRect(element)
            val hitX = rect.left + rect.width() * 0.1f
            val hitY = rect.top + rect.height() * 0.1f
            val hitW = rect.width() * 0.8f
            val hitH = rect.height() * 0.8f
            val hitRect = RectF(hitX, hitY, hitX + hitW, hitY + hitH)
            hitRect.contains(x, y)
        }
    }

    private fun hitTestHandle(x: Float, y: Float): Pair<Int, RectF>? {
        val sel = selectedElement ?: return null
        val rect = getElementRect(sel)
        val handles = getHandles(rect)
        for ((index, handle) in handles.withIndex()) {
            if (abs(x - handle.x) <= handleSize && abs(y - handle.y) <= handleSize) {
                return Pair(index, rect)
            }
        }
        return null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val hitHandle = hitTestHandle(event.x, event.y)
                if (hitHandle != null) {
                    dragMode = DragMode.RESIZE
                    dragHandleIndex = hitHandle.first
                    val rect = hitHandle.second
                    dragStartX = event.x
                    dragStartY = event.y
                    dragElementStartX = selectedElement?.x ?: 0f
                    dragElementStartY = selectedElement?.y ?: 0f
                    return true
                }

                val hitElement = hitTestElement(event.x, event.y)
                if (hitElement != null) {
                    selectedElement = hitElement
                    onElementSelected?.invoke(hitElement)
                    dragMode = DragMode.DRAG
                    dragStartX = event.x
                    dragStartY = event.y
                    dragElementStartX = hitElement.x
                    dragElementStartY = hitElement.y
                    invalidate()
                    return true
                }

                selectedElement = null
                onElementSelected?.invoke(null)
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                when (dragMode) {
                    DragMode.DRAG -> {
                        val sel = selectedElement ?: return true
                        val dx = (event.x - dragStartX) / width * 1000f
                        val dy = (event.y - dragStartY) / height * 1000f
                        sel.x = (dragElementStartX + dx).coerceIn(0f, 1000f - sel.width)
                        sel.y = (dragElementStartY + dy).coerceIn(0f, 1000f - sel.height)
                        onElementMoved?.invoke(sel)
                        invalidate()
                    }

                    DragMode.RESIZE -> {
                        val sel = selectedElement ?: return true
                        val dx = (event.x - dragStartX) / width * 1000f
                        val dy = (event.y - dragStartY) / height * 1000f

                        when (dragHandleIndex) {
                            0 -> {
                                sel.x = (dragElementStartX + dx).coerceIn(0f, 1000f - minElementSize)
                                sel.y = (dragElementStartY + dy).coerceIn(0f, 1000f - minElementSize)
                                sel.width = (dragElementStartX + (selectedElement?.width ?: 0f) - sel.x).coerceAtLeast(minElementSize)
                                sel.height = (dragElementStartY + (selectedElement?.height ?: 0f) - sel.y).coerceAtLeast(minElementSize)
                            }
                            1 -> {
                                sel.y = (dragElementStartY + dy).coerceIn(0f, 1000f - minElementSize)
                                sel.width = (selectedElement?.let { dragElementStartX + it.width - sel.x } ?: 0f).coerceAtLeast(minElementSize)
                                sel.height = (dragElementStartY + (selectedElement?.height ?: 0f) - sel.y).coerceAtLeast(minElementSize)
                            }
                            2 -> {
                                sel.x = (dragElementStartX + dx).coerceIn(0f, 1000f - minElementSize)
                                sel.width = (dragElementStartX + (selectedElement?.width ?: 0f) - sel.x).coerceAtLeast(minElementSize)
                                sel.height = (selectedElement?.let { dragElementStartY + it.height - sel.y } ?: 0f).coerceAtLeast(minElementSize)
                            }
                            3 -> {
                                sel.width = (dragElementStartX + (selectedElement?.width ?: 0f) + dx).coerceAtLeast(minElementSize)
                                sel.height = (dragElementStartY + (selectedElement?.height ?: 0f) + dy).coerceAtLeast(minElementSize)
                            }
                            4 -> {
                                sel.y = (dragElementStartY + dy).coerceIn(0f, 1000f - minElementSize)
                                sel.height = (dragElementStartY + (selectedElement?.height ?: 0f) - sel.y).coerceAtLeast(minElementSize)
                            }
                            5 -> {
                                sel.height = (dragElementStartY + (selectedElement?.height ?: 0f) + dy).coerceAtLeast(minElementSize)
                            }
                            6 -> {
                                sel.x = (dragElementStartX + dx).coerceIn(0f, 1000f - minElementSize)
                                sel.width = (dragElementStartX + (selectedElement?.width ?: 0f) - sel.x).coerceAtLeast(minElementSize)
                            }
                            7 -> {
                                sel.width = (dragElementStartX + (selectedElement?.width ?: 0f) + dx).coerceAtLeast(minElementSize)
                            }
                        }

                        sel.width = sel.width.coerceIn(minElementSize, 1000f)
                        sel.height = sel.height.coerceIn(minElementSize, 1000f)
                        sel.x = sel.x.coerceIn(0f, 1000f - sel.width)
                        sel.y = sel.y.coerceIn(0f, 1000f - sel.height)

                        onElementResized?.invoke(sel)
                        invalidate()
                    }

                    DragMode.NONE -> {}
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                dragMode = DragMode.NONE
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun adjustOpacity(color: Int, opacity: Int): Int {
        val alpha = (Color.alpha(color) * opacity / 255).coerceIn(0, 255)
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    fun getConfigBitmap(): Bitmap? {
        val config = widgetConfig ?: return null
        val bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bm)
        drawGrid(canvas)
        drawElements(canvas)
        return bm
    }

    private data class Handle(val x: Float, val y: Float)
}
