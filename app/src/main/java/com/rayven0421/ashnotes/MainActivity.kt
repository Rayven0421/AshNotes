package com.rayven0421.ashnotes

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.rayven0421.ashnotes.core.common.BiometricUtils
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.core.designsystem.LockScreen
import com.rayven0421.ashnotes.domain.repository.UserPreferencesRepository
import com.rayven0421.ashnotes.navigation.AshNotesNavHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private var isBiometricEnabled by mutableStateOf(false)
    private var isUnlocked by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            userPreferencesRepository.isBiometricEnabled.collect { enabled ->
                isBiometricEnabled = enabled
                if (!enabled) {
                    isUnlocked = true
                }
            }
        }

        setContent {
            AshNotesTheme {
                if (isBiometricEnabled && !isUnlocked) {
                    LockScreen(
                        onUnlock = {
                            promptBiometric()
                        }
                    )
                } else {
                    AshNotesNavHost()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isBiometricEnabled && !isUnlocked) {
            promptBiometric()
        }
    }

    private fun promptBiometric() {
        BiometricUtils.showBiometricPrompt(
            activity = this,
            title = getString(R.string.biometric_prompt_title),
            subtitle = getString(R.string.biometric_prompt_subtitle),
            onSuccess = {
                isUnlocked = true
            },
            onError = { _ ->
                // Stay locked, user can retry via Unlock button
            }
        )
    }
}
