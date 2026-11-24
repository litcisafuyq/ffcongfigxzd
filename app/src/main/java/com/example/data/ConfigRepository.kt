package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ffvn_config_prefs")

data class UserConfigPrefs(
  val customJson: String? = null,
  val activePreset: String = "manh",
  val useScale100: Boolean = false,
  val redDotDelta: Int = 0,
  val completedInstallSteps: Set<Int> = emptySet(),
  val nearHits: Int? = null,
  val midHits: Int? = null,
  val farHits: Int? = null,
)

class ConfigRepository(private val context: Context) {

  private object Keys {
    val CUSTOM_JSON = stringPreferencesKey("custom_json")
    val ACTIVE_PRESET = stringPreferencesKey("active_preset")
    val USE_SCALE_100 = booleanPreferencesKey("use_scale_100")
    val RED_DOT_DELTA = intPreferencesKey("red_dot_delta")
    val COMPLETED_STEPS = stringSetPreferencesKey("completed_install_steps")
    val NEAR_HITS = intPreferencesKey("cal_near_hits")
    val MID_HITS = intPreferencesKey("cal_mid_hits")
    val FAR_HITS = intPreferencesKey("cal_far_hits")
  }

  fun loadBundledJson(): String {
    return context.assets.open("vn_config.json").bufferedReader().use { it.readText() }
  }

  val prefsFlow: Flow<UserConfigPrefs> =
    context.dataStore.data.map { prefs ->
      UserConfigPrefs(
        customJson = prefs[Keys.CUSTOM_JSON],
        activePreset = prefs[Keys.ACTIVE_PRESET] ?: "manh",
        useScale100 = prefs[Keys.USE_SCALE_100] ?: false,
        redDotDelta = prefs[Keys.RED_DOT_DELTA] ?: 0,
        completedInstallSteps =
          (prefs[Keys.COMPLETED_STEPS] ?: emptySet()).mapNotNull { it.toIntOrNull() }.toSet(),
        nearHits = prefs[Keys.NEAR_HITS],
        midHits = prefs[Keys.MID_HITS],
        farHits = prefs[Keys.FAR_HITS],
      )
    }

  suspend fun saveCustomJson(rawJson: String, parsedPreset: String) {
    context.dataStore.edit { prefs ->
      prefs[Keys.CUSTOM_JSON] = rawJson
      prefs[Keys.ACTIVE_PRESET] = parsedPreset
      prefs[Keys.RED_DOT_DELTA] = 0
    }
  }

  suspend fun resetToBundledDefault() {
    context.dataStore.edit { prefs ->
      prefs.remove(Keys.CUSTOM_JSON)
      prefs[Keys.ACTIVE_PRESET] = "manh"
      prefs[Keys.RED_DOT_DELTA] = 0
    }
  }

  suspend fun setActivePreset(presetId: String) {
    context.dataStore.edit { it[Keys.ACTIVE_PRESET] = presetId }
  }

  suspend fun setUseScale100(use100: Boolean) {
    context.dataStore.edit { it[Keys.USE_SCALE_100] = use100 }
  }

  suspend fun adjustRedDotDelta(deltaChange: Int) {
    context.dataStore.edit { prefs ->
      val current = prefs[Keys.RED_DOT_DELTA] ?: 0
      prefs[Keys.RED_DOT_DELTA] = (current + deltaChange).coerceIn(-40, 40)
    }
  }

  suspend fun resetRedDotDelta() {
    context.dataStore.edit { it[Keys.RED_DOT_DELTA] = 0 }
  }

  suspend fun toggleInstallStep(index: Int) {
    context.dataStore.edit { prefs ->
      val current = (prefs[Keys.COMPLETED_STEPS] ?: emptySet()).toMutableSet()
      val key = index.toString()
      if (current.contains(key)) current.remove(key) else current.add(key)
      prefs[Keys.COMPLETED_STEPS] = current
    }
  }

  suspend fun setCalibrationScore(zone: String, hits: Int) {
    val clamped = hits.coerceIn(0, 10)
    context.dataStore.edit { prefs ->
      when (zone) {
        "near" -> prefs[Keys.NEAR_HITS] = clamped
        "mid" -> prefs[Keys.MID_HITS] = clamped
        "far" -> prefs[Keys.FAR_HITS] = clamped
      }
    }
  }
}
