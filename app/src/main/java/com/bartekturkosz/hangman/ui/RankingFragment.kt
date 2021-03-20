package com.bartekturkosz.hangman.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.bartekturkosz.hangman.MainActivity
import com.bartekturkosz.hangman.POINTS_RANKING
import com.bartekturkosz.hangman.WINS_RANKING
import com.bartekturkosz.hangman.databinding.FragmentRankingBinding
import com.bartekturkosz.hangman.domain.User
import com.bartekturkosz.hangman.repository.UserRepository
import com.bartekturkosz.hangman.viewmodels.RankingViewModel
import com.bartekturkosz.hangman.viewmodels.RankingViewModelFactory

class RankingFragment : Fragment() {

    lateinit var binding: FragmentRankingBinding

    private lateinit var rankingViewModel: RankingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rankingViewModel = RankingViewModelFactory(
            UserRepository(PreferenceManager.getDefaultSharedPreferences(requireContext()))
        ).create(RankingViewModel::class.java)
        binding = FragmentRankingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rankingViewModel.loading.observe(viewLifecycleOwner, Observer { loading ->
            if(loading) {
                (requireActivity() as MainActivity).showProgressBar()
            }
            else {
                (requireActivity() as MainActivity).dismissProgressBar()
            }
        }
        )

        val adapter = RankingAdapter(rankingViewModel.getUserName())
        binding.recyclerViewUsers.adapter = adapter

        binding.pointsRankingButton.setOnClickListener {
            adapter.sort(POINTS_RANKING)
            adapter.notifyDataSetChanged()
        }
        binding.winsRankingButton.setOnClickListener {
            adapter.sort(WINS_RANKING)
            adapter.notifyDataSetChanged()
        }

        rankingViewModel.users.observe(viewLifecycleOwner, Observer {
            adapter.users = it as MutableList<User>
            adapter.notifyDataSetChanged()
        })

        rankingViewModel.fetchUsers()
    }
}