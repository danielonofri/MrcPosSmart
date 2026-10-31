package com.mrcomanda.possmart.network

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val PREF_NAME = "pos_config_prefs"
    private const val KEY_BASE_URL = "base_url"
    private const val KEY_API_KEY = "api_key"
    private const val KEY_MAC_IMPRESORA = "mac_impresora"
    private const val KEY_SMART_POS = "es_smart_pos"

    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    var baseUrl: String
        get() = preferences.getString(KEY_BASE_URL, "https://10.0.2.2:5006/") ?: "https://10.0.2.2:5006/"
        set(value) = preferences.edit().putString(KEY_BASE_URL, value).apply()

    var apiKey: String
        get() = preferences.getString(KEY_API_KEY, "MRC_SecKey_9f8e7d6c5b4a3_KotlinClient") ?: ""
        set(value) = preferences.edit().putString(KEY_API_KEY, value).apply()

    var macImpresora: String
        get() = preferences.getString(KEY_MAC_IMPRESORA, "02:18:99:0C:00:39") ?: "02:18:99:0C:00:39"
        set(value) = preferences.edit().putString(KEY_MAC_IMPRESORA, value).apply()

    var esSmartPos: Boolean
        get() = preferences.getBoolean(KEY_SMART_POS, false)
        set(value) = preferences.edit().putBoolean(KEY_SMART_POS, value).apply()
}