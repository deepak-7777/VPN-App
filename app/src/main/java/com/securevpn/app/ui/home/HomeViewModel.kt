package com.securevpn.app.ui.home

import android.app.Application
import android.content.Intent
import android.net.VpnService
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.securevpn.app.data.model.ConnectData
import com.securevpn.app.data.model.ServerItem
import com.securevpn.app.data.repository.VpnRepository
import com.securevpn.app.service.SecureVpnService
import com.securevpn.app.utils.Constants
import com.securevpn.app.utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VpnRepository(application.applicationContext)

    private val _connectionState = MutableLiveData(Constants.STATE_DISCONNECTED)
    val connectionState: LiveData<String> = _connectionState

    private val _connectResult = MutableLiveData<Resource<ConnectData>>()
    val connectResult: LiveData<Resource<ConnectData>> = _connectResult

    private val _disconnectResult = MutableLiveData<Resource<Boolean>>()
    val disconnectResult: LiveData<Resource<Boolean>> = _disconnectResult

    private val _selectedServer = MutableLiveData<ServerItem?>()
    val selectedServer: LiveData<ServerItem?> = _selectedServer

    private val _currentIp = MutableLiveData("—")
    val currentIp: LiveData<String> = _currentIp

    private val _sessionSeconds = MutableLiveData(0L)
    val sessionSeconds: LiveData<Long> = _sessionSeconds

    private val _vpnPermissionIntent = MutableLiveData<Intent?>()
    val vpnPermissionIntent: LiveData<Intent?> = _vpnPermissionIntent

    private var timerJob: Job? = null

    fun selectServer(server: ServerItem) {
        _selectedServer.value = server
    }

    fun connect() {
        val appContext = getApplication<Application>().applicationContext
        val prepareIntent = VpnService.prepare(appContext)

        if (prepareIntent != null) {
            _vpnPermissionIntent.value = prepareIntent
            return
        }

        connectAfterPermissionGranted()
    }

    fun onVpnPermissionResult(granted: Boolean) {
        if (granted) {
            connectAfterPermissionGranted()
        } else {
            _connectionState.value = Constants.STATE_DISCONNECTED
            _connectResult.value = Resource.Error("VPN permission denied")
        }
    }

    private fun connectAfterPermissionGranted() {
        val server = _selectedServer.value
        if (server == null) {
            _connectionState.value = Constants.STATE_DISCONNECTED
            _connectResult.value = Resource.Error("Please select a server first")
            return
        }

        viewModelScope.launch {
            _connectionState.value = Constants.STATE_CONNECTING
            _connectResult.value = Resource.Loading

            when (val result = repository.connectVpn(server.id)) {
                is Resource.Success -> {
                    val connectData = result.data
                    val wg = connectData.wireGuard

                    if (wg == null) {
                        _connectionState.value = Constants.STATE_DISCONNECTED
                        _connectResult.value =
                            Resource.Error("WireGuard config missing from backend response")
                        return@launch
                    }

                    val appContext = getApplication<Application>().applicationContext
                    val serviceIntent = SecureVpnService.buildStartIntent(
                        context = appContext,
                        sessionToken = connectData.sessionToken,
                        config = wg
                    )

                    ContextCompat.startForegroundService(appContext, serviceIntent)

                    /*
                     * Abhi service start ho rahi hai aur config pass ho raha hai.
                     * Real tunnel up callback milne tak CONNECTING state rakhenge.
                     */
                    _currentIp.value = connectData.assignedIp ?: "—"
                    _connectResult.value = Resource.Success(connectData)
                }

                is Resource.Error -> {
                    _connectionState.value = Constants.STATE_DISCONNECTED
                    _connectResult.value = result
                }

                else -> Unit
            }
        }
    }

    /**
     * Is method ko tab call karna hai jab SecureVpnService se actual tunnel-up signal mile.
     */
    fun markTunnelConnected(displayIp: String?) {
        _connectionState.value = Constants.STATE_CONNECTED
        _currentIp.value = displayIp ?: "Protected"
        startSessionTimer()
    }

    /**
     * Is method ko tab call karna hai jab tunnel disconnect/fail ho.
     */
    fun markTunnelDisconnected(message: String? = null) {
        stopSessionTimer()
        repository.clearSession()
        _connectionState.value = Constants.STATE_DISCONNECTED
        _currentIp.value = "—"
        _sessionSeconds.value = 0L

        if (message != null) {
            _connectResult.value = Resource.Error(message)
        }
    }

    fun clearVpnPermissionIntent() {
        _vpnPermissionIntent.value = null
    }

    fun disconnect() {
        viewModelScope.launch {
            stopSessionTimer()

            val appContext = getApplication<Application>().applicationContext
            ContextCompat.startForegroundService(
                appContext,
                SecureVpnService.buildStopIntent(appContext)
            )

            when (repository.disconnectVpn()) {
                is Resource.Success -> {
                    _connectionState.value = Constants.STATE_DISCONNECTED
                    _currentIp.value = "—"
                    _sessionSeconds.value = 0L
                    _disconnectResult.value = Resource.Success(true)
                }

                is Resource.Error -> {
                    _connectionState.value = Constants.STATE_DISCONNECTED
                    _currentIp.value = "—"
                    _sessionSeconds.value = 0L
                    _disconnectResult.value = Resource.Error("Disconnect failed")
                }

                else -> Unit
            }
        }
    }

    private fun startSessionTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(Constants.SESSION_TIMER_INTERVAL_MS)
                _sessionSeconds.value = (_sessionSeconds.value ?: 0L) + 1L
            }
        }
    }

    private fun stopSessionTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        stopSessionTimer()
        super.onCleared()
    }
}