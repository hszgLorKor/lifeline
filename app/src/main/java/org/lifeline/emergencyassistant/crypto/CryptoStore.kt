package org.lifeline.emergencyassistant.crypto

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object Keys {
    const val DATA_KEY = "lifeline_data_key"
    const val AUTH_KEY = "lifeline_auth_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    private fun baseBuilder(alias: String, purposes: Int) = KeyGenParameterSpec.Builder(alias, purposes)
        .setKeySize(256)
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)

    private fun generate(builder: KeyGenParameterSpec.Builder): SecretKey {
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        return try {
            gen.init(builder.setIsStrongBoxBacked(true).build())
            gen.generateKey()
        } catch (e: Exception) {
            gen.init(builder.setIsStrongBoxBacked(false).build())
            gen.generateKey()
        }
    }

    fun dataKey(): SecretKey {
        val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (ks.getEntry(DATA_KEY, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        return generate(
            baseBuilder(DATA_KEY, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setRandomizedEncryptionRequired(true)
                .setUserAuthenticationRequired(false)
        )
    }

    fun authKey(): SecretKey {
        val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (ks.getEntry(AUTH_KEY, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        return generate(
            baseBuilder(AUTH_KEY, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationParameters(
                    0,
                    KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL
                )
                .setInvalidatedByBiometricEnrollment(true)
        )
    }

    fun destroyAll() {
        val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (ks.containsAlias(DATA_KEY)) ks.deleteEntry(DATA_KEY)
        if (ks.containsAlias(AUTH_KEY)) ks.deleteEntry(AUTH_KEY)
    }
}

class CryptoStore(private val context: Context) {
    private val dataKey: SecretKey by lazy { Keys.dataKey() }

    private fun cipher(mode: Int, iv: ByteArray? = null): Cipher =
        Cipher.getInstance("AES/GCM/NoPadding").apply {
            if (iv != null) init(mode, dataKey, GCMParameterSpec(128, iv))
            else init(mode, dataKey)
        }

    fun encrypt(plaintext: ByteArray): ByteArray {
        val c = cipher(Cipher.ENCRYPT_MODE)
        val iv = c.iv
        val ct = c.doFinal(plaintext)
        return ByteArray(2 + iv.size + ct.size).apply {
            this[0] = 1
            this[1] = iv.size.toByte()
            System.arraycopy(iv, 0, this, 2, iv.size)
            System.arraycopy(ct, 0, this, 2 + iv.size, ct.size)
        }
    }

    fun decrypt(blob: ByteArray): ByteArray {
        val ivLen = blob[1].toInt() and 0xff
        val iv = blob.copyOfRange(2, 2 + ivLen)
        val ct = blob.copyOfRange(2 + ivLen, blob.size)
        return cipher(Cipher.DECRYPT_MODE, iv).doFinal(ct)
    }

    fun encryptString(s: String): ByteArray = encrypt(s.toByteArray(Charsets.UTF_8))
    fun decryptString(blob: ByteArray): String = decrypt(blob).toString(Charsets.UTF_8)

    fun cryptoErase() {
        Keys.destroyAll()
        context.filesDir.listFiles()?.forEach { it.deleteRecursively() }
        context.cacheDir.listFiles()?.forEach { it.deleteRecursively() }
        File(context.filesDir, "lifeline.db").delete()
        File(context.filesDir, "lifeline.db-shm").delete()
        File(context.filesDir, "lifeline.db-wal").delete()
        File(context.filesDir, "crashlog.enc").delete()
    }
}
