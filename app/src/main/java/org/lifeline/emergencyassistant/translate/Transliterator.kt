package org.lifeline.emergencyassistant.translate

object Transliterator {
    private val cyrillic: Map<Char, String> = buildMap {
        put('а', "a"); put('б', "b"); put('в', "v"); put('г', "g"); put('д', "d")
        put('е', "e"); put('ё', "yo"); put('ж', "zh"); put('з', "z"); put('и', "i")
        put('й', "y"); put('к', "k"); put('л', "l"); put('м', "m"); put('н', "n")
        put('о', "o"); put('п', "p"); put('р', "r"); put('с', "s"); put('т', "t")
        put('у', "u"); put('ф', "f"); put('х', "kh"); put('ц', "ts"); put('ч', "ch")
        put('ш', "sh"); put('щ', "shch"); put('ъ', ""); put('ы', "y"); put('ь', "")
        put('э', "e"); put('ю', "yu"); put('я', "ya")
    }
    private val devanagari: Map<Char, String> = buildMap {
        put('अ', "a"); put('आ', "aa"); put('इ', "i"); put('ई', "ii"); put('उ', "u")
        put('ऊ', "uu"); put('ए', "e"); put('ऐ', "ai"); put('ओ', "o"); put('औ', "au")
        put('क', "k"); put('ख', "kh"); put('ग', "g"); put('घ', "gh"); put('च', "ch")
        put('छ', "chh"); put('ज', "j"); put('ट', "t"); put('ठ', "th"); put('ड', "d")
        put('ण', "n"); put('त', "t"); put('थ', "th"); put('द', "d"); put('ध', "dh")
        put('न', "n"); put('प', "p"); put('फ', "ph"); put('ब', "b"); put('भ', "bh")
        put('म', "m"); put('य', "y"); put('र', "r"); put('ल', "l"); put('व', "v")
        put('श', "sh"); put('ष', "sh"); put('स', "s"); put('ह', "h")
    }
    private val arabic: Map<Char, String> = buildMap {
        put('ا', "a"); put('ب', "b"); put('ت', "t"); put('ث', "th"); put('ج', "j")
        put('ح', "h"); put('خ', "kh"); put('د', "d"); put('ذ', "dh"); put('ر', "r")
        put('ز', "z"); put('س', "s"); put('ش', "sh"); put('ص', "s"); put('ض', "d")
        put('ط', "t"); put('ظ', "z"); put('ع', "a"); put('غ', "gh"); put('ف', "f")
        put('ق', "q"); put('ك', "k"); put('ل', "l"); put('م', "m"); put('ن', "n")
        put('ه', "h"); put('و', "w"); put('ي', "y"); put('ى', "a"); put('ة', "h")
        put('ء', "'"); put('ؤ', "'"); put('ئ', "'"); put('أ', "a"); put('إ', "i"); put('آ', "aa")
    }

    private fun scriptOf(s: String): String? {
        for (ch in s) {
            when {
                ch in 'а'..'я' || ch in 'А'..'Я' || ch == 'ё' || ch == 'Ё' -> return "Cyrillic"
                ch in '一'..'鿿' -> return "Han"
                ch in '\u0900'..'\u097F' -> return "Devanagari"
                ch in 'ا'..'ی' -> return "Arabic"
            }
        }
        return null
    }

    fun needsTransliteration(sourceText: String, targetScript: String): Boolean {
        val src = scriptOf(sourceText) ?: return false
        return src != targetScript && src != "Latin"
    }

    fun transliterate(text: String, targetScript: String): String {
        if (targetScript != "Latin") return text
        return text.map { ch ->
            val lower = ch.lowercaseChar()
            when (scriptOf(ch.toString())) {
                "Cyrillic" -> cyrillic[lower] ?: ch.toString()
                "Devanagari" -> devanagari[ch] ?: ch.toString()
                "Arabic" -> arabic[lower] ?: ch.toString()
                else -> ch.toString()
            }
        }.joinToString("")
    }
}
