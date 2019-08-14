package sample.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Log
import sample.model.User

class UserViewModel : ViewModel() {
    companion object {
        val TAG: String  = UserViewModel::class.java.simpleName
    }

    private val users: MutableLiveData<List<User>> by lazy {
        MutableLiveData<List<User>>().also {
            it.value = loadUsers()
        }
    }

    fun getUsers(): LiveData<List<User>> {
        return users
    }

    private fun loadUsers(): List<User> {
        // Could be asynchronous operation to fetch users
        Log.i(TAG, "Sleeping for 5 seconds. ie running async process")
        Thread.sleep(5000)

        Log.i(TAG, "Done sleeping")
        return listOf(
            User(1, "judy", "last name"),
            User(1, "danny", "last name"),
            User(1, "howard", "last name"))
    }
}