package com.bartekturkosz.hangman.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.bartekturkosz.hangman.MainActivity
import com.bartekturkosz.hangman.R
import com.bartekturkosz.hangman.USERNAME_KEY
import com.bartekturkosz.hangman.USER_ID_KEY
import com.bartekturkosz.hangman.databinding.FragmentMainBinding
import com.bartekturkosz.hangman.viewmodels.MainViewModel
import com.bartekturkosz.hangman.viewmodels.MainViewModelFactory
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class MainFragment : Fragment() {

    companion object {
        const val SIGN_IN_RESULT_CODE = 1001
    }

    val viewModel: MainViewModel by viewModels { MainViewModelFactory() }
    lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rankingButton.setOnClickListener {
            findNavController().navigate(R.id.action_MainFragment_to_RankingFragment)
        }

        viewModel.isLogged.observe(viewLifecycleOwner, Observer { isLogged ->
            if (isLogged) {
                (requireActivity() as MainActivity).showUserInfoBar()
                binding.showAdsForCoinsButton.visibility = Button.VISIBLE
                binding.loginPlayButton.apply {
                    text = getString(R.string.play_text)
                    setOnClickListener {
                        findNavController().navigate(R.id.action_MainFragment_to_GameFragment)
                    }
                }
            } else {
                binding.loginPlayButton.apply {
                    text = context.getString(R.string.login_text_button)
                    setOnClickListener {
                        launchSignInFlow()
                    }
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in
                viewModel.isLogged.value = true
                PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
                    .putString(
                        USER_ID_KEY, FirebaseAuth.getInstance().currentUser?.uid
                    )
                    .putString(
                        USERNAME_KEY, FirebaseAuth.getInstance().currentUser?.displayName
                    )
                    .commit()
            }
        }
    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                providers
            ).build(), SIGN_IN_RESULT_CODE
        )
    }
}