package com.securevpn.app.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.securevpn.app.R
import com.securevpn.app.databinding.FragmentSettingsBinding
import com.securevpn.app.ui.about.AboutActivity
import com.securevpn.app.ui.privacy.PrivacyPolicyActivity
import com.securevpn.app.utils.Constants

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRowTitles()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRowTitles() {
        binding.itemPrivacyPolicy.tvSettingsTitle.text =
            getString(R.string.settings_privacy_policy)

        binding.itemTerms.tvSettingsTitle.text =
            getString(R.string.settings_terms)

        binding.itemShareApp.tvSettingsTitle.text =
            getString(R.string.settings_share_app)

        binding.itemRateUs.tvSettingsTitle.text =
            getString(R.string.settings_rate_us)

        binding.itemContactUs.tvSettingsTitle.text =
            getString(R.string.settings_contact_us)

        binding.itemAbout.tvSettingsTitle.text =
            getString(R.string.settings_about)
    }

    private fun setupClickListeners() {
        binding.switchAutoConnect.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAutoConnect(isChecked)
        }

        binding.itemPrivacyPolicy.root.setOnClickListener {
            startActivity(Intent(requireContext(), PrivacyPolicyActivity::class.java))
        }

        binding.itemTerms.root.setOnClickListener {
            openUrl(Constants.TERMS_URL)
        }

        binding.itemShareApp.root.setOnClickListener {
            shareApp()
        }

        binding.itemRateUs.root.setOnClickListener {
            openUrl(Constants.PLAY_STORE_URL)
        }

        binding.itemContactUs.root.setOnClickListener {
            openEmail()
        }

        binding.itemAbout.root.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }
    }

    private fun observeViewModel() {
        binding.tvVersionValue.text = viewModel.appVersion

        viewModel.autoConnect.observe(viewLifecycleOwner) { enabled ->
            binding.switchAutoConnect.isChecked = enabled
        }
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_body))
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.settings_share_app)))
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Exception) {
        }
    }

    private fun openEmail() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${Constants.SUPPORT_EMAIL}")
            putExtra(Intent.EXTRA_SUBJECT, "SecureVPN Support")
        }
        try {
            startActivity(emailIntent)
        } catch (_: Exception) {
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}