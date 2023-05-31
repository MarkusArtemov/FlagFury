package de.hsfl.PixelPioneers.FlagFury

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {
    companion object {
        private const val PERMISSIONCODE = 200
        private val PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    private lateinit var client: FusedLocationProviderClient
    private val mainViewModel: MainViewModel by viewModels()

    private val callback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            for (location in p0.locations) {
                mainViewModel.setCurrentPosition(Pair(location.longitude, location.latitude))
                Log.d("Meine Position", "${mainViewModel.getCurrentPosition()}")
            }
        }
    }

    private val request = LocationRequest.create().apply {
        interval = 1000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private var isLocationPermissionGranted = false
    private var isAskEveryTimeSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        client = LocationServices.getFusedLocationProviderClient(this)
        requestLocationUpdates()
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            isLocationPermissionGranted = true
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONCODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONCODE) {
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
        startActivityForResult(intent, PERMISSIONCODE)
    }


    override fun onResume() {
        super.onResume()
        if (isLocationPermissionGranted) {
            requestLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        client.removeLocationUpdates(callback)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSIONCODE) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                isLocationPermissionGranted = true
                if (isAskEveryTimeSelected) {
                    requestLocationUpdates()
                }
            } else {
                showPermissionDeniedToast()
                showEnableGpsDialog()
            }
        }
    }

    private fun showPermissionDeniedToast() {
        Toast.makeText(this, "GPS ist zum spielen erforderlich", Toast.LENGTH_SHORT).show()
    }
}

