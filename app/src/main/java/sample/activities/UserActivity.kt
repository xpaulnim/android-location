package sample.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_users.*
import sample.R
import sample.adapters.UserAdapter
import sample.viewmodels.UserViewModel

class UserActivity : AppCompatActivity() {

    private val TAG by lazy { UserActivity::class.java.simpleName }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        val model = ViewModelProvider(this).get(UserViewModel::class.java)
        model.users.observe(this, Observer { users ->
            if (users.isNotEmpty()) {
                Log.i(TAG, "got list of users. eg ${users!![0].firstName}")

                val layoutManager = LinearLayoutManager(this)
                layoutManager.orientation = RecyclerView.VERTICAL
                rvUsers.layoutManager = layoutManager
                rvUsers.adapter = UserAdapter(this, users)
            } else {
                Log.i(TAG, "No users loaded")
            }
        })
    }
}