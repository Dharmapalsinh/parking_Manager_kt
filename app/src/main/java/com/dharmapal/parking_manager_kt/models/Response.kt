package com.dharmapal.parking_manager_kt.models


import com.google.gson.annotations.SerializedName

data class Response(
    @SerializedName("checkin_time")
    val checkinTime: String?,
    @SerializedName("CurrentTime")
    val currentTime: String?,
    @SerializedName("vehicle_no")
    val vehicleNo: String?,
    @SerializedName("vehicle_type")
    val vehicleType: String?,
    @SerializedName("amount")
    val amount: String?
)