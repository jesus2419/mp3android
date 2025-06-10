package com.example.riberasplayer.model

data class Song(
    val title: String,
    val artist: String,
    val duration: String
)

data class Playlist(
    val name: String,
    val songCount: String
)