package com.bartekturkosz.hangman.viewmodels

import androidx.lifecycle.*
import com.bartekturkosz.hangman.domain.User
import com.bartekturkosz.hangman.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RankingViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean>
        get() = _loading

    var users = userRepository.users

    fun getUserName() = userRepository.getUserName()

    fun fetchUsers() {
        viewModelScope.launch {
            _loading.value = true
            userRepository.fetchUsers()
            _loading.value = false
        }
    }
}

class RankingViewModelFactory(private val userRepository: UserRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        (RankingViewModel(userRepository) as T)
}