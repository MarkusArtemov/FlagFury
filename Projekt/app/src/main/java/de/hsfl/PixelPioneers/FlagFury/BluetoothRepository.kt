package de.hsfl.PixelPioneers.FlagFury


import android.util.Log
import com.lokibt.bluetooth.BluetoothAdapter
import com.lokibt.bluetooth.BluetoothDevice
import com.lokibt.bluetooth.BluetoothServerSocket
import com.lokibt.bluetooth.BluetoothSocket
import java.io.IOException
import java.util.*

class BluetoothRepository(private val serverUUID: UUID) {

    companion object {
        private const val TAG = "BluetoothRepository"
        private const val SERVER_NAME = "FlagFuryBluetoothServer"
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var serverSocket: BluetoothServerSocket? = null
    private var conquerThread: ConquerThread? = null

    fun startConquerService() {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth is not supported on this device")
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            Log.e(TAG, "Bluetooth is not enabled")
            return
        }

        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
        if (pairedDevices.isEmpty()) {
            Log.e(TAG, "No paired devices found")
            return
        }

        val targetDevice: BluetoothDevice? = pairedDevices.firstOrNull()
        if (targetDevice == null) {
            Log.e(TAG, "No target device found")
            return
        }

        conquerThread = ConquerThread(targetDevice)
        conquerThread?.start()
    }

    fun stopConquerService() {
        conquerThread?.cancel()
        conquerThread = null
    }

    private inner class ConquerThread(private val device: BluetoothDevice) : Thread() {
        private var socket: BluetoothSocket? = null

        override fun run() {
            try {
                socket = device.createRfcommSocketToServiceRecord(serverUUID)
                socket?.connect()
            } catch (e: IOException) {
                Log.e(TAG, "Error connecting to device: ${e.message}")
                return
            }

            socket?.close()
        }

        fun cancel() {
            socket?.close()
        }
    }
}
