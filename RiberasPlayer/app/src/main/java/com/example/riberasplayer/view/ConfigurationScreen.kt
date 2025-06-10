package com.example.riberasplayer.view

// ConfigurationScreen.kt
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.riberasplayer.ui.theme.MusicPlayerTheme

@Composable
fun ConfigurationScreen() {
    val darkTheme = remember { mutableStateOf(false) }
    val offlineMode = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Configuración",
            style = MaterialTheme.typography.titleLarge
        )

        SettingItem(
            title = "Modo oscuro",
            description = "Activar el tema oscuro",
            checked = darkTheme.value,
            onCheckedChange = { darkTheme.value = it }
        )

        SettingItem(
            title = "Modo offline",
            description = "Descargar música para escuchar sin conexión",
            checked = offlineMode.value,
            onCheckedChange = { offlineMode.value = it }
        )
    }
}

@Composable
fun SettingItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(text = description, style = MaterialTheme.typography.bodySmall)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Preview
@Composable
fun ConfigurationScreenPreview() {
    MusicPlayerTheme {
        ConfigurationScreen()
    }
}