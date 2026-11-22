package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
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

@Composable
fun AuthScreen(
    initialMode: AuthMode = AuthMode.LOGIN,
    onBack: () -> Unit,
    onAuthenticated: () -> Unit
) {
    val viewModel: AuthViewModel = viewModel(
        key = "auth_${initialMode.name}",
        factory = viewModelFactory {
            initializer { AuthViewModel(initialMode) }
        }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val triggerGoogleSignIn = {
        viewModel.setGoogleLoading(true)
        AuthManager.onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { user ->
                scope.launch {
                    runCatching { UserRepository(context).ensureUserProfile(user) }
                    viewModel.setGoogleLoading(false)
                    onAuthenticated()
                }
            },
            onAuthError = { msg -> viewModel.setError(msg) },
            scope = scope,
            onAuthCancelled = { viewModel.setGoogleLoading(false) }
        )
    }

    if (state.showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissForgotPasswordDialog,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            title = {
                Text(
                    text = "Reset Password",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Enter your account email to validate recovery readiness.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = state.forgotPasswordEmail,
                        onValueChange = viewModel::onForgotPasswordEmailChanged,
                        label = { Text(stringResource(R.string.auth_email_label)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("forgot_password_email_input")
                    )
                    if (!state.forgotPasswordStatus.isNullOrBlank()) {
                        Text(
                            text = state.forgotPasswordStatus.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::submitForgotPassword,
                    modifier = Modifier.testTag("forgot_password_submit_button")
                ) {
                    Text("Validate Recovery")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissForgotPasswordDialog) {
                    Text("Close")
                }
            }
        )
    }

    RoyalAtmosphereBackground(
        modifier = Modifier
            .fillMaxSize()
            .testTag("auth_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 520.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("auth_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to welcome",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    RoyalStatusBadge(
                        text = "FIREBASE AUTH READY",
                        accentColor = MaterialTheme.colorScheme.tertiary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                RoyalCrestLogo(size = 68.dp, animated = false)
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (state.mode == AuthMode.LOGIN) {
                        stringResource(R.string.auth_login_title)
                    } else {
                        stringResource(R.string.auth_register_title)
                    },
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Encrypted cloud workspace powered by Firebase Auth",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = state.mode == AuthMode.LOGIN,
                        onClick = { viewModel.setMode(AuthMode.LOGIN) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        modifier = Modifier.testTag("auth_mode_login")
                    ) {
                        Text(stringResource(R.string.action_login))
                    }
                    SegmentedButton(
                        selected = state.mode == AuthMode.CREATE_ACCOUNT,
                        onClick = { viewModel.setMode(AuthMode.CREATE_ACCOUNT) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        modifier = Modifier.testTag("auth_mode_register")
                    ) {
                        Text(stringResource(R.string.action_create_account))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                RoyalPrimaryButton(
                    text = stringResource(R.string.action_continue_google),
                    isLoading = state.isGoogleLoading,
                    onClick = triggerGoogleSignIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_google_signin_button"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.VerifiedUser,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "  OR EMAIL CREDENTIALS  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                RoyalLuxuryCard(modifier = Modifier.fillMaxWidth()) {
                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                    )

                    AnimatedVisibility(visible = state.mode == AuthMode.CREATE_ACCOUNT) {
                        Column {
                            OutlinedTextField(
                                value = state.fullName,
                                onValueChange = viewModel::onFullNameChanged,
                                label = { Text(stringResource(R.string.auth_name_label)) },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Person, contentDescription = null)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                shape = RoundedCornerShape(14.dp),
                                colors = fieldColors,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_name_input")
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChanged,
                        label = { Text(stringResource(R.string.auth_email_label)) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Email, contentDescription = null)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChanged,
                        label = { Text(stringResource(R.string.auth_password_label)) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = viewModel::togglePasswordVisibility) {
                                Icon(
                                    imageVector = if (state.passwordVisible) {
                                        Icons.Outlined.VisibilityOff
                                    } else {
                                        Icons.Outlined.Visibility
                                    },
                                    contentDescription = if (state.passwordVisible) {
                                        "Hide password"
                                    } else {
                                        "Show password"
                                    }
                                )
                            }
                        },
                        visualTransformation = if (state.passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = if (state.mode == AuthMode.LOGIN) ImeAction.Done else ImeAction.Next
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input")
                    )

                    AnimatedVisibility(visible = state.mode == AuthMode.CREATE_ACCOUNT) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = state.confirmPassword,
                                onValueChange = viewModel::onConfirmPasswordChanged,
                                label = { Text(stringResource(R.string.auth_confirm_password_label)) },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Lock, contentDescription = null)
                                },
                                visualTransformation = if (state.passwordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                shape = RoundedCornerShape(14.dp),
                                colors = fieldColors,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_confirm_password_input")
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = viewModel::openForgotPasswordDialog,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("auth_forgot_password_button")
                        ) {
                            Text(
                                text = stringResource(R.string.auth_forgot_password),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (!state.validationError.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.validationError.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    RoyalOutlinedButton(
                        text = if (state.mode == AuthMode.LOGIN) {
                            "Sign In with Email"
                        } else {
                            "Create Email Account"
                        },
                        onClick = viewModel::submitEmailForm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_submit_button")
                    )
                }

                if (!state.providerReadinessNotice.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    RoyalLuxuryCard(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        borderBrush = androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                            )
                        )
                    ) {
                        Text(
                            text = state.providerReadinessNotice.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        RoyalPrimaryButton(
                            text = "Sign In Now with Google",
                            onClick = triggerGoogleSignIn,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_notice_google_button")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                RoyalLuxuryCard(
                    onClick = viewModel::toggleVerificationDetails,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_verification_card")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MarkEmailRead,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.auth_email_verification_title),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (state.showVerificationInfo) "Tap to collapse details" else "Tap to inspect verification architecture",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    AnimatedVisibility(visible = state.showVerificationInfo) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = stringResource(R.string.auth_email_verification_body),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
