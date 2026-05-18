package com.hibol.miette.widget.model

data class Recipe(
    val id: Int,
    val title: String,
    val phases: List<Phase>
)

data class Phase(
    val id: Int,
    val label: String,
    val position: Int,
    val steps: List<Step>,
    val ingredients: List<Ingredient>
)

data class Ingredient(
    val id: Int,
    val label: String,
    val quantity: Double,
    val unit: String?,
    val position: Int
)

data class Step(
    val id: Int,
    val label: String,
    val position: Int
)