package org.lifeline.emergencyassistant.data

import android.content.Context
import org.lifeline.emergencyassistant.pack.PackLoader
import org.lifeline.emergencyassistant.pack.Packs
import org.lifeline.emergencyassistant.translate.Translator

class ProfileRepository(private val context: Context, private val store: EncryptedStore) {

    fun loadState(): ProfileState = store.getJson(STATE_KEY, ProfileState())

    fun saveProfile(profile: MedicalProfile): ProfileState {
        val state = loadState()
        val newVersion = state.version + 1
        val newState = state.copy(profile = profile, version = newVersion)
        store.putJson(STATE_KEY, newState, ProfileState.serializer())
        retranslate(newState)
        return loadState()
    }

    fun retranslate(state: ProfileState = loadState()): ProfileState {
        val sourcePack = PackLoader.load(context, motherTongue())
        val translations = mutableMapOf<String, TranslatedProfile>()
        for (meta in PackLoader.listInstalled(context)) {
            if (meta.bcp47 == sourcePack.meta.bcp47) continue
            val target = PackLoader.load(context, meta.bcp47)
            translations[meta.bcp47] = Translator(sourcePack, target).translate(state.profile, state.version)
        }
        val updated = state.copy(translations = translations)
        store.putJson(STATE_KEY, updated, ProfileState.serializer())
        return updated
    }

    fun translatedFor(lang: String): TranslatedProfile? {
        val state = loadState()
        state.translations[lang]?.let { return it }
        if (lang != Packs.ENGLISH) {
            return state.translations[Packs.ENGLISH]
        }
        return null
    }

    fun motherTongue(): String = store.getJson(AppSettings.KEY, AppSettings()).motherTongue

    fun settings(): AppSettings = store.getJson(AppSettings.KEY, AppSettings())

    fun saveSettings(s: AppSettings) = store.putJson(AppSettings.KEY, s, AppSettings.serializer())

    fun cryptoErase() {
        store.deleteAll()
    }

    companion object { const val STATE_KEY = "profile_state" }
}
