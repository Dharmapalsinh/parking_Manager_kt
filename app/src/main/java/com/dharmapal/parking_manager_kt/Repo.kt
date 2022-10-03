package com.dharmapal.parking_manager_kt

import com.dharmapal.parking_manager_kt.Retrofit.RetrofitService
import com.dharmapal.parking_manager_kt.models.LogInReq


class Repo constructor(private val retrofitService: RetrofitService)  {
    fun logIn(logInReq: LogInReq) =retrofitService.logIn(logInReq)

}