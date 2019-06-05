package sample.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_hobbies.*
import sample.adapters.HobbiesAdapter
import sample.R
import sample.Supplier

class HobbiesActivity : AppCompatActivity() {
    companion object {
        val TAG: String  = HobbiesActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hobbies)

        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager

        val queue = Volley.newRequestQueue(this)
        val url = "http://10.0.2.2:8080/messages"

        val stringRequest = StringRequest(Request.Method.GET, url, Response.Listener<String> {
            Log.i(TAG, "the response was: $it")
        }, Response.ErrorListener {
            Log.e(TAG, "That didn't work $it")
        })

        queue.add(stringRequest)

        val adapter = HobbiesAdapter(this, Supplier.hobbies)
        recyclerView.adapter = adapter
    }
}
