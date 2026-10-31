package org.lifeline.emergencyassistant.geo

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class NaturalEarthGeodata private constructor(
    private val countries: List<Pair<String, List<List<GeoPoint>>>>
) : CountryGeodata {

    override fun countryAt(point: GeoPoint): String? {
        for ((iso, polygons) in countries) {
            for (poly in polygons) {
                if (pointInPolygon(point, poly)) return iso
            }
        }
        return null
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun load(context: Context): NaturalEarthGeodata {
            val raw = context.assets.open("data/countries.geojson").use {
                it.readBytes().toString(Charsets.UTF_8)
            }
            val root = json.parseToJsonElement(raw).jsonObject
            val features = root["features"]!!.jsonArray
            val out = mutableListOf<Pair<String, List<List<GeoPoint>>>>()
            for (f in features) {
                val fo = f.jsonObject
                val iso = fo["properties"]!!.jsonObject["ISO_A2"]?.jsonPrimitive?.content
                    ?: fo["properties"]!!.jsonObject["ADM0_A3"]?.jsonPrimitive?.content
                    ?: continue
                val geom = fo["geometry"]!!.jsonObject
                val polys = mutableListOf<List<GeoPoint>>()
                fun coordsToPoints(e: JsonElement): List<GeoPoint> =
                    e.jsonArray.map { c ->
                        val a = c.jsonArray
                        GeoPoint(a[1].jsonPrimitive.content.toDouble(), a[0].jsonPrimitive.content.toDouble())
                    }
                when (geom["type"]!!.jsonPrimitive.content) {
                    "Polygon" -> geom["coordinates"]!!.jsonArray.forEach { ring -> polys.add(coordsToPoints(ring)) }
                    "MultiPolygon" -> geom["coordinates"]!!.jsonArray.forEach { poly ->
                        poly.jsonArray.forEach { ring -> polys.add(coordsToPoints(ring)) }
                    }
                }
                out.add(iso to polys)
            }
            return NaturalEarthGeodata(out)
        }
    }
}

fun pointInPolygon(point: GeoPoint, polygon: List<GeoPoint>): Boolean {
    var inside = false
    var j = polygon.size - 1
    for (i in polygon.indices) {
        val xi = polygon[i].lon; val yi = polygon[i].lat
        val xj = polygon[j].lon; val yj = polygon[j].lat
        if ((yi > point.lat) != (yj > point.lat) &&
            point.lon < (xj - xi) * (point.lat - yi) / (yj - yi) + xi
        ) inside = !inside
        j = i
    }
    return inside
}
