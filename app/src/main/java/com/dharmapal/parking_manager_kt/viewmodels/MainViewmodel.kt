package com.dharmapal.parking_manager_kt.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dharmapal.parking_manager_kt.Repo
import com.dharmapal.parking_manager_kt.models.Dash_Response
import com.dharmapal.parking_manager_kt.models.ForgotPassword_Req
import com.dharmapal.parking_manager_kt.models.ForgotPassword_Response
import com.dharmapal.parking_manager_kt.models.logInResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewmodel constructor(private val repository: Repo)  : ViewModel() {

    val errorMessage = MutableLiveData<String>()
    val logindata=MutableLiveData<logInResponse>()
    val DashboardData=MutableLiveData<Dash_Response>()

    fun logIn(number:String,pass:String){
        val response=repository.logIn(number,pass)
        response.enqueue(object :Callback<logInResponse>{
            override fun onResponse(call: Call<logInResponse>, response: Response<logInResponse>) {
                Log.d("tagged",response.body().toString())
                logindata.postValue(response.body())
            }
            override fun onFailure(call: Call<logInResponse>, t: Throwable) {
                errorMessage.postValue(t.message)
            }
        })
    }

    fun submit(){
        val response=repository.submit()
        response.enqueue(object : Callback<Dash_Response>{
            override fun onResponse(call: Call<Dash_Response>, response: Response<Dash_Response>) {
                Log.d("dashboardv",response.body().toString())
                DashboardData.postValue(response.body())
            }

            override fun onFailure(call: Call<Dash_Response>, t: Throwable) {
                errorMessage.postValue(t.message.toString())
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
