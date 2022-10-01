package com.dharmapal.parking_manager_kt.Retrofit


import com.dharmapal.parking_manager_kt.Utills.Config
import com.dharmapal.parking_manager_kt.models.logInResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface RetrofitService {

    @POST("login")
    @Headers("apikey:d29985af97d29a80e40cd81016d939af")
    fun logIn(
        @Query("mobile") mobile: String,
        @Query("pass") pass: String
        ) : Call<logInResponse>

    companion object {

        private var retrofitService: RetrofitService? = null

        fun getInstance() : RetrofitService {

            if (retrofitService == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://manage.spotiz.app/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                retrofitService = retrofit.create(RetrofitService::class.java)
            }
            return retrofitService!!
        }
    }
}