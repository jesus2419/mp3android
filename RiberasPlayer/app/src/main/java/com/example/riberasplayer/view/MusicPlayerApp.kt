package com.example.riberasplayer.view

// MusicPlayerApp.kt
import android.R.attr.padding
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.riberasplayer.utils.AppBarAction
import com.example.riberasplayer.utils.AppDrawer
import com.example.riberasplayer.utils.BottomNavigationBar
import com.example.riberasplayer.utils.DynamicTopAppBar
import com.example.riberasplayer.utils.MusicPlayerNavHost
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
            // Aplicamos el padding a nuestro contenido principal
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                MusicPlayerNavHost(navController = navController)
            }
        }
    }
}