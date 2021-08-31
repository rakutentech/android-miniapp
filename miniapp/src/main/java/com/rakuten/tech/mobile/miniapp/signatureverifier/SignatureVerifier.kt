package com.rakuten.tech.mobile.miniapp.signatureverifier

import android.content.Context
import com.rakuten.tech.mobile.miniapp.signatureverifier.api.ApiClient
import com.rakuten.tech.mobile.miniapp.signatureverifier.api.PublicKeyFetcher
import com.rakuten.tech.mobile.miniapp.signatureverifier.verification.PublicKeyCache
import java.io.InputStream

/**
 * Main entry point for the Signature Verifier SDK.
 * Should be accessed via [SignatureVerifier.instance].
 */
@Suppress("UnnecessaryAbstractClass", "ParameterListWrapping")
internal abstract class SignatureVerifier {

    /**
     * Verifies the [signature] of the [data] using the [publicKeyId].
     *
     * @return true if [signature] associated with [data] is valid.
     */
    abstract suspend fun verify(publicKeyId: String, data: InputStream, signature: String): Boolean

    companion object {
        var callback: ((ex: Exception) -> Unit)? = null

        /**
         * Initializes an instance of the Signature Verifier SDK based on the provided parameters.
         *
         * @param [context] application context
         * @param [baseUrl] endpoint used for public key fetching
         * @param [subscriptionKey] authorization key for the public key fetching endpoint
         * @param [errorCallback] optional callback function for app to receive any exception occurred in the SDK.
         *
         * @return `instance` if initialization is successful, and `null` otherwise.
         */
        @SuppressWarnings("LongMethod", "TooGenericExceptionCaught")
        fun init(
            context: Context,
            baseUrl: String,
            subscriptionKey: String,
            errorCallback: ((ex: Exception) -> Unit)? = null
        ): SignatureVerifier? {
            callback = errorCallback
            return try {

                val client = ApiClient(
                        baseUrl = baseUrl,
                        subscriptionKey = subscriptionKey,
                        context = context
                )

                RealSignatureVerifier(
                        PublicKeyCache(
                                keyFetcher = PublicKeyFetcher(client),
                                context = context,
                                baseUrl = baseUrl
                        )
                )
            } catch (ex: Exception) {
                callback?.let {
                    it(SignatureVerifierException("Signature Verifier initialization failed", ex))
                }
                null
            }
        }
    }
}

/**
 * Custom exception for Signature Verifier SDK.
 */
internal class SignatureVerifierException(name: String, cause: Throwable? = null) :
        RuntimeException(name, cause)
