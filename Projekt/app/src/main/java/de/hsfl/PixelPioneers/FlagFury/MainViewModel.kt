package de.hsfl.PixelPioneers.FlagFury

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import org.json.JSONObject

class MainViewModel(app : Application) : AndroidViewModel(app){
    private val apiRepository = ApiRepository.getInstance(app)

    private val name : MutableLiveData<String> = MutableLiveData()
    private val token : MutableLiveData<String> = MutableLiveData()
    private val gameId : MutableLiveData<String> = MutableLiveData()
    private val team : MutableLiveData<Int> = MutableLiveData()
    private val players : MutableLiveData<JSONObject> = MutableLiveData()


    fun getName() : String? = name.value
    fun getToken() : String? = token.value
    fun getGameId() : String? = gameId.value
    fun getTeam() : Int? = team.value
    fun getPlayers() : JSONObject? = players.value


    fun setName(name: String){
        this.name.value = name
    }
    fun setToken(token : String){
        this.token.value = token
    }
    fun setGameId(gameId : String){
        this.gameId.value = gameId
    }
    fun setPlayers(players : JSONObject){
        this.players.value = players
    }
    fun setTeam(team : Int){
        this.team.value = team
    }

    fun registerGame(name: String, callback: (game :String, token : String) -> Unit) {
        apiRepository.registerGame(name) { gameId, token ->
            callback(gameId, token)
        }
    }

    fun joinGame(gameId: String, name: String, callback: (team: Int, token : String) -> Unit) {
        apiRepository.joinGame(gameId, name) { team, token -> callback(team, token) }
    }


    fun getPlayers(gameId: String?, name: String?, token: String?, callback: (players: JSONObject?) -> Unit) {
        apiRepository.getPlayers(gameId, name, token) { players -> callback(players) }
    }



}
