package com.example.riberasplayer.view

// MetricsScreen.kt
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.riberasplayer.ui.theme.MusicPlayerTheme

@Composable
fun MetricsScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Tus estadísticas",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleLarge
        )
        // Aquí irían gráficos y estadísticas
        Text(
            text = "Tiempo escuchado: 25 horas",
            modifier = Modifier.padding(16.dp)
        )
        Text(
            text = "Artista más escuchado: Artista 1",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
fun MetricsScreenPreview() {
    MusicPlayerTheme {
        MetricsScreen()
    }
}