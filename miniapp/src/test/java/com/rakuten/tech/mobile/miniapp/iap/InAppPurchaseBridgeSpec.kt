package com.rakuten.tech.mobile.miniapp.iap

import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA
import com.rakuten.tech.mobile.miniapp.display.WebViewListener
import com.rakuten.tech.mobile.miniapp.js.*
import com.rakuten.tech.mobile.miniapp.permission.MiniAppDevicePermissionType
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class InAppPurchaseBridgeSpec {
    private lateinit var miniAppBridge: MiniAppMessageBridge
    private val callbackObj = CallbackObj(
        action = ActionType.PURCHASE_ITEM.action,
        param = null,
        id = TEST_CALLBACK_ID
    )
    private val webViewListener: WebViewListener = mock()
    private val bridgeExecutor = Mockito.spy(MiniAppBridgeExecutor(webViewListener))
    private val productPrice = ProductPrice(
        123, "JPY", "1234"
    )
    private val product = Product(
        "itemId", "title", "description", productPrice
    )
    private val purchasedProduct = PurchasedProduct(
        "dummy_orderId", product, "dummy_token"
    )
    private val purchaseJsonStr: String = Gson().toJson(
        CallbackObj(
            action = ActionType.PURCHASE_ITEM.action,
            param = PurchasedProductCallbackObj.ProductItem("itemId"),
            id = TEST_CALLBACK_ID
        )
    )

    @Before
    fun setup() {
        miniAppBridge = Mockito.spy(createMessageBridge())
        When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = webViewListener,
            customPermissionCache = mock(),
            downloadedManifestCache = mock(),
            miniAppId = TEST_MA.id,
            ratDispatcher = mock()
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

    @Test
    fun `postError should be called when there is no purchaseItem implementation`() {
        val dispatcher = Mockito.spy(
            createPurchaseDispatcher(
                shouldCreate = false,
                canPurchase = false
            )
        )
        miniAppBridge.setInAppPurchaseBridgeDispatcher(dispatcher)
        val errMsg = "${InAppPurchaseBridge.ERR_IN_APP_PURCHASE} null"
        miniAppBridge.postMessage(Gson().toJson(callbackObj))

        verify(bridgeExecutor).postError(callbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when can't purchase item successfully`() {
        val dispatcher = Mockito.spy(
            createPurchaseDispatcher(
                shouldCreate = true,
                canPurchase = false
            )
        )
        val wrapper = Mockito.spy(createIAPBridgeWrapper(dispatcher))
        wrapper.onPurchaseItem(callbackObj.id, purchaseJsonStr)

        verify(bridgeExecutor).postError(
            callbackObj.id,
            InAppPurchaseBridge.ERR_IN_APP_PURCHASE + " "
        )
    }

    @Test
    fun `postValue should be called when purchasing item successfully`() {
        val dispatcher = Mockito.spy(
            createPurchaseDispatcher(
                shouldCreate = true,
                canPurchase = true
            )
        )
        val wrapper = Mockito.spy(createIAPBridgeWrapper(dispatcher))
        wrapper.onPurchaseItem(callbackObj.id, purchaseJsonStr)

        verify(bridgeExecutor).postValue(callbackObj.id, Gson().toJson(purchasedProduct))
    }

    private fun createPurchaseDispatcher(
        shouldCreate: Boolean,
        canPurchase: Boolean
    ): InAppPurchaseBridgeDispatcher {
        return if (shouldCreate) object : InAppPurchaseBridgeDispatcher {
            override fun purchaseItem(
                itemId: String,
                onSuccess: (purchasedProduct: PurchasedProduct) -> Unit,
                onError: (message: String) -> Unit
            ) {
                if (canPurchase) onSuccess(purchasedProduct)
                else onError("")
            }
        } else {
            object : InAppPurchaseBridgeDispatcher {}
        }
    }

    private fun createIAPBridgeWrapper(dispatcher: InAppPurchaseBridgeDispatcher): InAppPurchaseBridge {
        val wrapper = InAppPurchaseBridge()
        wrapper.setMiniAppComponents(
            bridgeExecutor,
            TEST_MA.id
        )
        wrapper.setIAPBridgeDispatcher(dispatcher)
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
