package com.dharmapal.parking_manager_kt.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dharmapal.parking_manager_kt.Repo
import com.dharmapal.parking_manager_kt.models.logInResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewmodel constructor(private val repository: Repo)  : ViewModel() {

    val errorMessage = MutableLiveData<String>()
    val logindata=MutableLiveData<logInResponse>()

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