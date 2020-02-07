package com.rakuten.tech.mobile.testapp.ui.miniapplist

import android.webkit.WebView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException

class MiniAppListViewModel constructor(
    private val miniapp: MiniApp
) : ViewModel() {

    constructor() : this(MiniApp.instance())

    private val _miniAppListData =
        MutableLiveData<List<MiniAppInfo>>().apply { value = getDummyMiniAppList() }

    private val _errorData = MutableLiveData<String>()

    private val _miniAppView = MutableLiveData<WebView>()

    val miniAppListData: LiveData<List<MiniAppInfo>>
        get() = _miniAppListData

    val errorData: LiveData<String>
        get() = _errorData

    val miniAppView: LiveData<WebView>
        get() = _miniAppView

    //for brevity
    suspend fun getMiniAppList() {
        try {
            val miniAppsList = miniapp.listMiniApp()
            _miniAppListData.postValue(miniAppsList)
        } catch (error: MiniAppSdkException) {
            _errorData.postValue((error.message))
        }
    }

    fun getDummyMiniAppList() = listOf(
        MiniAppInfo(
            id = "c67f32b6-987d-405d-978f-6d8118b336ea",
            name = "Lookbook",
            description = "Description goes here",
            icon = "",
            versionId = "4b567725-7f3a-4374-b633-af764c7a1d2b",
            files = mutableListOf()
        ),
        MiniAppInfo(
            id = "287832fe-e802-43c8-a641-933670024953",
            name = "Panda Park",
            description = "Description goes here",
            icon = "",
            versionId = "51c70666-bbdc-4ce2-9924-aac3c48b0987",
            files = mutableListOf()
        ),
        MiniAppInfo(
            id = "0d207c56-6cbf-44ba-b550-64266869b83f",
            name = "Mixed Juice",
            description = "Description goes here",
            icon = "",
            versionId = "0d207c56-6cbf-44ba-b550-64266869b83f",
            files = mutableListOf()
        )
    )

}
