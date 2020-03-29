package sample

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import sample.dao.LocationDao
import sample.dao.UserDao
import sample.model.Location
import sample.model.User
import java.util.concurrent.Executors

@Database(entities = [User::class, Location::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun locationDao(): LocationDao

    companion object {
        @Volatile
        private var DB_INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return DB_INSTANCE ?: synchronized(this) {
                DB_INSTANCE ?: buildDatabase(context).also {
                    DB_INSTANCE = it
                }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "AppDatabase.db")
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)

                        Executors.newSingleThreadExecutor().execute {
                            getInstance(context).clearAllTables()

                            getInstance(context).userDao().insertAll(
                                listOf(
                                    User(1, "judy", "last name"),
                                    User(2, "danny", "last name"),
                                    User(3, "howard", "last name")
                                )
                            )
                        }
                    }
                })
                .build()
        }
    }
}