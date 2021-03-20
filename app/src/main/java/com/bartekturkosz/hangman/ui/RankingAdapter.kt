package com.bartekturkosz.hangman.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bartekturkosz.hangman.POINTS_RANKING
import com.bartekturkosz.hangman.R
import com.bartekturkosz.hangman.WINS_RANKING
import com.bartekturkosz.hangman.databinding.ItemUserRankingBinding
import com.bartekturkosz.hangman.domain.User

class RankingAdapter(private val username: String) :
    RecyclerView.Adapter<RankingAdapter.ViewHolder>() {
    var users: MutableList<User> = mutableListOf()

    fun sort(rankingType: Int) {
        when (rankingType) {
            POINTS_RANKING -> {
                users.sortBy { it.wins }
                users.sortBy { it.points }
            }
            WINS_RANKING -> {
                users.sortBy { it.points }
                users.sortBy { it.wins }
            }
        }
        users.reverse()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemUserRankingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.binding.apply {
            this.user = user
            this.position.text = (position + 1).toString()
            if (user.name == username) {
                root.setBackgroundResource(R.drawable.border_logged_user)
            } else {
                root.setBackgroundResource(R.drawable.border)
            }
        }
    }

    override fun getItemCount(): Int = users.size

    class ViewHolder(val binding: ItemUserRankingBinding) : RecyclerView.ViewHolder(binding.root)

}