package com.example.riberasplayer.model



// Modelos de datos
data class Song(
    val id: Int = 0,
    val title: String,
    val artist: String?,
    val album: String?,
    val duration: Long, // en milisegundos
    val path: String
)

data class Playlist(
    val id: Int = 0,
    val name: String,
    val createdAt: String? = null
)