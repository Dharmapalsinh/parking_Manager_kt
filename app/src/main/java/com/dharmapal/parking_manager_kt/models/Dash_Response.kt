package com.dharmapal.parking_manager_kt.models


import com.google.gson.annotations.SerializedName

data class DashResponse(
    @SerializedName("avail_per")
    val availPer: Double?,
    @SerializedName("available")
    val available: Int?,
    @SerializedName("missing_pass")
    val missingPass: Int?,
    @SerializedName("msg")
    val msg: String?,
    @SerializedName("occ_per")
    val occPer: Double?,
    @SerializedName("occupied")
    val occupied: Int?,
    @SerializedName("prepaid_user")
    val prepaidUser: Int?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("total_col")
    val totalCol: String?,
    @SerializedName("total_vehicle")
    val totalVehicle: Int?,
    @SerializedName("vip_user")
    val vipUser: Int?
)