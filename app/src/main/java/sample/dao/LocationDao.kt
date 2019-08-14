package sample.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import sample.model.Location

@Dao
interface LocationDao {
    @Query("SELECT * FROM location")
    fun getAll(): LiveData<List<Location>>

    @Insert
    suspend fun insertAll(vararg locations: Location)

    @Insert
    suspend fun insert(location: Location)
}