package com.dharmapal.parking_manager_kt.Retrofit

import android.util.Base64.NO_WRAP
import android.util.Base64.encodeToString
import com.dharmapal.parking_manager_kt.models.*
import retrofit2.Call
import retrofit2.http.*
import java.util.*

interface API2{
    @GET("payments/qr_codes/")
    fun tempQR(
    ):Call<RazorQrResponse>

}

interface API {


    @POST("login")
    fun logIn(
        @Query("mobile") mobile:String="1234567890",
        @Query("pass") pass:String="1234567890"
    ) : Call<LogInResponse>

    @GET("forget_password")
    fun forgotPassword(
        @Query("mail") mail:String?
    ) : Call<ForgotPasswordResponse>

    @GET("dashboard")
    fun submit(
    ) : Call<DashResponse>

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
    fun price():Call<PriceResponse>

    @POST("missing")
    fun missing(
        @Query("vehicle_no") vehicle_no : String
    ) : Call<MissingResponse>

    @POST("scan")
    fun scan(
        @Query("pass_no") pass_no : String
    ) : Call<ScanResponse>

    @GET("chekout")
    fun checkout(
        @Query("pass_no") pass_no : String
    ) : Call<CheckoutResponse>
}