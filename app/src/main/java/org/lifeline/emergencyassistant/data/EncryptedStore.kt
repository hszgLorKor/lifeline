package org.lifeline.emergencyassistant.data

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.lifeline.emergencyassistant.crypto.CryptoStore
import java.io.File
import kotlin.PublishedApi

class EncryptedStore(context: Context) {
    @PublishedApi internal val crypto = CryptoStore(context)
    @PublishedApi internal val json = Json { ignoreUnknownKeys = true }
    private val dir = File(context.filesDir, "kv").apply { mkdirs() }

    @PublishedApi internal fun fileFor(key: String) = File(dir, key + ".enc")

    fun <T> putJson(key: String, value: T, serializer: kotlinx.serialization.KSerializer<T>) {
        val raw = json.encodeToString(serializer, value)
        fileFor(key).writeBytes(crypto.encryptString(raw))
    }

    inline fun <reified T> getJson(key: String, default: T): T {
        val f = fileFor(key)
        if (!f.exists()) return default
        return try {
            json.decodeFromString<T>(crypto.decryptString(f.readBytes()))
        } catch (e: Exception) {
            android.util.Log.e("LifelineStore", "decrypt failed for $key: ${e.javaClass.simpleName}")
            default
        }
    }

    fun putBytes(key: String, bytes: ByteArray) {
        fileFor(key).writeBytes(crypto.encrypt(bytes))
    }

    fun getBytes(key: String): ByteArray? =
        fileFor(key).takeIf { it.exists() }?.readBytes()?.let { crypto.decrypt(it) }

    fun remove(key: String) { fileFor(key).delete() }

    fun deleteAll() { dir.listFiles()?.forEach { it.delete() } }
}
