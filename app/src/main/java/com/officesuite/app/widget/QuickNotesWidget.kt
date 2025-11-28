package com.officesuite.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.officesuite.app.MainActivity
import com.officesuite.app.R

/**
 * Quick Notes Widget for instant note capture.
 * Implements Medium Priority Feature from Phase 2 Section 7:
 * - Quick Notes Widget: Home screen floating note capture
 */
class QuickNotesWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_NEW_NOTE -> {
                // Launch app with new note action
                val newNoteIntent = Intent(context, MainActivity::class.java).apply {
                    action = ACTION_NEW_NOTE
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(newNoteIntent)
            }
            ACTION_VOICE_NOTE -> {
                // Launch app with voice note action
                val voiceIntent = Intent(context, MainActivity::class.java).apply {
                    action = ACTION_VOICE_NOTE
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(voiceIntent)
            }
            ACTION_PHOTO_NOTE -> {
                // Launch app with camera for photo note
                val photoIntent = Intent(context, MainActivity::class.java).apply {
                    action = ACTION_PHOTO_NOTE
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(photoIntent)
            }
            ACTION_CHECKLIST_NOTE -> {
                // Launch app with new checklist
                val checklistIntent = Intent(context, MainActivity::class.java).apply {
                    action = ACTION_CHECKLIST_NOTE
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(checklistIntent)
            }
        }
    }

    override fun onEnabled(context: Context) {
        // Widget first created
    }

    override fun onDisabled(context: Context) {
        // All widget instances removed
    }

    companion object {
        const val ACTION_NEW_NOTE = "com.officesuite.app.ACTION_NEW_NOTE"
        const val ACTION_VOICE_NOTE = "com.officesuite.app.ACTION_VOICE_NOTE"
        const val ACTION_PHOTO_NOTE = "com.officesuite.app.ACTION_PHOTO_NOTE"
        const val ACTION_CHECKLIST_NOTE = "com.officesuite.app.ACTION_CHECKLIST_NOTE"
        
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_notes)

            // New Note button
            val newNoteIntent = Intent(context, QuickNotesWidget::class.java).apply {
                action = ACTION_NEW_NOTE
            }
            val newNotePendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                newNoteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.button_new_note, newNotePendingIntent)

            // Voice Note button
            val voiceIntent = Intent(context, QuickNotesWidget::class.java).apply {
                action = ACTION_VOICE_NOTE
            }
            val voicePendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                voiceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.button_voice_note, voicePendingIntent)

            // Photo Note button
            val photoIntent = Intent(context, QuickNotesWidget::class.java).apply {
                action = ACTION_PHOTO_NOTE
            }
            val photoPendingIntent = PendingIntent.getBroadcast(
                context,
                2,
                photoIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.button_photo_note, photoPendingIntent)

            // Checklist button
            val checklistIntent = Intent(context, QuickNotesWidget::class.java).apply {
                action = ACTION_CHECKLIST_NOTE
            }
            val checklistPendingIntent = PendingIntent.getBroadcast(
                context,
                3,
                checklistIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.button_checklist, checklistPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        
        /**
         * Update all Quick Notes widgets
         */
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, QuickNotesWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}
