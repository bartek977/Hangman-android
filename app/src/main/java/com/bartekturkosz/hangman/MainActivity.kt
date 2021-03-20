package com.bartekturkosz.hangman

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import androidx.annotation.NonNull
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import com.bartekturkosz.hangman.ui.IOnBackPressed
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class MainActivity : AppCompatActivity() {

    private lateinit var progressBar: FrameLayout
    private lateinit var userInfo: LinearLayout
    private lateinit var rewardedAd: RewardedAd
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progressBar = findViewById(R.id.progress_bar)
        userInfo = findViewById(R.id.user_info)
        MobileAds.initialize(this) {}
        rewardedAd = createAndLoadRewardedAd()
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
    }

    fun showUserInfoBar() {
        updateUserInfo()
        findViewById<TextView>(R.id.user_info_login).text = prefs.getString(USERNAME_KEY, "")
        userInfo.visibility = LinearLayout.VISIBLE
    }

    fun updateUserInfo() {
        findViewById<TextView>(R.id.user_info_coins).text =
            prefs.getInt(COINS_KEY, INIT_COINS).toString()
        findViewById<TextView>(R.id.user_info_points).text =
            getString(R.string.user_info_points_format, prefs.getInt(POINTS_KEY, INIT_POINTS))
    }

    fun showProgressBar() {
        progressBar.visibility = FrameLayout.VISIBLE
    }

    fun dismissProgressBar() {
        progressBar.visibility = FrameLayout.INVISIBLE
    }

    override fun onBackPressed() {
        val fragment =
            this.supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        val currentFragment = fragment?.childFragmentManager?.fragments?.get(0) as? IOnBackPressed
        if (currentFragment != null) {
            currentFragment.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    fun back() {
        super.onBackPressed()
        dismissProgressBar()
    }

    fun createAndLoadRewardedAd(): RewardedAd {
        val rewardedAd = RewardedAd(this, "ca-app-pub-3940256099942544/5224354917")
        val adLoadCallback = object : RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                // Ad successfully loaded.
            }

            override fun onRewardedAdFailedToLoad(adError: LoadAdError) {
                // Ad failed to load.
            }
        }
        rewardedAd.loadAd(AdRequest.Builder().build(), adLoadCallback)
        return rewardedAd
    }

    fun onRewardedAdClosed() {
        this.rewardedAd = createAndLoadRewardedAd()
    }

    fun showAdsForCoins(view: View) {
        if (rewardedAd.isLoaded) {
            val activityContext: MainActivity = this@MainActivity
            val adCallback = object : RewardedAdCallback() {
                override fun onRewardedAdOpened() {
                    // Ad opened.
                }

                override fun onRewardedAdClosed() {
                    activityContext.onRewardedAdClosed()
                }

                override fun onUserEarnedReward(@NonNull reward: RewardItem) {
                    var coins = prefs.getInt(COINS_KEY, INIT_COINS)
                    coins += COINS_FOR_ADS
                    prefs.edit().putInt(COINS_KEY, coins).commit()
                    Toast.makeText(
                        activityContext,
                        getString(R.string.add_coins_for_ads_message, COINS_FOR_ADS),
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUserInfo()
                }

                override fun onRewardedAdFailedToShow(adError: AdError) {
                    // Ad failed to display.
                }
            }
            rewardedAd.show(activityContext, adCallback)
        } else {
            Toast.makeText(this, getString(R.string.error_loading_ads_text), Toast.LENGTH_SHORT)
                .show()
        }
    }

}