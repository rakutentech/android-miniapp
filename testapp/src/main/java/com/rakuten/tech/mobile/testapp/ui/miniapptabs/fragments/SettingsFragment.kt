package com.rakuten.tech.mobile.testapp.ui.miniapptabs.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.core.app.ActivityCompat.invalidateOptionsMenu
import androidx.databinding.DataBindingUtil
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.MiniAppTooManyRequestsError
import com.rakuten.tech.mobile.miniapp.testapp.BuildConfig
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.SettingsFragmentBinding
import com.rakuten.tech.mobile.testapp.BuildVariant
import com.rakuten.tech.mobile.testapp.helper.isAvailable
import com.rakuten.tech.mobile.testapp.helper.isInputEmpty
import com.rakuten.tech.mobile.testapp.helper.isInvalidUuid
import com.rakuten.tech.mobile.testapp.helper.showAlertDialog
import com.rakuten.tech.mobile.testapp.ui.base.BaseFragment
import com.rakuten.tech.mobile.testapp.ui.deeplink.DynamicDeepLinkActivity
import com.rakuten.tech.mobile.testapp.ui.permission.MiniAppDownloadedListActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
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

    private fun onSaveAction() {
        if (requireActivity().isAvailable) {
            settingsProgressDialog.show()
        }

        val tab1ProjectIdSubscriptionKeyPair: Pair<String, String>
        val tab2ProjectIdSubscriptionKeyPair: Pair<String, String>

        if (!isTab1Checked) {
            tab1ProjectIdSubscriptionKeyPair = getTypedSubscriptionKeyProjectIdPair()
            tab2ProjectIdSubscriptionKeyPair = settings.getCurrentTab2ProjectIdSubscriptionKeyPair(binding.switchProdVersion.isChecked)
        } else {
            tab1ProjectIdSubscriptionKeyPair = settings.getCurrentTab1ProjectIdSubscriptionKeyPair(binding.switchProdVersion.isChecked)
            tab2ProjectIdSubscriptionKeyPair = getTypedSubscriptionKeyProjectIdPair()
        }

        updateSetupIdSubscriptionData(
            tab1ProjectIdSubscriptionKeyPair,
            tab2ProjectIdSubscriptionKeyPair
        )

        updateSettings(
            binding.editProjectId.text.toString(),
            binding.editSubscriptionKey.text.toString(),
            binding.editParametersUrl.text.toString(),
            binding.switchPreviewMode.isChecked,
            binding.switchSignatureVerification.isChecked,
            binding.switchProdVersion.isChecked
        )
    }



    private fun updateSettings(
        projectId: String,
        subscriptionKey: String,
        urlParameters: String,
        isPreviewMode: Boolean,
        requireSignatureVerification: Boolean,
        isProdVersionEnabled: Boolean
    ) {
        val appIdHolder = settings.projectId
        val subscriptionKeyHolder = settings.subscriptionKey
        val urlParametersHolder = settings.urlParameters
        val isPreviewModeHolder = settings.isPreviewMode
        val requireSignatureVerificationHolder = settings.requireSignatureVerification
        if (toggleList1.isChecked) {
            settings.projectId = projectId
            settings.subscriptionKey = subscriptionKey
        } else {
            settings.projectId2 = projectId
            settings.subscriptionKey2 = subscriptionKey
        }
        settings.urlParameters = urlParameters
        settings.isPreviewMode = isPreviewMode
        settings.requireSignatureVerification = requireSignatureVerification
        settings.isProdVersionEnabled = isProdVersionEnabled

        launch {
            try {
                URL("https://www.test-param.com?$urlParameters").toURI()

                settings.isSettingSaved = true
                requireActivity().runOnUiThread {
                    if (requireActivity().isAvailable) {
                        settingsProgressDialog.cancel()
                    }
                    validateInputIDs()
                }
            } catch (error: MiniAppTooManyRequestsError) {
                onUpdateError(
                    appIdHolder,
                    subscriptionKeyHolder,
                    urlParametersHolder,
                    isPreviewModeHolder,
                    requireSignatureVerificationHolder,
                    "Error",
                    getString(R.string.error_desc_miniapp_too_many_request)
                )
            } catch (error: MiniAppSdkException) {
                onUpdateError(
                    appIdHolder,
                    subscriptionKeyHolder,
                    urlParametersHolder,
                    isPreviewModeHolder,
                    requireSignatureVerificationHolder,
                    "MiniApp SDK",
                    error.message.toString()
                )
            } catch (error: Exception) {
                onUpdateError(
                    appIdHolder,
                    subscriptionKeyHolder,
                    urlParametersHolder,
                    isPreviewModeHolder,
                    requireSignatureVerificationHolder,
                    "URL parameter",
                    error.message.toString()
                )
            }
        }
    }

    private fun onUpdateError(
        appIdHolder: String,
        subscriptionKeyHolder: String,
        urlParametersHolder: String,
        isPreviewModeHolder: Boolean,
        requireSignatureVerificationHolder: Boolean,
        errTitle: String,
        errMsg: String
    ) {
        settings.projectId = appIdHolder
        settings.subscriptionKey = subscriptionKeyHolder
        settings.urlParameters = urlParametersHolder
        settings.isPreviewMode = isPreviewModeHolder
        settings.requireSignatureVerification = requireSignatureVerificationHolder
        requireActivity().runOnUiThread {
            if (requireActivity().isAvailable) {
                settingsProgressDialog.cancel()
            }
            showAlertDialog(requireActivity(), errTitle, errMsg)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        binding.switchPreviewMode.isChecked = settings.isPreviewMode
        binding.switchSignatureVerification.isChecked = settings.requireSignatureVerification
        binding.switchProdVersion.isChecked = settings.isProdVersionEnabled
        if(BuildConfig.BUILD_TYPE == BuildVariant.RELEASE.value && !AppSettings.instance.isSettingSaved){
            switchProdVersion.isChecked = true
        }

        val projectIdSubscriptionKeyPair = settings.getDefaultProjectIdSubscriptionKeyPair()
        setUpIdSubscriptionView(projectIdSubscriptionKeyPair)

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
            if (isChecked) {
                settings.baseUrl = getString(R.string.prodBaseUrl)
            } else {
                settings.baseUrl = getString(R.string.stagingBaseUrl)
            }
            updateProjectIdAndSubscription()
        }

        binding.toggleList1.setOnClickListener {
            if(isTab1Checked) return@setOnClickListener
            isTab1Checked = true
            settings.setTab2CredentialData(getTypedSubscriptionKeyProjectIdPair())
            updateProjectIdAndSubscription()
        }

        binding.toggleList2.setOnClickListener {
            if(!isTab1Checked) return@setOnClickListener
            isTab1Checked = false
            settings.setTab1CredentialData(getTypedSubscriptionKeyProjectIdPair())
            updateProjectIdAndSubscription()
        }

        // enable the save button first time.
        validateInputIDs(true)
    }

    fun getTypedSubscriptionKeyProjectIdPair(): Pair<String, String> {
        return Pair(
            binding.editProjectId.text.toString(),
            binding.editSubscriptionKey.text.toString()
        )
    }

    private fun updateSetupIdSubscriptionData(
        tab1ProjectIdSubscriptionKeyPair: Pair<String, String>,
        tab2ProjectIdSubscriptionKeyPair: Pair<String, String>,
    ) {
        settings.projectId = tab1ProjectIdSubscriptionKeyPair.first
        settings.subscriptionKey = tab1ProjectIdSubscriptionKeyPair.second
        settings.projectId2 = tab2ProjectIdSubscriptionKeyPair.first
        settings.subscriptionKey2 = tab2ProjectIdSubscriptionKeyPair.second
    }

    private fun updateProjectIdAndSubscription() {
        val projectIdSubscriptionKeyPair: Pair<String, String> = if (isTab1Checked) {
            settings.getCurrentTab1ProjectIdSubscriptionKeyPair(
                binding.switchProdVersion.isChecked
            )
        } else {
            settings.getCurrentTab2ProjectIdSubscriptionKeyPair(
                binding.switchProdVersion.isChecked
            )
        }
        setUpIdSubscriptionView(projectIdSubscriptionKeyPair)
    }

    private fun setUpIdSubscriptionView(projectIdAndSubscriptionKeyPair: Pair<String, String>) {
        binding.editProjectId.setText(projectIdAndSubscriptionKeyPair.first)
        binding.editSubscriptionKey.setText(projectIdAndSubscriptionKeyPair.second)
    }

    private fun setUpIdSubscriptionView(projectId: String, subscriptionKey: String){
        binding.editProjectId.setText(projectId)
        binding.editSubscriptionKey.setText(subscriptionKey)
    }

    private fun createBuildInfo(): String {
        val sdkVersion = getString(R.string.miniapp_sdk_version)
        val buildVersion = getString(R.string.build_version)
        return "Build $sdkVersion - $buildVersion"
    }
}
