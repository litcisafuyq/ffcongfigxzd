package com.example.data

import org.json.JSONArray
import org.json.JSONObject

object ConfigParser {

  fun parse(rawJson: String): Result<FfConfig> = runCatching {
    val root = JSONObject(rawJson)

    val assistObj = root.optJSONObject("assist_strength") ?: JSONObject()
    val assistStrength =
      AssistStrength(
        lockStrength = assistObj.optInt("lock_strength", 78),
        aimLightness = assistObj.optInt("aim_lightness", 62),
        pullMultiplier = assistObj.optDouble("pull_multiplier", 1.25),
        safeBandLock = assistObj.optIntPair("safe_band_lock", 60, 82),
        safeBandLightness = assistObj.optIntPair("safe_band_lightness", 52, 72),
        hardCeilingLock = assistObj.optInt("hard_ceiling_lock", 88),
        hardCeilingLightness = assistObj.optInt("hard_ceiling_lightness", 80),
      )

    val devObj = root.optJSONObject("device_target") ?: JSONObject()
    val renderObj = devObj.optJSONObject("render") ?: JSONObject()
    val thermalObj = devObj.optJSONObject("thermal_guard") ?: JSONObject()
    val ramObj = devObj.optJSONObject("ram_guard") ?: JSONObject()
    val stutterObj = devObj.optJSONObject("stutter_compensation") ?: JSONObject()

    val deviceTarget =
      DeviceTarget(
        model = devObj.optString("model", "Samsung Galaxy A17 5G"),
        ramGb = devObj.optInt("ram_gb", 4),
        lowMemoryMode = devObj.optBoolean("low_memory_mode", true),
        closeBgBeforeMatch = devObj.optBoolean("close_bg_before_match", true),
        render =
          RenderTarget(
            resolution = renderObj.optString("resolution", "540x1200"),
            refreshHz = renderObj.optInt("refresh_hz", 60),
            frameMs = renderObj.optDouble("frame_ms", 16.7),
            dpi = renderObj.optInt("dpi", 384),
            fpsTarget = renderObj.optInt("fps_target", 55),
            graphicsQuality = renderObj.optString("graphics_quality", "smooth"),
            antialias = renderObj.optBoolean("antialias", false),
            shadows = renderObj.optBoolean("shadows", false),
            reduceResolution = renderObj.optBoolean("reduce_resolution", true),
            highFps = renderObj.optBoolean("high_fps", true),
          ),
        tickMs = devObj.optInt("tick_ms", 17),
        thermalGuard =
          ThermalGuard(
            enabled = thermalObj.optBoolean("enabled", true),
            reduceAfterMin = thermalObj.optInt("reduce_after_min", 10),
            pullReductionPct = thermalObj.optDouble("pull_reduction_pct", 0.2),
            stepReductionPct = thermalObj.optDouble("step_reduction_pct", 0.3),
          ),
        ramGuard =
          RamGuard(
            enabled = ramObj.optBoolean("enabled", true),
            freeRamMinMb = ramObj.optInt("free_ram_min_mb", 400),
            dropSmoothingSteps = ramObj.optInt("drop_smoothing_steps", 6),
            pauseOnRamLow = ramObj.optBoolean("pause_on_ram_low", true),
          ),
        stutterCompensation =
          StutterCompensation(
            enabled = stutterObj.optBoolean("enabled", true),
            frameDropMs = stutterObj.optInt("frame_drop_ms", 35),
            extraPullPx = stutterObj.optInt("extra_pull_px", 1),
          ),
      )

    val decObj = root.optJSONObject("decision_logic") ?: JSONObject()
    val decisionLogic =
      DecisionLogic(
        zonePriority = decObj.optStringList("zone_priority", listOf("head", "neck", "chest")),
        preferNeckWhenChestLocked = decObj.optBoolean("prefer_neck_when_chest_locked", true),
        softLockOnNeckAfterMisses = decObj.optInt("soft_lock_on_neck_after_misses", 2),
        wallCheck = decObj.optBoolean("wall_check", true),
        ignoreKnocked = decObj.optBoolean("ignore_knocked", true),
        zoneHysteresisMs = decObj.optInt("zone_hysteresis_ms", 100),
        switchPenaltyMs = decObj.optInt("switch_penalty_ms", 60),
        autoZoneByHeightRatio = decObj.optBoolean("auto_zone_by_height_ratio", true),
      )

    val distObj = root.optJSONObject("distance_profiles") ?: JSONObject()
    val distanceProfiles =
      DistanceProfiles(
        autoSwitch = distObj.optBoolean("auto_switch", true),
        hysteresisPx = distObj.optInt("hysteresis_px", 6),
        near = parseDistanceProfile(distObj.optJSONObject("near"), "near", "Cự ly gần", 0 to 15, 14, 78, 5.5, 1.35),
        mid = parseDistanceProfile(distObj.optJSONObject("mid"), "mid", "Cự ly trung", 15 to 40, 8, 92, 2.2, 1.35),
        far = parseDistanceProfile(distObj.optJSONObject("far"), "far", "Cự ly xa", 40 to 99, 3, 115, 1.0, 1.55),
      )

    val behObj = root.optJSONObject("behavior_simulation") ?: JSONObject()
    val reactObj = behObj.optJSONObject("reaction_latency_simulation") ?: JSONObject()
    val behaviorSimulation =
      BehaviorSimulation(
        curveType = behObj.optString("curve_type", "bezier_smooth_nested"),
        frictionMultiplier = behObj.optDouble("friction_multiplier", 0.3),
        curveOnTurnRadius = behObj.optDouble("curve_on_turn_radius", 0.3),
        arcVelocityCap = behObj.optInt("arc_velocity_cap", 300),
        smoothingSteps = behObj.optInt("smoothing_steps", 16),
        easingIn = behObj.optString("easing_in", "ease_out_quad"),
        easingOut = behObj.optString("easing_out", "ease_in_sine"),
        releaseReturnPct = behObj.optDouble("release_return_pct", 0.55),
        settleMs = behObj.optInt("settle_ms", 130),
        microDriftEnabled = behObj.optBoolean("micro_drift_enabled", true),
        driftAngle = behObj.optDouble("drift_angle", 0.05),
        randomJitterFactor = behObj.optDouble("random_jitter_factor", 0.1),
        undershootPct = behObj.optDouble("undershoot_pct", 0.02),
        minDelayMs = reactObj.optInt("min_delay_ms", 140),
        maxDelayMs = reactObj.optInt("max_delay_ms", 175),
        distributionType = reactObj.optString("distribution_type", "gaussian_random"),
        skipOnReacquire = reactObj.optBoolean("skip_on_reacquire", true),
      )

    val ffObj = root.optJSONObject("finger_feedback") ?: JSONObject()
    val shapeObj = ffObj.optJSONObject("input_shaping") ?: JSONObject()
    val learnerObj = ffObj.optJSONObject("timing_learner") ?: JSONObject()
    val hapticObj = ffObj.optJSONObject("haptic_feedback") ?: JSONObject()
    val burstObj = ffObj.optJSONObject("burst_governor") ?: JSONObject()
    val missObj = ffObj.optJSONObject("miss_recovery") ?: JSONObject()

    val fingerFeedback =
      FingerFeedback(
        earlyGuardMs = shapeObj.optInt("early_guard_ms", 65),
        lateGuardMs = shapeObj.optInt("late_guard_ms", 45),
        apexWindowMs = shapeObj.optInt("apex_window_ms", 100),
        fireGateEnabled = shapeObj.optBoolean("fire_gate_enabled", true),
        gateBypassOnSniper = shapeObj.optBoolean("gate_bypass_on_sniper", true),
        debounceMs = shapeObj.optInt("debounce_ms", 110),
        releaseRampMs = shapeObj.optInt("release_ramp_ms", 55),
        pressAreaJitterPx = shapeObj.optInt("press_area_jitter_px", 2),
        doubleTapMergeMs = shapeObj.optInt("double_tap_merge_ms", 90),
        timingLearnerEnabled = learnerObj.optBoolean("enabled", true),
        sampleWindowShots = learnerObj.optInt("sample_window_shots", 8),
        learningRate = learnerObj.optDouble("learning_rate", 0.25),
        maxCorrectionMs = learnerObj.optInt("max_correction_ms", 50),
        hapticEnabled = hapticObj.optBoolean("enabled", true),
        patternLock = hapticObj.optLongArray("pattern_lock", longArrayOf(20L, 45L, 20L)),
        intensityLock = hapticObj.optDouble("intensity_lock", 0.75),
        patternMiss = hapticObj.optLongArray("pattern_miss", longArrayOf(32L)),
        throttleMs = hapticObj.optInt("throttle_ms", 450),
        learnModeFirstNShots = hapticObj.optInt("learn_mode_first_n_shots", 25),
        burstLen = burstObj.optInt("burst_len", 3),
        burstGapMs = burstObj.optInt("burst_gap_ms", 145),
        maxShotsPerEngage = burstObj.optInt("max_shots_per_engage", 7),
        convertHoldToTapsOnFar = burstObj.optBoolean("convert_hold_to_taps_on_far", true),
        missRecoveryRePullPx = missObj.optInt("re_pull_px", 4),
        missRecoveryCooldownMs = missObj.optInt("cooldown_ms", 220),
        missRecoveryMaxRetries = missObj.optInt("max_retries", 2),
      )

    val ctrlObj = root.optJSONObject("controls_config") ?: JSONObject()
    val presetsObj = ctrlObj.optJSONObject("presets") ?: JSONObject()
    val presetsMap = linkedMapOf<String, SensitivityPreset>()
    listOf("manh", "can_bang", "thuan").forEach { key ->
      val pObj = presetsObj.optJSONObject(key)
      if (pObj != null) {
        presetsMap[key] =
          SensitivityPreset(
            id = key,
            displayName = presetDisplayName(key),
            note = pObj.optString("note", ""),
            s200 = parseSensitivityValues(pObj.optJSONObject("s200")),
            s100 = parseSensitivityValues(pObj.optJSONObject("s100")),
          )
      }
    }
    presetsObj.keys().forEach { key ->
      if (!presetsMap.containsKey(key)) {
        val pObj = presetsObj.optJSONObject(key)
        if (pObj != null) {
          presetsMap[key] =
            SensitivityPreset(
              id = key,
              displayName = presetDisplayName(key),
              note = pObj.optString("note", ""),
              s200 = parseSensitivityValues(pObj.optJSONObject("s200")),
              s100 = parseSensitivityValues(pObj.optJSONObject("s100")),
            )
        }
      }
    }

    val menuObj = ctrlObj.optJSONObject("ingame_menu_map") ?: JSONObject()
    val menuList = mutableListOf<Pair<String, String>>()
    menuObj.keys().forEach { k -> menuList.add(k to menuObj.optString(k, "")) }

    val controlsConfig =
      ControlsConfig(
        touchResistance = ctrlObj.optDouble("touch_resistance", 0.3),
        sensScaleNative = ctrlObj.optString("sens_scale_native", "0-200"),
        sensScaleFallback = ctrlObj.optString("sens_scale_fallback", "0-100"),
        activePreset = ctrlObj.optString("active_preset", "manh"),
        presets = presetsMap,
        ingameMenuMap = menuList,
      )

    val hudObj = root.optJSONObject("hud_layout") ?: JSONObject()
    val anchorMapObj = hudObj.optJSONObject("anchor_map") ?: JSONObject()
    val anchorMap = mutableMapOf<Int, String>()
    anchorMapObj.keys().forEach { k ->
      k.toIntOrNull()?.let { id -> anchorMap[id] = anchorMapObj.optString(k, "") }
    }

    val hudOrder =
      listOf(
        "hudauxaim" to "Ngắm phụ",
        "hudfire" to "Nút bắn",
        "hudsight" to "Nút ngắm",
        "hudjump" to "Nhảy",
        "hudcrouch" to "Ngồi",
        "hudcreep" to "Trườn",
        "hudreload" to "Nạp đạn",
        "hudswapweaponquick" to "Đổi súng ẩn",
      )
    val hudNodes =
      hudOrder.mapNotNull { (key, label) ->
        val node = hudObj.optJSONObject(key) ?: return@mapNotNull null
        HudButtonNode(
          key = key,
          displayName = label,
          dx = node.optDouble("dx", 0.0),
          dy = node.optDouble("dy", 0.0),
          anchor = node.optInt("anchor", 7),
          scale = node.optDouble("scale", 1.0),
          alpha = node.optDouble("alpha", 1.0),
          role = node.optString("role", defaultHudRole(key)),
        )
      }

    val hudLayout =
      HudLayout(
        anchorMap = anchorMap,
        cluster = hudObj.optString("cluster", "phải-dưới, một ngón cái"),
        nodes = hudNodes,
        androidNote = hudObj.optString("android_note", ""),
      )

    val audioObj = root.optJSONObject("audio") ?: JSONObject()
    val hapticsKeysObj = root.optJSONObject("haptics") ?: JSONObject()
    val acObj = root.optJSONObject("anti_cheat_stealth") ?: JSONObject()
    val capsObj = root.optJSONObject("safety_caps") ?: JSONObject()
    val calObj = root.optJSONObject("calibration") ?: JSONObject()
    val calArr = calObj.optJSONArray("log") ?: JSONArray()
    val calEntries =
      (0 until calArr.length()).mapNotNull { idx ->
        val item = calArr.optJSONObject(idx) ?: return@mapNotNull null
        CalibrationEntry(
          distanceM = item.optInt("distance_m", 0),
          zone = item.optString("zone", ""),
          headHits = item.optString("head_hits", "__/10"),
          adjust = item.optString("adjust", ""),
        )
      }

    val tuningObj = root.optJSONObject("tuning") ?: JSONObject()

    FfConfig(
      configId = root.optString("config_id", "VN_CONFIG"),
      build = root.optString("build", ""),
      platform = root.optString("platform", "android"),
      packageName = root.optString("package", "com.garena.ffvn"),
      game = root.optString("game", "Free Fire VN"),
      root = root.optBoolean("root", false),
      installTool = root.optString("install_tool", "ZArchiver"),
      installPath = root.optString("install_path", ""),
      requiresRoot = root.optBoolean("requires_root", false),
      fileRole = root.optString("file_role", ""),
      source = root.optString("source", ""),
      mechanism = root.optString("mechanism", ""),
      assistStrength = assistStrength,
      deviceTarget = deviceTarget,
      decisionLogic = decisionLogic,
      distanceProfiles = distanceProfiles,
      behaviorSimulation = behaviorSimulation,
      fingerFeedback = fingerFeedback,
      controlsConfig = controlsConfig,
      hudLayout = hudLayout,
      audio =
        AudioConfig(
          soundEffect = audioObj.optDouble("sound_effect", 0.8),
          killSound = audioObj.optDouble("kill_sound", 0.8),
          gameVoice = audioObj.optDouble("game_voice", 0.8),
          music = audioObj.optDouble("music", 0.0),
        ),
      haptics =
        HapticsKeysConfig(
          key0 = hapticsKeysObj.optInt("key_0", 1),
          key6 = hapticsKeysObj.optInt("key_6", 1),
          others = hapticsKeysObj.optInt("others", 0),
        ),
      antiCheatStealth =
        AntiCheatStealth(
          singleToolOnly = acObj.optBoolean("single_tool_only", true),
          forbiddenStack = acObj.optStringList("forbidden_stack", emptyList()),
          signalStabilization = acObj.optDouble("signal_stabilization", 0.98),
          randomizeCurveSeedPerBurst = acObj.optBoolean("randomize_curve_seed_per_burst", true),
          sessionWarmupShots = acObj.optInt("session_warmup_shots", 3),
          occasionalOvershootPct = acObj.optDouble("occasional_overshoot_pct", 0.03),
          sensLockMidMatch = acObj.optBoolean("sens_lock_mid_match", true),
          recalibrateEachSeason = acObj.optBoolean("recalibrate_each_season", true),
        ),
      safetyCaps =
        SafetyCaps(
          maxStepPx = capsObj.optDouble("max_step_px", 3.5),
          maxTotalPullPx = capsObj.optInt("max_total_pull_px", 26),
          minRampMs = capsObj.optInt("min_ramp_ms", 75),
          tickRangeMs = capsObj.optIntPair("tick_range_ms", 16, 22),
          maxPullShareOfScreenPct = capsObj.optDouble("max_pull_share_of_screen_pct", 2.0),
          minPullPxFloor = capsObj.optInt("min_pull_px_floor", 3),
        ),
      calibration =
        CalibrationConfig(
          protocol = calObj.optString("protocol", "training_ground_3_range"),
          passThreshold = calObj.optString("pass_threshold", "8/10 head"),
          stepPct = calObj.optInt("step_pct", 10),
          log = calEntries,
        ),
      installSteps = root.optStringList("install_steps", emptyList()),
      tuning =
        TuningRules(
          vangKhiXoay = tuningObj.optString("vang_khi_xoay", ""),
          keoKhongToi = tuningObj.optString("keo_khong_toi", ""),
          nongSau10p = tuningObj.optString("nong_sau_10p", ""),
          doNotTouch = tuningObj.optStringList("do_not_touch", emptyList()),
        ),
      rawJson = rawJson,
    )
  }

