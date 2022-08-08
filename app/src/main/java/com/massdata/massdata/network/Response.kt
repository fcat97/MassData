package com.massdata.massdata.network

import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Response
import java.io.IOException
import java.util.*


data class ApiQueryResponse(
    val statusCode: Int,
    val value: Boolean
)

data class SignUpResponse(
    val statusCode: Int,
    val message: String
)

data class SignUpUnsuccessfulResponse(
    val type: String = "",
    val title: String = "",
    val status: Int = 0,
    val traceId: String = "",
    val errors: Error = Error()
)

data class Error(
    val Email: List<String> = emptyList(),
    val Password: List<String> = emptyList(),
    val PhoneNumber: List<String> = emptyList()
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

data class ShortResponse(
    val statusCode: Int = 0,
    val message: String = "Parse Failed"
)