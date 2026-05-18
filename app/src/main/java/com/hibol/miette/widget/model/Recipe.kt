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
    val steps: List<Step>
)

data class Step(
    val id: Int,
    val label: String,
    val position: Int
)