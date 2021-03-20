package com.bartekturkosz.hangman.domain

data class User(
    var name: String? = null,
    var points: Int = 0,
    var wins: Int = 0)