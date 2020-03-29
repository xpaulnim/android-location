package sample.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.activity_dayplan.*
import org.json.JSONObject
import sample.AppNetwork
import sample.R
import sample.adapters.DayPlanAdapter
import sample.model.WikiPoi


class DayPlanActivity : OnMapReadyCallback, AppCompatActivity(), MapboxMap.OnMapClickListener {
    companion object {
        val TAG: String = DayPlanActivity::class.java.simpleName
        private const val MY_PERMISSION_REQUEST_LOCATION = 1
    }

    private var featureCollection: FeatureCollection? = null
    private var mapboxMap: MapboxMap? = null


    private val SYMBOL_ICON_ID = "SYMBOL_ICON_ID"
    private val SOURCE_ID = "SOURCE_ID"
    private val LAYER_ID = "LAYER_ID"

    private val wikiTestUrl =
        "https://en.wikipedia.org/w/api.php?action=query" +
                "&list=geosearch" +
                "&gsradius=500" +
                "&gscoord={lat}%7C{lng}" +
                "&format=json" +
                "&gslimit=50" +
                "&prop=coordinates|info"

    lateinit var queue: AppNetwork

    private val wikiPoiList = mutableListOf<WikiPoi>()

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    var lastKnownLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        queue = AppNetwork.getInstance(this.applicationContext)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        Mapbox.getInstance(this, getString(R.string.access_token))

        setContentView(R.layout.activity_dayplan)

        dayPlanMapView?.onCreate(savedInstanceState)
        dayPlanMapView?.getMapAsync(this)

    }

    private fun getWikiPointsOfInterest(location: Location?) {
        val finalWikiUrl = wikiTestUrl
            .replace("{lat}", location!!.latitude.toString())
            .replace("{lng}", location.longitude.toString())

        val jsonRequest =
            JsonObjectRequest(
                Request.Method.GET,
                finalWikiUrl,
                null,
                Response.Listener<JSONObject> {
                    Log.i(TAG, "the response was: $it")

                    val data = it.getJSONObject("query").getJSONArray("geosearch")

                    for (item in 0 until data.length() - 1) {
                        val jsonObject = data.getJSONObject(item)

                        wikiPoiList.add(
                            WikiPoi(
                                jsonObject.getInt("pageid"),
                                jsonObject.getString("title"),
                                jsonObject.getDouble("lat"),
                                jsonObject.getDouble("lon")
                            )
                        )
                    }

                    Log.i(TAG, wikiPoiList.size.toString())

                    initFeatureCollection()
                    initMarkerIcons(mapboxMap!!.style!!)
                    initRecyclerView()

                    mapboxMap!!.easeCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                .target(LatLng(wikiPoiList[0].latitude, wikiPoiList[0].longitude))
                                .zoom(12.0)
                                .build()
                        )
                    )
                },
                Response.ErrorListener {
                    Log.e(TAG, "That didn't work $it")
                })

        jsonRequest.tag = this

        queue.addToRequestQueue(jsonRequest)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        dayPlanMapView?.getMapAsync {
            it.setStyle(Style.MAPBOX_STREETS, Style.OnStyleLoaded { _ ->
                getLastKnownLocation()
            })
        }

    }

    private fun getLastKnownLocation() {
        if (checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    lastKnownLocation = location
                    val locationString = "${location.latitude},${location.longitude}"

                    Log.i(LocationActivity.TAG, "Got last known location at $locationString")

                    getWikiPointsOfInterest(lastKnownLocation)
                    mapboxMap!!.addOnMapClickListener(this)

                    Log.i(TAG, wikiPoiList.size.toString())

                    Toast.makeText(this, "Done initializing map", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Log.i(LocationActivity.TAG, "Location not yet granted. Requesting location")

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            ) {
                Log.i(LocationActivity.TAG, "Requesting current location")
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    MY_PERMISSION_REQUEST_LOCATION
                )
            }
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun initFeatureCollection() {
        featureCollection = FeatureCollection.fromFeatures(emptyArray())

        if (featureCollection != null) {
            featureCollection = FeatureCollection.fromFeatures(wikiPoiList.map {
                Feature.fromGeometry(
                    Point.fromLngLat(it.longitude, it.latitude), null, it.pageid.toString()
                )
            })
        }
    }

    private fun initMarkerIcons(style: Style) {
        style.addImage(
            SYMBOL_ICON_ID, BitmapFactory.decodeResource(
                this.resources, R.drawable.red_marker
            )
        )

        style.addSource(GeoJsonSource(SOURCE_ID, featureCollection))

        Log.i(TAG, "POIs should now be visible")

        style.addLayer(
            SymbolLayer(LAYER_ID, SOURCE_ID).withProperties(
                iconImage(SYMBOL_ICON_ID),
                iconAllowOverlap(true),
                iconOffset(arrayOf(0f, -4f))
            )
        )
    }

    private fun initRecyclerView() {
        val dayPlanAdapter = DayPlanAdapter(wikiPoiList, mapboxMap)

        rvOnTopOfMap.layoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        rvOnTopOfMap.itemAnimator = DefaultItemAnimator()
        rvOnTopOfMap.adapter = dayPlanAdapter

        LinearSnapHelper().attachToRecyclerView(rvOnTopOfMap)
    }

    override fun onMapClick(point: LatLng): Boolean {
        val pointf = mapboxMap!!.projection.toScreenLocation(point)
        val rectF = RectF(pointf.x - 10, pointf.y - 10, pointf.x + 10, pointf.y + 10)

        val featureList = mapboxMap!!.queryRenderedFeatures(rectF, LAYER_ID)
        Log.i(TAG, "You clicked on " + featureList.size.toString())

        if (featureList.size > 0) {
            val selectedFeature = featureList[0].geometry() as Point

            mapboxMap!!.easeCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(LatLng(selectedFeature.latitude(), selectedFeature.longitude()))
                        .zoom(17.0)
                        .build()
                )
            )

            for (i in 1..wikiPoiList.size) {
                if (wikiPoiList[i].pageid.toString() == featureList[0].id()) {
                    rvOnTopOfMap.layoutManager!!.scrollToPosition(i)
                    break
                }
            }

            featureList.forEach {
                Log.i(TAG, "Feature found with id " + it.toJson())
            }

            return true
        }

        return false
    }

    override fun onStart() {
        super.onStart()
        dayPlanMapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        dayPlanMapView?.onStop()
    }

    public override fun onPause() {
        super.onPause()
        dayPlanMapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        dayPlanMapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mapboxMap != null) {
            mapboxMap!!.removeOnMapClickListener(this);
        }
        dayPlanMapView.onDestroy();
        dayPlanMapView?.onDestroy()
    }
}