package com.rakuten.tech.mobile.miniapp.navigator

/** The navigation controller of sdk mini app view. **/
interface MiniAppNavigator {

    /**
     * Open the external url by browser or webview.
     * @param url The detected external url. This url is sent from mini app view.
     * @param externalResultHandler Use this to send any result such as url to mini app view.
     */
    fun openExternalUrl(url: String, externalResultHandler: ExternalResultHandler)
}
