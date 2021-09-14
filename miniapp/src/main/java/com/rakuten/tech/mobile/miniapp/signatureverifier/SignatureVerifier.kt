package com.rakuten.tech.mobile.miniapp.signatureverifier

import android.content.Context
import android.util.Base64
import com.rakuten.tech.mobile.miniapp.signatureverifier.api.PublicKeyFetcher
import com.rakuten.tech.mobile.miniapp.signatureverifier.api.SignatureApiClient
import com.rakuten.tech.mobile.miniapp.signatureverifier.verification.PublicKeyCache
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.internal.and
import java.io.File
import java.io.InputStream
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec

/**
 * Main entry point for the Signature Verifier.
 * Should be accessed via [SignatureVerifier.init].
 */
internal class SignatureVerifier(
    private val cache: PublicKeyCache,
    private val basePath: File,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Verifies the [signature] of the [data] using the [publicKeyId].
     *
     * @return true if [signature] associated with [data] is valid.
     */
    @SuppressWarnings("LabeledExpression", "MaxLineLength")
    suspend fun verify(publicKeyId: String, versionId: String, inputStream: InputStream, signature: String) = withContext(dispatcher) {
        // always return false when EncryptedSharedPreferences was not initialized
        // due to keystore validation.
        val key = cache[publicKeyId] ?: return@withContext false

        // preparing hash using miniapp zip file
        val zipFile = MiniAppFileUtil(basePath).createFile(inputStream)
        val hash = calculateSha256Hash(zipFile.inputStream().readBytes())

        zipFile.deleteRecursively()

        // preparing data byte stream
        val data = (versionId + hash).byteInputStream()

        // verifying signature
        val isVerified = Signature.getInstance("SHA256withECDSA").apply {
            initVerify(rawToEncodedECPublicKey(key))

            val buffer = ByteArray(SIXTEEN_KILOBYTES)
            var read = data.read(buffer)
            while (read != -1) {
                update(buffer, 0, read)

                read = data.read(buffer)
            }
        }.verify(Base64.decode(signature, Base64.DEFAULT))

        if (!isVerified) {
            cache.remove(publicKeyId)
        }

        return@withContext isVerified
    }

    private fun rawToEncodedECPublicKey(key: String): ECPublicKey {
        val parameters = ecParameterSpecForCurve("secp256r1")
        val keySizeBytes = parameters.order.bitLength() / java.lang.Byte.SIZE
        val pubKey = Base64.decode(key, Base64.DEFAULT)

        // First Byte represents compressed/uncompressed status
        // We're expecting it to always be uncompressed (04)
        var offset = UNCOMPRESSED_OFFSET
        val x = BigInteger(POSITIVE_BIG_INTEGER, pubKey.copyOfRange(offset, offset + keySizeBytes))

        offset += keySizeBytes
        val y = BigInteger(POSITIVE_BIG_INTEGER, pubKey.copyOfRange(offset, offset + keySizeBytes))

        val keySpec = ECPublicKeySpec(ECPoint(x, y), parameters)
        val keyFactory = KeyFactory.getInstance("EC")
        return keyFactory.generatePublic(keySpec) as ECPublicKey
    }

    private fun ecParameterSpecForCurve(curveName: String): ECParameterSpec {
        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(ECGenParameterSpec(curveName))

        return (kpg.generateKeyPair().public as ECPublicKey).params
    }

    @SuppressWarnings("MagicNumber", "PrintStackTrace")
    private fun calculateSha256Hash(byteArray: ByteArray): String {
        var generated: String? = null
        try {
            val md = MessageDigest.getInstance("SHA-256")
            val bytes = md.digest(byteArray)
            val sb = java.lang.StringBuilder()
            for (i in bytes.indices) {
                sb.append(((bytes[i] and 0xff) + 0x100).toString(16).substring(1))
            }
            generated = sb.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return generated.toString()
    }

    companion object {
        private const val SIXTEEN_KILOBYTES = 16 * 1024

        private const val UNCOMPRESSED_OFFSET = 1
        private const val POSITIVE_BIG_INTEGER = 1

        /**
         * Initializes an instance of the Signature Verifier SDK based on the provided parameters.
         *
         * @param [context] application context
         * @param [baseUrl] endpoint used for public key fetching
         * @param [subscriptionKey] authorization key for the public key fetching endpoint
         *
         * @return `instance` of [SignatureVerifier] if initialization is successful, and `null` otherwise.
         */
        @SuppressWarnings("LongMethod", "TooGenericExceptionCaught", "PrintStackTrace")
        fun init(
            context: Context,
            baseUrl: String,
            subscriptionKey: String
        ): SignatureVerifier? {
            return try {

                val client = SignatureApiClient(
                    baseUrl = baseUrl,
                    subscriptionKey = subscriptionKey,
                    context = context
                )

                SignatureVerifier(
                    PublicKeyCache(
                        keyFetcher = PublicKeyFetcher(client),
                        context = context,
                        baseUrl = baseUrl
                    ),
                    context.filesDir
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
                null
            }
        }
    }
}
