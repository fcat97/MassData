package com.massdata.massdata.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface HomeScreenService {
    @GET("registeruser")
    fun isTokenValid(@Header("Authorization") token: String): Call<TokenValidityResponse>

    @POST("refresh-token")
    fun refreshToken(@Body credential: RefreshTokenCredential): Call<LogInResponse>
}