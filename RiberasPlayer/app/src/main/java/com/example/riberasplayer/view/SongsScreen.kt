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

import android.Manifest
import android.content.Context
import android.os.Environment
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File

import android.media.MediaMetadataRetriever
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted


private fun formatDuration(millis: Long?): String {
    if (millis == null) return "--:--"
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SongsScreen(
    onSongSelected: (File) -> Unit = {}
) {

    val context = LocalContext.current
    val storagePermission = rememberPermissionState(
        permission = Manifest.permission.READ_EXTERNAL_STORAGE
    )
    // Versión actualizada del estado de permisos
    val permissionState = rememberPermissionState(
        permission = Manifest.permission.READ_EXTERNAL_STORAGE
    )

    // Estado para la lista de canciones
    var songs by remember { mutableStateOf<List<SongFile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(permissionState.status) {
        if (permissionState.status.isGranted) {
            isLoading = true
            errorMessage = null
            try {
                songs = loadAudioFiles(context)
            } catch (e: Exception) {
                errorMessage = "Error al cargar canciones: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Solicitar permiso si no está concedido
        if (!permissionState.status.isGranted) {
            PermissionRequest(
                permissionState = storagePermission,
                rationale = "Necesitamos acceso a tus archivos de audio para mostrar tus canciones",
                onPermissionDenied = { errorMessage = "Permiso denegado" }
            )
        }

        Text(
            text = "Todas las canciones",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleLarge
        )

        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
                Button(
                    onClick = {
                        errorMessage = null
                        if (permissionState.status.isGranted) {
                            songs = loadAudioFiles(context)
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Reintentar")
                }
            }
            songs.isEmpty() -> {
                Text(
                    text = if (permissionState.status.isGranted)
                        "No se encontraron canciones"
                    else
                        "Permiso no concedido",
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                LazyColumn {
                    items(songs) { song ->
                        SongFileItem(
                            song = song,
                            onClick = { onSongSelected(song.file) }
                        )
                    }
                }
            }
        }
    }
}

// Modelo para archivos de audio
data class SongFile(
    val file: File,
    val title: String,
    val artist: String,
    val duration: String
)

// Componente para ítem de canción
@Composable
fun SongFileItem(
    song: SongFile,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = song.title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${song.artist} • ${song.duration}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Componente para solicitud de permiso
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequest(
    permissionState: PermissionState,
    rationale: String,
    onPermissionDenied: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = rationale,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Button(onClick = { permissionState.launchPermissionRequest() }) {
            Text("Conceder permiso")
        }
    }
}

// Función para cargar archivos de audio
fun loadAudioFiles(context: Context): List<SongFile> {
    val audioFiles = mutableListOf<SongFile>()
    val externalDirs = context.getExternalFilesDirs(null)

    externalDirs.forEach { dir ->
        dir?.let {
            scanDirectoryForAudio(it, audioFiles)
        }
    }

    // También escanear el almacenamiento externo primario
    Environment.getExternalStorageDirectory()?.let {
        scanDirectoryForAudio(it, audioFiles)
    }

    return audioFiles
}

private fun scanDirectoryForAudio(directory: File, audioFiles: MutableList<SongFile>) {
    directory.walk().maxDepth(10).forEach { file ->
        if (file.isFile && file.extension.equals("mp3", ignoreCase = true)) {
            val metadata = getAudioMetadata(file)
            audioFiles.add(
                SongFile(
                    file = file,
                    title = metadata.title ?: file.nameWithoutExtension,
                    artist = metadata.artist ?: "Artista desconocido",
                    duration = metadata.duration ?: "--:--"
                )
            )
        }
    }
}

// Estructura para metadatos
data class AudioMetadata(
    val title: String?,
    val artist: String?,
    val duration: String?
)

// Función para obtener metadatos (simplificada)
private fun getAudioMetadata(file: File): AudioMetadata {
    // En una implementación real usarías MediaMetadataRetriever
    return AudioMetadata(
        title = file.nameWithoutExtension,
        artist = null,
        duration = null
    )
}

private fun getAudioMetadata2(file: File): AudioMetadata {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(file.absolutePath)
        AudioMetadata(
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: file.nameWithoutExtension,
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?: "Artista desconocido",
            duration = formatDuration(
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
            )
        )
    } catch (e: Exception) {
        AudioMetadata(
            title = file.nameWithoutExtension,
            artist = "Artista desconocido",
            duration = "--:--"
        )
    } finally {
        retriever.release()
    }
}

@Preview
@Composable
fun SongsScreenPreview() {
    MusicPlayerTheme {
        SongsScreen()
    }
}