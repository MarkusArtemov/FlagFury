package de.hsfl.PixelPioneers.FlagFury

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.lokibt.bluetooth.BluetoothAdapter
import com.lokibt.bluetooth.BluetoothDevice
import com.lokibt.bluetooth.BluetoothServerSocket
import com.lokibt.bluetooth.BluetoothSocket
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
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
    private val serverUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val teamUUIDs = mapOf(
        "rot" to UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"),
        "blau" to UUID.fromString("00001101-0000-1000-8000-00805F9B34FC")
    )

    private var serverSocket: BluetoothServerSocket? = null


    fun startDiscovery(context: Context) {
        context.registerReceiver(discoveryReceiver, discoveryFilter)
        _bluetoothAdapter.startDiscovery()
    }

    fun cancelDiscovery(context: Context) {
        _bluetoothAdapter.cancelDiscovery()
        context.unregisterReceiver(discoveryReceiver)
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

                socket = serverDevice.createRfcommSocketToServiceRecord(serverUuid)
                socket.connect()

                responseCallback(true)
            } catch (e: IOException) {
                val errorMessage = e.message ?: "Unknown error occurred"
                if (errorMessage.contains("address of service does not exist")) {
                    errorCallback(errorMessage)
                    responseCallback(false)
                } else {
                    errorCallback(errorMessage)
                }
            } finally {
                socket?.close()
            }
        }
        clientThread.start()
    }




    fun startServer(
        callback: (Int, String) -> Boolean,
        errorCallback: (Error) -> Unit
    ) {
        val serverThread = Thread {
            try {
                isServerActive = true
                serverSocket = _bluetoothAdapter.listenUsingRfcommWithServiceRecord("FlagFuryServer", serverUUID)
                while (isServerActive) {
                    val socket = serverSocket?.accept()
                    socket?.close()
                }
            } catch (e: IOException) {
                errorCallback(Error(e.message))
                isServerActive = false
            } finally {
                serverSocket?.close()
            }
        }
        serverThread.start()
    }

    fun stopServer() {
        isServerActive = false
        serverSocket?.close()
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
