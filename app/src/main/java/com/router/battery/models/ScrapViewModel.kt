package com.router.battery.models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.router.battery.data.RouterParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScrapViewModel : ViewModel(){


    private var values: Map<String, String> = emptyMap()
    private val valuesLiveData = MutableLiveData<Map<String,String>>()
    val valuesLiveDataValue: LiveData<Map<String,String>>
    get() = valuesLiveData

    fun retrieveValues(forceRefresh: Boolean = false ){
        Log.d("ViewModel", "Retrieving")
        if(values.isEmpty() || forceRefresh) {
            launchDataLoad()
        }
        valuesLiveData.postValue(values)
    }
    private fun launchDataLoad() {
        Log.d("ViewModel", "Loading data")
        viewModelScope.launch {
            loadValues()
        }

    }

    private suspend fun loadValues() = withContext(Dispatchers.IO){
        values = RouterParameters().getValues()

    }
}