package com.dharmapal.parking_manager_kt.Retrofit

import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.internal.http2.ConnectionShutdownException
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class RetrofitClientCopy(private val serverUrl: String = "https://manage.spotiz.app/api/") {

    private val okHttpBuilder: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .addInterceptor(RetrofitInterceptor())
        .callTimeout(2, TimeUnit.MINUTES)
        .readTimeout(2, TimeUnit.MINUTES)
        .build()

    private val okHttpBuilder2: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .addInterceptor(RetrofitInterceptor2())
        .callTimeout(2, TimeUnit.MINUTES)
        .readTimeout(2, TimeUnit.MINUTES)
        .build()


    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(serverUrl)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().serializeNulls().serializeSpecialFloatingPointValues().setLenient().create()))
            .client(okHttpBuilder)
            .build()
    }


    private val retrofit2: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.razorpay.com/v1/")
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().serializeNulls().serializeSpecialFloatingPointValues().setLenient().create()))
            .client(okHttpBuilder2)
            .build()
    }

    val instance: API by lazy { retrofit.create(API::class.java) }
    val instance2: API2 by lazy { retrofit2.create(API2::class.java) }
}

class RetrofitInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val request = chain.request().newBuilder()
                .addHeader("Accept", "application/json")
                .addHeader("apiKey", "d29985af97d29a80e40cd81016d939af")
                .build()
            return chain.proceed(request)
        } catch (e: Exception) {
            e.printStackTrace()
            val msg: String = when (e) {
                is SocketTimeoutException -> "Timeout - Please check your internet connection"
                is UnknownHostException -> "Unable to make a connection. Please check your internet"
                is ConnectionShutdownException -> "Connection shutdown. Please check your internet"
                is IOException -> "Server is unreachable, please try again later."
                is IllegalStateException -> "${e.message}"
                else -> "${e.message}"
            }

            return Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(999)
                .message(msg)
                .body("{${e}}".toResponseBody(null)).build()
        }
    }
}

class RetrofitInterceptor2: Interceptor {

    private var credentials: String = Credentials.basic("rzp_test_CYmjPvZ9udBdjl", "xGwUIty7DsvhNxNZj6sehVu5")

    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val request = chain.request().newBuilder()
                .addHeader("Accept", "application/json")
                .header("Authorization",credentials)
                .build()
            return chain.proceed(request)
        } catch (e: Exception) {
            e.printStackTrace()
            val msg: String = when (e) {
                is SocketTimeoutException -> "Timeout - Please check your internet connection"
                is UnknownHostException -> "Unable to make a connection. Please check your internet"
                is ConnectionShutdownException -> "Connection shutdown. Please check your internet"
                is IOException -> "Server is unreachable, please try again later."
                is IllegalStateException -> "${e.message}"
                else -> "${e.message}"
            }

            return Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(999)
                .message(msg)
                .body("{${e}}".toResponseBody(null)).build()
        }
    }
}
