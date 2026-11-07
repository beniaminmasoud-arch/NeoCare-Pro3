package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.example.ui.NeoCareMainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.NeoCareViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: NeoCareViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = state.isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NeoCareMainScreen(viewModel = viewModel)
                }
            }
        }
    }
}
