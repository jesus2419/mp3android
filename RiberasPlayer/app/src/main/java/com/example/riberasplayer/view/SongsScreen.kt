package com.example.riberasplayer.view

// SongsScreen.kt
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
import com.example.riberasplayer.model.Song
import com.example.riberasplayer.ui.theme.MusicPlayerTheme

@Composable
fun SongsScreen() {
    val songs = listOf(
        Song("Canción 1", "Artista 1", "3:45"),
        Song("Canción 2", "Artista 2", "4:20"),
        Song("Canción 3", "Artista 3", "2:55")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Todas las canciones", modifier = Modifier.padding(16.dp))
        LazyColumn {
            items(songs) { song ->
                SongItem(song = song)
            }
        }
    }
}

@Composable
fun SongItem(song: Song) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(text = song.title, style = MaterialTheme.typography.titleMedium)
        Text(text = "${song.artist} • ${song.duration}", style = MaterialTheme.typography.bodySmall)
    }
}

@Preview
@Composable
fun SongsScreenPreview() {
    MusicPlayerTheme {
        SongsScreen()
    }
}