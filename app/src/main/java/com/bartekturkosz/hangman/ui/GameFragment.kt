package com.bartekturkosz.hangman.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.view.get
import androidx.core.view.iterator
import androidx.core.view.size
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.bartekturkosz.hangman.COINS_FOR_GAME_WITHOUT_MISTAKES
import com.bartekturkosz.hangman.MainActivity
import com.bartekturkosz.hangman.R
import com.bartekturkosz.hangman.database.SentencesDatabase
import com.bartekturkosz.hangman.databinding.FragmentGameBinding
import com.bartekturkosz.hangman.repository.SentenceRepository
import com.bartekturkosz.hangman.repository.UserRepository
import com.bartekturkosz.hangman.viewmodels.GameViewModel
import com.bartekturkosz.hangman.viewmodels.GameViewModelFactory
import com.google.android.material.snackbar.Snackbar


class GameFragment : Fragment(), IOnBackPressed {

    private lateinit var gameViewModel: GameViewModel
    lateinit var binding: FragmentGameBinding

    var wrongLetterCounter = 0.1f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGameBinding.inflate(inflater, container, false)
        gameViewModel = GameViewModelFactory(
            SentenceRepository(SentencesDatabase.getInstance(requireContext())),
            UserRepository(PreferenceManager.getDefaultSharedPreferences(requireContext()))
        ).create(GameViewModel::class.java)
        gameViewModel.initRepository()
        binding.viewmodel = gameViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gameViewModel.loading.observe(viewLifecycleOwner, Observer { loading ->
            if (loading) {
                (requireActivity() as MainActivity).showProgressBar()
            } else {
                (requireActivity() as MainActivity).dismissProgressBar()
            }
        })
        setOnClicks()
        displayChooserLevel()
        gameViewModel.endGame.observe(viewLifecycleOwner, Observer { endGame ->
            if (endGame) {
                disableButtons()
                val message = if (gameViewModel.isGameWon()) {
                    if(gameViewModel.incorrectLetters.size==0) {
                        gameViewModel.addCoins(COINS_FOR_GAME_WITHOUT_MISTAKES)
                        (requireActivity() as MainActivity).updateUserInfo()
                        getString(
                            R.string.game_won_without_mistakes_message,
                            gameViewModel.calculatePoints(),
                            COINS_FOR_GAME_WITHOUT_MISTAKES
                        )
                    }
                    else {
                        (requireActivity() as MainActivity).updateUserInfo()
                        getString(
                            R.string.game_won_message,
                            gameViewModel.calculatePoints()
                        )
                    }
                }
                else getString((R.string.game_over_message))

                AlertDialog.Builder(requireContext())
                    .setMessage(message)
                    .setPositiveButton(
                        getString(R.string.yes_text),
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            displayChooserLevel()
                        })
                    .setNegativeButton(
                        getString(R.string.no_text),
                        DialogInterface.OnClickListener { dialogInterface, i ->
                        })
                    .create().show()
            }
        })
    }

    private fun displayChooserLevel() {
        AlertDialog.Builder(requireContext())
            .setItems(
                resources.getStringArray(R.array.game_level_array),
                DialogInterface.OnClickListener { dialogInterface, index ->
                    when (index) {
                        0 -> {
                            gameViewModel.maxIncorrectLetters =
                                resources.getIntArray(R.array.game_level_max_incorrect_letters)[0]
                        }
                        1 -> {
                            gameViewModel.maxIncorrectLetters =
                                resources.getIntArray(R.array.game_level_max_incorrect_letters)[1]
                        }
                        2 -> {
                            gameViewModel.maxIncorrectLetters =
                                resources.getIntArray(R.array.game_level_max_incorrect_letters)[2]
                        }
                    }
                    initGame()
                })
            .setOnCancelListener {
                disableButtons()
            }
            .create()
            .show()
    }

    private fun initGame() {
        if (gameViewModel.emptyListOfSentences) {
            Toast.makeText(
                requireContext(),
                getString(R.string.not_available_sentences_text),
                Toast.LENGTH_SHORT
            ).show()
            disableButtons()
            return
        }
        wrongLetterCounter = 0.1f
        restartButtonsAndSetImg()
        gameViewModel.initGame()
    }

    fun setOnClicks() {
        for (i in 0..binding.letters.size-2) {
            val button = binding.letters[i] as Button
            button.setOnClickListener { onClickLetter(button) }
        }
        val showLetterForCoinsButton = binding.letters[binding.letters.size-1]
        showLetterForCoinsButton.setOnClickListener { showLetterForCoins(showLetterForCoinsButton) }
    }

    private fun showLetterForCoins(view: View) {
        if(gameViewModel.showLetterForCoins()) {
            (requireActivity() as MainActivity).updateUserInfo()
        }
        else {
            Snackbar.make(view, getString(R.string.not_enough_coins_text), Snackbar.LENGTH_SHORT).show()
        }
    }

    fun onClickLetter(button: Button) {
        if (gameViewModel.checkLetter(button.text[0])) {
            button.background.setColorFilter(
                resources.getColor(R.color.green),
                PorterDuff.Mode.MULTIPLY
            )
        } else {
            button.background.setColorFilter(
                resources.getColor(R.color.tomato),
                PorterDuff.Mode.MULTIPLY
            )
            wrongLetterCounter += when (gameViewModel.maxIncorrectLetters) {
                9 -> 0.9f
                6 -> 1.5f
                else -> 3.9f
            }
            setImg()
        }
        button.isClickable = false
    }

    private fun setImg() {
        binding.image.setBackgroundResource(
            when (wrongLetterCounter.toInt()) {
                0 -> R.drawable.img0
                1 -> R.drawable.img1
                2 -> R.drawable.img2
                3 -> R.drawable.img3
                4 -> R.drawable.img4
                5 -> R.drawable.img5
                6 -> R.drawable.img6
                7 -> R.drawable.img7
                else -> R.drawable.img8
            }
        )
    }

    private fun restartButtonsAndSetImg() {
        setImg()
        for (button in binding.letters) {
            if (button is Button) {
                when (button.text) {
//                    in viewModel.correctLetters -> {
//                        button.isClickable = false
//                        button.background.setColorFilter(
//                            resources.getColor(R.color.green),
//                            PorterDuff.Mode.MULTIPLY
//                        )
//                    }
//                    in viewModel.incorrectLetters -> {
//                        button.isClickable = false
//                        button.background.setColorFilter(
//                            resources.getColor(R.color.tomato),
//                            PorterDuff.Mode.MULTIPLY
//                        )
//                    }
                    else -> {
                        button.isClickable = true
                        button.background.clearColorFilter()
                    }
                }
            }
        }
    }

    fun disableButtons() {
        for (button in binding.letters) {
            if (button is Button) {
                button.isClickable = false
            }
        }
    }

    override fun onBackPressed() {
        if (!gameViewModel.endGame.value!!) {
            AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.exit_game_message))
                .setPositiveButton(
                    getString(R.string.exit_text),
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        (requireActivity() as MainActivity).back()
                    })
                .setNegativeButton(
                    getString(R.string.cancel_text),
                    DialogInterface.OnClickListener { dialogInterface, i ->
                    })
                .create().show()
        } else {
            (requireActivity() as MainActivity).back()
        }
    }
}

interface IOnBackPressed {
    fun onBackPressed()
}