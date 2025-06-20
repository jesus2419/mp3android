package com.example.riberasplayer.viewmodel

import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.riberasplayer.view.SongFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {
    private val _currentSong = MutableStateFlow<SongFile?>(null)
    private val _isPlaying = MutableStateFlow(false)
    private var mediaPlayer: MediaPlayer? = null

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition

    private val _durationMs = MutableStateFlow(0)
    val durationMs: StateFlow<Int> = _durationMs

    private var progressJobActive = false

    // Lista de canciones y el índice actual
    private var songList: List<SongFile> = emptyList()
    private var currentIndex: Int = -1

    val currentSong: StateFlow<SongFile?> = _currentSong
    val isPlaying: StateFlow<Boolean> = _isPlaying

    // Llama esto antes de reproducir para actualizar la lista de canciones
    fun setSongList(songs: List<SongFile>) {
        songList = songs
    }

    fun playSong(song: SongFile) {
        // Actualiza el índice actual si la canción está en la lista
        currentIndex = songList.indexOfFirst { it.file.path == song.file.path }
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
                        _durationMs.value = duration
                        startProgressUpdates()
                    }
                    setOnCompletionListener {
                        _isPlaying.value = false
                        stopProgressUpdates()
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
                stopProgressUpdates()
            } else {
                player.start()
                _isPlaying.value = true
                startProgressUpdates()
            }
        }
    }

    fun stop() {
        mediaPlayer?.release()
        mediaPlayer = null
        _currentSong.value = null
        _isPlaying.value = false
        _currentPosition.value = 0
        _durationMs.value = 0
        stopProgressUpdates()
    }

    fun seekTo(ms: Int) {
        mediaPlayer?.let { player ->
            player.seekTo(ms)
            _currentPosition.value = ms
        }
    }

    private fun startProgressUpdates() {
        if (progressJobActive) return
        progressJobActive = true
        viewModelScope.launch {
            while (progressJobActive && mediaPlayer != null && _isPlaying.value) {
                _currentPosition.value = mediaPlayer?.currentPosition ?: 0
                delay(300L)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJobActive = false
    }

    fun playNext() {
        if (songList.isNotEmpty() && currentIndex >= 0) {
            val nextIndex = (currentIndex + 1) % songList.size
            playSong(songList[nextIndex])
        }
    }

    fun playPrevious() {
        if (songList.isNotEmpty() && currentIndex >= 0) {
            val prevIndex = if (currentIndex - 1 < 0) songList.size - 1 else currentIndex - 1
            playSong(songList[prevIndex])
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        stopProgressUpdates()
    }
}