package com.example.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ConfigParser
import com.example.data.ConfigRepository
import com.example.data.DeviceTelemetry
import com.example.data.DeviceTelemetryMonitor
import com.example.data.FfConfig
import com.example.data.SensitivityPreset
import com.example.data.SensitivityValues
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppTab {
  OVERVIEW,
  SENSITIVITY,
  HUD,
  CALIBRATION,
}

enum class HudSubMode {
  BLUEPRINT,
  PRACTICE_PAD,
}

data class PracticeAttempt(
  val distanceProfileId: String,
  val targetZone: String,
  val pullPx: Float,
  val rampMs: Long,
  val isHeadLock: Boolean,
  val feedbackText: String,
)

data class MainUiState(
  val isLoading: Boolean = true,
  val errorMessage: String? = null,
  val config: FfConfig? = null,
  val isCustomJsonLoaded: Boolean = false,
  val telemetry: DeviceTelemetry? = null,
  val activePresetId: String = "manh",
  val useScale100: Boolean = false,
  val redDotDelta: Int = 0,
  val completedInstallSteps: Set<Int> = emptySet(),
  val nearHits: Int? = null,
  val midHits: Int? = null,
  val farHits: Int? = null,
  val selectedHudKey: String = "hudauxaim",
  val hudSubMode: HudSubMode = HudSubMode.BLUEPRINT,
  val showDragZoneOverlay: Boolean = true,
  val showHudGrid: Boolean = true,
  val showHudDistances: Boolean = true,
  val practiceProfileId: String = "near",
  val practiceAttempts: Int = 0,
  val practiceHeadHits: Int = 0,
  val lastPracticeAttempt: PracticeAttempt? = null,
  val matchTimerRunning: Boolean = false,
  val matchElapsedSeconds: Int = 0,
) {
  val currentPreset: SensitivityPreset?
    get() = config?.controlsConfig?.presets?.get(activePresetId)
      ?: config?.controlsConfig?.presets?.values?.firstOrNull()

  fun effectiveSensitivity(): SensitivityValues? {
    val preset = currentPreset ?: return null
    val base = if (useScale100) preset.s100 else preset.s200
    val maxVal = if (useScale100) 100 else 200
    val delta = if (useScale100) redDotDelta / 2 else redDotDelta
    return base.copy(redDot = (base.redDot + delta).coerceIn(0, maxVal))
  }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = ConfigRepository(application)
  private val telemetryMonitor = DeviceTelemetryMonitor(application)

  private val _uiState = MutableStateFlow(MainUiState())
  val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

  private val _events = MutableSharedFlow<String>()
  val events: SharedFlow<String> = _events.asSharedFlow()

  private var timerJob: Job? = null

  init {
    val bundledJson = repository.loadBundledJson()
    viewModelScope.launch {
      repository.prefsFlow.collect { prefs ->
        val raw = prefs.customJson ?: bundledJson
        val parsed = ConfigParser.parse(raw)
        parsed
          .onSuccess { cfg ->
            val telemetry = telemetryMonitor.capture(cfg.packageName)
            val resolvedPreset =
              if (cfg.controlsConfig.presets.containsKey(prefs.activePreset)) {
                prefs.activePreset
              } else {
                cfg.controlsConfig.activePreset
              }
            _uiState.update {
              it.copy(
                isLoading = false,
                errorMessage = null,
                config = cfg,
                isCustomJsonLoaded = prefs.customJson != null,
                telemetry = telemetry,
                activePresetId = resolvedPreset,
                useScale100 = prefs.useScale100,
                redDotDelta = prefs.redDotDelta,
                completedInstallSteps = prefs.completedInstallSteps,
                nearHits = prefs.nearHits,
                midHits = prefs.midHits,
                farHits = prefs.farHits,
              )
            }
          }
          .onFailure { err ->
            _uiState.update {
              it.copy(
                isLoading = false,
                errorMessage = "Lỗi đọc JSON: ${err.message ?: "Định dạng không hợp lệ"}",
              )
            }
          }
      }
    }
  }

  fun refreshTelemetry() {
    val pkg = _uiState.value.config?.packageName ?: "com.garena.ffvn"
    val telemetry = telemetryMonitor.capture(pkg)
    _uiState.update { it.copy(telemetry = telemetry) }
    emitEvent("Đã cập nhật RAM (${telemetry.availRamMb} MB trống) & nhiệt độ (${telemetry.batteryTempCelsius}°C)")
  }

  fun importJson(rawJson: String) {
    val parsed = ConfigParser.parse(rawJson)
    parsed
      .onSuccess { cfg ->
        viewModelScope.launch {
          repository.saveCustomJson(rawJson, cfg.controlsConfig.activePreset)
          emitEvent("Đã nạp cấu hình ${cfg.configId}")
        }
      }
      .onFailure { err ->
        emitEvent("Tệp JSON không hợp lệ: ${err.message ?: "Kiểm tra lại cú pháp"}")
      }
  }

  fun resetToDefaultJson() {
    viewModelScope.launch {
      repository.resetToBundledDefault()
      emitEvent("Đã khôi phục cấu hình gốc VN_A17_NHETAM_V6_FINAL")
    }
  }

  fun buildExportableJson(): String {
    val state = _uiState.value
    val raw = state.config?.rawJson ?: repository.loadBundledJson()
    return ConfigParser.buildExportJson(
      baseRawJson = raw,
      activePreset = state.activePresetId,
      redDotDelta = state.redDotDelta,
      nearHits = state.nearHits,
      midHits = state.midHits,
      farHits = state.farHits,
    )
  }

  fun selectPreset(presetId: String) {
    viewModelScope.launch {
      repository.setActivePreset(presetId)
      val name = _uiState.value.config?.controlsConfig?.presets?.get(presetId)?.displayName ?: presetId
      emitEvent("Đã chuyển sang preset $name")
    }
  }

  fun toggleScale100(use100: Boolean) {
    viewModelScope.launch { repository.setUseScale100(use100) }
  }

  fun bumpRedDotPlus10() {
    viewModelScope.launch {
      repository.adjustRedDotDelta(10)
      emitEvent("Đã tăng Hồng tâm +10 (giữ nguyên Ngắm phụ)")
    }
  }

  fun resetRedDotOffset() {
    viewModelScope.launch {
      repository.resetRedDotDelta()
      emitEvent("Đã đặt lại bù Hồng tâm về 0")
    }
  }

  fun toggleInstallStep(index: Int) {
    viewModelScope.launch { repository.toggleInstallStep(index) }
  }

  fun selectHudNode(key: String) {
    _uiState.update { it.copy(selectedHudKey = key) }
  }

  fun setHudSubMode(mode: HudSubMode) {
    _uiState.update { it.copy(hudSubMode = mode) }
  }

  fun toggleDragZoneOverlay() {
    _uiState.update { it.copy(showDragZoneOverlay = !it.showDragZoneOverlay) }
  }

  fun toggleHudGrid() {
    _uiState.update { it.copy(showHudGrid = !it.showHudGrid) }
  }

  fun toggleHudDistances() {
    _uiState.update { it.copy(showHudDistances = !it.showHudDistances) }
  }

  fun selectPracticeProfile(profileId: String) {
    _uiState.update { it.copy(practiceProfileId = profileId) }
  }

  fun recordPracticeSwipe(pullPx: Float, rampMs: Long, hitZone: String) {
    val cfg = _uiState.value.config ?: return
    val profile =
      when (_uiState.value.practiceProfileId) {
        "mid" -> cfg.distanceProfiles.mid
        "far" -> cfg.distanceProfiles.far
        else -> cfg.distanceProfiles.near
      }
    val caps = cfg.safetyCaps
    val isHeadLock = hitZone == "head" && rampMs >= caps.minRampMs && pullPx <= caps.maxTotalPullPx * 2.5f
    val feedback =
      when {
        pullPx < caps.minPullPxFloor -> "Kéo quá ngắn (<${caps.minPullPxFloor}px) — tăng lực kéo nhẹ lên ${profile.pullPx}px"
        rampMs < caps.minRampMs -> "Quá nhanh (${rampMs}ms < ${caps.minRampMs}ms) — dễ văng tâm qua đầu"
        hitZone == "head" -> "Khóa đầu chuẩn! (${pullPx.toInt()}px / ${rampMs}ms)"
        hitZone == "neck" -> "Chạm cổ — nhích thêm ~${(profile.pullPx * 0.25f).toInt().coerceAtLeast(1)}px"
        else -> "Trúng ngực / hụt đầu — thử ramp ~${profile.rampMs}ms"
      }

    if (cfg.fingerFeedback.hapticEnabled) {
      if (isHeadLock) {
        val amp = (cfg.fingerFeedback.intensityLock * 255).toInt().coerceIn(80, 255)
        telemetryMonitor.playHapticPattern(cfg.fingerFeedback.patternLock, amp)
      } else {
        telemetryMonitor.playHapticPattern(cfg.fingerFeedback.patternMiss, 130)
      }
    }

    _uiState.update { state ->
      state.copy(
        practiceAttempts = state.practiceAttempts + 1,
        practiceHeadHits = state.practiceHeadHits + if (isHeadLock) 1 else 0,
        lastPracticeAttempt =
          PracticeAttempt(
            distanceProfileId = profile.id,
            targetZone = hitZone,
            pullPx = pullPx,
            rampMs = rampMs,
            isHeadLock = isHeadLock,
            feedbackText = feedback,
          ),
      )
    }
  }

  fun resetPracticeStats() {
    _uiState.update {
      it.copy(practiceAttempts = 0, practiceHeadHits = 0, lastPracticeAttempt = null)
    }
  }

  fun setCalibrationScore(zone: String, hits: Int) {
    viewModelScope.launch { repository.setCalibrationScore(zone, hits) }
  }

  fun toggleMatchThermalTimer() {
    if (_uiState.value.matchTimerRunning) {
      timerJob?.cancel()
      _uiState.update { it.copy(matchTimerRunning = false, matchElapsedSeconds = 0) }
    } else {
      _uiState.update { it.copy(matchTimerRunning = true, matchElapsedSeconds = 0) }
      timerJob?.cancel()
      timerJob =
        viewModelScope.launch {
          val thresholdMin = _uiState.value.config?.deviceTarget?.thermalGuard?.reduceAfterMin ?: 10
          var notified = false
          while (true) {
            delay(1000L)
            val nextSec = _uiState.value.matchElapsedSeconds + 1
            _uiState.update { it.copy(matchElapsedSeconds = nextSec) }
            if (!notified && nextSec >= thresholdMin * 60) {
              notified = true
              emitEvent("Đã chạm mốc ${thresholdMin}p! Nên chuyển preset Thuần hoặc nghỉ 2 phút")
            }
          }
        }
    }
  }

  fun createLaunchFreeFireIntent(): Intent? {
    val pkg = _uiState.value.config?.packageName ?: "com.garena.ffvn"
    return telemetryMonitor.createLaunchIntent(pkg)
  }

  fun buildFormattedSensitivitySummary(): String {
    val state = _uiState.value
    val cfg = state.config ?: return ""
    val preset = state.currentPreset ?: return ""
    val eff = state.effectiveSensitivity() ?: return ""
    val scaleLabel = if (state.useScale100) "0-100" else "0-200"
    return buildString {
      appendLine("FFVN Config (${cfg.configId}) - Preset: ${preset.displayName} [$scaleLabel]")
      appendLine("• Nhìn xung quanh: ${eff.general}")
      appendLine("• Hồng tâm (Red Dot): ${eff.redDot}")
      appendLine("• Ống ngắm 2x: ${eff.scope2x}")
      appendLine("• Ống ngắm 4x: ${eff.scope4x}")
      appendLine("• Ống ngắm AWM: ${eff.awmScope}")
      appendLine("• Ngắm phụ (Aux Aim): ${eff.auxAim} (Kéo hết thanh)")
      appendLine("• Nhìn tự do: ${eff.freeLook}")
      appendLine("• Trợ ngắm: TẮT | Độ chính xác: Rất chính xác | Bắn: Hold")
    }
  }

  fun notifyMessage(msg: String) {
    emitEvent(msg)
  }

  private fun emitEvent(msg: String) {
    viewModelScope.launch { _events.emit(msg) }
  }

  override fun onCleared() {
    super.onCleared()
    timerJob?.cancel()
  }
}
