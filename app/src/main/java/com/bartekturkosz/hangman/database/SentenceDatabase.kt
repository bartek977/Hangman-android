package com.bartekturkosz.hangman.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bartekturkosz.hangman.domain.Sentence


@Database(entities = [Sentence::class], version = 1, exportSchema = false)
abstract class SentencesDatabase : RoomDatabase() {
    abstract val sentenceDao: SentenceDao

    companion object {

        @Volatile
        private var INSTANCE: SentencesDatabase? = null

        fun getInstance(context: Context): SentencesDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SentencesDatabase::class.java,
                        "sentences_database"
                    )
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}