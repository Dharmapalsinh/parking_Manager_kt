package com.dharmapal.parking_manager_kt.Retrofit

import com.dharmapal.parking_manager_kt.models.*
import retrofit2.Call
import retrofit2.http.*

interface api {
    @GET("login")
    fun logIn(
        @Query("mobile") mobile:String="1234567890" ,
        @Query("pass") pass:String="1234567890"
    ) : Call<logInResponse>

    @POST("forget_password")
    fun forgotPassword(
        @Body forgotpasswordReq: ForgotPassword_Req?
    ) : Call<ForgotPassword_Response>

    @GET("dashboard")
    fun submit(
    ) : Call<DashboardResponse>
}