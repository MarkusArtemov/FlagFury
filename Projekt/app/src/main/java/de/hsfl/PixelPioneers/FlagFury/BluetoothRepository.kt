package de.hsfl.PixelPioneers.FlagFury


import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.lokibt.bluetooth.BluetoothAdapter
import com.lokibt.bluetooth.BluetoothDevice
import com.lokibt.bluetooth.BluetoothServerSocket
import com.lokibt.bluetooth.BluetoothSocket
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.*

class BluetoothRepository {

    companion object {
        private var instance : BluetoothRepository? = null

        fun getInstance() : BluetoothRepository{
            instance = instance ?: BluetoothRepository()
            return instance!!
        }
    }

    private var isServerActive = true
    set(value) {
        field = value
        serverCallback(field)
    }
    private val _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    val serverUUID = UUID.randomUUID()
    val serverName = "bluetooth Server"
    var serverSocket : BluetoothServerSocket? = null


    fun startDiscovery(application: Application){
        application.registerReceiver(discoveryReceiver,discoveryFilter)
        _bluetoothAdapter.startDiscovery()
    }


    fun cancelDiscovery(application: Application){
        _bluetoothAdapter.cancelDiscovery()
        application.unregisterReceiver(discoveryReceiver)
    }


    fun startServer(receiveCallback: (String) -> Unit, errorCallback: (Error) -> Unit) = Thread{
        var socket : BluetoothSocket? = null
        try {
            isServerActive = true
            serverSocket = _bluetoothAdapter.listenUsingRfcommWithServiceRecord(serverName,serverUUID)
            while(true){
             val socket = serverSocket?.accept()
                val writer = BufferedWriter(OutputStreamWriter(socket?.outputStream))
                val reader = BufferedReader(InputStreamReader(socket?.inputStream))
                val scannedLine = reader.readLine()
                receiveCallback(scannedLine)
                writer.write("Hello World ${_bluetoothAdapter.name} \n")
                writer.flush()
                socket?.close()
            }
        }catch (e : Exception){
            errorCallback(Error(e.message))
            isServerActive = false
            serverSocket?.close()
                socket?.close()
        }

    }.start()

    fun stopServer() = Thread{
        isServerActive = false
        serverSocket?.close()
    }.start()


    val discoveryReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            if(p1!!.action == BluetoothDevice.ACTION_FOUND){
                val device = p1.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                device?.let { discoveryCallback(it) }
            }
        }

    }

    var discoveryCallback : (BluetoothDevice) -> Unit =  {}
    var serverCallback : (Boolean) -> Unit =  {}


    val discoveryFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)






}

