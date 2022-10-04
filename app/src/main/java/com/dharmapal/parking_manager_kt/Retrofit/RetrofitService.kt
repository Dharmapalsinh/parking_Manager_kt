package com.dharmapal.parking_manager_kt.Retrofit


import com.dharmapal.parking_manager_kt.Utills.Config
import com.dharmapal.parking_manager_kt.models.LogInReq
import com.dharmapal.parking_manager_kt.models.logInResponse
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

//interface RetrofitService {
//
//    @POST("login")
//    @Headers("apikey:d29985af97d29a80e40cd81016d939af")
//    fun logIn(
//        @Body logInReq: LogInReq?
//        ) : Call<logInResponse>
//
//    companion object {
//
//        private var retrofitService: RetrofitService? = null
//
//        fun getInstance() : RetrofitService {
//
//            if (retrofitService == null) {
//                val retrofit = Retrofit.Builder()
//                    .baseUrl("https://manage.spotiz.app/api/")
//                    .client(okHttpBuilder)
//                    .addConverterFactory(GsonConverterFactory.create(GsonBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().serializeSpecialFloatingPointValues().setLenient().create()))
//                    .build()
//                retrofitService = retrofit.create(RetrofitService::class.java)
//            }
//            return retrofitService!!
//        }
//
//        private val okHttpBuilder: OkHttpClient = OkHttpClient.Builder()
//            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
//            .addInterceptor(RetrofitInterceptor())
//            .callTimeout(2, TimeUnit.MINUTES)
//            .readTimeout(2, TimeUnit.MINUTES)
//            .build()
//
//    }
//}