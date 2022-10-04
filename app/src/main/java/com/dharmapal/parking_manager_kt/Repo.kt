package com.dharmapal.parking_manager_kt

//import com.dharmapal.parking_manager_kt.Retrofit.RetrofitClientCopy
import com.dharmapal.parking_manager_kt.Retrofit.RetrofitClientCopy
import com.dharmapal.parking_manager_kt.models.ForgotPassword_Req
import com.dharmapal.parking_manager_kt.models.LogInReq


class Repo constructor(private val retrofitService: RetrofitClientCopy)  {
    fun logIn(logInReq: LogInReq) =retrofitService.spotizInstance.logIn()

    fun forgot_Password(forgotpasswordReq: ForgotPassword_Req)=retrofitService.spotizInstance.forgotPassword(forgotpasswordReq)
}