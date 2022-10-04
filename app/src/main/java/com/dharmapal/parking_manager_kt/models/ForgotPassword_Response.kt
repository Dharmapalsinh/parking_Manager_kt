package com.dharmapal.parking_manager_kt.models


import com.google.gson.annotations.SerializedName

data class ForgotPassword_Response(
    @SerializedName("msg")
    val msg: String?,
    @SerializedName("status")
    val status: String?
)

data class ForgotPassword_Req(
    val mail:String?
)