package com.securevpn.app.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.securevpn.app.databinding.ActivitySplashBinding
import com.securevpn.app.ui.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * SplashActivity — shown briefly on app launch.
 * Uses AndroidX SplashScreen API for smooth transition.
 *
 * Privacy note: No user data or network calls are made here.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen BEFORE super.onCreate()
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show animated logo then navigate to MainActivity
        lifecycleScope.launch {
            animateIn()
            delay(1800L)  // Show splash for ~1.8 seconds
            goToMain()
        }
    }

    private fun animateIn() {
        binding.ivSplashLogo.alpha = 0f
        binding.tvAppName.alpha    = 0f
        binding.tvTagline.alpha    = 0f

        binding.ivSplashLogo.animate().alpha(1f).setDuration(600).start()
        binding.tvAppName.animate().alpha(1f).setStartDelay(300).setDuration(500).start()
        binding.tvTagline.animate().alpha(1f).setStartDelay(600).setDuration(500).start()
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
