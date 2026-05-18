package com.hibol.miette.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.hibol.miette.widget.network.MietteApiClient

class DeepLinkActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.data ?: run { finish(); return }
        val recipeId = uri.lastPathSegment?.toIntOrNull() ?: run { finish(); return }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val recipe = MietteApiClient.api.getRecipe(recipeId)
                val prefs: SharedPreferences =
                    getSharedPreferences("miette_prefs", MODE_PRIVATE)

                prefs.edit()
                    .putString("recipe_title", recipe.title)
                    .putInt("recipe_id", recipe.id)
                    .putInt("current_step", 0)
                    .commit()

                val phases = recipe.phases.sortedBy { it.position }

                // Étapes aplaties avec index de phase
                val steps = mutableListOf<Pair<String, Int>>()
                phases.forEachIndexed { phaseIndex, phase ->
                    phase.steps.sortedBy { it.position }.forEach { step ->
                        steps.add(Pair("${phase.label} — ${step.label}", phaseIndex))
                    }
                }

                val editor = prefs.edit()
                editor.putInt("step_count", steps.size)
                steps.forEachIndexed { index, (stepText, phaseIndex) ->
                    editor.putString("step_$index", stepText)
                    editor.putInt("step_phase_$index", phaseIndex)
                }

                // Ingrédients par phase
                editor.putInt("phase_count", phases.size)
                phases.forEachIndexed { phaseIndex, phase ->
                    val ingredients = phase.ingredients.sortedBy { it.position }
                    editor.putInt("phase_${phaseIndex}_ingredient_count", ingredients.size)
                    ingredients.forEachIndexed { i, ingredient ->
                        val unit = ingredient.unit ?: ""
                        val qty = if (ingredient.quantity % 1.0 == 0.0)
                            ingredient.quantity.toInt().toString()
                        else
                            ingredient.quantity.toString()
                        editor.putString("phase_${phaseIndex}_ingredient_$i",
                            "${ingredient.label}|$qty|$unit")
                    }
                }

                editor.commit()

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                launch(Dispatchers.Main) {
                    val manager = AppWidgetManager.getInstance(this@DeepLinkActivity)
                    val ids = manager.getAppWidgetIds(
                        android.content.ComponentName(this@DeepLinkActivity, RecipeWidgetProvider::class.java)
                    )
                    ids.forEach { id ->
                        RecipeWidgetProvider.updateWidget(this@DeepLinkActivity, manager, id)
                    }
                    finish()
                }
            }
        }
    }
}