package com.trixxwids.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class TrixxWidgetProvider4x4 : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            TrixxWidgetProvider.updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}
