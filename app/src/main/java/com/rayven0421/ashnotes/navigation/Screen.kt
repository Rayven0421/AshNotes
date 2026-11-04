package com.rayven0421.ashnotes.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable
    data object Home : Screen

    @Serializable
    data object Notes : Screen

    @Serializable
    data object Budget : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data class Editor(val noteId: String) : Screen
}
