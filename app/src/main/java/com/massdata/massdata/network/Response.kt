package com.massdata.massdata.network

import java.util.*

data class ApiQueryResponse(
    val statusCode: Int,
    val value: Boolean
)

data class SignUpResponse(
    val statusCode: Int,
    val message: String
)

data class LogInResponse(
    val statusCode: Int,
    val value: LogInValue,
) {
    companion object {
        fun getEmptyResponse(): LogInResponse {
            return LogInResponse(
                statusCode = 0,
                value = LogInValue.getEmptyInstance()
            )
        }
    }
}

data class LogInValue(
    val name: String,
    val roles: List<String>,
    val email: String,
    val id: String,
    val databaseId: String,
    val token: String,
    val refreshToken: String,
    val tokenExpireTime: Date,
    val refreshTokenExpireTime: Date
) {
    companion object {
        fun getEmptyInstance(): LogInValue {
            return LogInValue(
                name = "",
                roles = emptyList(),
                email = "",
                id = "",
                databaseId = "",
                token = "",
                refreshToken = "",
                tokenExpireTime = Date(),
                refreshTokenExpireTime = Date()
            )
        }
    }
}

data class TokenValidityResponse(
    val name: String,
    val email: String,
    val id: String,
    val playerId: String
)