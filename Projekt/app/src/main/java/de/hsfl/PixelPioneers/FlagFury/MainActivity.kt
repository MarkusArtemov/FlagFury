package de.hsfl.PixelPioneers.FlagFury

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Debug
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {
    companion object {
        private const val PERMISSIONCODE = 200;
        private val PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    lateinit var client : FusedLocationProviderClient
    val mainViewModel: MainViewModel by viewModels()

    val callback = object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            for (location in p0.locations){
                Log.d("MainActivity", "gps position : $location")
            }
        }
    }

    private val request = LocationRequest.create().apply {
        interval = 1000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        client = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONCODE) {
            if ((grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                startLocation()
            } else {
                Toast.makeText(this, "GPS ist zum spielen erforderlich", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun startLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())

        } else {
            requestPermissions(PERMISSIONS, PERMISSIONCODE)
        }
    }

}









