package com.example.riberasplayer.utils
// DynamicTopAppBar.kt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicTopAppBar(
    navController: NavController,
    onActionClick: (AppBarAction) -> Unit = {},
    onNavigationClick: () -> Unit = {},
    onMenuClick: () -> Unit = {} // Nuevo parámetro para manejar el clic del menú
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    val currentScreens = when (currentRoute) {
        Screens.Songs.route -> Screens.Songs
        Screens.Playlist.route -> Screens.Playlist
        Screens.Metrics.route -> Screens.Metrics
        Screens.Configuration.route -> Screens.Configuration
        else -> null
    }

    currentScreens?.let { Screens ->
        CenterAlignedTopAppBar(
            title = { Text(text = Screens.title) },
            navigationIcon = {
                // Mostramos el icono de menú o de retroceso según corresponda
                if (navController.previousBackStackEntry != null) {
                    IconButton(onClick = onNavigationClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                } else {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menú"
                        )
                    }
                }
            },
            actions = {
                Screens.actions.forEach { action ->
                    IconButton(onClick = { onActionClick(action) }) {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = action.description
                        )
                    }
                }
            }
        )
    }
}