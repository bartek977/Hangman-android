package com.bartekturkosz.hangman.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Sentence(
    @PrimaryKey
    var text: String,
    var played: Boolean = false
)