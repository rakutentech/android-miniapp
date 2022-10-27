package com.rakuten.tech.mobile.testapp.ui.miniapptabs.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.core.app.ActivityCompat.invalidateOptionsMenu
import androidx.databinding.DataBindingUtil
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.SettingsFragmentBinding
import com.rakuten.tech.mobile.testapp.helper.hideSoftKeyboard
import com.rakuten.tech.mobile.testapp.helper.isAvailable
import com.rakuten.tech.mobile.testapp.helper.isInputEmpty
import com.rakuten.tech.mobile.testapp.helper.isInvalidUuid
import com.rakuten.tech.mobile.testapp.ui.base.BaseFragment
import com.rakuten.tech.mobile.testapp.ui.deeplink.DynamicDeepLinkActivity
import com.rakuten.tech.mobile.testapp.ui.permission.MiniAppDownloadedListActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import com.rakuten.tech.mobile.testapp.ui.settings.MiniAppCredentialData
import com.rakuten.tech.mobile.testapp.ui.settings.SettingsProgressDialog
import com.rakuten.tech.mobile.testapp.ui.userdata.*
import kotlinx.android.synthetic.main.settings_fragment.*
import kotlinx.coroutines.launch
import java.net.URL
import kotlin.properties.Delegates


@Suppress("TooManyFunctions")
class SettingsFragment : BaseFragment() {

    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""
    private lateinit var binding: SettingsFragmentBinding
    private lateinit var settings: AppSettings
    private lateinit var settingsProgressDialog: SettingsProgressDialog
    private var isTab1Checked = true
    private val settingsTextWatcher = object : TextWatcher {
        private var old_text = ""
        override fun afterTextChanged(s: Editable?) {
            //empty function intended
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            old_text = binding.editProjectId.text.toString()
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            validateInputIDs(old_text != s.toString())
        }
    }
    private var saveViewEnabled by Delegates.observable(true) { _, old, new ->
        if (new != old) {
            invalidateOptionsMenu(requireActivity())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_menu, menu)
        menu.findItem(R.id.settings_menu_save).isEnabled = saveViewEnabled
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return when (item.itemId) {
            R.id.settings_menu_save -> {
                onSaveAction()
                item.isEnabled = false
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        // enable the save button first time.
        validateInputIDs(true)
    }

    private fun onSaveAction() {
        if (requireActivity().isAvailable) {
            settingsProgressDialog.show()
        }

        val currentCredentialData = getCurrentTypedCredentialData()

        if (isTab1Checked) {
            settings.setTempTab1CredentialData(currentCredentialData)
        } else {
            settings.setTempTab2CredentialData(currentCredentialData)
        }

        settings.saveData()
        updateSettings()
    }

    private fun updateSettings() {
        settings.urlParameters = binding.editParametersUrl.text.toString()
        settings.isDisplayInputPreviewMode = binding.switchPreviewMode.isChecked

        launch {
            URL("https://www.test-param.com?${binding.editParametersUrl.text.toString()}").toURI()
            settings.isSettingSaved = true
            with(requireActivity()) {
                currentFocus?.let {
                    hideSoftKeyboard(it)
                }
                runOnUiThread {
                    if (isAvailable) {
                        settingsProgressDialog.cancel()
                    }
                    validateInputIDs()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        settings = AppSettings.instance
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.settings_fragment,
            container,
            false
        )
        settingsProgressDialog = SettingsProgressDialog(requireActivity())
        renderAppSettingsScreen()
        return binding.root
    }

    private fun validateInputIDs(inputChanged: Boolean = false) {
        val isAppIdInvalid = !isInputIDValid()
        val isInputEmpty = !(isInputEmpty(binding.editProjectId)
                || isInputEmpty(binding.editSubscriptionKey))
        val isToggleListEnabled = isInputEmpty
                || isAppIdInvalid

        if (isTab1Checked) {
            binding.toggleList2.isEnabled = isToggleListEnabled
        } else {
            binding.toggleList1.isEnabled = isToggleListEnabled
        }

        saveViewEnabled = isToggleListEnabled && inputChanged

        if (isInputEmpty(binding.editProjectId) || isAppIdInvalid)
            binding.inputProjectId.error = getString(R.string.error_invalid_input)
        else binding.inputProjectId.error = null

        if (isInputEmpty(binding.editSubscriptionKey))
            binding.inputSubscriptionKey.error = getString(R.string.error_invalid_input)
        else binding.inputSubscriptionKey.error = null
    }

    private fun isInputIDValid(): Boolean {
        return !binding.editProjectId.text.toString().isInvalidUuid()
    }

    private fun renderAppSettingsScreen() {
        binding.textInfo.text = createBuildInfo()
        binding.editParametersUrl.setText(settings.urlParameters)
        binding.switchPreviewMode.isChecked = settings.isDisplayInputPreviewMode


        val defaultCredentialData = settings.getDefaultCredentialData()

        setupCredentialDataToView(defaultCredentialData)

        binding.editProjectId.addTextChangedListener(settingsTextWatcher)
        binding.editSubscriptionKey.addTextChangedListener(settingsTextWatcher)

        binding.buttonProfile.setOnClickListener {
            ProfileSettingsActivity.start(requireActivity())
        }

        binding.buttonContacts.setOnClickListener {
            ContactListActivity.start(requireActivity())
        }

        binding.buttonCustomPermissions.setOnClickListener {
            MiniAppDownloadedListActivity.start(requireActivity())
        }

        binding.buttonAccessToken.setOnClickListener {
            AccessTokenActivity.start(requireActivity())
        }

        binding.buttonPoints.setOnClickListener {
            PointsActivity.start(requireActivity())
        }

        binding.buttonDeeplink.setOnClickListener {
            DynamicDeepLinkActivity.start(requireActivity())
        }

        binding.buttonQA.setOnClickListener {
            QASettingsActivity.start(requireActivity())
        }

        binding.switchPreviewModeTab.setOnCheckedChangeListener { _, isChecked ->
            if (isTab1Checked) {
                settings.setTempTab1IsPreviewMode(isChecked)
            } else {
                settings.setTempTab2IsPreviewMode(isChecked)
            }
            binding.switchPreviewModeTab.isChecked = isChecked
            validateInputIDs(true)
        }

        binding.switchProdVersion.setOnCheckedChangeListener { _, isChecked ->
            if (isTab1Checked) {
                settings.setTempTab1IsProduction(isChecked)
            } else {
                settings.setTempTab2IsProduction(isChecked)
            }
            binding.switchProdVersion.isChecked = isChecked
            validateInputIDs(true)
        }

        binding.switchSignatureVerification.setOnCheckedChangeListener { _, isChecked ->
            if (isTab1Checked) {
                settings.setTempTab1IsVerificationRequired(isChecked)
            } else {
                settings.setTempTab2IsVerificationRequired(isChecked)
            }
            binding.switchSignatureVerification.isChecked = isChecked
            validateInputIDs(true)
        }

        binding.toggleList1.setOnClickListener {
            if (isTab1Checked) {
                binding.toggleList1.isChecked = true
                return@setOnClickListener
            }
            isTab1Checked = true
            settings.setTempTab2CredentialData(getCurrentTypedCredentialData())
            updateTabProjectIdAndSubscription()
        }

        binding.toggleList2.setOnClickListener {
            if (!isTab1Checked) {
                binding.toggleList2.isChecked = true
                return@setOnClickListener
            }
            isTab1Checked = false
            settings.setTempTab1CredentialData(getCurrentTypedCredentialData())
            updateTabProjectIdAndSubscription()
        }
    }

    fun getCurrentTypedCredentialData(): MiniAppCredentialData {
        return MiniAppCredentialData(
            binding.switchProdVersion.isChecked,
            binding.switchSignatureVerification.isChecked,
            binding.switchPreviewModeTab.isChecked,
            binding.editProjectId.text.toString().trim(),
            binding.editSubscriptionKey.text.toString().trim()
        )
    }


    private fun updateTabProjectIdAndSubscription() {
        val credentialData: MiniAppCredentialData = if (isTab1Checked) {
            settings.getCurrentTab1CredentialData()
        } else {
            settings.getCurrentTab2CredentialData()
        }
        setupCredentialDataToView(credentialData)
    }

    private fun setupCredentialDataToView(credentialData: MiniAppCredentialData) {
        binding.switchProdVersion.isChecked = credentialData.isProduction
        binding.switchPreviewModeTab.isChecked = credentialData.isPreviewMode
        binding.switchSignatureVerification.isChecked = credentialData.isVerificationRequired
        binding.editProjectId.setText(credentialData.projectId)
        binding.editSubscriptionKey.setText(credentialData.subscriptionId)
    }

    private fun createBuildInfo(): String {
        val sdkVersion = getString(R.string.miniapp_sdk_version)
        val buildVersion = getString(R.string.build_version)
        return "Build $sdkVersion - $buildVersion"
    }
}
