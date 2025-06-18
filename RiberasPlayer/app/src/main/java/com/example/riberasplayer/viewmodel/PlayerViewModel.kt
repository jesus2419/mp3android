package com.example.riberasplayer.viewmodel

import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.riberasplayer.view.SongFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {
    private val _currentSong = MutableStateFlow<SongFile?>(null)
    private val _isPlaying = MutableStateFlow(false)
    private var mediaPlayer: MediaPlayer? = null

    val currentSong: StateFlow<SongFile?> = _currentSong
    val isPlaying: StateFlow<Boolean> = _isPlaying

    fun playSong(song: SongFile) {
        viewModelScope.launch {
            try {
                mediaPlayer?.release() // Libera el reproductor anterior

                mediaPlayer = MediaPlayer().apply {
                    setDataSource(song.file.path)
                    prepareAsync()
                    setOnPreparedListener {
                        _currentSong.value = song
                        _isPlaying.value = true
                        start()
                    }
                    setOnCompletionListener {
                        _isPlaying.value = false
                    }
                }
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Error al reproducir canción", e)
            }
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _isPlaying.value = false
            } else {
                player.start()
                _isPlaying.value = true
            }
        }
    }

    fun stop() {
        mediaPlayer?.release()
        mediaPlayer = null
        _currentSong.value = null
        _isPlaying.value = false
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
    }
}