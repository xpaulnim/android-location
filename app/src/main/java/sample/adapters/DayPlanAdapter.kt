package sample.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import kotlinx.android.synthetic.main.rv_on_top_of_map_card.view.*
import sample.R
import sample.model.WikiPoi

class DayPlanAdapter(private val poiList: List<WikiPoi>, private val mapboxMap: MapboxMap?) :
    RecyclerView.Adapter<DayPlanAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setData(poi: WikiPoi?, position: Int) {
            poi?.let {
                itemView.poiTitleTv.text = poi.title
                itemView.poiDescriptionTv.text = poi.pageid.toString()
            }

            itemView.setOnClickListener{
                mapboxMap!!.easeCamera(CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(LatLng(poiList[position].latitude, poiList[position].longitude))
                        .zoom(17.0)
                        .build()
                ))
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_on_top_of_map_card, parent, false))
    }

    override fun getItemCount(): Int {
        return poiList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
       holder.setData(poiList[position], position)
    }
}