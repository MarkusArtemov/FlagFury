package de.hsfl.PixelPioneers.FlagFury

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class MainViewModel(app : Application) : AndroidViewModel(app){
    private val apiRepository = ApiRepository.getInstance(app)

    private val name : MutableLiveData<String> = MutableLiveData()
    private val token : MutableLiveData<String> = MutableLiveData()
    private val gameId : MutableLiveData<Int> = MutableLiveData()
    fun getName() : MutableLiveData<String> = name
    fun getToken() : MutableLiveData<String> = token
    fun getGameId() : MutableLiveData<Int> = gameId
    fun setName(name: String){
        this.name.value = name
    }
    fun setToken(token : String){
        this.token.value = token
    }
    fun setGameId(gameId : Int){
        this.gameId.value = gameId
    }

    fun registerGame(name: String, callback: (gameId: Int, token: String) -> Unit) {
        apiRepository.registerGame(name) { gameId, token ->
            callback(gameId, token)
        }
    }

    fun joinGame(gameId: Int, name: String, callback: (gameId: Int, name: String, team: Int, token : String) -> Unit) {
        apiRepository.joinGame(gameId, name) { gameId, name, team, token ->
            callback(gameId, token,team,token)
        }
    }


}
