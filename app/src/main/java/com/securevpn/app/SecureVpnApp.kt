package com.securevpn.app

import android.app.Application
import android.util.Log

/**
 * Application class for SecureVPN.
 * Privacy note: No user data, browsing data, or VPN traffic is logged or collected here.
 * Only minimal debug logs are used for crash diagnosis during development.
 */
class SecureVpnApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "SecureVPN App started")
        // TODO: Initialize crash reporting (e.g. Firebase Crashlytics) here if needed
        // TODO: Initialize AdMob here when premium/ads feature is added
    }

    companion object {
        private const val TAG = "SecureVpnApp"
    }
}
