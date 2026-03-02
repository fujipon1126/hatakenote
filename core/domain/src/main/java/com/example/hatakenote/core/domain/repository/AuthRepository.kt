package com.example.hatakenote.core.domain.repository

import com.example.hatakenote.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    val isSignedIn: Flow<Boolean>

    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signOut()
}
