package com.massdata.massdata.network

import retrofit2.Call
import retrofit2.http.*

interface SignUpServices {
    @GET("ifuseremailalreadyexists/{email}")
    fun isEmailTaken(@Path("email") email: String): Call<ApiQueryResponse>

    @GET("ifuserphonenumberalreadyexists/{phoneNumber}")
    fun isNumberTaken(@Path("phoneNumber") phoneNumber: String): Call<ApiQueryResponse>

    @GET("ifaccountidalreadyexists/{accountID}")
    fun isAccountIDExists(@Path("accountID") accountID: String): Call<ApiQueryResponse>

    @POST("registration")
    fun addUser(@Header("Authorization") token: String, @Body credential: SignUpCredential): Call<SignUpResponse>

    @POST("registration")
    fun createUser(@Body credential: SignUpCredential): Call<SignUpResponse>
}