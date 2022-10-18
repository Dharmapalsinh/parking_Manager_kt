package com.dharmapal.parking_manager_kt.models


import com.google.gson.annotations.SerializedName

data class RazorQr_Response(
    @SerializedName("count")
    val count: Int?,
    @SerializedName("entity")
    val entity: String?,
    @SerializedName("items")
    val items: List<Item>?
)