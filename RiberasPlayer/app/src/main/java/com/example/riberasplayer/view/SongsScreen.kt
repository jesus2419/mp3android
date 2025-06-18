package com.example.riberasplayer.view

import android.Manifest
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.riberasplayer.utils.MiniPlayer
import com.example.riberasplayer.viewmodel.PlayerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(
    onSongSelected: (File) -> Unit = {},
    viewModel: PlayerViewModel = viewModel()
) {
    val context = LocalContext.current

    // Seleccionar el permiso correcto según la versión de Android
    val permission = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> Manifest.permission.READ_MEDIA_AUDIO
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> Manifest.permission.READ_MEDIA_AUDIO
        else -> Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionState = rememberPermissionState(permission = permission)
    var songs by remember { mutableStateOf<List<SongFile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(permissionState.status) {
        if (permissionState.status.isGranted) {
            loadSongs(context, onSuccess = { loadedSongs ->
                songs = loadedSongs
            }, onError = { error ->
                errorMessage = error
            })
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (!permissionState.status.isGranted) {
            PermissionRequest(
                permissionState = permissionState,
                rationale = "Necesitamos acceso a tus archivos de audio para mostrar tus canciones",
                onPermissionDenied = { errorMessage = "Permiso denegado" }
            )
        }

        Text(
            text = "Todas las canciones",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp))
            }
            errorMessage != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        errorMessage = null
                        if (permissionState.status.isGranted) {
                            loadSongs(context, onSuccess = { loadedSongs ->
                                songs = loadedSongs
                            }, onError = { error ->
                                errorMessage = error
                            })
                        }
                    }) {
                        Text("Reintentar")
                    }
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
                            onClick = {
                                viewModel.playSong(song)
                                //onSongSelected(song) // Opcional: navegación si es necesaria
                            }
                        )
                    }
                }
            }
        }
    }

    // Estado para controlar la hoja modal (bottom sheet)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showPlayerSheet by remember { mutableStateOf(false) }

    // Mostrar el reproductor como hoja modal cuando hay canción seleccionada
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    // Mostrar el reproductor automáticamente al seleccionar canción
    LaunchedEffect(currentSong) {
        if (currentSong != null) {
            showPlayerSheet = true
        }
    }

    if (currentSong != null && showPlayerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPlayerSheet = false },
            sheetState = sheetState,
            dragHandle = null
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                MiniPlayer(
                    song = currentSong!!,
                    isPlaying = isPlaying,
                    onPlayPause = { viewModel.togglePlayPause() },
                    onStop = {
                        viewModel.stop()
                        showPlayerSheet = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }
        }
    }

    // Burbuja flotante cuando hay canción y el reproductor está cerrado
    if (currentSong != null && !showPlayerSheet) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            FloatingActionButton(
                onClick = { showPlayerSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Abrir reproductor"
                )
            }
        }
    }
}

private fun loadSongs(
    context: Context,
    onSuccess: (List<SongFile>) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val songs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            loadAudioFilesWithMediaStore(context)
        } else {
            loadAudioFilesLegacy(context)
        }
        onSuccess(songs)
    } catch (e: Exception) {
        onError("Error al cargar canciones: ${e.message ?: "Error desconocido"}")
    }
}

@Suppress("DEPRECATION")
private fun loadAudioFilesLegacy(context: Context): List<SongFile> {
    val audioFiles = mutableListOf<SongFile>()

    // Directorios comunes donde buscar música
    val directoriesToScan = mutableListOf<File>()

    // Directorio de música estándar
    directoriesToScan.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC))

    // Directorios externos de la app
    context.getExternalFilesDirs(null).forEach { dir ->
        dir?.let { directoriesToScan.add(it) }
    }

    // Almacenamiento interno
    directoriesToScan.add(context.getExternalFilesDir(null) ?: context.filesDir)

    // Escanear directorios
    directoriesToScan.forEach { dir ->
        if (dir.exists() && dir.canRead()) {
            scanDirectoryForAudio(dir, audioFiles)
        }
    }

    return audioFiles.sortedBy { it.title }
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun loadAudioFilesWithMediaStore(context: Context): List<SongFile> {
    val audioFiles = mutableListOf<SongFile>()
    val collection = MediaStore.Audio.Media.getContentUri(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.VOLUME_EXTERNAL
        } else {
            MediaStore.VOLUME_EXTERNAL_PRIMARY
        }
    )

    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ALBUM
    )

    val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

    context.contentResolver.query(
        collection,
        projection,
        selection,
        null,
        "${MediaStore.Audio.Media.TITLE} ASC"
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

        while (cursor.moveToNext()) {
            val path = cursor.getString(pathColumn)
            if (path != null && path.endsWith(".mp3", ignoreCase = true)) {
                val file = File(path)
                if (file.exists()) {
                    val title = cursor.getString(titleColumn) ?: file.nameWithoutExtension
                    val artist = cursor.getString(artistColumn) ?: "Artista desconocido"
                    val duration = cursor.getLong(durationColumn)

                    audioFiles.add(
                        SongFile(
                            file = file,
                            title = title,
                            artist = artist,
                            duration = formatDuration(duration)
                        )
                    )
                }
            }
        }
    }

    return audioFiles
}

private fun scanDirectoryForAudio(directory: File, audioFiles: MutableList<SongFile>) {
    try {
        directory.walk()
            .maxDepth(10)
            .filter { it.isFile && it.extension.equals("mp3", ignoreCase = true) }
            .forEach { file ->
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
    } catch (e: SecurityException) {
        // No tenemos permisos para este directorio
    }
}

private fun getAudioMetadata(file: File): AudioMetadata {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(file.absolutePath)
        AudioMetadata(
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
            duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
                ?.let { formatDuration(it) }
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

private fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

data class SongFile(
    val file: File,
    val title: String,
    val artist: String,
    val duration: String
)

data class AudioMetadata(
    val title: String?,
    val artist: String?,
    val duration: String?
)

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
        Button(onClick = {
            permissionState.launchPermissionRequest()
        }) {
            Text("Conceder permiso")
        }
    }
}

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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${song.artist} • ${song.duration}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}