package org.lifeline.emergencyassistant.pack

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.File

object PackLoader {
    private val json = Json {
        ignoreUnknownKeys = true
        allowStructuredMapKeys = true
    }

    private const val PACK_DIR = "packs"

    fun listInstalled(context: Context): List<PackMeta> {
        val dir = File(context.assetsPackDir(), PACK_DIR)
        return dir.listFiles { f -> f.name.endsWith(".json") }
            ?.mapNotNull { loadMeta(context, it.name.removeSuffix(".json")) }
            ?: emptyList()
    }

    fun load(context: Context, bcp47: String): LanguagePack {
        val raw = context.assets.open("$PACK_DIR/$bcp47.json").use { it.readBytes().toString(Charsets.UTF_8) }
        val pack = json.decodeFromString<LanguagePack>(raw)
        return Packs.validateSchema(pack).getOrThrow()
    }

    private fun loadMeta(context: Context, bcp47: String): PackMeta? = try {
        val raw = context.assets.open("$PACK_DIR/$bcp47.json").use { it.readBytes().toString(Charsets.UTF_8) }
        json.decodeFromString<LanguagePack>(raw).meta
    } catch (e: Exception) {
        null
    }

    private fun Context.assetsPackDir(): File = File("assets")
}
