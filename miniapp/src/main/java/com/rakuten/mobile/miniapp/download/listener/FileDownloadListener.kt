package com.rakuten.mobile.miniapp.download.listener

import android.content.Context
import com.rakuten.mobile.miniapp.download.DownloadMiniAppImpl
import com.rakuten.mobile.miniapp.download.utility.MiniAppFileWriter
import javax.inject.Inject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

/**
 * Listener for downloading file request. Handle onResponse and onFailure callbacks accordingly.
 */
class FileDownloadListener(val appId: String, val fileUrl: String) : Callback<ResponseBody> {

  /**
   * Dagger injected variable.
   */
  @Inject
  lateinit var context: Context
  /**
   * Dagger injected variable.
   */
  @Inject
  lateinit var fileWriter: MiniAppFileWriter

  init {
    // Dagger injection.
    DownloadMiniAppImpl.daggerDownloadComponent.inject(this)

    Timber.tag(TAG).d("Creating a new FileDownloadListener and DI.")
  }

  /**
   * Handle successful response, write response body to disk.
   */
  override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
    if (::context.isInitialized && ::fileWriter.isInitialized) {
      fileWriter
          .writeResponseBodyToDisk(response.body() ?: return, appId, fileUrl)
    }
    Timber.tag(TAG).d(response.message())
  }

  /**
   * Handle failure response, print log.
   */
  override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
    Timber.tag(TAG).e(t.message ?: "onFailure()")
  }

  companion object {
    private const val TAG = "Mini_FileDlListener"
  }
}
