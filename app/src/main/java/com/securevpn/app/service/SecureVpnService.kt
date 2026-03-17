package com.securevpn.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import java.net.InetAddress
import android.util.Log
import androidx.core.app.NotificationCompat
import com.securevpn.app.R
import com.securevpn.app.data.model.WireGuardConfigDto
import com.securevpn.app.utils.Constants
import com.securevpn.app.vpn.WireGuardManager
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import com.wireguard.config.InetEndpoint
import com.wireguard.config.InetNetwork
import com.wireguard.config.Interface
import com.wireguard.config.Peer

class SecureVpnService : VpnService() {

    companion object {
        private const val TAG = "SecureVpnService"

        const val ACTION_TUNNEL_CONNECTED = "com.securevpn.app.action.TUNNEL_CONNECTED"
        const val ACTION_TUNNEL_DISCONNECTED = "com.securevpn.app.action.TUNNEL_DISCONNECTED"
        const val ACTION_TUNNEL_ERROR = "com.securevpn.app.action.TUNNEL_ERROR"

        const val EXTRA_ERROR_MESSAGE = "extra_error_message"
        const val EXTRA_DISPLAY_IP = "extra_display_ip"

        fun buildStartIntent(
            context: Context,
            sessionToken: String?,
            config: WireGuardConfigDto
        ): Intent {
            return Intent(context, SecureVpnService::class.java).apply {
                action = Constants.ACTION_START_VPN
                putExtra(Constants.EXTRA_SESSION_TOKEN, sessionToken)
                putExtra(Constants.EXTRA_SERVER_PUBLIC_KEY, config.serverPublicKey)
                putExtra(Constants.EXTRA_CLIENT_PRIVATE_KEY, config.clientPrivateKey)
                putExtra(Constants.EXTRA_ENDPOINT_HOST, config.endpointHost)
                putExtra(Constants.EXTRA_ENDPOINT_PORT, config.endpointPort ?: 0)
                putExtra(Constants.EXTRA_CLIENT_ADDRESS, config.clientAddress)
                putStringArrayListExtra(
                    Constants.EXTRA_DNS_SERVERS,
                    ArrayList(config.dnsServers ?: emptyList())
                )
                putStringArrayListExtra(
                    Constants.EXTRA_ALLOWED_IPS,
                    ArrayList(config.allowedIps ?: emptyList())
                )
                putExtra(Constants.EXTRA_KEEPALIVE, config.persistentKeepalive ?: 0)
            }
        }

        fun buildStopIntent(context: Context): Intent {
            return Intent(context, SecureVpnService::class.java).apply {
                action = Constants.ACTION_STOP_VPN
            }
        }
    }

    private lateinit var backend: GoBackend
    private val tunnel = AppTunnel("securevpn")

