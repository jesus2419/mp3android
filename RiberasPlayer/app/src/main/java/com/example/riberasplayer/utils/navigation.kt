package com.example.riberasplayer.utils

// Navigation.kt
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.riberasplayer.view.ConfigurationScreen
import com.example.riberasplayer.view.MetricsScreen
import com.example.riberasplayer.view.PlaylistScreen
import com.example.riberasplayer.view.SongsScreen
import com.example.riberasplayer.viewmodel.PlayerViewModel


sealed class Screen(val route: String) {
    object Songs : Screen("songs")
    object Playlist : Screen("playlist")
    object Metrics : Screen("metrics")
    object Configuration : Screen("configuration")
}

@Composable
fun MusicPlayerNavHost(
    navController: NavHostController,
    playerViewModel: PlayerViewModel // Nuevo parámetro
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Songs.route
    ) {
        composable(Screen.Songs.route) { SongsScreen(viewModel = playerViewModel) }
        composable(Screen.Playlist.route) { PlaylistScreen() }
        composable(Screen.Metrics.route) { MetricsScreen() }
        composable(Screen.Configuration.route) { ConfigurationScreen() }
    }
}