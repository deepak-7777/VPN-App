package com.securevpn.app.utils

/**
 * App-wide constants.
 * Privacy note: No user identifiers, private keys, or sensitive session data stored here.
 */
object Constants {

    // -----------------------------
    // Network
    // -----------------------------
    // Emulator ke liye:
    // const val BASE_URL = "http://10.0.2.2:8080/"
    //
    // Real device + local PC testing ke liye:
    // const val BASE_URL = "http://192.168.1.101:8080/"
    //
    // Production ke liye:
    // const val BASE_URL = "https://your-domain.com/"
    const val BASE_URL = "http://192.168.1.103:8080/"
    const val CONNECT_TIMEOUT_SECONDS = 30L
    const val READ_TIMEOUT_SECONDS = 30L
    const val WRITE_TIMEOUT_SECONDS = 30L

    // -----------------------------
    // API Endpoints
    // -----------------------------
    const val ENDPOINT_SERVERS = "api/v1/servers"
    const val ENDPOINT_CONNECT = "api/v1/vpn/connect"
    const val ENDPOINT_DISCONNECT = "api/v1/vpn/disconnect"
    const val ENDPOINT_HEALTH = "api/v1/health"
    const val ENDPOINT_PRIVACY_POLICY = "api/v1/privacy-policy"

    // -----------------------------
    // App Info
    // -----------------------------
    const val APP_NAME = "SecureVPN"
    const val APP_VERSION = "1.0.0"

    const val PLAY_STORE_URL =
        "https://play.google.com/store/apps/details?id=com.securevpn.app"

    const val PRIVACY_POLICY_URL = "https://deepak-kumar07.netlify.app/"                           /// https://your-domain.com/privacy-policy
    const val TERMS_URL = "https://deepak-kumar07.netlify.app/"                                    /// https://your-domain.com/terms
    const val SUPPORT_EMAIL = "vmpk77@gmail.com"                                /// support@your-domain.com
    const val WEBSITE_URL = "https://deepak-kumar07.netlify.app/"                                   /// https://your-domain.com

    // -----------------------------
    // Session / Timer
    // -----------------------------
    const val SESSION_TIMER_INTERVAL_MS = 1000L

    // -----------------------------
    // Connection States
    // -----------------------------
    const val STATE_DISCONNECTED = "DISCONNECTED"
    const val STATE_CONNECTING = "CONNECTING"
    const val STATE_CONNECTED = "CONNECTED"

    // -----------------------------
    // SharedPreferences
    // -----------------------------
    const val PREF_NAME = "secure_vpn_prefs"
    const val PREF_AUTO_CONNECT = "pref_auto_connect"
    const val PREF_LAST_SERVER_ID = "pref_last_server_id"

    // -----------------------------
    // VPN / WireGuard
    // -----------------------------
    const val VPN_NOTIFICATION_CHANNEL_ID = "secure_vpn_channel"
    const val VPN_NOTIFICATION_CHANNEL_NAME = "SecureVPN"
    const val VPN_NOTIFICATION_ID = 1001

    const val VPN_PERMISSION_REQUEST_KEY = "vpn_permission_request"

    const val ACTION_START_VPN = "com.securevpn.app.action.START_VPN"
    const val ACTION_STOP_VPN = "com.securevpn.app.action.STOP_VPN"

    const val EXTRA_SESSION_TOKEN = "extra_session_token"
    const val EXTRA_SERVER_PUBLIC_KEY = "extra_server_public_key"
    const val EXTRA_CLIENT_PRIVATE_KEY = "extra_client_private_key"
    const val EXTRA_ENDPOINT_HOST = "extra_endpoint_host"
    const val EXTRA_ENDPOINT_PORT = "extra_endpoint_port"
    const val EXTRA_CLIENT_ADDRESS = "extra_client_address"
    const val EXTRA_DNS_SERVERS = "extra_dns_servers"
    const val EXTRA_ALLOWED_IPS = "extra_allowed_ips"
    const val EXTRA_KEEPALIVE = "extra_keepalive"

    // -----------------------------
    // Defaults for WireGuard
    // -----------------------------
    const val DEFAULT_DEVICE_TYPE = "android"
    const val DEFAULT_PERSISTENT_KEEPALIVE = 25
}