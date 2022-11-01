package com.dharmapal.parking_manager_kt.models


import com.google.gson.annotations.SerializedName

data class ArrivingVehicleResponse(
    @SerializedName("msg")
    val msg: String?,
    @SerializedName("response")
    val response: Response?,
    @SerializedName("status")
    val status: String?
)