package de.hsfl.PixelPioneers.FlagFury

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lokibt.bluetooth.BluetoothDevice
import org.json.JSONArray
import org.json.JSONObject

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val apiRepository = ApiRepository.getInstance(app)


    private val _discoveredDevices: MutableLiveData<List<BluetoothDevice>> = MutableLiveData()

    private val _name: MutableLiveData<String> = MutableLiveData()
    private val _token: MutableLiveData<String> = MutableLiveData()
    private val _gameId: MutableLiveData<String> = MutableLiveData()
    private val _team: MutableLiveData<Int> = MutableLiveData()
    private val _players: MutableLiveData<JSONObject> = MutableLiveData()
    private val _currentPosition: MutableLiveData<Pair<Double, Double>> = MutableLiveData()
    private val _markerPosition: MutableLiveData<Pair<Double, Double>> = MutableLiveData()

    private val lastMessage: MutableLiveData<String> = MutableLiveData()
    private val lastErrorMessage: MutableLiveData<Error> = MutableLiveData()




    private val bluetoothRepository = BluetoothRepository.getInstance().apply {
        discoveryCallback = {device ->
            val updatedList = _discoveredDevices.value?.toMutableList() ?: mutableListOf()
            updatedList.add(device)
            _discoveredDevices.postValue(updatedList)
            Log.d("Mainviewmodel","${_discoveredDevices.value}")
        }
    }

    val discoveredDevices: LiveData<List<BluetoothDevice>>
        get() = _discoveredDevices

    fun discoverDevices() {
        bluetoothRepository.startDiscovery(getApplication())
    }

    fun startServer(){
        Log.d("MainViewModel", "Starting Server")
        bluetoothRepository.startServer({message->
            lastMessage.value = message
        },{error->
            lastErrorMessage.value = error
        })
    }

    fun stopServer(){
        Log.d("MainViewModel", "Stopping Server")
        bluetoothRepository.stopServer()
    }

    val name: LiveData<String>
        get() = _name

    val token: LiveData<String>
        get() = _token

    val gameId: LiveData<String>
        get() = _gameId

    val team: LiveData<Int>
        get() = _team

    val players: LiveData<JSONObject>
        get() = _players

    val currentPosition: LiveData<Pair<Double, Double>>
        get() = _currentPosition

    val markerPosition: LiveData<Pair<Double, Double>>
        get() = _markerPosition

    fun setName(name: String) {
        _name.value = name
    }

    fun setToken(token: String) {
        _token.value = token
    }

    fun setGameId(gameId: String) {
        _gameId.value = gameId
    }

    fun setPlayers(players: JSONObject) {
        _players.value = players
    }

    fun setTeam(team: Int) {
        _team.value = team
    }

    fun setMarkerPosition(markerPosition: Pair<Double, Double>) {
        _markerPosition.value = markerPosition
    }

    fun setCurrentPosition(currentPosition: Pair<Double, Double>) {
        _currentPosition.value = currentPosition
    }

    fun registerGame(
        name: String,
        points: List<Pair<Double, Double>>,
        callback: (gameId: String, token: String) -> Unit,
        errorCallback: (error: String?) -> Unit
    ) {
        apiRepository.registerGame(name, points, { gameId, token ->
            setGameId(gameId)
            setToken(token)
            callback(gameId, token)
        }, errorCallback)
    }

    fun joinGame(
        gameId: String,
        name: String,
        callback: (team: Int, token: String) -> Unit,
        errorCallback: (error: String?) -> Unit
    ) {
        apiRepository.joinGame(gameId, name, { team, token ->
            callback(team, token)
        }, errorCallback)
    }

    fun getPlayers(
        gameId: String?,
        name: String?,
        token: String?,
        callback: (players: JSONObject?) -> Unit,
        errorCallback: (error: String?) -> Unit
    ) {
        apiRepository.getPlayers(gameId, name, token, { players ->
            callback(players)
        }, errorCallback)
    }

    fun getPoints(
        gameId: String?,
        name: String?,
        token: String?,
        callback: (points: List<Point>?, state: String?, game: String?) -> Unit,
        errorCallback: (error: String?) -> Unit
    ) {
        apiRepository.getPoints(gameId, name, token, { points, state, game ->
            callback(points, state, game)
        }, errorCallback)
    }

    fun removePlayer(
        game: String?,
        name: String?,
        token: String?,
        callback: (game: String, name: String?) -> Unit,
        errorCallback: (error: String?) -> Unit
    ) {
        apiRepository.removePlayer(game, name, token, { game, name ->
            callback(game, name)
        }, errorCallback)
    }



}
