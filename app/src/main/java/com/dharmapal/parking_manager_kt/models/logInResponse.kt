package com.dharmapal.parking_manager_kt.models


import com.google.gson.annotations.SerializedName

data class logInResponse(
    @SerializedName("id")
    val id: String?,
    @SerializedName("msg")
    val msg: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("status")
    val status: String?
)