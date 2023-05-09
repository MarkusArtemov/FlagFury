package de.hsfl.PixelPioneers.FlagFury

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel(app : Application) : AndroidViewModel(app){
    private val apiRepository = ApiRepository.getInstance(app)

    private val name : MutableLiveData<String> = MutableLiveData()




}
