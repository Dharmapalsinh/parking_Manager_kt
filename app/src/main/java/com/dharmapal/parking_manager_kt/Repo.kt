package com.dharmapal.parking_manager_kt

import com.dharmapal.parking_manager_kt.Retrofit.RetrofitClientCopy
import com.dharmapal.parking_manager_kt.models.ForgotPassword_Req
import com.dharmapal.parking_manager_kt.models.SaveParameters
import com.dharmapal.parking_manager_kt.models.SlotParameters


class Repo constructor(private val retrofitService: RetrofitClientCopy)  {
    fun logIn(number: String,pass :String)=retrofitService.instance.logIn(number,pass)
    fun submit() = retrofitService.instance.submit()

    fun forgot_Password(forgotpasswordReq: ForgotPassword_Req)=retrofitService.instance.forgotPassword(forgotpasswordReq)

    fun save(saveParameters: SaveParameters)=retrofitService.instance.save(saveParameters)

    fun slot(slotParameters: SlotParameters)=retrofitService.instance.slot(slotParameters)

    fun price()=retrofitService.instance.price()
}