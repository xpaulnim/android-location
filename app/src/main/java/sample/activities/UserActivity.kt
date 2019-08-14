package sample.activities

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_users.*
import sample.AppDatabase
import sample.R
import sample.adapters.UserAdapter
import sample.model.User
import sample.viewmodels.UserViewModel

class UserActivity : AppCompatActivity() {
    companion object {
        val TAG: String = HobbiesActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "android-location").build()
        val daoUsers = db.userDao().getAll()

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        rvUsers.layoutManager = layoutManager
        rvUsers.adapter = UserAdapter(this, daoUsers)

        val model = ViewModelProviders.of(this).get(UserViewModel::class.java)
        model.getUsers().observe(this, Observer<List<User>> { users ->
            Log.i(TAG, "got list of users. eg ${users!![0].firstName}")
        })


    }
}