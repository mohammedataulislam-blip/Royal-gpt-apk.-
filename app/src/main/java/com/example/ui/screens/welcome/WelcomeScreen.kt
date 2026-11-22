package com.example.ui.screens.welcome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.firebase.AuthManager
import com.example.data.repository.UserRepository
import com.example.ui.components.RoyalAtmosphereBackground
import com.example.ui.components.RoyalCrestLogo
import com.example.ui.components.RoyalLuxuryCard
import com.example.ui.components.RoyalOutlinedButton
import com.example.ui.components.RoyalPrimaryButton
import com.example.ui.components.RoyalStatusBadge
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onAuthenticated: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var visible by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        visible = true
    }

    RoyalAtmosphereBackground(
        modifier = Modifier
            .fillMaxSize()
            .testTag("welcome_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 560.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RoyalCrestLogo(size = 40.dp, animated = false)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.brand_wordmark),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    RoyalStatusBadge(
                        text = stringResource(R.string.welcome_badge),
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 6 })
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(RoundedCornerShape(26.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_welcome_hero_1795322302607),
                                contentDescription = "Royal GPT luxury sapphire prism artwork",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.background.copy(alpha = 0.85f)
                                            )
                                        )
                                    )
                            )
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RoyalStatusBadge(
                                    text = "END-TO-END CLOUD SYNC",
                                    accentColor = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = stringResource(R.string.welcome_headline),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.welcome_subtitle),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            WelcomeFeaturePill(Icons.Outlined.AutoAwesome, "6 AI Tools")
                            WelcomeFeaturePill(Icons.Outlined.Lock, "Zero-Trust Rules")
                            WelcomeFeaturePill(Icons.Outlined.Shield, "Google Credential Auth")
                        }

                        if (!errorMessage.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            RoyalLuxuryCard(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f)
                            ) {
                                Text(
                                    text = errorMessage.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        RoyalPrimaryButton(
                            text = stringResource(R.string.action_login),
                            onClick = onLoginClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("welcome_login_button"),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Login,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        RoyalOutlinedButton(
                            text = stringResource(R.string.action_create_account),
                            onClick = onCreateAccountClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("welcome_create_account_button"),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        RoyalOutlinedButton(
                            text = stringResource(R.string.action_continue_google),
                            isLoading = isGoogleLoading,
                            onClick = {
                                errorMessage = null
                                isGoogleLoading = true
                                AuthManager.onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = { user ->
                                        scope.launch {
                                            runCatching { UserRepository(context).ensureUserProfile(user) }
                                            isGoogleLoading = false
                                            onAuthenticated()
                                        }
                                    },
                                    onAuthError = { msg ->
                                        isGoogleLoading = false
                                        errorMessage = msg
                                    },
                                    scope = scope,
                                    onAuthCancelled = {
                                        isGoogleLoading = false
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("welcome_google_button")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeFeaturePill(
    icon: ImageVector,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
