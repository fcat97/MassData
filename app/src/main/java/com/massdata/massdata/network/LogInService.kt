package com.massdata.massdata.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LogInService {
    @POST("login")
    fun logIn(@Body credential: LogInCredential): Call<LogInResponse>
}