    override fun onCreate() {
        super.onCreate()
        backend = GoBackend(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.ACTION_START_VPN -> handleStart(intent)
            Constants.ACTION_STOP_VPN -> handleStop()
            else -> {
                Log.w(TAG, "Unknown action received")
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun handleStart(intent: Intent) {
        createNotificationChannel()
        startForeground(
            Constants.VPN_NOTIFICATION_ID,
            buildNotification("Connecting secure tunnel...")
        )

        val config = WireGuardConfigDto(
            serverPublicKey = intent.getStringExtra(Constants.EXTRA_SERVER_PUBLIC_KEY),
            clientPrivateKey = intent.getStringExtra(Constants.EXTRA_CLIENT_PRIVATE_KEY),
            endpointHost = intent.getStringExtra(Constants.EXTRA_ENDPOINT_HOST),
            endpointPort = intent.getIntExtra(Constants.EXTRA_ENDPOINT_PORT, 0),
            clientAddress = intent.getStringExtra(Constants.EXTRA_CLIENT_ADDRESS),
            dnsServers = intent.getStringArrayListExtra(Constants.EXTRA_DNS_SERVERS),
            allowedIps = intent.getStringArrayListExtra(Constants.EXTRA_ALLOWED_IPS),
            persistentKeepalive = intent.getIntExtra(Constants.EXTRA_KEEPALIVE, 0)
        )

        val sessionToken = intent.getStringExtra(Constants.EXTRA_SESSION_TOKEN)

        val validationError = WireGuardManager.validateConfig(config)
        if (validationError != null) {
            sendError(validationError)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        try {
            val wgConfig = buildWireGuardConfig(config)

            backend.setState(tunnel, Tunnel.State.UP, wgConfig)

            WireGuardManager.cacheSession(sessionToken, config)
            WireGuardManager.setTunnelUp(true)

            updateNotification("Connected")
            sendConnected(config.clientAddress)
            Log.i(TAG, "WireGuard tunnel is UP")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start WireGuard tunnel", e)
            WireGuardManager.clear()
            sendError(e.message ?: "Failed to start VPN tunnel")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun handleStop() {
        try {
            if (WireGuardManager.isTunnelUp()) {
                backend.setState(tunnel, Tunnel.State.DOWN, null)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to stop tunnel cleanly", e)
        } finally {
            WireGuardManager.clear()
            sendDisconnected()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun buildWireGuardConfig(dto: WireGuardConfigDto): Config {
        val privateKey = requireNotNull(dto.clientPrivateKey) { "Missing client private key" }
        val clientAddress = requireNotNull(dto.clientAddress) { "Missing client address" }
        val serverPublicKey = requireNotNull(dto.serverPublicKey) { "Missing server public key" }
        val endpointHost = requireNotNull(dto.endpointHost) { "Missing endpoint host" }
        val endpointPort = requireNotNull(dto.endpointPort) { "Missing endpoint port" }

        val interfaceBuilder = Interface.Builder()
            .parsePrivateKey(privateKey)

        interfaceBuilder.addAddress(InetNetwork.parse(clientAddress))

        dto.dnsServers.orEmpty().forEach { dns ->
            interfaceBuilder.addDnsServer(InetAddress.getByName(dns))
        }

        val peerBuilder = Peer.Builder()
            .parsePublicKey(serverPublicKey)
            .parseEndpoint(InetEndpoint.parse("$endpointHost:$endpointPort") as String)

        dto.allowedIps.orEmpty().forEach { allowedIp ->
            peerBuilder.addAllowedIp(InetNetwork.parse(allowedIp))
        }

        val keepAlive = dto.persistentKeepalive ?: 0
        if (keepAlive > 0) {
            peerBuilder.setPersistentKeepalive(keepAlive)
        }

        return Config.Builder()
            .setInterface(interfaceBuilder.build())
            .addPeer(peerBuilder.build())
            .build()
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, Constants.VPN_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("SecureVPN")
            .setContentText(text)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(Constants.VPN_NOTIFICATION_ID, buildNotification(text))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                Constants.VPN_NOTIFICATION_CHANNEL_ID,
                Constants.VPN_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendConnected(displayIp: String?) {
        sendBroadcast(Intent(ACTION_TUNNEL_CONNECTED).apply {
            setPackage(packageName)
            putExtra(EXTRA_DISPLAY_IP, displayIp)
        })
    }

    private fun sendDisconnected() {
        sendBroadcast(Intent(ACTION_TUNNEL_DISCONNECTED).apply {
            setPackage(packageName)
        })
    }

    private fun sendError(message: String) {
        sendBroadcast(Intent(ACTION_TUNNEL_ERROR).apply {
            setPackage(packageName)
            putExtra(EXTRA_ERROR_MESSAGE, message)
        })
    }

    override fun onDestroy() {
        WireGuardManager.clear()
        super.onDestroy()
    }

    private class AppTunnel(
        private val name: String
    ) : Tunnel {

        @Volatile
        private var state: Tunnel.State = Tunnel.State.DOWN

        override fun getName(): String = name

        override fun onStateChange(newState: Tunnel.State) {
            state = newState
        }
    }
}