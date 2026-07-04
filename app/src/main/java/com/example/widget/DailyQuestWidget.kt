package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.R

class DailyQuestWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences("moments_prefs", Context.MODE_PRIVATE)
        val dailyQuestDate = prefs.getString("daily_quest_date", "")
        val title = if (dailyQuestDate?.isNotEmpty() == true) {
            "Today's Quest"
        } else {
            "Open App to Discover"
        }
        
        // We do a simple read from SharedPreferences (could be updated by app when a quest is set)
        val questText = prefs.getString("widget_quest_text", "Tap to open Moments")

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, title, questText ?: "")
        }
    }
}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, title: String, text: String) {
    val views = RemoteViews(context.packageName, R.layout.widget_daily_quest)
    views.setTextViewText(R.id.widget_title, title)
    views.setTextViewText(R.id.widget_quest_text, text)
    
    // Create intent to launch MainActivity
    val intent = android.content.Intent(context, com.example.MainActivity::class.java)
    val pendingIntent = android.app.PendingIntent.getActivity(context, 0, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
    views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}
