package org.lifeline.emergencyassistant.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class EventLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestampIso: String,
    val triggerType: String,
    val country: String? = null,
    val emergencyNumber: String? = null,
    val actions: List<String> = emptyList(),
    val smsStatus: Map<String, String> = emptyMap()
)

class EventLog(private val store: EncryptedStore) {
    fun add(entry: EventLogEntry) {
        val list = current() + entry
        store.putJson(KEY, list, kotlinx.serialization.builtins.ListSerializer(EventLogEntry.serializer()))
    }

    fun current(): List<EventLogEntry> = store.getJson(KEY, emptyList<EventLogEntry>())

    fun delete(id: String) {
        store.putJson(KEY, current().filterNot { it.id == id }, kotlinx.serialization.builtins.ListSerializer(EventLogEntry.serializer()))
    }

    fun clear() = store.remove(KEY)

    fun lastTrigger(): EventLogEntry? =
        current().filter { it.triggerType.contains("fall", true) || it.triggerType.contains("manual", true) }
            .maxByOrNull { it.timestampIso }

    companion object { const val KEY = "event_log" }
}
