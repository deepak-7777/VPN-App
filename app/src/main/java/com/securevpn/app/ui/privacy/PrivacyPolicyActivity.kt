package com.securevpn.app.ui.privacy

import android.os.Bundle
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.securevpn.app.R
import com.securevpn.app.databinding.ActivityPrivacyPolicyBinding
import com.securevpn.app.utils.Constants
import com.securevpn.app.utils.hide
import com.securevpn.app.utils.show

class PrivacyPolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        setupWebView()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.title_privacy_policy)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupWebView() {
        binding.webviewPrivacy.apply {
            settings.javaScriptEnabled = false  // disabled for security
            settings.domStorageEnabled = false
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    binding.progressPrivacy.show()
                    binding.llError.hide()
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.progressPrivacy.hide()
                }

                override fun onReceivedError(
                    view: WebView?, request: WebResourceRequest?, error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                        binding.progressPrivacy.hide()
                        binding.webviewPrivacy.hide()
                        binding.llError.show()
                    }
                }
            }

            loadUrl(Constants.PRIVACY_POLICY_URL)
        }

        binding.btnRetry.setOnClickListener {
            binding.llError.hide()
            binding.webviewPrivacy.show()
            binding.webviewPrivacy.reload()
        }
    }

    override fun onBackPressed() {
        if (binding.webviewPrivacy.canGoBack()) {
            binding.webviewPrivacy.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
