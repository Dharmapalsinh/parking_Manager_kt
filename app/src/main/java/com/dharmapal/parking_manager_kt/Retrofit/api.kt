package com.dharmapal.parking_manager_kt.Retrofit

import com.dharmapal.parking_manager_kt.models.*
import retrofit2.Call
import retrofit2.http.*

interface api {
    @POST("login")
    fun logIn(
        @Query("mobile") mobile:String="1234567890",
        @Query("pass") pass:String="1234567890"
    ) : Call<logInResponse>

    @POST("forget_password")
    fun forgotPassword(
        @Body forgotpasswordReq: ForgotPassword_Req?
    ) : Call<ForgotPassword_Response>

    @GET("dashboard")
    fun submit(
    ) : Call<Dash_Response>

    @GET("save")
    fun save (
        @Query("vehicle_no")vehicle_no:String,
        @Query("vehicle_type")vehicle_type:String,
        @Query("slot_number")slot_number:String,
        @Query("slot_id")slot_id:String,
        @Query("type")type:String,
        @Query("smart_code")smart_code:String
    ): Call<SaveResponse>

    @GET("get_slots")
    fun slot(
        @Query("vehicle_type") vehicle_type:String
    ): Call<SlotResponse>

    @POST("parking_cost")
    fun price():Call<Price_Response>

    @POST("missing")
    fun missing(
        @Query("vehicle_no") vehicle_no:String
    ) : Call<MissingResponse>
}