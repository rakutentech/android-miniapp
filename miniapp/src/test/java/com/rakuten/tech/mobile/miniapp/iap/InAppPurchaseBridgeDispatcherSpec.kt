package com.rakuten.tech.mobile.miniapp.iap

import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.display.WebViewListener
import com.rakuten.tech.mobile.miniapp.js.*
import com.rakuten.tech.mobile.miniapp.js.iap.*
import com.rakuten.tech.mobile.miniapp.permission.MiniAppDevicePermissionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.text.SimpleDateFormat
import java.util.*

class InAppPurchaseBridgeDispatcherSpec : RobolectricBaseSpec() {
    private lateinit var miniAppBridge: MiniAppMessageBridge
    private val callbackObj = CallbackObj(
        action = ActionType.PURCHASE_ITEM.action,
        param = null,
        id = TEST_CALLBACK_ID
    )
    private val webViewListener: WebViewListener = mock()
    private val bridgeExecutor = Mockito.spy(MiniAppBridgeExecutor(webViewListener))
    private val productPrice = com.rakuten.tech.mobile.miniapp.iap.ProductPrice(
        "JPY", "1234"
    )
    private val productInfo = com.rakuten.tech.mobile.miniapp.iap.ProductInfo(
        "itemId", "title", "description", productPrice
    )
    private val purchasedProductInfo = PurchasedProductInfo(
        productInfo, "dummy_transactionId", 0
    )
    private val purchasedProductResponse =
        PurchasedProductResponse(
            PurchasedProductResponseStatus.PURCHASED,
            purchasedProductInfo,
            "",
            ""
        )
    private val purchaseJsonStr: String = Gson().toJson(
        CallbackObj(
            action = ActionType.PURCHASE_ITEM.action,
            param = PurchasedProductCallbackObj.ProductItem("itemId"),
            id = TEST_CALLBACK_ID
        )
    )
    private val apiClient: ApiClient = mock()
    private val miniAppIAPVerifier: MiniAppIAPVerifier = mock()

    private fun createPurchaseRequest() = MiniAppPurchaseRecord(
        platform = InAppPurchaseBridgeDispatcher.PLATFORM,
        productId = purchasedProductResponse.purchasedProductInfo.productInfo.id,
        transactionState = TransactionState.PURCHASED.state,
        transactionId = purchasedProductResponse.purchasedProductInfo.transactionId,
        transactionDate = formatTransactionDate(purchasedProductResponse.purchasedProductInfo.transactionDate),
        transactionReceipt = purchasedProductResponse.transactionReceipt,
        purchaseToken = purchasedProductResponse.purchaseToken
    )

    private fun createMiniAppPurchaseResponse() = MiniAppPurchaseResponse(
        "",
        "",
        ""
    )

    private fun formatTransactionDate(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        return format.format(date)
    }
    @Before
    fun setup() {
        miniAppBridge = Mockito.spy(createMessageBridge())
        miniAppBridge.apiClient = apiClient
        When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = webViewListener,
            customPermissionCache = mock(),
            downloadedManifestCache = mock(),
            miniAppId = TEST_MA.id,
            ratDispatcher = mock(),
            secureStorageDispatcher = mock(),
            miniAppIAPVerifier
        )
    }

    @Test
    fun `postError should be called when there is no InAppPurchaseBridgeDispatcher`() {
        val errMsg = ErrorBridgeMessage.NO_IMPL
        miniAppBridge.postMessage(Gson().toJson(callbackObj))
        val miniAppBridge = Mockito.spy(createMessageBridge())
        miniAppBridge.postMessage(Gson().toJson(callbackObj))

        verify(bridgeExecutor).postError(callbackObj.id, errMsg)
    }

    @Test(expected = MiniAppSdkException::class)
    fun `postError should be called when there is no purchaseItem implementation`() {
        val provider = Mockito.spy(
            createPurchaseProvider(
                shouldCreate = false,
                canPurchase = false
            )
        )
        miniAppBridge.setInAppPurchaseProvider(provider)
    }

    @Test
    fun `postError should be called when can't purchase item`() {
        val provider = Mockito.spy(
            createPurchaseProvider(
                shouldCreate = true,
                canPurchase = false
            )
        )
        val wrapper = Mockito.spy(createIAPBridgeWrapper(provider))
        When calling miniAppIAPVerifier.getStoreIdByProductId(TEST_MA_ID, "itemId") itReturns ""
        wrapper.onPurchaseItem(callbackObj.id, purchaseJsonStr)
        val errorMsg = "Cannot purchase item: Invalid Product Id."
        verify(bridgeExecutor).postError(
            callbackObj.id,
            errorMsg
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `postValue should be called when purchasing item successfully`() {
        val provider = Mockito.spy(
            createPurchaseProvider(
                shouldCreate = true,
                canPurchase = true
            )
        )
        TestCoroutineScope().launch {
            When calling apiClient.purchaseItem(
                TEST_MA_ID,
                createPurchaseRequest()
            ) itReturns createMiniAppPurchaseResponse()
            val wrapper = Mockito.spy(createIAPBridgeWrapper(provider))
            wrapper.onPurchaseItem(callbackObj.id, purchaseJsonStr)

            verify(bridgeExecutor).postValue(
                callbackObj.id,
                Gson().toJson(createMiniAppPurchaseResponse())
            )
        }
    }

    @Suppress("EmptyFunctionBlock")
    private fun createPurchaseProvider(
        shouldCreate: Boolean,
        canPurchase: Boolean
    ): InAppPurchaseProvider {
        return if (shouldCreate) object :
            InAppPurchaseProvider {
            override fun getAllProducts(
                androidStoreIds: List<String>,
                onSuccess: (productInfos: List<ProductInfo>) -> Unit,
                onError: (message: String) -> Unit
            ) {
            }

            override fun purchaseProductWith(
                androidStoreId: String,
                onSuccess: (purchasedProductResponse: PurchasedProductResponse) -> Unit,
                onError: (message: String) -> Unit
            ) {
                if (canPurchase) onSuccess(purchasedProductResponse)
                else onError("")
            }

            override fun consumePurchaseWIth(
                purhcaseToken: String,
                onSuccess: (title: String, description: String) -> Unit,
                onError: (message: String) -> Unit
            ) {
            }

            override fun onEndConnection() {}
        } else {
            throw MiniAppSdkException("No method has been implemented.")
        }
    }

    private fun createIAPBridgeWrapper(provider: InAppPurchaseProvider): InAppPurchaseBridgeDispatcher {
        val wrapper = InAppPurchaseBridgeDispatcher()
        wrapper.setMiniAppComponents(
            bridgeExecutor,
            TEST_MA.id,
            apiClient,
            miniAppIAPVerifier
        )
        wrapper.setInAppPurchaseProvider(provider)
        return wrapper
    }

    private fun createMessageBridge(): MiniAppMessageBridge =
        object : MiniAppMessageBridge() {

            override fun getUniqueId(
                onSuccess: (uniqueId: String) -> Unit,
                onError: (message: String) -> Unit
            ) {
                onSuccess(TEST_CALLBACK_VALUE)
            }

            override fun requestDevicePermission(
                miniAppPermissionType: MiniAppDevicePermissionType,
                callback: (isGranted: Boolean) -> Unit
            ) {
                onRequestDevicePermissionsResult(TEST_CALLBACK_ID, false)
            }
        }
}
