package com.securevpn.app.ui.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.securevpn.app.utils.Constants

/**
 * ViewModel for SettingsFragment.
 * Manages user preferences stored in SharedPreferences.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(
        Constants.PREF_NAME, Context.MODE_PRIVATE
    )

    private val _autoConnect = MutableLiveData(
        prefs.getBoolean(Constants.PREF_AUTO_CONNECT, false)
    )
    val autoConnect: LiveData<Boolean> = _autoConnect

    fun setAutoConnect(enabled: Boolean) {
        _autoConnect.value = enabled
        prefs.edit().putBoolean(Constants.PREF_AUTO_CONNECT, enabled).apply()
    }

    val appVersion: String = Constants.APP_VERSION
}
