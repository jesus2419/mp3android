package com.example.riberasplayer.view

// MusicPlayerApp.kt
import android.R.attr.padding
import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.riberasplayer.utils.AppBarAction
import com.example.riberasplayer.utils.AppDrawer
import com.example.riberasplayer.utils.BottomNavigationBar
import com.example.riberasplayer.utils.DynamicTopAppBar
import com.example.riberasplayer.utils.MusicPlayerNavHost
import com.example.riberasplayer.utils.MiniPlayer
import com.example.riberasplayer.viewmodel.PlayerViewModel
import com.example.riberasplayer.viewmodel.PlayerViewModelProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val playerViewModel: PlayerViewModel = viewModel()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        playerViewModel.setContext(context)
        PlayerViewModelProvider.set(playerViewModel)
    }

    // MiniPlayer: Estado global para toda la app
    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val currentPosition by playerViewModel.currentPosition.collectAsState(0)
    val durationMs by playerViewModel.durationMs.collectAsState(0)
    var showMiniPlayer by remember { mutableStateOf(false) }

    // Estado para mostrar el diálogo de permiso de notificación
    var askNotificationPermission by remember { mutableStateOf(false) }

    // Launcher para solicitar el permiso POST_NOTIFICATIONS
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            askNotificationPermission = false
        }
    )

    // Mostrar el MiniPlayer automáticamente al seleccionar canción
    LaunchedEffect(currentSong) {
        if (currentSong != null) {
            showMiniPlayer = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionCheck = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                )
                if (permissionCheck != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    askNotificationPermission = true
                }
            }
        }
    }

    // Diálogo para solicitar permiso de notificaciones
    if (askNotificationPermission) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { askNotificationPermission = false },
            title = { androidx.compose.material3.Text("Permitir notificaciones") },
            text = { androidx.compose.material3.Text("¿Quieres recibir notificaciones de reproducción de música?") },
            confirmButton = {
                TextButton(onClick = {
                    askNotificationPermission = false
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }) {
                    androidx.compose.material3.Text("Permitir")
                }
            },
            dismissButton = {
                TextButton(onClick = { askNotificationPermission = false }) {
                    androidx.compose.material3.Text("No permitir")
                }
            }
        )
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