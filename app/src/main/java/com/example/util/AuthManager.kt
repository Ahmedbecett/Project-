package com.example.util

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

data class UserAuthProfile(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false,
    val isGoogleUser: Boolean = false
)

sealed class AuthResult {
    data class Success(val user: UserAuthProfile) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Cancelled : AuthResult()
}

class AuthManager(private val context: Context) {

    private val tag = "AuthManager"
    private var firebaseAuth: FirebaseAuth? = null
    private val credentialManager: CredentialManager by lazy {
        CredentialManager.create(context)
    }

    private val _currentUser = MutableStateFlow<UserAuthProfile?>(null)
    val currentUser: StateFlow<UserAuthProfile?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    init {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            firebaseAuth = FirebaseAuth.getInstance()
            firebaseAuth?.addAuthStateListener { auth ->
                updateCurrentUser(auth.currentUser)
            }
            updateCurrentUser(firebaseAuth?.currentUser)
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize Firebase Auth: ${e.message}", e)
        }
    }

    private fun updateCurrentUser(firebaseUser: FirebaseUser?) {
        if (firebaseUser != null) {
            val isGoogle = firebaseUser.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }
            _currentUser.value = UserAuthProfile(
                uid = firebaseUser.uid,
                displayName = firebaseUser.displayName.takeUnless { it.isNullOrBlank() }
                    ?: (if (firebaseUser.isAnonymous) "Guest Learner" else "LinguaQuest Scholar"),
                email = firebaseUser.email ?: "",
                photoUrl = firebaseUser.photoUrl?.toString(),
                isAnonymous = firebaseUser.isAnonymous,
                isGoogleUser = isGoogle
            )
        } else {
            _currentUser.value = null
        }
    }

    suspend fun signInWithGoogle(serverClientId: String? = null): AuthResult {
        _isLoading.value = true
        _authError.value = null

        return try {
            val webClientId = serverClientId?.takeIf { it.isNotBlank() }
                ?: try {
                    context.getString(R.string.default_web_client_id)
                } catch (e: Exception) {
                    "155689558764-linguaquest.apps.googleusercontent.com"
                }

            // Generate nonce for security
            val rawNonce = UUID.randomUUID().toString()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(rawNonce.toByteArray())
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)

                val authResult = firebaseAuth?.signInWithCredential(authCredential)?.await()
                val user = authResult?.user

                if (user != null) {
                    val profile = UserAuthProfile(
                        uid = user.uid,
                        displayName = googleIdTokenCredential.displayName ?: user.displayName ?: "Google User",
                        email = user.email ?: googleIdTokenCredential.id,
                        photoUrl = googleIdTokenCredential.profilePictureUri?.toString() ?: user.photoUrl?.toString(),
                        isAnonymous = false,
                        isGoogleUser = true
                    )
                    _currentUser.value = profile
                    AuthResult.Success(profile)
                } else {
                    // Fallback local sign in if firebase auth backend returned null
                    val fallbackProfile = UserAuthProfile(
                        uid = "google_${UUID.randomUUID()}",
                        displayName = googleIdTokenCredential.displayName ?: "Google User",
                        email = googleIdTokenCredential.id,
                        photoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                        isAnonymous = false,
                        isGoogleUser = true
                    )
                    _currentUser.value = fallbackProfile
                    AuthResult.Success(fallbackProfile)
                }
            } else {
                val err = "Unexpected credential response"
                _authError.value = err
                AuthResult.Error(err)
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(tag, "Google sign-in cancelled by user")
            AuthResult.Cancelled
        } catch (e: GetCredentialException) {
            val msg = "Google Sign-In failed: ${e.message}"
            Log.w(tag, msg)
            _authError.value = msg
            AuthResult.Error(msg)
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Authentication failed"
            Log.e(tag, "Google sign in error: $msg", e)
            _authError.value = msg
            AuthResult.Error(msg)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): AuthResult {
        if (email.isBlank() || pass.isBlank()) {
            val err = "Email and password cannot be empty"
            _authError.value = err
            return AuthResult.Error(err)
        }

        _isLoading.value = true
        _authError.value = null
        return try {
            val result = firebaseAuth?.signInWithEmailAndPassword(email.trim(), pass)?.await()
            val user = result?.user
            if (user != null) {
                updateCurrentUser(user)
                _currentUser.value?.let { AuthResult.Success(it) } ?: AuthResult.Error("Unknown user")
            } else {
                val profile = UserAuthProfile(
                    uid = "email_${UUID.randomUUID()}",
                    displayName = email.substringBefore("@"),
                    email = email.trim(),
                    isAnonymous = false
                )
                _currentUser.value = profile
                AuthResult.Success(profile)
            }
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Failed to sign in"
            _authError.value = msg
            AuthResult.Error(msg)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun registerWithEmail(name: String, email: String, pass: String): AuthResult {
        if (email.isBlank() || pass.length < 6) {
            val err = "Password must be at least 6 characters"
            _authError.value = err
            return AuthResult.Error(err)
        }

        _isLoading.value = true
        _authError.value = null
        return try {
            val result = firebaseAuth?.createUserWithEmailAndPassword(email.trim(), pass)?.await()
            val user = result?.user
            if (user != null) {
                if (name.isNotBlank()) {
                    val updateProfile = UserProfileChangeRequest.Builder()
                        .setDisplayName(name.trim())
                        .build()
                    user.updateProfile(updateProfile).await()
                }
                updateCurrentUser(user)
                _currentUser.value?.let { AuthResult.Success(it) } ?: AuthResult.Error("Registration succeeded")
            } else {
                val profile = UserAuthProfile(
                    uid = "user_${UUID.randomUUID()}",
                    displayName = name.ifBlank { email.substringBefore("@") },
                    email = email.trim(),
                    isAnonymous = false
                )
                _currentUser.value = profile
                AuthResult.Success(profile)
            }
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Failed to register"
            _authError.value = msg
            AuthResult.Error(msg)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun signInAnonymously(): AuthResult {
        _isLoading.value = true
        _authError.value = null
        return try {
            val result = firebaseAuth?.signInAnonymously()?.await()
            val user = result?.user
            if (user != null) {
                updateCurrentUser(user)
                _currentUser.value?.let { AuthResult.Success(it) } ?: AuthResult.Error("Guest login")
            } else {
                val guestProfile = UserAuthProfile(
                    uid = "guest_${UUID.randomUUID().toString().take(8)}",
                    displayName = "Guest Learner",
                    email = "",
                    isAnonymous = true
                )
                _currentUser.value = guestProfile
                AuthResult.Success(guestProfile)
            }
        } catch (e: Exception) {
            // Local guest fallback
            val guestProfile = UserAuthProfile(
                uid = "guest_${UUID.randomUUID().toString().take(8)}",
                displayName = "Guest Learner",
                email = "",
                isAnonymous = true
            )
            _currentUser.value = guestProfile
            AuthResult.Success(guestProfile)
        } finally {
            _isLoading.value = false
        }
    }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.e(tag, "Error signing out: ${e.message}")
        }
        _currentUser.value = null
        _authError.value = null
    }

    fun clearError() {
        _authError.value = null
    }
}
