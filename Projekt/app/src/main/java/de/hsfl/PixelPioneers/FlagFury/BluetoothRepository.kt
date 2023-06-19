package de.hsfl.PixelPioneers.FlagFury

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.lokibt.bluetooth.BluetoothAdapter
import com.lokibt.bluetooth.BluetoothDevice
import com.lokibt.bluetooth.BluetoothServerSocket
import com.lokibt.bluetooth.BluetoothSocket
import java.io.IOException
import java.util.*

class BluetoothRepository {

    companion object {
        @Volatile
        private var instance: BluetoothRepository? = null

        fun getInstance(): BluetoothRepository {
            return instance ?: synchronized(this) {
                instance ?: BluetoothRepository().also { instance = it }
            }
        }
    }

    private var isServerActive = false
    private val _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val redUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FC")
    private val blueUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FD")

    private val teamUUIDs = mapOf(
        "rot" to redUUID,
        "blau" to blueUUID
    )

    private val serverSockets = mutableMapOf<String, BluetoothServerSocket?>()

    fun startDiscovery(app: Application) {
        app.registerReceiver(discoveryReceiver, discoveryFilter)
        _bluetoothAdapter.startDiscovery()
    }

    fun cancelDiscovery(app: Application) {
        app.unregisterReceiver(discoveryReceiver)
        _bluetoothAdapter.cancelDiscovery()
    }

    fun connectToServer(
        serverDevice: BluetoothDevice,
        team: String,
        responseCallback: (Boolean) -> Unit,
        errorCallback: (String) -> Unit
    ) {
        val clientThread = Thread {
            var socket: BluetoothSocket? = null
            try {
                val serverUuid = teamUUIDs[team]
                if (serverUuid != null) {
                    socket = serverDevice.createRfcommSocketToServiceRecord(serverUuid)
                    socket.connect()
                    responseCallback(true)
                } else {
                    val errorMessage = "Invalid team UUID for team $team"
                    errorCallback(errorMessage)
                    responseCallback(false)
                }
            } catch (e: IOException) {
                val errorMessage = e.message ?: "Unknown error occurred"
                if (errorMessage.contains("address of service does not exist")) {
                    errorCallback(errorMessage)
                    responseCallback(false)
                } else {
                    errorCallback(errorMessage)
                }
            } finally {
                socket?.let {
                    try {
                        it.close()
                    } catch (e: IOException) {
                        // Handle exception if necessary
                    }
                }
            }
        }
        clientThread.start()
    }

    fun startServer(
        team: String,
        callback: (Int, String) -> Boolean,
        errorCallback: (Error) -> Unit
    ) {
        val serverThread = Thread {
            try {
                isServerActive = true
                val serverUuid = teamUUIDs[team]
                if (serverUuid != null) {
                    val serverSocket = _bluetoothAdapter.listenUsingRfcommWithServiceRecord("FlagFuryServer_$team", serverUuid)
                    serverSockets[team] = serverSocket

                    while (isServerActive) {
                        val socket = serverSocket.accept()
                        socket.close()
                    }
                } else {
                    errorCallback(Error("Invalid team UUID for team $team"))
                }
            } catch (e: IOException) {
                errorCallback(Error(e.message))
                isServerActive = false
            } finally {
                serverSockets[team]?.close()
                serverSockets.remove(team)
            }
        }
        serverThread.start()
    }

    fun stopServer(team: String) {
        isServerActive = false
        serverSockets[team]?.close()
        serverSockets.remove(team)
    }

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                device?.let { discoveryCallback(it) }
            }
        }
    }

    var discoveryCallback: (BluetoothDevice) -> Unit = {}
    val discoveryFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
}
