package com.dharmapal.parking_manager_kt.models


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class logInResponse(
    @SerializedName("id")
    @Expose
    val id: String?,
    @SerializedName("msg")
    @Expose
    val msg: String?,
    @SerializedName("name")
    @Expose
    val name: String?,
    @SerializedName("status")
    @Expose
    val status: String?
)

data class LogInReq(
    @SerializedName("mobile")
    @Expose
    val mobile:String="1234567890",
    @SerializedName("pass")
    @Expose
    val pass:String="1234567890"
)