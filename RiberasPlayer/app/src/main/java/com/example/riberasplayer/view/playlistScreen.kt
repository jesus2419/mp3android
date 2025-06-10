package com.example.riberasplayer.view

// PlaylistScreen.kt
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.riberasplayer.model.Playlist
import com.example.riberasplayer.ui.theme.MusicPlayerTheme

@Composable
fun PlaylistScreen() {
    val playlists = listOf(
        Playlist("Favoritas", "15 canciones"),
        Playlist("Trabajo", "8 canciones"),
        Playlist("Viaje", "12 canciones")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Tus playlists", modifier = Modifier.padding(16.dp))
        LazyColumn {
            items(playlists) { playlist ->
                PlaylistItem(playlist = playlist)
            }
        }
    }
}

@Composable
fun PlaylistItem(playlist: Playlist) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(text = playlist.name, style = MaterialTheme.typography.titleMedium)
        Text(text = playlist.songCount, style = MaterialTheme.typography.bodySmall)
    }
}

@Preview
@Composable
fun PlaylistScreenPreview() {
    MusicPlayerTheme {
        PlaylistScreen()
    }
}