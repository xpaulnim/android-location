package sample.repository

import sample.dao.UserDao

class UserRepository(private val userDao: UserDao) {
    fun getUsers() = userDao.getUsers()
}