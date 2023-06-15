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
import java.io.IOException
import java.util.*

class BluetoothRepository {

    companion object {
        private var instance : BluetoothRepository? = null

        fun getInstance() : BluetoothRepository{
            instance = instance ?: BluetoothRepository()
            return instance!!
        }
    }


    private val _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()


    fun startDiscovery(application: Application){
        application.registerReceiver(discoveryReceiver,discoveryFilter)
        _bluetoothAdapter.startDiscovery()
    }


    fun cancelDiscovery(application: Application){
        _bluetoothAdapter.cancelDiscovery()
        application.unregisterReceiver(discoveryReceiver)
    }


    val discoveryReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            if(p1!!.action == BluetoothDevice.ACTION_FOUND){
                val device = p1.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                device?.let { discoveryCallback(it) }
            }
        }

    }

    var discoveryCallback : (BluetoothDevice) -> Unit =  {

    }


    val discoveryFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)






}

