package com.massdata.massdata.network

data class LogInCredential(
    val email: String,
    val password: String
)

data class RefreshTokenCredential(
    val actualToken: String,
    val refreshToken: String
)

data class SignUpCredential(
    val name: String,
    val email: String,
    val password: String,
    val accountId: String,
    val phoneNumber: String,
)