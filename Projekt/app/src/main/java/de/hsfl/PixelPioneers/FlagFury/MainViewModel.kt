package de.hsfl.PixelPioneers.FlagFury

import android.app.Application
import android.location.Location
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
    private val currentPosition: MutableLiveData<Location> = MutableLiveData()
    private val markerPosition: MutableLiveData<Pair<Double, Double>> = MutableLiveData()
    private val conquestPoints: MutableList<Pair<Double, Double>> = mutableListOf()




    fun getName() : String? = name.value
    fun getToken() : String? = token.value
    fun getGameId() : String? = gameId.value
    fun getTeam() : Int? = team.value
    fun getPlayers() : JSONObject? = players.value
    fun getMarkerPosition(): Pair<Double, Double>? = markerPosition.value
    fun getCurrentPosition() : Location? = currentPosition.value

    fun getConquestPoints(): MutableList<Pair<Double, Double>> = conquestPoints


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
    fun setMarkerPosition(markerPosition : Pair<Double, Double>){
        this.markerPosition.value = markerPosition
    }
    fun setCurrentPosition(currentPosition : Location){
        this.currentPosition.value = currentPosition
    }
    fun setConquestPoint(latitude: Double, longitude: Double) {
        val point = Pair(latitude, longitude)
        conquestPoints.add(point)
    }



    fun registerGame(name: String, points: List<Pair<Double, Double>>, callback: (gameId: String, token: String) -> Unit, errorCallback: (error: String?) -> Unit) {
        apiRepository.registerGame(name, points, { gameId, token ->
            setGameId(gameId)
            setToken(token)
            callback(gameId, token)
        }, errorCallback)
    }

    fun joinGame(gameId: String, name: String, callback: (team: Int, token : String) -> Unit, errorCallback: (error: String?) -> Unit) {

            apiRepository.joinGame(gameId, name, { team, token ->
                callback(team, token)
            }, errorCallback)
    }


    fun getPlayers(gameId: String?, name: String?, token: String?, callback: (players: JSONObject?) -> Unit, errorCallback: (error: String?) -> Unit) {
        apiRepository.getPlayers(gameId, name, token, { players ->
            callback(players)
        }, errorCallback)
    }


    fun getPoints(gameId: String, name: String, token: String, callback: (points: List<Point>?) -> Unit, errorCallback: (error: String?) -> Unit) {
        apiRepository.getPoints(gameId, name, token, { points ->
            callback(points)
        }, errorCallback)
    }

    fun addPointToList(markerPosition: Pair<Double, Double>) {
        conquestPoints.add(markerPosition)
    }


}
