package com.rakuten.tech.mobile.testapp.ui.miniapptabs.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.core.app.ActivityCompat.invalidateOptionsMenu
import androidx.databinding.DataBindingUtil
import com.rakuten.tech.mobile.miniapp.testapp.BuildConfig
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.SettingsFragmentBinding
import com.rakuten.tech.mobile.testapp.BuildVariant
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

class SettingsFragment : BaseFragment() {

    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""
    private lateinit var binding: SettingsFragmentBinding
    private lateinit var settings: AppSettings
    private lateinit var settingsProgressDialog: SettingsProgressDialog
    private var isTab1Checked = true
    private val settingsTextWatcher = object : TextWatcher {
        private var old_text = ""
        override fun afterTextChanged(s: Editable?) {}

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
            if(isTab1Checked){
                binding.toggleList2.isEnabled = new
            } else {
                binding.toggleList1.isEnabled = new
            }
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

        val tab1CredentialData: MiniAppCredentialData
        val tab2CredentialData: MiniAppCredentialData

        if (isTab1Checked) {
            tab1CredentialData = getCurrentTypedCredentialData()
            tab2CredentialData = settings.getCurrentTab2CredentialData()
        } else {
            tab1CredentialData = settings.getCurrentTab1CredentialData()
            tab2CredentialData = getCurrentTypedCredentialData()
        }

        saveCredentialData(
            tab1CredentialData,
            tab2CredentialData
        )

        updateSettings()
    }



    private fun updateSettings() {
        settings.urlParameters = binding.editParametersUrl.text.toString()
        settings.isDisplayInputPreviewMode = binding.switchPreviewMode.isChecked

        launch {
            URL("https://www.test-param.com?${binding.editParametersUrl.text.toString()}").toURI()
            settings.isSettingSaved = true
            requireActivity().runOnUiThread {
                if (requireActivity().isAvailable) {
                    settingsProgressDialog.cancel()
                }
                validateInputIDs()
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

        saveViewEnabled = !(isInputEmpty(binding.editProjectId)
                || isInputEmpty(binding.editSubscriptionKey)
                || isAppIdInvalid) && inputChanged

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

        if(BuildConfig.BUILD_TYPE == BuildVariant.RELEASE.value && !AppSettings.instance.isSettingSaved){
            switchProdVersion.isChecked = true
        }

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

        binding.switchProdVersion.setOnCheckedChangeListener { _, isChecked ->
            if(isTab1Checked){
                settings.setTempTab1IsProduction(isChecked)
            } else {
                settings.setTempTab2IsProduction(isChecked)
            }
            updateTabProjectIdAndSubscription()
        }

        binding.toggleList1.setOnClickListener {
            if(isTab1Checked) return@setOnClickListener
            isTab1Checked = true
            settings.setTempTab2CredentialData(getCurrentTypedCredentialData())
            updateTabProjectIdAndSubscription()
        }

        binding.toggleList2.setOnClickListener {
            if(!isTab1Checked) return@setOnClickListener
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
            binding.editProjectId.text.toString(),
            binding.editSubscriptionKey.text.toString()
        )
    }

    private fun saveCredentialData(
        tab1CredentialData: MiniAppCredentialData,
        tab2CredentialData: MiniAppCredentialData
    ) {
        settings.saveTab1CredentialData(tab1CredentialData)
        settings.saveTab2CredentialData(tab2CredentialData)
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
        binding.switchSignatureVerification.isChecked = credentialData.requireSignatureVerification
        binding.editProjectId.setText(credentialData.projectId)
        binding.editSubscriptionKey.setText(credentialData.subscriptionId)
    }

    private fun createBuildInfo(): String {
        val sdkVersion = getString(R.string.miniapp_sdk_version)
        val buildVersion = getString(R.string.build_version)
        return "Build $sdkVersion - $buildVersion"
    }
}
