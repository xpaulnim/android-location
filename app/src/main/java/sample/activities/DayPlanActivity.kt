package sample.activities

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.activity_dayplan.*
import kotlinx.android.synthetic.main.activity_hobbies.*
import org.json.JSONObject
import sample.AppNetwork
import sample.R
import sample.adapters.DayPlanAdapter
import sample.model.WikiPoi


class DayPlanActivity : OnMapReadyCallback, AppCompatActivity() {

    private var featureCollection: FeatureCollection? = null
    private var mapboxMap: MapboxMap? = null

    private val SYMBOL_ICON_ID = "SYMBOL_ICON_ID"
    private val SOURCE_ID = "SOURCE_ID"
    private val LAYER_ID = "LAYER_ID"

    private val wikiTestUrl =
        "https://en.wikipedia.org/w/api.php?action=query" +
                "&list=geosearch" +
                "&gsradius=500" +
                "&gscoord=51.5144411%7C-0.2018387" +
                "&format=json" +
                "&gslimit=50" +
                "&prop=coordinates|info"

    lateinit var queue: AppNetwork

    private val wikiPoiList = mutableListOf<WikiPoi>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        queue = AppNetwork.getInstance(this.applicationContext)

        Mapbox.getInstance(this, getString(R.string.access_token))

        setContentView(R.layout.activity_dayplan)

        dayPlanMapView?.onCreate(savedInstanceState)
        dayPlanMapView?.getMapAsync(this)

    }

    private fun getWikiPointsOfInterest() {
        val jsonRequest =
            JsonObjectRequest(Request.Method.GET, wikiTestUrl, null, Response.Listener<JSONObject> {
                Log.i(HobbiesActivity.TAG, "the response was: $it")

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

                Log.i(HobbiesActivity.TAG, wikiPoiList.size.toString())

                initFeatureCollection()
                initMarkerIcons(mapboxMap!!.style!!)
                initRecyclerView()
            }, Response.ErrorListener {
                Log.e(HobbiesActivity.TAG, "That didn't work $it")
            })

        jsonRequest.tag = this

        queue.addToRequestQueue(jsonRequest)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        dayPlanMapView?.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS, Style.OnStyleLoaded { style ->
                getWikiPointsOfInterest()

                Toast.makeText(this, "Done initializing map", Toast.LENGTH_SHORT).show()
            })
        }

    }

    private fun initFeatureCollection() {
        featureCollection = FeatureCollection.fromFeatures(emptyArray())
        var featureList = emptyArray<Feature>()

        if (featureCollection != null) {
            for (poi in wikiPoiList) {
                featureList =
                    featureList.plus(
                        Feature.fromGeometry(
                            Point.fromLngLat(
                                poi.longitude,
                                poi.latitude
                            )
                        )
                    )
            }

            featureCollection = FeatureCollection.fromFeatures(featureList)

            Log.i(HobbiesActivity.TAG, "POIs should now be visible")
        }
    }

    private fun initMarkerIcons(style: Style) {
        style.addImage(
            SYMBOL_ICON_ID, BitmapFactory.decodeResource(
                this.resources, R.drawable.red_marker
            )
        )

        style.addSource(GeoJsonSource(SOURCE_ID, featureCollection))

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

        rvOnTopOfMap.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, true)
        rvOnTopOfMap.itemAnimator = DefaultItemAnimator()
        rvOnTopOfMap.adapter = dayPlanAdapter

        LinearSnapHelper().attachToRecyclerView(rvOnTopOfMap)
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

        dayPlanMapView?.onDestroy()
    }
}