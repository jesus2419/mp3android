package com.example.riberasplayer.utils

// Navigation.kt
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.riberasplayer.view.ConfigurationScreen
import com.example.riberasplayer.view.MetricsScreen
import com.example.riberasplayer.view.PlaylistScreen
import com.example.riberasplayer.view.SongsScreen
import com.example.riberasplayer.view.SongPlaylistScreen
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
    // Usa siempre el mismo navController que recibe la función
    NavHost(
        navController = navController,
        startDestination = "songs"
    ) {
        composable("songs") {
            SongsScreen(
                viewModel = playerViewModel,
                navController = navController // Usa el navController recibido, no uno nuevo
            )
        }
        composable(
            "playlist_songs/{playlistId}",
            arguments = listOf(navArgument("playlistId") { type = NavType.IntType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getInt("playlistId") ?: 0
            SongPlaylistScreen(playlistId)
        }
        composable(Screen.Playlist.route) { PlaylistScreen(navController = navController) }
        composable(Screen.Metrics.route) { MetricsScreen() }
        composable(Screen.Configuration.route) { ConfigurationScreen() }
    }
}