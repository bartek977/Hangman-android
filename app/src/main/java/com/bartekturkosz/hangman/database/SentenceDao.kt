package com.bartekturkosz.hangman.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bartekturkosz.hangman.domain.Sentence

@Dao
interface SentenceDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(sentence: Sentence)

    @Query("UPDATE sentence SET played = 1 WHERE text= :text")
    fun setSentencePlayed(text: String)

    @Query("SELECT * FROM sentence WHERE played=0")
    fun getSentences(): List<Sentence>

}