  fun buildExportJson(
    baseRawJson: String,
    activePreset: String,
    redDotDelta: Int,
    nearHits: Int?,
    midHits: Int?,
    farHits: Int?,
  ): String {
    return runCatching {
      val root = JSONObject(baseRawJson)
      val controls = root.optJSONObject("controls_config")
      if (controls != null) {
        controls.put("active_preset", activePreset)
        if (redDotDelta != 0) {
          val presets = controls.optJSONObject("presets")
          val activeObj = presets?.optJSONObject(activePreset)
          val s200 = activeObj?.optJSONObject("s200")
          val s100 = activeObj?.optJSONObject("s100")
          if (s200 != null) {
            val baseRed = s200.optInt("red_dot", 160)
            s200.put("red_dot", (baseRed + redDotDelta).coerceIn(0, 200))
          }
          if (s100 != null) {
            val baseRed100 = s100.optInt("red_dot", 80)
            s100.put("red_dot", (baseRed100 + redDotDelta / 2).coerceIn(0, 100))
          }
        }
      }
      val cal = root.optJSONObject("calibration")
      val logArr = cal?.optJSONArray("log")
      if (logArr != null) {
        val values = listOf(nearHits, midHits, farHits)
        for (i in 0 until minOf(logArr.length(), values.size)) {
          val score = values[i]
          if (score != null) {
            logArr.optJSONObject(i)?.put("head_hits", "$score/10")
          }
        }
      }
      root.toString(2)
    }.getOrElse { baseRawJson }
  }

