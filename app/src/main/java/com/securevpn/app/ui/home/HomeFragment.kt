package com.securevpn.app.ui.home

import android.app.Activity
import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.securevpn.app.R
import com.securevpn.app.data.model.ServerItem
import com.securevpn.app.databinding.FragmentHomeBinding
import com.securevpn.app.service.SecureVpnService
import com.securevpn.app.utils.Constants
import com.securevpn.app.utils.Resource
import com.securevpn.app.utils.hide
import com.securevpn.app.utils.show
import com.securevpn.app.utils.showSnackbar
import com.securevpn.app.utils.toTimerFormat

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()

    private val vpnPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.onVpnPermissionResult(result.resultCode == Activity.RESULT_OK)
        }

    private val tunnelStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                SecureVpnService.ACTION_TUNNEL_CONNECTED -> {
                    val displayIp = intent.getStringExtra(SecureVpnService.EXTRA_DISPLAY_IP)
                    viewModel.markTunnelConnected(displayIp)
                    binding.root.showSnackbar(getString(R.string.status_connected))
                }

                SecureVpnService.ACTION_TUNNEL_DISCONNECTED -> {
                    viewModel.markTunnelDisconnected()
                }

                SecureVpnService.ACTION_TUNNEL_ERROR -> {
                    val errorMessage = intent.getStringExtra(SecureVpnService.EXTRA_ERROR_MESSAGE)
                        ?: "VPN tunnel failed"
                    viewModel.markTunnelDisconnected(errorMessage)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
    }

    override fun onStart() {
        super.onStart()
        registerTunnelReceiver()
    }

    override fun onStop() {
        unregisterTunnelReceiver()
        super.onStop()
    }

    private fun registerTunnelReceiver() {
        val filter = IntentFilter().apply {
            addAction(SecureVpnService.ACTION_TUNNEL_CONNECTED)
            addAction(SecureVpnService.ACTION_TUNNEL_DISCONNECTED)
            addAction(SecureVpnService.ACTION_TUNNEL_ERROR)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(
                tunnelStateReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            requireContext().registerReceiver(tunnelStateReceiver, filter)
        }
    }

    private fun unregisterTunnelReceiver() {
        try {
            requireContext().unregisterReceiver(tunnelStateReceiver)
        } catch (_: Exception) {
        }
    }

    private fun setupClickListeners() {
        binding.btnConnect.setOnClickListener {
            when (viewModel.connectionState.value) {
                Constants.STATE_DISCONNECTED -> viewModel.connect()
                Constants.STATE_CONNECTED -> viewModel.disconnect()
                Constants.STATE_CONNECTING -> Unit
                else -> Unit
            }
        }

        binding.cardSelectedServer.setOnClickListener {
            findNavController().navigate(R.id.serverListFragment)
        }

        binding.tvChangeServer.setOnClickListener {
            findNavController().navigate(R.id.serverListFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.connectionState.observe(viewLifecycleOwner) { state ->
            updateConnectionStateUI(state)
        }

        viewModel.connectResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.flLoading.show()
                }

                is Resource.Success -> {
                    binding.flLoading.hide()
                }

                is Resource.Error -> {
                    binding.flLoading.hide()
                    binding.root.showSnackbar(result.message)
                }
            }
        }

        viewModel.disconnectResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    binding.root.showSnackbar(getString(R.string.status_disconnected))
                }

                is Resource.Error -> {
                    binding.root.showSnackbar(result.message)
                }

                else -> Unit
            }
        }

        viewModel.selectedServer.observe(viewLifecycleOwner) { server ->
            server?.let { updateServerCard(it) }
        }

        viewModel.currentIp.observe(viewLifecycleOwner) { ip ->
            binding.tvCurrentIp.text = ip
        }

        viewModel.sessionSeconds.observe(viewLifecycleOwner) { seconds ->
            binding.tvSessionTimer.text = seconds.toTimerFormat()
        }

        viewModel.vpnPermissionIntent.observe(viewLifecycleOwner) { intent ->
            if (intent != null) {
                vpnPermissionLauncher.launch(intent)
                viewModel.clearVpnPermissionIntent()
            }
        }
    }

    private fun updateConnectionStateUI(state: String) {
        when (state) {
            Constants.STATE_DISCONNECTED -> {
                binding.tvConnectionStatus.text = getString(R.string.status_disconnected)
                binding.tvBtnLabel.text = getString(R.string.btn_connect)

                binding.vStatusDot.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.colorDisconnected)
                )

                binding.llStatusBanner.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_status_disconnected
                )

                binding.btnConnect.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.connect_button_bg
                )

                stopPulseAnimation()
            }

            Constants.STATE_CONNECTING -> {
                binding.tvConnectionStatus.text = getString(R.string.status_connecting)
                binding.tvBtnLabel.text = getString(R.string.btn_connecting)

                binding.vStatusDot.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.colorConnecting)
                )

                binding.llStatusBanner.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_status_connecting
                )

                binding.btnConnect.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.connect_button_bg
                )

                startPulseAnimation()
            }

            Constants.STATE_CONNECTED -> {
                binding.tvConnectionStatus.text = getString(R.string.status_connected)
                binding.tvBtnLabel.text = getString(R.string.btn_disconnect)

                binding.vStatusDot.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.colorConnected)
                )

                binding.llStatusBanner.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_status_connected
                )

                binding.btnConnect.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.disconnect_button_bg
                )

                stopPulseAnimation()
                animateButtonBounce()
            }
        }
    }

    private fun updateServerCard(server: ServerItem) {
        binding.tvServerFlag.text = server.flag
        binding.tvServerCountry.text = server.country
        binding.tvServerCity.text = server.city
        binding.tvServerPing.text = getString(R.string.label_ping_ms, server.ping)

        val pingColor = when {
            server.ping < 60 -> R.color.colorConnected
            server.ping < 100 -> R.color.colorConnecting
            else -> R.color.colorDisconnected
        }

        binding.tvServerPing.setTextColor(
            ContextCompat.getColor(requireContext(), pingColor)
        )
    }

    private var pulseAnimator: ObjectAnimator? = null

    private fun startPulseAnimation() {
        if (pulseAnimator?.isRunning == true) return

        pulseAnimator = ObjectAnimator.ofFloat(binding.ivButtonRing, "alpha", 0.2f, 0.8f).apply {
            duration = 800
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            start()
        }
    }

    private fun stopPulseAnimation() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        binding.ivButtonRing.alpha = 0.6f
    }

    private fun animateButtonBounce() {
        binding.btnConnect.animate()
            .scaleX(1.08f)
            .scaleY(1.08f)
            .setDuration(150)
            .withEndAction {
                binding.btnConnect.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .setInterpolator(OvershootInterpolator())
                    .start()
            }
            .start()
    }

    override fun onDestroyView() {
        pulseAnimator?.cancel()
        _binding = null
        super.onDestroyView()
    }
}