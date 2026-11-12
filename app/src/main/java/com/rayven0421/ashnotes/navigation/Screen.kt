package com.rayven0421.ashnotes.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable
    data object Home : Screen

    @Serializable
    data object Notes : Screen

    @Serializable
    data class Budget(val openAddExpense: Boolean = false) : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data class Editor(val noteId: String) : Screen

    @Serializable
    data object Login : Screen

    @Serializable
    data object Register : Screen
}
