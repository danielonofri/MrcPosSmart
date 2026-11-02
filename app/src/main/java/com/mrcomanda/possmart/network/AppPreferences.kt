package com.mrcomanda.possmart.network

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object AppPreferences {
    private const val PREF_NAME = "pos_config_prefs"
    private const val KEY_BASE_URL = "base_url"
    private const val KEY_API_KEY = "api_key"
    private const val KEY_MAC_IMPRESORA = "mac_impresora"
    private const val KEY_SMART_POS = "es_smart_pos"
    private const val KEY_INTEGRACION = "integracion"
    private const val KEY_LICENCIA = "licencia"
    private const val KEY_CUIT = "cuit"
    private const val KEY_TOKEN_MRC = "token_mrc"
    private const val KEY_OAUTH = "oauth"
    private const val KEY_URL_TOKEN_MP = "url_token_mp"
    private const val KEY_ACCESS_TOKEN_MP = "access_token_mp"
    private const val KEY_FECHA_TOKEN_MP = "fecha_token_mp"
    private const val KEY_DEVICE = "device"
    private const val KEY_PAIS = "pais"
    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    var baseUrl: String
        get() = preferences.getString(KEY_BASE_URL, "https://10.0.2.2:5006/") ?: "https://10.0.2.2:5006/"
        set(value) = preferences.edit { putString(KEY_BASE_URL, value) }

    var apiKey: String
        get() = preferences.getString(KEY_API_KEY, "MRC_SecKey_9f8e7d6c5b4a3_KotlinClient") ?: ""
        set(value) = preferences.edit { putString(KEY_API_KEY, value) }

    var macImpresora: String
        get() = preferences.getString(KEY_MAC_IMPRESORA, "02:18:99:0C:00:39") ?: "02:18:99:0C:00:39"
        set(value) = preferences.edit { putString(KEY_MAC_IMPRESORA, value) }

    var esSmartPos: Boolean
        get() = preferences.getBoolean(KEY_SMART_POS, false)
        set(value) = preferences.edit { putBoolean(KEY_SMART_POS, value) }
    var integracion: String
        get() = preferences.getString(KEY_INTEGRACION, "8398295950239706") ?: "8398295950239706"
        set(value) = preferences.edit { putString(KEY_INTEGRACION, value) }

    var licencia: String
        get() = preferences.getString(KEY_LICENCIA, "") ?: "01234"
        set(value) = preferences.edit { putString(KEY_LICENCIA, value) }

    var cuit: String
        get() = preferences.getString(KEY_CUIT, "") ?: ""
        set(value) = preferences.edit { putString(KEY_CUIT, value) }

    var tokenMrc: String
        get() = preferences.getString(KEY_TOKEN_MRC, "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImh0dHA6Ly9zY2hlbWFzLm1pY3Jvc29mdC5jb20vd3MvMjAwOC8wNi9pZGVudGl0eS9jbGFpbXMvcm9sZSI6IkFkbWluIiwiaXNzIjoiaHR0cHM6Ly93d3cubXJjMzA3LmNvbSIsImF1ZCI6Imh0dHBzOi8vd3d3Lm1yYzMwNy5jb20ifQ.Bp7IhFJ9fC5_Ohgn1a_T8ns_5Smd1huJVZ0NIIvNDwM")
            ?: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImh0dHA6Ly9zY2hlbWFzLm1pY3Jvc29mdC5jb20vd3MvMjAwOC8wNi9pZGVudGl0eS9jbGFpbXMvcm9sZSI6IkFkbWluIiwiaXNzIjoiaHR0cHM6Ly93d3cubXJjMzA3LmNvbSIsImF1ZCI6Imh0dHBzOi8vd3d3Lm1yYzMwNy5jb20ifQ.Bp7IhFJ9fC5_Ohgn1a_T8ns_5Smd1huJVZ0NIIvNDwM"
        set(value) = preferences.edit { putString(KEY_TOKEN_MRC, value) }

    // Propiedad para Oauth (por defecto false)
    var oauth: Boolean
        get() = preferences.getBoolean(KEY_OAUTH, false)
        set(value) = preferences.edit { putBoolean(KEY_OAUTH, value) }

    var urlTokenMp: String
        get() = preferences.getString(KEY_URL_TOKEN_MP, "https://www.mrc307.com:490/api/Token") ?: "https://www.mrc307.com:490/api/Token"
        set(value) = preferences.edit { putString(KEY_URL_TOKEN_MP, value) }

    var accessTokenMp: String
        get() = preferences.getString(KEY_ACCESS_TOKEN_MP, "") ?: ""
        set(value) = preferences.edit { putString(KEY_ACCESS_TOKEN_MP, value) }

    var fechaTokenMp: String
        get() = preferences.getString(KEY_FECHA_TOKEN_MP, "") ?: ""
        set(value) = preferences.edit { putString(KEY_FECHA_TOKEN_MP, value) }

    var device: String
        get() = preferences.getString(KEY_DEVICE, "NEWLAND_N950__N950NCBB02072967") ?: "NEWLAND_N950__N950NCBB02072967"
        set(value) = preferences.edit { putString(KEY_DEVICE, value) }

    var pais: String
        get() = preferences.getString(KEY_PAIS, "Chile") ?: "Chile"
        set(value) = preferences.edit { putString(KEY_PAIS, value) }
}
