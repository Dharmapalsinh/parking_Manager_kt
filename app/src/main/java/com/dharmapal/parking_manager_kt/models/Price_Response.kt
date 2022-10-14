package com.dharmapal.parking_manager_kt.models


import com.google.gson.annotations.SerializedName

data class PriceResponse(
    @SerializedName("msg")
    val msg: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("two_wheeler")
    val twoWheeler: List<TwoWheeler>?
)