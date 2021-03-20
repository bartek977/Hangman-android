package com.bartekturkosz.hangman.viewmodels

import androidx.lifecycle.*
import com.bartekturkosz.hangman.PRICE_FOR_SHOW_LETTER
import com.bartekturkosz.hangman.repository.SentenceRepository
import com.bartekturkosz.hangman.repository.UserRepository
import kotlinx.coroutines.launch
import java.lang.StringBuilder

class GameViewModel(
    private val sentenceRepository: SentenceRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _gameSentence: MutableLiveData<String> = MutableLiveData("")
    val gameSentence: LiveData<String>
        get() = _gameSentence

    var currentSentence: String = ""

    var correctLetters = mutableSetOf<String>()
    var incorrectLetters = mutableSetOf<String>()

    private val _endGame = MutableLiveData(false)
    val endGame: LiveData<Boolean>
        get() = _endGame

    var emptyListOfSentences = false

    var maxIncorrectLetters: Int = -1

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean>
        get() = _loading

    fun initRepository() {
        viewModelScope.launch {
            _loading.value = true
            val sentencesNumber = sentenceRepository.initRepository()
            println("sentences number:")
            println(sentencesNumber)
            if(sentencesNumber==0) {
                sentenceRepository.downloadSentences()
            }
            _loading.value = false
        }
    }

    fun initGame() {
        viewModelScope.launch {
            _loading.value = true
            correctLetters.clear()
            incorrectLetters.clear()
            _endGame.value = false
            val sentence = sentenceRepository.getSentence()
            if (sentence == null) {
                emptyListOfSentences = true
                _loading.value = false
                _endGame.value = true
                return@launch
            }
            currentSentence = sentence.text.toUpperCase()
            _gameSentence.value = changeLettersToUnderlines(currentSentence)
            _loading.value = false
        }
    }

    private fun changeLettersToUnderlines(sentence: String): String {
        var str = ""
        for (i in sentence.indices) {
            str += when {
                sentence[i] == ' ' -> {
                    " "
                }
                sentence[i] == ',' -> {
                    ","
                }
                sentence[i] == '.' -> {
                    "."
                }
                else -> {
                    "_"
                }
            }
        }
        return str
    }


    fun checkLetter(character: Char): Boolean {
        var isLetterInSentence = false
        for (i in currentSentence.indices) {
            if (currentSentence[i] == character) {
                val builder = StringBuilder(gameSentence.value!!)
                builder[i] = currentSentence[i]
                _gameSentence.value = builder.toString()
                isLetterInSentence = true
                correctLetters.add(character.toString())
            }
        }
        if (!isLetterInSentence) {
            incorrectLetters.add(character.toString())
        }
        if (isGameOver()) {
            _endGame.value = true
        } else if (isGameWon()) {
            _endGame.value = true
            addPoints()
        }
        return isLetterInSentence
    }

    fun isGameOver(): Boolean = incorrectLetters.size >= maxIncorrectLetters
    fun isGameWon(): Boolean = currentSentence == _gameSentence.value
    fun calculatePoints(): Int =
        (32 - incorrectLetters.size - correctLetters.size) * 56 / maxIncorrectLetters

    private fun addPoints() {
        viewModelScope.launch {
            userRepository.addPoints(calculatePoints())
        }
    }

    fun showLetterForCoins(): Boolean {
        if(userRepository.getCoins() >= PRICE_FOR_SHOW_LETTER) {
            showOneLetter()
            userRepository.updateCoins(-PRICE_FOR_SHOW_LETTER)
            return true
        }
        return false
    }

    private fun showOneLetter() {
        for (i in currentSentence.indices) {
            if (gameSentence.value!![i] == '_') {
                val builder = StringBuilder(gameSentence.value!!)
                builder[i] = currentSentence[i]
                _gameSentence.value = builder.toString()
                break
            }
        }
    }

    fun addCoins(coins: Int) {
        userRepository.updateCoins(coins)
    }
}

class GameViewModelFactory(
    private val sentenceRepository: SentenceRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        (GameViewModel(sentenceRepository, userRepository) as T)
}