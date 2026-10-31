package org.lifeline.emergencyassistant.geo

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.pow
import kotlin.math.sqrt

@Serializable
data class Place(val id: String, val name: String, val asciiname: String, val lat: Double, val lon: Double, val cc: String, val pop: Int)

class PlaceIndex private constructor(private val places: List<Place>) {
    fun nearest(lat: Double, lon: Double, maxKm: Double = 25.0): Place? {
        var best: Place? = null
        var bestD = maxKm
        for (p in places) {
            val dLat = (p.lat - lat) * 111.0
            val dLon = (p.lon - lon) * 111.0 * kotlin.math.cos(Math.toRadians(lat))
            val d = sqrt(dLat * dLat + dLon * dLon)
            if (d < bestD) { bestD = d; best = p }
        }
        return best
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun load(context: Context): PlaceIndex {
            val raw = context.assets.open("data/places.json").use { it.readBytes().toString(Charsets.UTF_8) }
            return PlaceIndex(json.decodeFromString<List<Place>>(raw))
        }
    }
}
