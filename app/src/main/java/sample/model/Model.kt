package sample.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

data class Hobby(var title: String)

@Entity
data class User(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "first_name") var firstName: String?,
    @ColumnInfo(name = "last_name") var lastName: String?
)

@Entity
data class Location(
    @PrimaryKey @ColumnInfo(name = "timestamp") var timestamp: Long,
    @ColumnInfo(name = "lat") var lat: Double,
    @ColumnInfo(name = "lng") var lng: Double
)

object Supplier {
    val hobbies = listOf(
        Hobby("Swim"),
        Hobby("Bike"),
        Hobby("Run")
    )
}

data class WikiPoi(val title: String, val latitude: Double, val longitude: Double)