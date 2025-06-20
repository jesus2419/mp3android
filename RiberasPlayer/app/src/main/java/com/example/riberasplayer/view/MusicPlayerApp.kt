package com.example.riberasplayer.view

// MusicPlayerApp.kt
import android.R.attr.padding
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.riberasplayer.utils.AppBarAction
import com.example.riberasplayer.utils.AppDrawer
import com.example.riberasplayer.utils.BottomNavigationBar
import com.example.riberasplayer.utils.DynamicTopAppBar
import com.example.riberasplayer.utils.MusicPlayerNavHost
import com.example.riberasplayer.utils.MiniPlayer
import com.example.riberasplayer.viewmodel.PlayerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // MiniPlayer: Estado global para toda la app
    val playerViewModel: PlayerViewModel = viewModel()
    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val currentPosition by playerViewModel.currentPosition.collectAsState(0)
    val durationMs by playerViewModel.durationMs.collectAsState(0)
    var showMiniPlayer by remember { mutableStateOf(false) }

    // Mostrar el MiniPlayer automáticamente al seleccionar canción
    LaunchedEffect(currentSong) {
        if (currentSong != null) {
            showMiniPlayer = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                onDestinationClicked = { route ->
                    scope.launch { drawerState.close() }
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                DynamicTopAppBar(
                    navController = navController,
                    onActionClick = { action ->
                        when (action) {
                            AppBarAction.Search -> { /* Lógica para búsqueda */ }
                            AppBarAction.Add -> { /* Lógica para añadir playlist */ }
                            AppBarAction.Sort -> { /* Lógica para ordenar */ }
                            AppBarAction.Info -> { /* Lógica para información */ }
                            AppBarAction.Share -> { /* Lógica para compartir */ }
                            AppBarAction.Menu -> scope.launch { drawerState.open() }
                            AppBarAction.Back -> navController.popBackStack()
                        }
                    },
                    onNavigationClick = { navController.popBackStack() },
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    navController = navController,
                    onMenuClicked = { scope.launch { drawerState.open() } }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                MusicPlayerNavHost(
                    navController = navController,
                    playerViewModel = playerViewModel // Pasar el viewModel a las pantallas
                )
                // MiniPlayer flotante, siempre visible si hay canción
                if (currentSong != null && showMiniPlayer) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 56.dp) // Menos padding para pegarlo al BottomNav
                    ) {
                        MiniPlayer(
                            song = currentSong!!,
                            isPlaying = isPlaying,
                            onPlayPause = { playerViewModel.togglePlayPause() },
                            onStop = {
                                playerViewModel.stop()
                                showMiniPlayer = false
                            },
                            onSeekTo = { ms -> playerViewModel.seekTo(ms) },
                            currentPosition = currentPosition,
                            durationMs = durationMs,
                            onNext = { playerViewModel.playNext() },         // Nuevo
                            onPrevious = { playerViewModel.playPrevious() }  // Nuevo
                        )
                    }
                }
            }
        }
    }
}