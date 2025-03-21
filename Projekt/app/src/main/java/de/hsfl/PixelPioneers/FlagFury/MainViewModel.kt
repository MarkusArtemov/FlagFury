package de.hsfl.PixelPioneers.FlagFury

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.lokibt.bluetooth.BluetoothDevice
import org.json.JSONArray
import org.json.JSONObject

class MainViewModel(app: Application) : AndroidViewModel(app) {


    private val apiRepository = ApiRepository.getInstance(app)

    private val bluetoothRepository = BluetoothRepository.getInstance().apply {
        discoveryCallback = { device ->
            val updatedMap = _discoveredDevices.value ?: hashMapOf()
            updatedMap[device.address] = device
            _discoveredDevices.postValue(updatedMap)
        }
    }


    private val _state = MutableLiveData<String>()
    val state: LiveData<String>
        get() = _state

    private val _isDefended = MutableLiveData(false)
    val isDefended: LiveData<Boolean>
        get() = _isDefended


    private val _isHost = MutableLiveData<Boolean>()
    val isHost: LiveData<Boolean>
        get() = _isHost

    private val _discoveredDevices = MutableLiveData<HashMap<String, BluetoothDevice>>()
    val discoveredDevices: LiveData<HashMap<String, BluetoothDevice>>
        get() = _discoveredDevices

    private val _oldConquerPointTeam = MutableLiveData<String>()
    val oldConquerPointTeam: LiveData<String>
        get() = _oldConquerPointTeam

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

    private val _players = MutableLiveData<List<JSONObject>>()
    val players: LiveData<List<JSONObject>>
        get() = _players

    private val _currentPosition = MutableLiveData<Pair<Double, Double>>()
    val currentPosition: LiveData<Pair<Double, Double>>
        get() = _currentPosition

    private val _markerPosition = MutableLiveData<Pair<Double, Double>>()
    val markerPosition: LiveData<Pair<Double, Double>>
        get() = _markerPosition

    private val _lastMessage = MutableLiveData<String>()

    private val _lastErrorMessage = MutableLiveData<Error>()

    private var currentToast: Toast? = null

    private val messageObserver = Observer<String> { message ->
        currentToast?.cancel()
        Log.d("MainviewModel", message)
        currentToast?.show()
    }

    private val errorObserver = Observer<Error> { error ->
        val message = if (error.message!!.contains("server is already in use")) {
            "Eroberungspunkt wird verteidigt"
        } else {
            "Es ist ein Fehler bei der Verbindung zum Server aufgetreten"
        }

        Log.d("MainViewModel", message)
    }

    init {
        _isHost.value = false
        _lastMessage.observeForever(messageObserver)
        _lastErrorMessage.observeForever(errorObserver)
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

    fun setTeam(team: Int) {
        _team.value = team
    }

    fun setIsHost(isHost: Boolean) {
        _isHost.value = isHost
    }

    fun startDiscoverDevices() {
        bluetoothRepository.startDiscovery(getApplication())
        Log.d("MainViewModel", "Discovering devices")
    }

    fun stopDiscoverDevices() {
        bluetoothRepository.cancelDiscovery(getApplication())
    }

    fun connectToServer(serverDevice: BluetoothDevice, team: String) {
        bluetoothRepository.connectToServer(serverDevice, team, { defended ->
            val message =
                if (defended) "Eroberungspunkt wird verteidigt" else "Eroberungspunkt ist angreifbar"
            _isDefended.postValue(defended)
            _lastMessage.postValue(message)
        }, { error ->
            _lastErrorMessage.postValue(Error(error))
        })
    }

    fun setOldConquerPointTeamValue(team: String) {
        _oldConquerPointTeam.value = team
    }

    fun startServer() {
        val team = if (_team.value == 1) "rot" else "blau"
        bluetoothRepository.startServer(
            team
        ) { error ->
            _lastErrorMessage.postValue(Error(error.message))
        }
    }

    fun stopServer() {
        val team = if (_team.value == 1) "rot" else "blau"
        bluetoothRepository.stopServer(team)
    }

    fun setMarkerPosition(markerPosition: Pair<Double, Double>) {
        _markerPosition.value = markerPosition
    }

    fun setCurrentPosition(currentPosition: Pair<Double, Double>) {
        _currentPosition.value = currentPosition
    }

    private fun showErrorToast(error: String) {
        Toast.makeText(getApplication(), error, Toast.LENGTH_SHORT).show()
    }

    private fun jsonArrayToList(jsonArray: JSONArray): List<JSONObject> {
        val list = mutableListOf<JSONObject>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            list.add(jsonObject)
        }
        return list
    }

