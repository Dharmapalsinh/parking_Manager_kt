package com.dharmapal.parking_manager_kt

import com.dharmapal.parking_manager_kt.Retrofit.RetrofitClientCopy
import com.dharmapal.parking_manager_kt.models.ForgotPasswordReq
import com.dharmapal.parking_manager_kt.models.SaveParameters
import retrofit2.http.Header

class Repo2 constructor(private val retrofitService: RetrofitClientCopy){
    fun QRtest() =retrofitService.instance2.tempQR()

}

class Repo constructor(private val retrofitService: RetrofitClientCopy)  {
    fun logIn(number: String,pass :String)=retrofitService.instance.logIn(number,pass)
    fun submit() = retrofitService.instance.submit()

    fun forgotPassword(forgotPasswordReq: ForgotPasswordReq)=retrofitService.instance.forgotPassword(forgotPasswordReq.mail)

    fun save(saveParameters: SaveParameters)=retrofitService.instance.save(saveParameters.vehicle_no,
                    saveParameters.vehicle_type,saveParameters.slot_number,saveParameters.slot_id,
                    saveParameters.type,saveParameters.smart_code)

    fun slot(slotParameters: String)=retrofitService.instance.slot(slotParameters)

    fun price()=retrofitService.instance.price()

    fun missing(vehicle_no: String)=retrofitService.instance.missing(vehicle_no)

    fun scan(pass_no: String)=retrofitService.instance.scan(pass_no)

    fun checkout(pass_no: String)=retrofitService.instance.checkout(pass_no)
}