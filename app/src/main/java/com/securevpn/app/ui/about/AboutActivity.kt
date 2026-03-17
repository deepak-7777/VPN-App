package com.securevpn.app.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
//import com.securevpn.app.BuildConfig
import com.securevpn.app.R
import com.securevpn.app.databinding.ActivityAboutBinding
import com.securevpn.app.utils.Constants

/**
 * AboutActivity — displays app info, developer details, and open source notice.
 */
class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        setupContent()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.title_about)
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupContent() {
        binding.tvVersion.text = "${getString(R.string.app_version_prefix)} ${Constants.APP_VERSION}"
        binding.tvWebsite.text = Constants.WEBSITE_URL
    }

    private fun setupClickListeners() {
        binding.tvWebsite.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.WEBSITE_URL)))
            } catch (e: Exception) { /* no browser */ }
        }
    }
}