  private fun parseDistanceProfile(
    obj: JSONObject?,
    id: String,
    labelVi: String,
    defaultRange: Pair<Int, Int>,
    defaultPull: Int,
    defaultRamp: Int,
    defaultCreep: Double,
    defaultCap: Double,
  ): DistanceProfile {
    if (obj == null) {
      return DistanceProfile(
        id = id,
        labelVi = labelVi,
        rangeM = defaultRange,
        headAnchorPx = defaultPull,
        pullPx = defaultPull,
        rampMs = defaultRamp,
        creepPxS = defaultCreep,
        capMult = defaultCap,
      )
    }
    return DistanceProfile(
      id = id,
      labelVi = labelVi,
      rangeM = obj.optIntPair("range_m", defaultRange.first, defaultRange.second),
      headAnchorPx = obj.optInt("head_anchor_px", defaultPull),
      pullPx = obj.optInt("pull_px", defaultPull),
      rampMs = obj.optInt("ramp_ms", defaultRamp),
      creepPxS = obj.optDouble("creep_px_s", defaultCreep),
      capMult = obj.optDouble("cap_mult", defaultCap),
    )
  }

  private fun parseSensitivityValues(obj: JSONObject?): SensitivityValues {
    if (obj == null) {
      return SensitivityValues(175, 160, 200, 200, 200, 200, 15)
    }
    return SensitivityValues(
      general = obj.optInt("general", 175),
      redDot = obj.optInt("red_dot", 160),
      scope2x = obj.optInt("scope_2x", 200),
      scope4x = obj.optInt("scope_4x", 200),
      awmScope = obj.optInt("awm_scope", 200),
      auxAim = obj.optInt("aux_aim", 200),
      freeLook = obj.optInt("free_look", 15),
    )
  }

