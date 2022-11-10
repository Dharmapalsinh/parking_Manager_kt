package com.dharmapal.parking_manager_kt.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dharmapal.parking_manager_kt.Repo
import com.dharmapal.parking_manager_kt.Repo2
import com.dharmapal.parking_manager_kt.models.*
import com.dharmapal.parking_manager_kt.models.DashResponse
import com.dharmapal.parking_manager_kt.models.ForgotPasswordReq
import com.dharmapal.parking_manager_kt.models.ForgotPasswordResponse
import com.dharmapal.parking_manager_kt.models.LogInResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel constructor(private val repository: Repo)  : ViewModel() {

    val errorMessage = MutableLiveData<String>()
    val loginData=MutableLiveData<LogInResponse>()
    val forgotPassData=MutableLiveData<String>()
    val dashboardData=MutableLiveData<DashResponse>()
    val saveData=MutableLiveData<SaveResponse>()
    val slotData=MutableLiveData<SlotResponse>()
    val priceData=MutableLiveData<PriceResponse>()
    val missingData=MutableLiveData<MissingResponse>()
    val checkoutData=MutableLiveData<CheckoutResponse>()
    val razorQrData=MutableLiveData<RazorQrResponse>()
    val arrivingVehicleData=MutableLiveData<ArrivingVehicleResponse>()

    fun testQr(repository: Repo2){
        val response=repository.qrTest()
        response.enqueue(object :Callback<RazorQrResponse>{
            override fun onResponse(
                call: Call<RazorQrResponse>,
                response: Response<RazorQrResponse>
            ) {
                razorQrData.postValue(response.body())
                Log.d("RazorQr",response.body().toString())
            }

            override fun onFailure(call: Call<RazorQrResponse>, t: Throwable) {
                Log.d("RazorQr",t.message.toString())
            }
        })
    }

    fun logIn(number:String,pass:String){
        val response=repository.logIn(number,pass)
        response.enqueue(object :Callback<LogInResponse>{
            override fun onResponse(call: Call<LogInResponse>, response: Response<LogInResponse>) {
                Log.d("tagged",response.body().toString())
                loginData.postValue(response.body())
            }
            override fun onFailure(call: Call<LogInResponse>, t: Throwable) {
                errorMessage.postValue(t.message)
            }
        })
    }

    fun arrivingVehicle(vehicle_no: String){
        val response=repository.arrivingVehicle(vehicle_no)
        response.enqueue(object :Callback<ArrivingVehicleResponse>{
            override fun onResponse(
                call: Call<ArrivingVehicleResponse>,
                response: Response<ArrivingVehicleResponse>
            ) {
                arrivingVehicleData.postValue(response.body())
            }

            override fun onFailure(call: Call<ArrivingVehicleResponse>, t: Throwable) {
                Log.d("err",t.message.toString())
            }
        })
    }

    fun submit(){
        val response=repository.submit()
        response.enqueue(object :Callback<DashResponse>{
            override fun onResponse(call: Call<DashResponse>, response: Response<DashResponse>) {
                Log.d("dashboard",response.body().toString())
                dashboardData.postValue(response.body())
            }

            override fun onFailure(call: Call<DashResponse>, t: Throwable) {
                Log.d("dashboard",t.message.toString())
                errorMessage.postValue(t.message)
            }

        })
    }
    fun forgotPassword(forgotPasswordReq: ForgotPasswordReq){
        val response=repository.forgotPassword(forgotPasswordReq)
        response.enqueue(object :Callback<ForgotPasswordResponse>{
            override fun onResponse(
                call: Call<ForgotPasswordResponse>,
                response: Response<ForgotPasswordResponse>
            ) {
                Log.d("FPass",response.body()!!.msg.toString())
                forgotPassData.postValue(response.body()!!.msg)
                Log.d("fPass",response.body()!!.msg.toString())
            }

            override fun onFailure(call: Call<ForgotPasswordResponse>, t: Throwable) {
                Log.d("FPass",t.message.toString())
            }
        })
    }

    fun price(){
        val response=repository.price()
        response.enqueue(object : Callback<PriceResponse>{
            override fun onResponse(
                call: Call<PriceResponse>,
                response: Response<PriceResponse>
            ) {
                priceData.postValue(response.body())
                Log.d("price",response.body().toString())
            }

            override fun onFailure(call: Call<PriceResponse>, t: Throwable) {
                Log.d("price",t.message.toString())
            }
        })
    }
    fun save(saveParameters: SaveParameters){
        val response=repository.save(saveParameters)
        response.enqueue(object :Callback<SaveResponse>{
            override fun onResponse(call: Call<SaveResponse>, response: Response<SaveResponse>) {
                Log.d("save",response.body().toString())
                saveData.postValue(response.body())
            }

            override fun onFailure(call: Call<SaveResponse>, t: Throwable) {
                Log.d("save",t.message.toString())
                errorMessage.postValue(t.message)
            }

        })
    }

    fun slot(slotParameters: String){
        val response=repository.slot(slotParameters)
        response.enqueue(object :Callback<SlotResponse>{
            override fun onResponse(call: Call<SlotResponse>, response: Response<SlotResponse>) {
                Log.d("slot",response.body().toString())
                slotData.postValue(response.body())
            }

            override fun onFailure(call: Call<SlotResponse>, t: Throwable) {
                Log.d("slot",t.message.toString())
                errorMessage.postValue(t.message)
            }

        })
    }

    fun missing(vehicle_no: String){
        val response=repository.missing(vehicle_no)
        response.enqueue(object :Callback<MissingResponse>{
            override fun onResponse(call: Call<MissingResponse>, response: Response<MissingResponse>) {
                Log.d("tagged",response.body().toString())
                missingData.postValue(response.body())
            }
            override fun onFailure(call: Call<MissingResponse>, t: Throwable) {
                errorMessage.postValue(t.message)
            }
        })
    }

    fun checkout(vehicle_no: String){
        val response=repository.checkout(vehicle_no)
        response.enqueue(object :Callback<CheckoutResponse>{
            override fun onResponse(call: Call<CheckoutResponse>, response: Response<CheckoutResponse>) {
                Log.d("tagged",response.body().toString())
                checkoutData.postValue(response.body())
            }
            override fun onFailure(call: Call<CheckoutResponse>, t: Throwable) {
                errorMessage.postValue(t.message)
            }
        })
    }
}

class MainViewModelFactory(private val repository: Repo):
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            MainViewModel(this.repository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
