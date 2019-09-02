package sample.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_hobbies.*
import org.json.JSONObject
import sample.AppNetwork
import sample.adapters.HobbiesAdapter
import sample.R
import sample.model.Supplier

class HobbiesActivity : AppCompatActivity() {
    companion object {
        val TAG: String  = HobbiesActivity::class.java.simpleName
    }

    lateinit var queue: AppNetwork

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hobbies)

        queue = AppNetwork.getInstance(this.applicationContext)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        recyclerView.layoutManager = layoutManager

        val url = "https://catfact.ninja/breeds?limit=1"

        val jsonRequest = JsonObjectRequest(Request.Method.GET,url, null, Response.Listener<JSONObject> {
            Log.i(TAG, "the response was: $it")

            val data = it.getJSONArray("data")
            val firstCat = data.getJSONObject(0)
            Log.i(TAG, firstCat.getString("country"))
        }, Response.ErrorListener {
            Log.e(TAG, "That didn't work $it")
        })

        jsonRequest.tag = this

        queue.addToRequestQueue(jsonRequest)

        val adapter = HobbiesAdapter(this, Supplier.hobbies)
        recyclerView.adapter = adapter
    }

    override fun onStop() {
        super.onStop()

        queue.cancelAll(this)
    }
}
