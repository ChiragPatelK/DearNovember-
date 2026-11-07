package com.dearnovember.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dearnovember.app.data.AppSettings
import com.dearnovember.app.ui.AppRoot
import com.dearnovember.app.ui.theme.DearNovemberTheme

object AppLock {
    private const val AUTH = BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    fun available(activity: FragmentActivity) =
        BiometricManager.from(activity).canAuthenticate(AUTH) == BiometricManager.BIOMETRIC_SUCCESS

    fun prompt(activity: FragmentActivity, onSuccess: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(activity)
        val bp = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onSuccess()
        })
        val info = BiometricPrompt.PromptInfo.Builder().setTitle("Unlock Dear November")
            .setAllowedAuthenticators(AUTH).build()
        bp.authenticate(info)
    }
}

class MainActivity : FragmentActivity() {
    private var unlocked by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = (application as DearNovemberApp).repository
        setContent {
            val settings by repo.m.settings().collectAsStateWithLifecycle(initialValue = null as AppSettings?)
            val s = settings
            DearNovemberTheme(style = s?.style ?: "ROMANTIC", mode = s?.mode ?: "SYSTEM") {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    when {
                        s == null -> Box(Modifier.fillMaxSize())
                        s.appLock && !unlocked -> LockScreen { AppLock.prompt(this@MainActivity) { unlocked = true } }
                        else -> AppRoot()
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        unlocked = false // re-lock whenever the app leaves the foreground
    }
}

@Composable
private fun LockScreen(onUnlock: () -> Unit) {
    LaunchedEffect(Unit) { onUnlock() }
    Column(Modifier.fillMaxSize().padding(32.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("Dear November", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("Your journal is locked.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onUnlock) { Text("Unlock") }
    }
}
