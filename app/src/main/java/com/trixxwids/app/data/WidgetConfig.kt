package com.trixxwids.app.data

import android.graphics.Color
import android.graphics.Typeface

data class WidgetConfig(
    val elements: MutableList<WidgetElement> = mutableListOf(),
    var backgroundColor: Int = Color.WHITE,
    var backgroundOpacity: Int = 255,
    var widgetSizeTag: String = "4x2"
)

data class WidgetElement(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: ElementType = ElementType.TEXT,
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 200f,
    var height: Float = 100f,
    var rotation: Float = 0f,
    var text: String = "Text",
    var fontSize: Float = 24f,
    var fontColor: Int = Color.BLACK,
    var fontType: Int = Typeface.NORMAL,
    var backgroundColor: Int = Color.TRANSPARENT,
    var opacity: Int = 255,
    var borderRadius: Float = 0f,
    var borderWidth: Float = 0f,
    var borderColor: Int = Color.BLACK,
    var shapeType: ShapeType = ShapeType.RECTANGLE,
    var imageUri: String? = null,
    var layer: Int = 0
)

enum class ElementType {
    TEXT, CLOCK, DATE, WEATHER, SHAPE, IMAGE
}

enum class ShapeType {
    RECTANGLE, CIRCLE
}
