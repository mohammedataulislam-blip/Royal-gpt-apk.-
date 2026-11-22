package com.example.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.firebase.AuthManager
import com.example.data.repository.UserRepository
import com.example.ui.components.RoyalAtmosphereBackground
import com.example.ui.components.RoyalCrestLogo
import com.example.ui.components.RoyalStatusBadge
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onFinished: (isAuthenticated: Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(0.75f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
            )
        }

        val credentialManager = CredentialManager.create(context)
        var resolvedAuth: Boolean? = null

        AuthManager.attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { user ->
                scope.launch {
                    runCatching { UserRepository(context).ensureUserProfile(user) }
                    resolvedAuth = true
                }
            },
            onUnauthenticated = {
                resolvedAuth = false
            },
            scope = scope
        )

        delay(1450L)
        while (resolvedAuth == null) {
            delay(100L)
        }
        onFinished(resolvedAuth == true)
    }

    RoyalAtmosphereBackground(
        modifier = Modifier
            .fillMaxSize()
            .testTag("splash_screen")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .scale(scale.value)
                    .alpha(alpha.value)
            ) {
                RoyalCrestLogo(size = 132.dp, animated = true)
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = stringResource(R.string.brand_wordmark),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.brand_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                RoyalStatusBadge(
                    text = "NEURAL SUITE READY",
                    accentColor = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.height(36.dp))
                LinearProgressIndicator(
                    modifier = Modifier.height(3.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.splash_initializing),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
