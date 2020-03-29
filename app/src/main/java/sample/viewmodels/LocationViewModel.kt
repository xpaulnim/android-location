package sample.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sample.AppDatabase
import sample.model.Location
import sample.repository.LocationRepository

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LocationRepository
    val locations: LiveData<List<Location>>

    init {
        val locationDao = AppDatabase.getInstance(application).locationDao()

        repository = LocationRepository(locationDao)
        locations = repository.locations
    }

    fun insertLocation(location: Location)  = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(location)
    }
}