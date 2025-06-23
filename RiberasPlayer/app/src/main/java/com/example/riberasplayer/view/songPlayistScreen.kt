package com.example.riberasplayer.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import com.example.riberasplayer.model.MusicDatabaseHandler
import com.example.riberasplayer.model.Song

@Composable
fun SongPlaylistScreen(playlistId: Int) {
    val context = LocalContext.current
    val dbHandler = remember { MusicDatabaseHandler(context) }
    var songs by remember { mutableStateOf(dbHandler.getSongsInPlaylist(playlistId)) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Canciones en la playlist",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn {
            items(songs) { song ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(song.title, style = MaterialTheme.typography.titleMedium)
                        Text(song.artist ?: "Artista desconocido", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
