package com.bartekturkosz.hangman.repository

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bartekturkosz.hangman.*
import com.bartekturkosz.hangman.domain.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepository(private val prefs: SharedPreferences) {

    private val _points = MutableLiveData(INIT_POINTS)
    val points: LiveData<Int>
        get() = _points

    private val _wins = MutableLiveData(INIT_POINTS)
    val wins: LiveData<Int>
        get() = _wins

    fun getUserId(): String {
        return prefs.getString(USER_ID_KEY, "adfbsadjf")!!
    }

    fun getUserName(): String {
        return prefs.getString(USERNAME_KEY, "barte")!!
    }

    fun getUserPoints(): Int {
        return prefs.getInt(POINTS_KEY, INIT_POINTS)
    }

    fun getNumberOfWins(): Int {
        return prefs.getInt(WINS_KEY, INIT_WINS)
    }

    fun getCoins(): Int {
        return prefs.getInt(COINS_KEY, INIT_COINS)
    }

    private fun updateRanking() {
        println("update ranking")
        FirebaseDatabase.getInstance().getReference(NOD_USERS)
            .child(getUserId())
            .child("name").setValue(getUserName())

        FirebaseDatabase.getInstance().getReference(NOD_USERS)
            .child(getUserId())
            .child("points").setValue(points.value)

        FirebaseDatabase.getInstance().getReference(NOD_USERS)
            .child(getUserId())
            .child("wins").setValue(wins.value)
    }


    fun addPoints(points: Int) {
        var pointsNow = getUserPoints()
        pointsNow += points
        _points.value = pointsNow
        var wins = getNumberOfWins()
        wins += 1
        _wins.value = wins
        prefs.edit().putInt(POINTS_KEY, pointsNow).commit()
        prefs.edit().putInt(WINS_KEY, _wins.value!!).commit()
        updateRanking()
    }

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>>
        get() = _users
    private val dbUsers = FirebaseDatabase.getInstance().getReference(NOD_USERS)

    fun fetchUsers() {
        dbUsers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val users = mutableListOf<User>()
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        user.let { users.add(it!!) }
                    }
                    users.sortBy { it.wins }
                    users.sortBy { it.points }
                    users.reverse()
                    _users.value = users
                }
            }
        })
    }

    fun updateCoins(coins: Int) {
        var currentCoins = getCoins()
        currentCoins += coins
        prefs.edit().putInt(COINS_KEY, currentCoins).commit()
    }

}