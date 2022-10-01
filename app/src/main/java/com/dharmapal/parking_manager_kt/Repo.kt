package com.dharmapal.parking_manager_kt

import com.dharmapal.parking_manager_kt.Retrofit.RetrofitService


class Repo constructor(private val retrofitService: RetrofitService)  {
    fun logIn(number: String,pass :String)=retrofitService.logIn(number,pass)

}