    fun cancelAllRequests() {
        apiRepository.cancelAllRequests()
    }

    fun registerGame(
        name: String,
        points: List<Pair<Double, Double>>,
        callback: (gameId: String, token: String) -> Unit,
        errorCallback: (error: String?) -> Unit
    ) {
        apiRepository.registerGame(
            name, points, { gameId, token ->
                setGameId(gameId)
                setToken(token)
                callback(gameId, token)
            }, errorCallback
        )
    }

    fun joinGame(
        gameId: String,
        name: String,
        team: Int,
        callback: (team: Int, token: String) -> Unit,
        errorCallback: (error: String?) -> Unit
    ) {
        apiRepository.joinGame(
            gameId, name, team, { team1, token ->
                callback(team, token)
            }, errorCallback
        )
    }

    fun getPlayers(errorCallback: (error: String?) -> Unit) {
        apiRepository.getPlayers(gameId.value, name.value, token.value, { players ->
            players?.let {
                val playerJSONArray = it.getJSONArray("players")
                _state.value = it.getString("state")
                val playerName = name.value
                val playerTeam: Int?
                val playersList = jsonArrayToList(playerJSONArray)

                for (i in 0 until playerJSONArray.length()) {
                    val player = playerJSONArray.getJSONObject(i)
                    val currentPlayerName = player.getString("name")
                    val currentPlayerTeam = player.getInt("team")

                    if (player.has("token")) {
                        val currentPlayerToken = player.getString("token")
                        Log.d("MainViewModel", "Player token: $currentPlayerToken")
                    }

                    if (currentPlayerName == playerName) {
                        playerTeam = currentPlayerTeam
                        setTeam(playerTeam)
                        break
                    }
                }
                _players.value = playersList
            }
        }, errorCallback)
    }


    fun getPoints(callback: (points: List<Point>?, state: String?) -> Unit) {
        apiRepository.getPoints(gameId.value, name.value, token.value, { points, state, game ->
            callback(points, state)
        }, { error ->
            error?.let { showErrorToast(it) }
        })
    }

    fun startGame() {
        apiRepository.startGame(
            gameId.value,
            name.value,
            token.value
        ) { error -> error?.let { showErrorToast(it) } }
    }

    fun endGame() {
        apiRepository.endGame(
            gameId.value,
            name.value,
            token.value
        ) { error ->
            error?.let { showErrorToast(it) }
        }
    }

    fun conquerPoint(
        game: String?,
        point: String?,
        team: String?,
        name: String?,
        token: String?,
        callback: (obj: JSONObject?) -> Unit,
        errorCallback: (error: String?) -> Unit
    ) {
        apiRepository.conquerPoint(
            gameId.value, point, team, name, token, { response ->
                callback(response)
            }, errorCallback
        )
    }

    fun removePlayer(
        game: String?,
        name: String?,
        token: String?,
        callback: (game: String, name: String?) -> Unit,
        errorCallback: (error: String?) -> Unit
    ) {
        apiRepository.removePlayer(
            game, name, token, { gameId, playerName ->
                gameId?.let { callback(it, playerName) }
            }, errorCallback
        )
    }

}
