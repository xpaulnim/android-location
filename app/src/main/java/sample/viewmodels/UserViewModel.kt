package sample.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.AndroidViewModel
import sample.AppDatabase
import sample.model.User
import sample.repository.UserRepository

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository
    val users: LiveData<List<User>>

    init {
        val userDao = AppDatabase.getInstance(application).userDao()
        repository = UserRepository(userDao)
        users = repository.getUsers()
    }
}