package com.example.riberasplayer.utils

// BottomNavigationBar.kt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(
    navController: NavController,
    onMenuClicked: () -> Unit = {}
) {
    val items = listOf(
        Screen.Songs,
        Screen.Playlist,
        Screen.Metrics,
        Screen.Configuration
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    when (screen) {
                        is Screen.Songs -> Icon(Icons.Default.Home, contentDescription = "Canciones")
                        is Screen.Playlist -> Icon(Icons.Default.List, contentDescription = "Playlists")
                        is Screen.Metrics -> Icon(Icons.Default.DateRange, contentDescription = "Métricas")
                        is Screen.Configuration -> Icon(Icons.Default.Settings, contentDescription = "Configuración")
                    }
                },
                label = { Text(text = screen.route.capitalize()) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}