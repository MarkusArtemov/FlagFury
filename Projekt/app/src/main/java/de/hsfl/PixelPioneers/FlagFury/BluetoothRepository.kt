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

        fun generateUUIDFromTeamName(teamName: String): UUID {
            val nameUUID = UUID.nameUUIDFromBytes(teamName.toByteArray())
            return UUID(0x00001101_0000_1000_8000L, nameUUID.leastSignificantBits)
        }
    }

    private var isServerActive = false
    private val _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

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
                val serverUuid = generateUUIDFromTeamName(team)
                socket = serverDevice.createRfcommSocketToServiceRecord(serverUuid)
                socket.connect()
                responseCallback(true)
            } catch (e: IOException) {
                val errorMessage = e.message ?: "Unknown error occurred"
                if (errorMessage.contains("service is currently in use")) {
                    responseCallback(true)
                } else {
                    errorCallback(errorMessage)
                    responseCallback(false)
                }
            } finally {
                socket?.let {
                    try {
                        it.close()
                    } catch (e: IOException) {
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
                Log.d("BluetoothRepo","Server erfolgreich gestartet mit $team")
                isServerActive = true
                val serverUuid = generateUUIDFromTeamName(team)
                val serverSocket = _bluetoothAdapter.listenUsingRfcommWithServiceRecord("FlagFuryServer_$team", serverUuid)
                serverSockets[team] = serverSocket

                while (isServerActive) {
                    val socket = serverSocket.accept()
                    socket.close()
                }
            } catch (e: IOException) {
                Log.d("BluetoothRepo","Server nicht erfolgreich gestartet")
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
