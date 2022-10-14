package com.dharmapal.parking_manager_kt.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dharmapal.parking_manager_kt.Repo
import com.dharmapal.parking_manager_kt.models.*
import com.dharmapal.parking_manager_kt.models.Dash_Response
import com.dharmapal.parking_manager_kt.models.ForgotPassword_Req
import com.dharmapal.parking_manager_kt.models.ForgotPassword_Response
import com.dharmapal.parking_manager_kt.models.logInResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewmodel constructor(private val repository: Repo)  : ViewModel() {

    val errorMessage = MutableLiveData<String>()
    val loginData=MutableLiveData<logInResponse>()
    val dashboardData=MutableLiveData<Dash_Response>()
    val saveData=MutableLiveData<SaveResponse>()
    val slotData=MutableLiveData<SlotResponse>()
    val priceData=MutableLiveData<Price_Response>()
    val missingData=MutableLiveData<MissingResponse>()
    val scanData=MutableLiveData<ScanResponse>()
    val checkoutData=MutableLiveData<CheckoutResponse>()

    fun logIn(number:String,pass:String){
        val response=repository.logIn(number,pass)
        response.enqueue(object :Callback<logInResponse>{
            override fun onResponse(call: Call<logInResponse>, response: Response<logInResponse>) {
                Log.d("tagged",response.body().toString())
                loginData.postValue(response.body())
            }
            override fun onFailure(call: Call<logInResponse>, t: Throwable) {
                errorMessage.postValue(t.message)
            }
        })
    }

    fun submit(){
        val response=repository.submit()
        response.enqueue(object :Callback<Dash_Response>{
            override fun onResponse(call: Call<Dash_Response>, response: Response<Dash_Response>) {
                Log.d("dashboard",response.body().toString())
                dashboardData.postValue(response.body())
            }

            override fun onFailure(call: Call<Dash_Response>, t: Throwable) {
                Log.d("dashboard",t.message.toString())
                errorMessage.postValue(t.message)
            }

        })
    }
    fun forgotPassword(forgotpasswordReq: ForgotPassword_Req){
        val response=repository.forgot_Password(forgotpasswordReq)
        response.enqueue(object :Callback<ForgotPassword_Response>{
            override fun onResponse(
                call: Call<ForgotPassword_Response>,
                response: Response<ForgotPassword_Response>
            ) {
                Log.d("fpass",response.body()!!.msg.toString())
            }

            override fun onFailure(call: Call<ForgotPassword_Response>, t: Throwable) {
                Log.d("fpass",t.message.toString())
            }
        })
    }

    fun price(){
        val response=repository.price()
        response.enqueue(object : Callback<Price_Response>{
            override fun onResponse(
                call: Call<Price_Response>,
                response: Response<Price_Response>
            ) {
                priceData.postValue(response.body())
                Log.d("priceee",response.body().toString())
            }

            override fun onFailure(call: Call<Price_Response>, t: Throwable) {
                Log.d("priceee",t.message.toString())
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

    fun scan(pass_no: String){
        val response=repository.scan(pass_no)
        response.enqueue(object :Callback<ScanResponse>{
            override fun onResponse(call: Call<ScanResponse>, response: Response<ScanResponse>) {
                Log.d("tagged",response.body().toString())
                scanData.postValue(response.body())
            }
            override fun onFailure(call: Call<ScanResponse>, t: Throwable) {
                errorMessage.postValue(t.message)
            }
        })
    }

    fun checkout(pass_no: String){
        val response=repository.checkout(pass_no)
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

class MainViewmodelFactory(private val repository: Repo
):
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MainViewmodel::class.java)) {
            MainViewmodel(this.repository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
