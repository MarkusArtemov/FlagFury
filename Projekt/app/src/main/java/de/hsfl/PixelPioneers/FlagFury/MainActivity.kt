package de.hsfl.PixelPioneers.FlagFury

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.lokibt.bluetooth.BluetoothAdapter

class MainActivity : AppCompatActivity() {
    companion object {
        private const val LOCATION_PERMISSION_CODE = 200
        private const val BLUETOOTH_DISCOVERABLE_REQUEST_CODE = 202
        private val PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private lateinit var client: FusedLocationProviderClient
    private val mainViewModel: MainViewModel by viewModels()

    private var isLocationPermissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        client = LocationServices.getFusedLocationProviderClient(this)
        requestLocationUpdates()
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            isLocationPermissionGranted = true
            client.requestLocationUpdates(getLocationRequest(), getLocationCallback(), Looper.getMainLooper())
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, LOCATION_PERMISSION_CODE)
        }
    }

    private fun getLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun getLocationCallback(): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                for (location in p0.locations) {
                    val currentPosition = Pair(location.longitude, location.latitude)
                    mainViewModel.setCurrentPosition(currentPosition)
                    Log.d("Meine Position", "$currentPosition")
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isLocationPermissionGranted = true
                requestLocationUpdates()
            } else {
                showPermissionDeniedToast()
                showEnableGpsDialog()
            }
        }
    }

    private fun showEnableGpsDialog() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, LOCATION_PERMISSION_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            LOCATION_PERMISSION_CODE -> {
                if (ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    isLocationPermissionGranted = true
                    requestLocationUpdates()
                } else {
                    showPermissionDeniedToast()
                    showEnableGpsDialog()
                }
            }

            BLUETOOTH_DISCOVERABLE_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(
                        this,
                        "Bluetooth-Entdeckbarkeit wurde erfolgreich aktiviert",
                        Toast.LENGTH_SHORT
                    ).show()
                    mainViewModel.startServer()
                    mainViewModel.startDiscoverDevices()
                } else {
                    Toast.makeText(
                        this,
                        "Bluetooth-Entdeckbarkeit Aktivierung wurde abgelehnt",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showPermissionDeniedToast() {
        Toast.makeText(this, "GPS ist zum Spielen erforderlich", Toast.LENGTH_SHORT).show()
    }

    fun startServerAndDiscovery() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter.state != BluetoothAdapter.STATE_ON) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600)
            }
            startActivityForResult(intent, BLUETOOTH_DISCOVERABLE_REQUEST_CODE)
        } else {
            mainViewModel.startServer()
            mainViewModel.startDiscoverDevices()
        }
    }
}
