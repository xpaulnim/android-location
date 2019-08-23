package sample.activities

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.activity_dayplan.*
import kotlinx.android.synthetic.main.activity_location.*
import sample.R

import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset


class DayPlanActivity : OnMapReadyCallback, AppCompatActivity() {

    private var featureCollection: FeatureCollection? = null
    private var mapboxMap: MapboxMap? = null

    private val SYMBOL_ICON_ID = "SYMBOL_ICON_ID"
    private val SOURCE_ID = "SOURCE_ID"
    private val LAYER_ID = "LAYER_ID"

    private val coordinates = arrayOf(
        LatLng(-34.6054099, -58.363654800000006),
        LatLng(-34.6041508, -58.38555650000001),
        LatLng(-34.6114412, -58.37808899999999),
        LatLng(-34.6097604, -58.382064000000014),
        LatLng(-34.596636, -58.373077999999964),
        LatLng(-34.590548, -58.38256609999996),
        LatLng(-34.5982127, -58.38110440000003)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, getString(R.string.access_token))

        setContentView(R.layout.activity_dayplan)

        dayPlanMapView?.onCreate(savedInstanceState)
        dayPlanMapView?.getMapAsync(this)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        dayPlanMapView?.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS, Style.OnStyleLoaded { style ->
                initFeatureCollection()
                initMarkerIcons(style)
                initRecyclerView()

                Toast.makeText(this, "Done initializing map", Toast.LENGTH_SHORT).show()
            })
        }

    }

    private fun initFeatureCollection() {
        featureCollection = FeatureCollection.fromFeatures(emptyArray())
        var featureList = emptyArray<Feature>()

        if (featureCollection != null) {
            for (latLng in coordinates) {
                featureList =
                    featureList.plus(Feature.fromGeometry(Point.fromLngLat(latLng.longitude, latLng.latitude)))
            }

            featureCollection = FeatureCollection.fromFeatures(featureList)
        }
    }

    private fun initMarkerIcons(loadedMapStyle: Style) {
        loadedMapStyle.addImage(SYMBOL_ICON_ID, BitmapFactory.decodeResource(
            this.resources, R.drawable.red_marker
        ))

        loadedMapStyle.addSource(GeoJsonSource(SOURCE_ID, featureCollection))

        loadedMapStyle.addLayer(SymbolLayer(LAYER_ID, SOURCE_ID).withProperties(
            iconImage(SYMBOL_ICON_ID),
            iconAllowOverlap(true),
            iconOffset(arrayOf(0f, -4f))
        ))
    }

    private fun initRecyclerView() {
//        RecyclerView recyclerView = findViewById(R.id.rv_on_top_of_map)
//
//        LocationRecyclerViewAdapter locationAdapter = LocationRecyclerViewAdapter(createRe)
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