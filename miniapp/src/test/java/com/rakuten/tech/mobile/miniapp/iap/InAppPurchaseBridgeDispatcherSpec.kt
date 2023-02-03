package com.rakuten.tech.mobile.miniapp.iap

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.display.WebViewListener
import com.rakuten.tech.mobile.miniapp.js.*
import com.rakuten.tech.mobile.miniapp.permission.MiniAppDevicePermissionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import java.text.SimpleDateFormat
import java.util.*

@RunWith(RobolectricTestRunner::class)
class InAppPurchaseBridgeDispatcherSpec {
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
    private val product = com.rakuten.tech.mobile.miniapp.iap.Product(
        "itemId", "title", "description", productPrice
    )
    private val purchasedProduct = PurchasedProduct(
        product, "dummy_transactionId", "YYYY-MM-DD", "", 0
    )
    private val purchasedProductResponse =
        PurchasedProductResponse(
            PurchasedProductResponseStatus.PURCHASED,
            purchasedProduct
        )
    private val purchaseJsonStr: String = Gson().toJson(
        CallbackObj(
            action = ActionType.PURCHASE_ITEM.action,
            param = PurchasedProductCallbackObj.ProductItem("itemId"),
            id = TEST_CALLBACK_ID
        )
    )
    private val apiClient: ApiClient = mock()

    private fun createPurchaseRequest() =  MiniAppPurchaseRequest(
        platform = InAppPurchaseBridgeDispatcher.PLATFORM,
        productId = purchasedProductResponse.purchasedProduct.product.id,
        transactionState = TransactionState.PURCHASED.state,
        transactionId = purchasedProductResponse.purchasedProduct.transactionId,
        transactionDate = formatTransactionDate(purchasedProductResponse.purchasedProduct.transactionDate),
        transactionReceipt = purchasedProductResponse.purchasedProduct.transactionReceipt,
        purchaseToken = purchasedProductResponse.purchasedProduct.purchaseToken
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
            secureStorageDispatcher = mock()
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
        wrapper.onPurchaseItem(callbackObj.id, purchaseJsonStr)

        verify(bridgeExecutor).postError(
            callbackObj.id,
            InAppPurchaseBridgeDispatcher.ERR_IN_APP_PURCHASE + " "
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
        runBlockingTest {
            When calling apiClient.purchaseItem(TEST_MA_ID, createPurchaseRequest()) itReturns createMiniAppPurchaseResponse()
            val wrapper = Mockito.spy(createIAPBridgeWrapper(provider))
            wrapper.onPurchaseItem(callbackObj.id, purchaseJsonStr)

            verify(bridgeExecutor).postValue(callbackObj.id, Gson().toJson(createMiniAppPurchaseResponse()))
        }
    }

    @Suppress("EmptyFunctionBlock")
    private fun createPurchaseProvider(
        shouldCreate: Boolean,
        canPurchase: Boolean
    ): InAppPurchaseProvider {
        return if (shouldCreate) object :
            InAppPurchaseProvider {

            override fun purchaseItem(
                itemId: String,
                onSuccess: (purchasedProductResponse: PurchasedProductResponse) -> Unit,
                onError: (message: String) -> Unit
            ) {
                if (canPurchase) onSuccess(purchasedProductResponse)
                else onError("")
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
            apiClient
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
