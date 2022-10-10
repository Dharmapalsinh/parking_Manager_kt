package com.dharmapal.parking_manager_kt.models


import com.google.gson.annotations.SerializedName

data class TwoWheeler(
    @SerializedName("id")
    val id: String?,
    @SerializedName("price")
    val price: String?,
    @SerializedName("vehicle_type")
    val vehicleType: String?
)