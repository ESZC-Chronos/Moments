package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MomentsApp
import com.example.ui.theme.MomentsTheme
import com.example.viewmodel.MomentsViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MomentsTheme {
        val viewModel: MomentsViewModel = viewModel()
        MomentsApp(viewModel)
      }
    }
  }
}
