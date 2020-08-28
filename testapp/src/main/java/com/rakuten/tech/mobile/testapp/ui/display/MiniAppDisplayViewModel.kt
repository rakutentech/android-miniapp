package com.rakuten.tech.mobile.testapp.ui.display

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ListCustomPermissionBinding
import com.rakuten.tech.mobile.testapp.ui.permission.CustomPermissionAdapter
import com.rakuten.tech.mobile.testapp.ui.permission.CustomPermissionDialog
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MiniAppDisplayViewModel constructor(
    private val miniapp: MiniApp
) : ViewModel() {

    constructor() : this(MiniApp.instance(AppSettings.instance.miniAppSettings))

    private lateinit var miniAppDisplay: MiniAppDisplay
    private var hostLifeCycle: Lifecycle? = null

    private val _miniAppView = MutableLiveData<View>()
    private val _errorData = MutableLiveData<String>()
    private val _isLoading = MutableLiveData<Boolean>()

    val miniAppView: LiveData<View>
        get() = _miniAppView
    val errorData: LiveData<String>
        get() = _errorData
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    fun obtainMiniAppDisplay(
        context: Context,
        appId: String,
        miniAppMessageBridge: MiniAppMessageBridge) = viewModelScope.launch(Dispatchers.IO) {
        try {
            _isLoading.postValue(true)
            miniAppDisplay = miniapp.create(appId, miniAppMessageBridge)
            hostLifeCycle?.addObserver(miniAppDisplay)
            _miniAppView.postValue(miniAppDisplay.getMiniAppView(context))
        } catch (e: MiniAppSdkException) {
            e.printStackTrace()
            _errorData.postValue(e.message)
        } finally {
            _isLoading.postValue(false)
        }
    }

    fun setHostLifeCycle(lifecycle: Lifecycle) {
        this.hostLifeCycle = lifecycle
    }

    fun promptCustomPermission(
        context: Context,
        miniAppInfo: MiniAppInfo,
        permissions: List<Pair<MiniAppCustomPermissionType, String>>,
        callback: (grantResult: String) -> Unit
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val permissionLayout = ListCustomPermissionBinding.inflate(layoutInflater, null, false)
        permissionLayout.listCustomPermission.layoutManager = LinearLayoutManager(context)
        val adapter = CustomPermissionAdapter()

        permissionLayout.listCustomPermission.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )

        // prepare adapter for showing items
        val namesForAdapter: ArrayList<MiniAppCustomPermissionType> = arrayListOf()
        val resultsForAdapter: ArrayList<MiniAppCustomPermissionResult> = arrayListOf()
        val descriptionForAdapter: ArrayList<String> = arrayListOf()

        permissions.forEach {
            namesForAdapter.add(it.first)
            descriptionForAdapter.add(it.second)
        }

        val cachedList = miniapp.getCustomPermissions(miniAppInfo.id).pairValues
        val filteredPair =
            mutableListOf<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()
        permissions.forEach { (first) ->
            filteredPair.addAll(cachedList.filter {
                first.type == it.first.type
            })
        }
        filteredPair.forEach {
            resultsForAdapter.add(it.second)
        }

        adapter.addPermissionList(namesForAdapter, resultsForAdapter, descriptionForAdapter)
        permissionLayout.listCustomPermission.adapter = adapter

        val objToSend = MiniAppCustomPermission(miniAppInfo.id, adapter.permissionPairs)

        val listener = DialogInterface.OnClickListener { _, _ ->
            val response = miniapp.setCustomPermissions(
                objToSend
            )
            callback.invoke(response)
        }

        val permissionDialogBuilder =
            CustomPermissionDialog.Builder().build(context, miniAppInfo.displayName).apply {
                setView(permissionLayout.root)
                setListener(listener)
            }

        permissionDialogBuilder.show()
    }

    fun canGoBackwards(): Boolean =
        if (::miniAppDisplay.isInitialized)
            miniAppDisplay.navigateBackward()
        else
            false
}
