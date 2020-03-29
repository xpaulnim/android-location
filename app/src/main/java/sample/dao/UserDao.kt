package sample.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import sample.model.User

@Dao
interface UserDao {
    @Query("SELECT * FROM User")
    fun getUsers(): LiveData<List<User>>

    @Query("SELECT * FROM User WHERE first_name LIKE :first AND last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): User

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg users: User)

    @Insert
    fun insertAll(userList: List<User>)

    @Delete
    fun delete(user: User)
}