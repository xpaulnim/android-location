package sample.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.*
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.activity_location.*
import sample.R
import sample.services.CollectLocationService
import sample.viewmodels.LocationViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class LocationActivity : OnMapReadyCallback, AppCompatActivity() {
    override fun onMapReady(mapboxMap: MapboxMap) {
        mapReady = true

        updateMap()
    }

    companion object {
        val TAG: String = LocationActivity::class.java.simpleName
        private const val MY_PERMISSION_REQUEST_LOCATION = 1
        private const val MY_PERMISSION_REQUEST_FINE_LOCATION = 2
        private const val REQUESTING_LOCATION_UPDATES_KEY = "requestingLocUpdates"
    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var mapReady = false

    private var myLocation: Location? = null

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var locationViewModel: LocationViewModel

    private var routeLocations: MutableList<sample.model.Location> = mutableListOf()

    private var lineManager: LineManager? = null

    private var requestingLocationUpdates = false


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "Creating location activity: $requestingLocationUpdates")
        requestingLocationUpdates = savedInstanceState?.getBoolean(REQUESTING_LOCATION_UPDATES_KEY) ?: false

        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.access_token))
        setContentView(R.layout.activity_location)

        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS, Style.OnStyleLoaded { style ->
                lineManager = LineManager(mapView, mapboxMap, style)
            })
        }

        tbStartRecordingLocation.isChecked = requestingLocationUpdates
        tbStartRecordingLocation.setOnClickListener {
            if (tbStartRecordingLocation.isChecked) {
                startLocationUpdates()
            }
        }

        btnGetCurrentLocation.setOnClickListener {
            updateMap()
            textView.text = routeLocations.size.toString()
        }

        locationRequest = LocationRequest()
        locationRequest.interval = 120000L
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationViewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                Log.i(TAG, "Got this many locations: ${locationResult.locations.size}")

                for (location in locationResult.locations) {
                    val currentLocation = "${location.latitude},${location.longitude}"

                    val updatedAt = Calendar.getInstance().getTime()
                    textView3.text = String.format("%s - %d:%d:%d", currentLocation, updatedAt.hours, updatedAt.minutes, updatedAt.seconds)
                    Log.i(TAG, currentLocation)

                    locationViewModel.locations.observe(this@LocationActivity, Observer { locations ->
                        locations?.let {
                            routeLocations.addAll(it)
                        }
                    })

                    locationViewModel.insertLocation(
                        sample.model.Location(System.currentTimeMillis(), location.latitude, location.longitude)
                    )
                }
            }
        }
    }

    private fun updateMap() {
        lineManager ?: return

        lineManager?.let { lineManager ->
            lineManager.deleteAll()

            val sortedLocations = routeLocations
                .sortedBy { location -> location.timestamp }
                .map { location -> Point.fromLngLat(location.lng, location.lat) }


            Log.i(TAG, "${sortedLocations.size}")

            lineManager.create(
                LineOptions()
                    .withLineColor("#b30000")
                    .withLineWidth(5.0f)
                    .withGeometry(LineString.fromLngLats(sortedLocations))
            )
        }

        if (routeLocations.size > 0) {
            mapView?.getMapAsync { mapboxMap ->
                val zoomPoint = routeLocations[0]

                mapboxMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(zoomPoint.lat, zoomPoint.lng), 16.0
                    )
                )
            }
        }
    }

    private fun getCurrentLocation() {
        if (checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    myLocation = location

                    val locationString = "${location.latitude},${location.longitude}"

                    Log.i(TAG, locationString)
                    Log.i(TAG, etLocationMessage.text.toString())
                }
            }
        } else {
            Log.i(TAG, "Location not yet granted. Requesting location")

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            ) {
                Log.i(TAG, "Requesting current location")
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    MY_PERMISSION_REQUEST_LOCATION
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            MY_PERMISSION_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                    Log.i(TAG, "Location request granted.")
                    getCurrentLocation()
                }
            }

            MY_PERMISSION_REQUEST_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                    Log.i(TAG, "Permission granted for location updates")
                }
            }
        }
    }

    public override fun onResume() {
        Log.i(TAG, "Resuming application")
        super.onResume()
        mapView?.onResume()

        tbStartRecordingLocation.isChecked = requestingLocationUpdates

        if (requestingLocationUpdates) {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            val task = fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
            Log.i(TAG, "${task.isSuccessful}")

        } else {
            Log.i(TAG, "Location not yet granted. Requesting location")

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.i(TAG, "Requesting current location")
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSION_REQUEST_FINE_LOCATION
                )
            }
        }
    }

    private fun stopLocationUpdates() {
        Log.i(TAG, "Stopping location updates")
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    public override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()

        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.i(TAG, "Saving app state")
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, requestingLocationUpdates)
        mapView?.onSaveInstanceState(outState)

        super.onSaveInstanceState(outState)
    }

    override fun finish() {
        super.finish()

        Log.i(TAG, "finishing")
    }

}