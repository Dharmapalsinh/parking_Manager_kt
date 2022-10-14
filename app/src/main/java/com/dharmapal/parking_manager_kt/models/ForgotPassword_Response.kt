package com.dharmapal.parking_manager_kt.models


import com.google.gson.annotations.SerializedName

data class ForgotPasswordResponse(
    @SerializedName("msg")
    val msg: String?,
    @SerializedName("status")
    val status: String?
)

data class ForgotPasswordReq(
    val mail:String?
)