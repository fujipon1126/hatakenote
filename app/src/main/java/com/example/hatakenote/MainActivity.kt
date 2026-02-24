package com.example.hatakenote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.hatakenote.core.ui.theme.HatakeNoteTheme
import com.example.hatakenote.navigation.HatakeNoteNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HatakeNoteTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HatakeNoteNavHost()
                }
            }
        }
    }
}
