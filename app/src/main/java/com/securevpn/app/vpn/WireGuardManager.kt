package com.securevpn.app.vpn

import com.securevpn.app.data.model.WireGuardConfigDto

object WireGuardManager {

    @Volatile
    private var currentConfig: WireGuardConfigDto? = null

    @Volatile
    private var currentSessionToken: String? = null

    @Volatile
    private var isTunnelUp: Boolean = false

    fun validateConfig(config: WireGuardConfigDto): String? {
        if (config.serverPublicKey.isNullOrBlank()) return "Missing server public key"
        if (config.clientPrivateKey.isNullOrBlank()) return "Missing client private key"
        if (config.endpointHost.isNullOrBlank()) return "Missing endpoint host"
        if (config.endpointPort == null || config.endpointPort <= 0) return "Invalid endpoint port"
        if (config.clientAddress.isNullOrBlank()) return "Missing client address"
        if (config.allowedIps.isNullOrEmpty()) return "Missing allowed IPs"
        return null
    }

    fun cacheSession(sessionToken: String?, config: WireGuardConfigDto) {
        currentSessionToken = sessionToken
        currentConfig = config
    }

    fun getCurrentConfig(): WireGuardConfigDto? = currentConfig

    fun getCurrentSessionToken(): String? = currentSessionToken

    fun setTunnelUp(value: Boolean) {
        isTunnelUp = value
    }

    fun isTunnelUp(): Boolean = isTunnelUp

    fun clear() {
        currentConfig = null
        currentSessionToken = null
        isTunnelUp = false
    }
}