  private fun presetDisplayName(key: String): String =
    when (key) {
      "manh" -> "Mạnh"
      "can_bang" -> "Cân bằng"
      "thuan" -> "Thuần"
      else -> key.replaceFirstChar { it.uppercase() }
    }

  private fun defaultHudRole(key: String): String =
    when (key) {
      "hudjump" -> "Nhảy nhanh trong cụm ngón cái phải"
      "hudcrouch" -> "Ngồi / né đạn nhanh"
      "hudcreep" -> "Nằm / trườn bên trái nút ngồi"
      "hudreload" -> "Nạp đạn tầm với dưới trái"
      else -> "Vị trí chuẩn theo bảng tọa độ HUD"
    }

  private fun JSONObject.optIntPair(key: String, firstDef: Int, secondDef: Int): Pair<Int, Int> {
    val arr = optJSONArray(key) ?: return firstDef to secondDef
    val a = if (arr.length() > 0) arr.optInt(0, firstDef) else firstDef
    val b = if (arr.length() > 1) arr.optInt(1, secondDef) else secondDef
    return a to b
  }

  private fun JSONObject.optStringList(key: String, default: List<String>): List<String> {
    val arr = optJSONArray(key) ?: return default
    return (0 until arr.length()).map { arr.optString(it, "") }.filter { it.isNotBlank() }
  }

  private fun JSONObject.optLongArray(key: String, default: LongArray): LongArray {
    val arr = optJSONArray(key) ?: return default
    if (arr.length() == 0) return default
    return LongArray(arr.length()) { idx -> arr.optLong(idx, 20L).coerceAtLeast(5L) }
  }
}
