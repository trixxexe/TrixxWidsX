package com.trixxwids.app.util

import android.content.res.Resources

object WidgetSizeUtil {

    data class WidgetSize(val widthDp: Int, val heightDp: Int, val label: String)

    val SIZES = mapOf(
        "2x1" to WidgetSize(110, 40, "2 x 1"),
        "2x2" to WidgetSize(110, 110, "2 x 2"),
        "4x1" to WidgetSize(250, 40, "4 x 1"),
        "4x2" to WidgetSize(250, 110, "4 x 2"),
        "4x4" to WidgetSize(250, 250, "4 x 4")
    )

    fun getAspectRatio(tag: String): Float {
        val size = SIZES[tag] ?: SIZES["4x2"]!!
        return size.widthDp.toFloat() / size.heightDp.toFloat()
    }

    fun dpToPx(dp: Float): Float {
        return dp * Resources.getSystem().displayMetrics.density
    }

    fun pxToDp(px: Float): Float {
        return px / Resources.getSystem().displayMetrics.density
    }

    fun getSizeLabel(tag: String): String {
        return SIZES[tag]?.label ?: "4 x 2"
    }
}
