package com.rakuten.tech.mobile.miniapp.signatureverifier

import android.util.Base64
import com.rakuten.tech.mobile.miniapp.signatureverifier.verification.PublicKeyCache
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec

internal class RealSignatureVerifier(
    private val cache: PublicKeyCache,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SignatureVerifier() {

    @SuppressWarnings("LabeledExpression")
    override suspend fun verify(publicKeyId: String, data: InputStream, signature: String) =
            withContext(dispatcher) {
                // always return false when EncryptedSharedPreferences was not initialized
                // due to keystore validation.
                val key = cache[publicKeyId] ?: return@withContext false

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

                isVerified
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
        return keyFactory
                .generatePublic(keySpec) as ECPublicKey
    }

    private fun ecParameterSpecForCurve(curveName: String): ECParameterSpec {
        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(ECGenParameterSpec(curveName))

        return (kpg.generateKeyPair().public as ECPublicKey).params
    }

    companion object {
        private const val SIXTEEN_KILOBYTES = 16 * 1024

        private const val UNCOMPRESSED_OFFSET = 1
        private const val POSITIVE_BIG_INTEGER = 1
    }
}
