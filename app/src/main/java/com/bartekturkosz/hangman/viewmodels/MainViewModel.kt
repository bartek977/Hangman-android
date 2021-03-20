package com.bartekturkosz.hangman.viewmodels

import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth

class MainViewModel() : ViewModel() {
    val isLogged: MutableLiveData<Boolean> =
        MutableLiveData(FirebaseAuth.getInstance().currentUser != null
        )
}

class MainViewModelFactory() : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        (MainViewModel() as T)
}