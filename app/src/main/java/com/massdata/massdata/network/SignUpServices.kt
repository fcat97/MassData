package com.massdata.massdata.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SignUpServices {
    @GET("ifuseremailalreadyexists/{email}")
    fun isEmailTaken(@Path("email") email: String): Call<ApiQueryResponse>

    @GET("ifuserphonenumberalreadyexists/{phoneNumber}")
    fun isNumberTaken(@Path("phoneNumber") phoneNumber: String): Call<ApiQueryResponse>

    @GET("ifaccountidalreadyexists/{accountID}")
    fun isAccountIDExists(@Path("accountID") accountID: String): Call<ApiQueryResponse>

    @POST("registration")
    fun createUser(@Body credential: SignUpCredential): Call<SignUpResponse>
}