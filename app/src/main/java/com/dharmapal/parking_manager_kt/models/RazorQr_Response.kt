package com.dharmapal.parking_manager_kt.models


import com.google.gson.annotations.SerializedName

data class RazorQrResponse(
    @SerializedName("count")
    val count: Int?,
    @SerializedName("entity")
    val entity: String?,
    @SerializedName("items")
    val items: List<Item>?
)