package com.bartekturkosz.hangman.repository

import com.bartekturkosz.hangman.NOD_SENTENCES
import com.bartekturkosz.hangman.database.SentencesDatabase
import com.bartekturkosz.hangman.domain.Sentence
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*

class SentenceRepository(private val database: SentencesDatabase) {
    private var sentences: MutableList<Sentence> = mutableListOf()

    suspend fun initRepository() : Int {
        return withContext(Dispatchers.IO) {
            sentences = database.sentenceDao.getSentences() as MutableList<Sentence>
            sentences.size
        }
    }

    suspend fun getSentence(): Sentence? {
        if(sentences.isNullOrEmpty()) {
            downloadSentences()
        }
        return withContext(Dispatchers.IO) {
            sentences.randomOrNull()
        }
    }

    private val dbSentences = FirebaseDatabase.getInstance().getReference(NOD_SENTENCES)

    fun downloadSentences() {
        dbSentences.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val sentencesDB = mutableListOf<String>()
                    for (userSnapshot in snapshot.children) {
                        val sentenceText = userSnapshot.getValue(String::class.java)
                        val sentence = Sentence(sentenceText!!)
                        sentences.add(sentence)
                        CoroutineScope(Dispatchers.IO).launch {
                            database.sentenceDao.insert(sentence)
                        }
                        sentenceText.let { sentencesDB.add(it!!) }
                    }
                }
            }
        })
    }
}