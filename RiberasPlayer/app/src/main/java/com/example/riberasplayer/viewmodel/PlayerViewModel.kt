package com.example.riberasplayer.viewmodel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import android.media.MediaPlayer
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.riberasplayer.R
import com.example.riberasplayer.view.SongFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// --- BroadcastReceiver para acciones de notificación ---
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

open class PlayerViewModel : ViewModel() {
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

    // --- Notificación ---
    private var notificationManager: NotificationManager? = null
    private var context: Context? = null
    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "music_player_channel"

    // Llama a esto desde tu Activity/Composable principal para pasar el contexto
    fun setContext(context: Context) {
        this.context = context.applicationContext
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reproductor de música",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun showNotification(song: SongFile, isPlaying: Boolean) {
        val ctx = context ?: return

        // Intents para acciones
        val playPauseIntent = Intent(ctx, NotificationActionReceiver::class.java).apply {
            action = if (isPlaying) "ACTION_PAUSE" else "ACTION_PLAY"
        }
        val playPausePendingIntent = PendingIntent.getBroadcast(
            ctx, 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = Intent(ctx, NotificationActionReceiver::class.java).apply {
            action = "ACTION_NEXT"
        }
        val nextPendingIntent = PendingIntent.getBroadcast(
            ctx, 1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = Intent(ctx, NotificationActionReceiver::class.java).apply {
            action = "ACTION_PREV"
        }
        val prevPendingIntent = PendingIntent.getBroadcast(
            ctx, 2, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(ctx, NotificationActionReceiver::class.java).apply {
            action = "ACTION_STOP"
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            ctx, 3, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // IMPORTANTE: Usa un ícono válido y pequeño para notificaciones (PNG en drawable)
        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setSmallIcon(R.drawable.play) // Asegúrate de tener este ícono en res/drawable
            .setOnlyAlertOnce(true)
            .setOngoing(isPlaying)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Usa PRIORITY_HIGH para asegurar visibilidad
            .addAction(R.drawable.back, "Anterior", prevPendingIntent)
            .addAction(
                if (isPlaying) R.drawable.pause else R.drawable.play,
                if (isPlaying) "Pausar" else "Reproducir",
                playPausePendingIntent
            )
            .addAction(R.drawable.forward, "Siguiente", nextPendingIntent)
            .addAction(R.drawable.play, "Cerrar", stopPendingIntent)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2)
            )

        // Notifica usando el contexto de la app
        notificationManager?.notify(NOTIFICATION_ID, builder.build())
    }

    private fun cancelNotification() {
        notificationManager?.cancel(NOTIFICATION_ID)
    }

    // --- Notificación reactiva ---
    private var lastNotifiedSong: SongFile? = null
    private var lastNotifiedPlaying: Boolean = false

    private fun updateNotificationIfNeeded() {
        val song = _currentSong.value
        val playing = _isPlaying.value
        if (song != null && (song != lastNotifiedSong || playing != lastNotifiedPlaying)) {
            showNotification(song, playing)
            lastNotifiedSong = song
            lastNotifiedPlaying = playing
        } else if (song == null) {
            cancelNotification()
            lastNotifiedSong = null
        }
    }

    init {
        // Observa cambios y actualiza la notificación en tiempo real
        viewModelScope.launch {
            _currentSong.collect { updateNotificationIfNeeded() }
        }
        viewModelScope.launch {
            _isPlaying.collect { updateNotificationIfNeeded() }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        stopProgressUpdates()
        cancelNotification()
    }
}

// --- BroadcastReceiver para acciones de notificación ---
class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val viewModel = PlayerViewModelProvider.get() ?: return
        when (intent.action) {
            "ACTION_PLAY" -> viewModel.togglePlayPause()
            "ACTION_PAUSE" -> viewModel.togglePlayPause()
            "ACTION_NEXT" -> viewModel.playNext()
            "ACTION_PREV" -> viewModel.playPrevious()
            "ACTION_STOP" -> viewModel.stop()
        }
    }
}

// --- Proveedor singleton para acceder al ViewModel desde el receiver ---
object PlayerViewModelProvider {
    private var instance: PlayerViewModel? = null
    fun set(viewModel: PlayerViewModel) { instance = viewModel }
    fun get(): PlayerViewModel? = instance
}
