package com.hibol.miette.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.net.Uri
import android.widget.RemoteViews

class RecipeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        const val ACTION_PREV = "com.hibol.miette.widget.ACTION_PREV"
        const val ACTION_NEXT = "com.hibol.miette.widget.ACTION_NEXT"
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences("miette_prefs", Context.MODE_PRIVATE)
            val title = prefs.getString("recipe_title", "Aucune recette") ?: "Aucune recette"
            val stepCount = prefs.getInt("step_count", 0)
            var currentStep = prefs.getInt("current_step", 0)

            if (currentStep >= stepCount && stepCount > 0) currentStep = stepCount - 1

            val stepText = if (stepCount > 0)
                prefs.getString("step_$currentStep", "") ?: ""
            else
                "Envoie une recette depuis le site"

            val stepLabel = if (stepCount > 0) "Étape ${currentStep + 1} / $stepCount" else ""

            val views = RemoteViews(context.packageName, R.layout.widget_recipe)
            views.setTextViewText(R.id.widget_title, title)
            views.setTextViewText(R.id.widget_step_label, stepLabel)
            views.setTextViewText(R.id.widget_step_text, stepText)

            // Bouton précédent
            val prevIntent = Intent(context, RecipeWidgetProvider::class.java).apply {
                action = ACTION_PREV
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val prevPending = PendingIntent.getBroadcast(
                context, appWidgetId * 10 + 0, prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_prev, prevPending)

            // Bouton suivant
            val nextIntent = Intent(context, RecipeWidgetProvider::class.java).apply {
                action = ACTION_NEXT
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val nextPending = PendingIntent.getBroadcast(
                context, appWidgetId * 10 + 1, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_next, nextPending)

            // Bouton voir la recette
            val recipeId = prefs.getInt("recipe_id", -1)
            if (recipeId != -1) {
                val webIntent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://chez-miette.xyz/recette/$recipeId"))
                val webPending = PendingIntent.getActivity(
                    context, appWidgetId * 10 + 2, webIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_web, webPending)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        val prefs = context.getSharedPreferences("miette_prefs", Context.MODE_PRIVATE)
        val stepCount = prefs.getInt("step_count", 0)
        var currentStep = prefs.getInt("current_step", 0)

        when (intent.action) {
            ACTION_NEXT -> if (currentStep < stepCount - 1) currentStep++
            ACTION_PREV -> if (currentStep > 0) currentStep--
        }

        prefs.edit().putInt("current_step", currentStep).apply()

        val manager = AppWidgetManager.getInstance(context)
        updateWidget(context, manager, appWidgetId)
    }
}