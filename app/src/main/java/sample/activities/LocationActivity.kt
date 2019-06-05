package sample.activities

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_location.*
import sample.R

class LocationActivity : AppCompatActivity() {

    companion object {
        val TAG: String = LocationActivity::class.java.simpleName
        private val MY_PERMISSION_REQUEST_LOCATION = 10
    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        button.setOnClickListener {
            Log.i(TAG, "getLocationButtonClicked")

            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val locationString = "${location.latitude},${location.longitude}"

                    Log.i(TAG, locationString)
                    Log.i(TAG, etLocationMessage.text.toString())
                }
            }
        } else {
            Log.i(TAG, "Location not yet granted. Requesting location")

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Log.i(TAG, "Requesting current location")
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), MY_PERMISSION_REQUEST_LOCATION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            MY_PERMISSION_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                    Log.i(TAG, "Location request granted.")
                    getCurrentLocation()
                }
            }
        }
    }
}