package sample

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import sample.dao.LocationDao
import sample.dao.UserDao
import sample.model.Location
import sample.model.User

@Database(entities = [User::class, Location::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        @Volatile
        private var DB_INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = DB_INSTANCE
            if(tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "AppDatabase"
                ).build()

                DB_INSTANCE = instance
                return instance
            }
        }
    }

    abstract fun userDao(): UserDao
    abstract fun locationDao(): LocationDao
}