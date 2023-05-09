package de.hsfl.PixelPioneers.FlagFury

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel(){
    private val input : MutableLiveData<String> = MutableLiveData();

   fun getInput() : LiveData<String> = input;

}
