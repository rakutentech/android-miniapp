package com.rakuten.tech.mobile.miniapp.navigator

/** The navigation controller of sdk mini app view. **/
abstract class MiniAppNavigator {

    abstract fun openExternalUrl(url: String, resultHandler: ExternalResultHandler)
}
