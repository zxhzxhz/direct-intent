package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.ShortcutRepository
import com.example.ui.MainScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private lateinit var viewModel: MainViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = AppDatabase.getDatabase(applicationContext)
    val repository = ShortcutRepository(database.shortcutDao())
    val factory = MainViewModel.Factory(application, repository)
    viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

    setContent {
      val systemDark = isSystemInDarkTheme()
      var isDarkTheme by remember { mutableStateOf(systemDark) }

      MyApplicationTheme(darkTheme = isDarkTheme) {
        MainScreen(
          viewModel = viewModel,
          isDarkTheme = isDarkTheme,
          onToggleTheme = { isDarkTheme = !isDarkTheme }
        )
      }
    }
  }
}

