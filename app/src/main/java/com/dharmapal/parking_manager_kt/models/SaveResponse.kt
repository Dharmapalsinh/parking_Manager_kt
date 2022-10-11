package com.dharmapal.parking_manager_kt.models

data class SaveResponse(
    val amount: String,
    val checked_in: String,
    val msg: String,
    val pass_no: String,
    val slot_number: String,
    val status: String,
    val type: String,
    val vehicle_no: String,
    val vehicle_type: String
)

data class SaveParameters(
    val vehicle_no:String,
    val vehicle_type:String,
    val slot_number:String,
    val slot_id:String,
    val type:String,
    val smart_code:String)