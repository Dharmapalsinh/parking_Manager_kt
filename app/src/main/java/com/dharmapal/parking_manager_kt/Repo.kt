package com.dharmapal.parking_manager_kt

import com.dharmapal.parking_manager_kt.Retrofit.RetrofitClientCopy
import com.dharmapal.parking_manager_kt.models.ForgotPassword_Req


class Repo constructor(private val retrofitService: RetrofitClientCopy)  {
    fun logIn(number: String,pass :String)=retrofitService.spotizInstance.logIn(number,pass)
    fun submit() = retrofitService.spotizInstance.submit()

    fun forgot_Password(forgotpasswordReq: ForgotPassword_Req)=retrofitService.spotizInstance.forgotPassword(forgotpasswordReq)
}