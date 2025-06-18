package com.example.riberasplayer

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.riberasplayer.ui.theme.MusicPlayerTheme
import com.example.riberasplayer.ui.theme.RiberasPlayerTheme
import com.example.riberasplayer.view.MusicPlayerApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusicPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MusicPlayerApp()
                }
            }
        }
    }
}


fun isAndroid10OrAbove(): Boolean {
    val isAndroid10OrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    Log.d("VERSION_CHECK", "¿Es Android 10 o superior? $isAndroid10OrAbove")
    return isAndroid10OrAbove
}
