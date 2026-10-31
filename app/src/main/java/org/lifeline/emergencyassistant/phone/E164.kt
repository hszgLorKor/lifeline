package org.lifeline.emergencyassistant.phone

import com.google.i18n.phonenumbers.PhoneNumberUtil

object E164 {
    private val util = PhoneNumberUtil.getInstance()

    fun normalize(raw: String, defaultCountryIso2: String): Result<String> {
        return try {
            val parsed = util.parse(raw, defaultCountryIso2.uppercase())
            if (!util.isValidNumber(parsed)) {
                return Result.failure(IllegalArgumentException("Malformed number: not a valid number for $defaultCountryIso2"))
            }
            Result.success(util.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Malformed number: ${e.message}"))
        }
    }

    fun isPlausible(raw: String, defaultCountryIso2: String): Boolean = normalize(raw, defaultCountryIso2).isSuccess
}
