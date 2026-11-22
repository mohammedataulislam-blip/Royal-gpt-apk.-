package com.example.ui.screens.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AuthMode {
    LOGIN,
    CREATE_ACCOUNT
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val showForgotPasswordDialog: Boolean = false,
    val forgotPasswordEmail: String = "",
    val forgotPasswordStatus: String? = null,
    val showVerificationInfo: Boolean = false,
    val validationError: String? = null,
    val providerReadinessNotice: String? = null
)

class AuthViewModel(initialMode: AuthMode = AuthMode.LOGIN) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState(mode = initialMode))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun setMode(mode: AuthMode) {
        _uiState.update {
            it.copy(
                mode = mode,
                validationError = null,
                providerReadinessNotice = null
            )
        }
    }

    fun onFullNameChanged(value: String) {
        _uiState.update { it.copy(fullName = value, validationError = null) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, validationError = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, validationError = null) }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { it.copy(confirmPassword = value, validationError = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun setGoogleLoading(loading: Boolean) {
        _uiState.update { it.copy(isGoogleLoading = loading, validationError = null) }
    }

    fun setError(message: String?) {
        _uiState.update { it.copy(validationError = message, isGoogleLoading = false) }
    }

    fun toggleVerificationDetails() {
        _uiState.update { it.copy(showVerificationInfo = !it.showVerificationInfo) }
    }

    fun openForgotPasswordDialog() {
        _uiState.update {
            it.copy(
                showForgotPasswordDialog = true,
                forgotPasswordEmail = it.email,
                forgotPasswordStatus = null
            )
        }
    }

    fun dismissForgotPasswordDialog() {
        _uiState.update {
            it.copy(showForgotPasswordDialog = false, forgotPasswordStatus = null)
        }
    }

    fun onForgotPasswordEmailChanged(value: String) {
        _uiState.update { it.copy(forgotPasswordEmail = value, forgotPasswordStatus = null) }
    }

    fun submitForgotPassword() {
        val targetEmail = _uiState.value.forgotPasswordEmail.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(targetEmail).matches()) {
            _uiState.update {
                it.copy(forgotPasswordStatus = "Please enter a valid email address first.")
            }
            return
        }
        _uiState.update {
            it.copy(
                forgotPasswordStatus = "Password recovery flow validated for $targetEmail. " +
                    "To dispatch live reset emails, enable Email/Password in Firebase Console → Authentication → Sign-in method. " +
                    "Google accounts can sign in immediately with Continue with Google."
            )
        }
    }

    fun submitEmailForm() {
        val state = _uiState.value
        val trimmedEmail = state.email.trim()
        val trimmedName = state.fullName.trim()

        if (state.mode == AuthMode.CREATE_ACCOUNT && trimmedName.length < 2) {
            _uiState.update { it.copy(validationError = "Please enter your full name (at least 2 characters).") }
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _uiState.update { it.copy(validationError = "Please enter a valid email address.") }
            return
        }
        if (state.password.length < 8) {
            _uiState.update { it.copy(validationError = "Password must be at least 8 characters for Royal GPT security.") }
            return
        }
        if (state.mode == AuthMode.CREATE_ACCOUNT && state.password != state.confirmPassword) {
            _uiState.update { it.copy(validationError = "Passwords do not match.") }
            return
        }

        val actionLabel = if (state.mode == AuthMode.CREATE_ACCOUNT) {
            "Account registration & email verification payload validated"
        } else {
            "Email credentials validated"
        }

        _uiState.update {
            it.copy(
                validationError = null,
                showVerificationInfo = true,
                providerReadinessNotice = "$actionLabel for $trimmedEmail. " +
                    "Google Sign-In is currently provisioned and ready immediately below. " +
                    "To activate Email/Password login and verification links, enable the Email/Password provider in the Firebase Console."
            )
        }
    }
}
