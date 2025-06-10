package com.example.riberasplayer.utils

// AppDrawer.kt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppDrawer(
    onDestinationClicked: (route: String) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(modifier) {
        Text(
            text = "Music Player",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        Divider()
        Column(modifier = Modifier.fillMaxWidth()) {
            DrawerItem("Songs", Screen.Songs.route, onDestinationClicked)
            DrawerItem("Playlists", Screen.Playlist.route, onDestinationClicked)
            DrawerItem("Metrics", Screen.Metrics.route, onDestinationClicked)
            DrawerItem("Configuration", Screen.Configuration.route, onDestinationClicked)
        }
    }
}

@Composable
private fun DrawerItem(
    title: String,
    route: String,
    onDestinationClicked: (route: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onDestinationClicked(route) }
            .padding(16.dp)
    )
}