package sample.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import sample.dao.LocationDao
import sample.model.Location

class LocationRepository (private val locationDao: LocationDao) {
    val locations: LiveData<List<Location>> = locationDao.getAll()

    @WorkerThread
    suspend fun insert(location: Location) {
        locationDao.insert(location)
    }
}