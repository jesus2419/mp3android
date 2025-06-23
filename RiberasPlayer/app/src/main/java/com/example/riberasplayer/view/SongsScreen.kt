package com.example.riberasplayer.view

import android.Manifest
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.riberasplayer.R
import com.example.riberasplayer.model.MusicDatabaseHandler
import com.example.riberasplayer.model.Song
import com.example.riberasplayer.viewmodel.PlayerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.riberasplayer.model.Playlist

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(
    onSongSelected: (File) -> Unit = {},
    viewModel: PlayerViewModel,
    navController: NavController = rememberNavController() // Añade NavController para navegación
) {
    val context = LocalContext.current
    val dbHandler = remember { MusicDatabaseHandler(context) }

    // Seleccionar el permiso correcto según la versión de Android
    val permission = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> Manifest.permission.READ_MEDIA_AUDIO
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> Manifest.permission.READ_MEDIA_AUDIO
        else -> Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionState = rememberPermissionState(permission = permission)
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Cargar canciones desde archivos y sincronizar con la base de datos
    LaunchedEffect(permissionState.status) {
        if (permissionState.status.isGranted) {
            isLoading = true
            try {
                // Cambia aquí: ignora el valor de retorno de loadSongsFromFilesAndSyncDB
                loadSongsFromFilesAndSyncDB(context, dbHandler)
                songs = dbHandler.getAllSongs()
                viewModel.setSongListFromDb(songs)
            } catch (e: Exception) {
                errorMessage = "Error al cargar canciones: ${e.message ?: "Error desconocido"}"
            }
            isLoading = false
        }
    }

    // Estado para mostrar el nombre de la canción seleccionada en el menú
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var showEditTitleDialog by remember { mutableStateOf(false) }
    var showEditArtistDialog by remember { mutableStateOf(false) }
    var showSongNameDialog by remember { mutableStateOf(false) }

    // Estado para mostrar el diálogo de selección de playlist
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var playlists by remember { mutableStateOf<List<Playlist>>(emptyList()) }
    var songToAdd by remember { mutableStateOf<Song?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.fondo), // Cambia por tu nombre real
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

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

            // Mostrar el nombre de la canción seleccionada si corresponde
            if (showSongNameDialog && selectedSong != null) {
                Dialog(onDismissRequest = { showSongNameDialog = false }) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Canción seleccionada:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = selectedSong?.title ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { showSongNameDialog = false }) {
                                Text("Cerrar")
                            }
                        }
                    }
                }
            }

            // Diálogo para editar nombre de la canción
            if (showEditTitleDialog && selectedSong != null) {
                var newTitle by remember { mutableStateOf(selectedSong!!.title) }
                Dialog(onDismissRequest = { showEditTitleDialog = false }) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Editar nombre de la canción", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newTitle,
                                onValueChange = { newTitle = it },
                                label = { Text("Nombre de la canción") }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row {
                                Button(onClick = {
                                    showEditTitleDialog = false
                                }) { Text("Cancelar") }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = {
                                    selectedSong?.let {
                                        val updated = it.copy(title = newTitle)
                                        dbHandler.updateSong(updated)
                                        songs = dbHandler.getAllSongs()
                                        viewModel.setSongListFromDb(songs)
                                    }
                                    showEditTitleDialog = false
                                }) { Text("Guardar") }
                            }
                        }
                    }
                }
            }

            // Diálogo para editar artista
            if (showEditArtistDialog && selectedSong != null) {
                var newArtist by remember { mutableStateOf(selectedSong!!.artist ?: "") }
                Dialog(onDismissRequest = { showEditArtistDialog = false }) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Editar artista", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newArtist,
                                onValueChange = { newArtist = it },
                                label = { Text("Artista") }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row {
                                Button(onClick = {
                                    showEditArtistDialog = false
                                }) { Text("Cancelar") }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = {
                                    selectedSong?.let {
                                        val updated = it.copy(artist = newArtist)
                                        dbHandler.updateSong(updated)
                                        songs = dbHandler.getAllSongs()
                                        viewModel.setSongListFromDb(songs)
                                    }
                                    showEditArtistDialog = false
                                }) { Text("Guardar") }
                            }
                        }
                    }
                }
            }

            // Diálogo para seleccionar playlist
            if (showPlaylistDialog && songToAdd != null) {
                playlists = dbHandler.getAllPlaylists()
                AlertDialog(
                    onDismissRequest = { showPlaylistDialog = false },
                    title = { Text("Agregar a playlist") },
                    text = {
                        if (playlists.isEmpty()) {
                            Text("No hay playlists. Crea una primero.")
                        } else {
                            Column {
                                playlists.forEach { playlist ->
                                    TextButton(
                                        onClick = {
                                            dbHandler.addSongToPlaylist(playlist.id, songToAdd!!.id)
                                            showPlaylistDialog = false
                                            // Navega a la pantalla de canciones de la playlist
                                            navController.navigate("playlist_songs/${playlist.id}")
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(playlist.name)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showPlaylistDialog = false }) { Text("Cancelar") }
                    }
                )
            }

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
                                // Solución: recarga desde archivos y sincroniza con la base de datos
                                loadSongsFromFilesAndSyncDB(context, dbHandler)
                                songs = dbHandler.getAllSongs()
                                viewModel.setSongListFromDb(songs)
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
                    // Hacer la lista scrolleable usando weight
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        items(songs) { song ->
                            SongDbItem(
                                song = song,
                                onClick = {
                                    viewModel.setSongListFromDb(songs)
                                    viewModel.playSongFromDb(song)
                                },
                                onOptionsClick = { option ->
                                    selectedSong = song
                                    when (option) {
                                        "Cambiar nombre" -> showEditTitleDialog = true
                                        "Cambiar artista" -> showEditArtistDialog = true
                                        "Agregar a playlist" -> {
                                            songToAdd = song
                                            showPlaylistDialog = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Eliminar todo lo relacionado con ModalBottomSheet y MiniPlayer aquí
    // Ya no se necesita el estado sheetState, showPlayerSheet, ni el FloatingActionButton
    // El MiniPlayer se muestra globalmente en MusicPlayerApp
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
fun SongDbItem(
    song: Song,
    onClick: () -> Unit,
    onOptionsClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${song.artist ?: "Artista desconocido"} • ${formatDuration(song.duration)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Opciones"
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Cambiar nombre") },
                        onClick = {
                            expanded = false
                            onOptionsClick("Cambiar nombre")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Cambiar artista") },
                        onClick = {
                            expanded = false
                            onOptionsClick("Cambiar artista")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Agregar a playlist") },
                        onClick = {
                            expanded = false
                            onOptionsClick("Agregar a playlist")
                        }
                    )
                }
            }
        }
    }
}

// Sincroniza archivos con la base de datos y retorna la lista de Song
private fun loadSongsFromFilesAndSyncDB(context: Context, dbHandler: MusicDatabaseHandler): List<Song> {
    val files = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        loadAudioFilesWithMediaStore(context)
    } else {
        loadAudioFilesLegacy(context)
    }
    // Insertar en la base de datos si no existen
    files.forEach { file ->
        val exists = dbHandler.getAllSongs().any { it.path == file.file.absolutePath }
        if (!exists) {
            dbHandler.addSong(
                Song(
                    title = file.title,
                    artist = file.artist,
                    album = null,
                    duration = parseDuration(file.duration),
                    path = file.file.absolutePath
                )
            )
        }
    }
    return dbHandler.getAllSongs()
}

// Convierte "mm:ss" a milisegundos
private fun parseDuration(duration: String): Long {
    return try {
        val parts = duration.split(":")
        if (parts.size == 2) {
            val min = parts[0].toLong()
            val sec = parts[1].toLong()
            (min * 60 + sec) * 1000
        } else 0L
    } catch (_: Exception) { 0L }
}
