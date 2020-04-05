package sample.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
import kotlinx.android.synthetic.main.fragment_dayplan.*
import sample.AppNetwork
import sample.R
import sample.activities.LocationActivity
import sample.adapters.DayPlanAdapter
import sample.model.WikiPoi

class DayPlanFragment : Fragment(), OnMapReadyCallback, MapboxMap.OnMapClickListener {
    companion object {
        private val TAG: String = DayPlanFragment::class.java.simpleName
        private const val MY_PERMISSION_REQUEST_LOCATION = 1

        private const val SYMBOL_ICON_ID = "SYMBOL_ICON_ID"
        private const val SOURCE_ID = "SOURCE_ID"
        private const val LAYER_ID = "LAYER_ID"
    }

    private var featureCollection: FeatureCollection? = null
    private var mapboxMap: MapboxMap? = null

    private val wikiUrlTemplate =
        "https://en.wikipedia.org/w/api.php?action=query" +
                "&list=geosearch" +
                "&gsradius=500" +
                "&gscoord={lat}%7C{lng}" +
                "&format=json" +
                "&gslimit=50" +
                "&prop=coordinates|info"

    private val wikiPoiList = mutableListOf<WikiPoi>()

    private lateinit var appNetwork: AppNetwork

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var lastKnownLocation: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(activity!!, getString(R.string.access_token))

        return inflater.inflate(R.layout.fragment_dayplan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        day_plan_map_view?.onCreate(savedInstanceState)
        day_plan_map_view?.getMapAsync(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        appNetwork = AppNetwork.getInstance(activity!!)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!)
    }

    private fun getWikiPointsOfInterest(location: Location?) {
        val finalWikiUrl = wikiUrlTemplate
            .replace("{lat}", location!!.latitude.toString())
            .replace("{lng}", location.longitude.toString())

        val jsonRequest = JsonObjectRequest(
            Request.Method.GET,
            finalWikiUrl,
            null,
            Response.Listener {
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

        appNetwork.addToRequestQueue(jsonRequest)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        day_plan_map_view?.getMapAsync {
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

                    Toast.makeText(activity!!, "Done initializing map", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Log.i(LocationActivity.TAG, "Location not yet granted. Requesting location")

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            ) {
                Log.i(LocationActivity.TAG, "Requesting current location")
            } else {
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    MY_PERMISSION_REQUEST_LOCATION
                )
            }
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            activity!!,
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
            SymbolLayer(
                LAYER_ID,
                SOURCE_ID
            ).withProperties(
                iconImage(SYMBOL_ICON_ID),
                iconAllowOverlap(true),
                iconOffset(arrayOf(0f, -4f))
            )
        )
    }

    private fun initRecyclerView() {
        val dayPlanAdapter = DayPlanAdapter(wikiPoiList, mapboxMap)

        rv_on_top_of_map.layoutManager = LinearLayoutManager(
            activity!!.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        rv_on_top_of_map.itemAnimator = DefaultItemAnimator()
        rv_on_top_of_map.adapter = dayPlanAdapter

        LinearSnapHelper().attachToRecyclerView(rv_on_top_of_map)
    }

    override fun onMapClick(point: LatLng): Boolean {
        val pointf = mapboxMap!!.projection.toScreenLocation(point)
        val rectF = RectF(pointf.x - 10, pointf.y - 10, pointf.x + 10, pointf.y + 10)

        val featureList = mapboxMap!!.queryRenderedFeatures(
            rectF,
            LAYER_ID
        )
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
                    rv_on_top_of_map.layoutManager!!.scrollToPosition(i)
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
        day_plan_map_view?.onStart()
    }

    override fun onStop() {
        super.onStop()
        day_plan_map_view?.onStop()
    }

    override fun onPause() {
        super.onPause()
        day_plan_map_view?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        day_plan_map_view?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mapboxMap != null) {
            mapboxMap!!.removeOnMapClickListener(this);
        }
        day_plan_map_view?.onDestroy();
    }
}
