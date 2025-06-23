package com.example.riberasplayer.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.riberasplayer.model.MusicDatabaseHandler
import com.example.riberasplayer.model.Playlist
import com.example.riberasplayer.ui.theme.MusicPlayerTheme
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun PlaylistScreen(
    navController: NavController = rememberNavController() // Permite inyectar NavController
) {
    val context = LocalContext.current
    val dbHandler = remember { MusicDatabaseHandler(context) }
    var playlists by remember { mutableStateOf(dbHandler.getAllPlaylists()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var playlistToEdit by remember { mutableStateOf<Playlist?>(null) }
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tus playlists",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { showAddDialog = true }) {
                Text("Agregar")
            }
        }
        LazyColumn {
            items(playlists) { playlist ->
                PlaylistItem(
                    playlist = playlist,
                    onEdit = {
                        playlistToEdit = playlist
                        showEditDialog = true
                    },
                    onDelete = {
                        playlistToDelete = playlist
                        showDeleteDialog = true
                    },
                    onOpen = {
                        navController.navigate("playlist_songs/${playlist.id}")
                    }
                )
            }
        }
    }

    // Diálogo para agregar playlist
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nueva playlist") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la playlist") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            dbHandler.createPlaylist(name)
                            playlists = dbHandler.getAllPlaylists()
                        }
                        showAddDialog = false
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo para editar playlist
    if (showEditDialog && playlistToEdit != null) {
        var name by remember { mutableStateOf(playlistToEdit!!.name) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar playlist") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la playlist") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            dbHandler.updatePlaylist(playlistToEdit!!.copy(name = name))
                            playlists = dbHandler.getAllPlaylists()
                        }
                        showEditDialog = false
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo para confirmar borrado
    if (showDeleteDialog && playlistToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar playlist") },
            text = { Text("¿Seguro que deseas eliminar la playlist \"${playlistToDelete!!.name}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        dbHandler.deletePlaylist(playlistToDelete!!.id)
                        playlists = dbHandler.getAllPlaylists()
                        showDeleteDialog = false
                    }
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun PlaylistItem(
    playlist: Playlist,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpen: () -> Unit // Nuevo parámetro para abrir la playlist
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onOpen() }, // Navega al hacer click en la tarjeta
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Opciones")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = {
                            expanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        onClick = {
                            expanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
