package org.lifeline.emergencyassistant.logging

import android.content.Context
import org.lifeline.emergencyassistant.crypto.CryptoStore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashLog(private val context: Context, private val crypto: CryptoStore) {
    private val file get() = File(context.filesDir, "crashlog.enc")

    fun record(stepFlag: String, throwable: Throwable?) {
        val line = buildString {
            append(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).format(Date()))
            append(" step=").append(stepFlag)
            if (throwable != null) {
                append(" exc=").append(throwable.javaClass.simpleName)
                append(" msg=").append(ScrubbedLogger.scrub(throwable.message ?: ""))
                append(" stack=").append(scrubStack(throwable.stackTraceToString()))
            }
        }
        val existing = if (file.exists()) crypto.decrypt(file.readBytes()).toString(Charsets.UTF_8) else ""
        val combined = (existing + line + "\n").takeLast(200_000)
        file.writeBytes(crypto.encrypt(combined.toByteArray(Charsets.UTF_8)))
    }

    private fun scrubStack(stack: String): String =
        stack.lines().joinToString("|") { line ->
            if (ScrubbedLogger.isSensitive(line)) "[scrubbed]" else line
        }.take(4000)

    fun readAll(): String =
        if (file.exists()) crypto.decrypt(file.readBytes()).toString(Charsets.UTF_8) else ""

    fun clear() { file.delete() }
}
