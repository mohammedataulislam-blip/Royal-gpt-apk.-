package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.firebase.authStateFlow
import com.example.data.repository.AppSettingsPreferences
import com.example.data.repository.SettingsRepository
import com.example.ui.navigation.RoyalAppRoot
import com.example.ui.theme.RoyalGptTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsRepository = remember(applicationContext) {
                SettingsRepository(applicationContext)
            }
            val preferences by settingsRepository.preferencesFlow.collectAsStateWithLifecycle(
                initialValue = AppSettingsPreferences()
            )
            val currentUser by remember { Firebase.auth.authStateFlow() }
                .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

            RoyalGptTheme(darkTheme = preferences.darkThemeEnabled) {
                RoyalAppRoot(
                    currentUser = currentUser,
                    settingsRepository = settingsRepository
                )
            }
        }
    }
}
