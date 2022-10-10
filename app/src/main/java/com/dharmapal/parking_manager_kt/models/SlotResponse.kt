package com.dharmapal.parking_manager_kt.models

data class SlotResponse(
    val msg: String,
    val slot: String,
    val slot_id: String,
    val status: String
)

data class SlotParameters(
    val vehicle_type:String?
)