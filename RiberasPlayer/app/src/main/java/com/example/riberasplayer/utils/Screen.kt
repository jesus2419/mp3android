package com.example.riberasplayer.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.graphics.vector.ImageVector
// Screens.kt
sealed class Screens(
    val route: String,
    val title: String,
    val actions: List<AppBarAction>
) {
    object Songs : Screens(
        route = "songs",
        title = "Todas las canciones",
        actions = listOf(AppBarAction.Search, AppBarAction.Sort)
    )

    object Playlist : Screens(
        route = "playlist",
        title = "Tus playlists",
        actions = listOf(AppBarAction.Add, AppBarAction.Search)
    )

    object Metrics : Screens(
        route = "metrics",
        title = "Estadísticas",
        actions = listOf(AppBarAction.Info, AppBarAction.Share)
    )

    object Configuration : Screens(
        route = "configuration",
        title = "Configuración",
        actions = emptyList()
    )
}

sealed class AppBarAction(val icon: ImageVector, val description: String) {
    object Search : AppBarAction(Icons.Default.Search, "Buscar")
    object Sort : AppBarAction(Icons.Default.Build, "Ordenar")
    object Add : AppBarAction(Icons.Default.Add, "Añadir")
    object Info : AppBarAction(Icons.Default.Info, "Información")
    object Share : AppBarAction(Icons.Default.Share, "Compartir")
    object Back : AppBarAction(Icons.Default.ArrowBack, "Volver")
    object Menu : AppBarAction(Icons.Default.Menu, "Menú")
}