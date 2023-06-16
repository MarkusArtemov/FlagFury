package de.hsfl.PixelPioneers.FlagFury

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lokibt.bluetooth.BluetoothDevice
import org.json.JSONObject

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val apiRepository = ApiRepository.getInstance(app)

    private val _oldConquerPointTeam = MutableLiveData<Int>()
    val oldConquerPointTeam: MutableLiveData<Int>
        get() = _oldConquerPointTeam


    private val _isDefended = MutableLiveData<Boolean>()
    val isDefended: LiveData<Boolean>
        get() = _isDefended


    private val _discoveryEnabled = MutableLiveData<Boolean>()
    val discoveryEnabled: LiveData<Boolean>
        get() = _discoveryEnabled

    private val _discoveredDevices = MutableLiveData<List<BluetoothDevice>>()
    val discoveredDevices: LiveData<List<BluetoothDevice>>
        get() = _discoveredDevices

    private val _name = MutableLiveData<String>()
    val name: LiveData<String>
        get() = _name

    private val _token = MutableLiveData<String>()
    val token: LiveData<String>
        get() = _token

    private val _gameId = MutableLiveData<String>()
    val gameId: LiveData<String>
        get() = _gameId

    private val _team = MutableLiveData<Int>()
    val team: LiveData<Int>
        get() = _team

    private val _players = MutableLiveData<JSONObject>()
    val players: LiveData<JSONObject>
        get() = _players

    private val _currentPosition = MutableLiveData<Pair<Double, Double>>()
    val currentPosition: LiveData<Pair<Double, Double>>
        get() = _currentPosition

    private val _markerPosition = MutableLiveData<Pair<Double, Double>>()
    val markerPosition: LiveData<Pair<Double, Double>>
        get() = _markerPosition

    private val _lastMessage = MutableLiveData<String>()
    val lastMessage: LiveData<String>
        get() = _lastMessage

    private val _lastErrorMessage = MutableLiveData<Error>()
    val lastErrorMessage: LiveData<Error>
        get() = _lastErrorMessage

    private val bluetoothRepository = BluetoothRepository.getInstance().apply {
        discoveryCallback = { device ->
            val updatedList = _discoveredDevices.value?.toMutableList() ?: mutableListOf()
            updatedList.add(device)
            _discoveredDevices.postValue(updatedList)
        }
    }



    fun connectToServer(serverDevice: BluetoothDevice, team: String) {
        bluetoothRepository.connectToServer(serverDevice, team,
            { defended ->
                val message = if (defended) "Eroberungspunkt wird verteidigt" else "Eroberungspunkt ist angreifbar"
                _isDefended.postValue(defended)
                _lastMessage.postValue(message)
                Log.d("MainViewModel", "Verbindung zum Server erfolgreich: isDefended=$defended")
            },
            { error ->
                _lastErrorMessage.postValue(Error(error.toString()))
                Log.e("MainViewModel", "Fehler bei der Verbindung zum Server: $error")
            }
        )
    }


    fun discoverDevices() {
        bluetoothRepository.startDiscovery(getApplication())
        Log.d("MainViewModel", "Discovering devices")
    }

    fun stopDiscoverDevices() {
        bluetoothRepository.cancelDiscovery(getApplication())
    }


    fun startServer() {
        val gameId = gameId.value?.toInt() ?: 0
        val name = name.value ?: ""

        bluetoothRepository.startServer({ clientGameId, clientName ->
                val isAttacking = false
                isAttacking
            },
            { error ->
                _lastErrorMessage.postValue(Error(error.message))
            })
    }

    fun stopServer() {
        bluetoothRepository.stopServer()
    }

    fun setDiscoverEnabled(state: Boolean) {
        _discoveryEnabled.value = state
    }

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

    fun setIsDefended(state : Boolean){
        _isDefended.value = state
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
