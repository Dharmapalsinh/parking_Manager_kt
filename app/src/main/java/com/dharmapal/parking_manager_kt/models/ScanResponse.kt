package com.dharmapal.parking_manager_kt.models

data class ScanResponse(
    val msg: String,
    val status: String,
    val vehicle_no: String
)