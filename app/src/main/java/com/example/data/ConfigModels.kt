package com.example.data

data class FfConfig(
  val configId: String,
  val build: String,
  val platform: String,
  val packageName: String,
  val game: String,
  val root: Boolean,
  val installTool: String,
  val installPath: String,
  val requiresRoot: Boolean,
  val fileRole: String,
  val source: String,
  val mechanism: String,
  val assistStrength: AssistStrength,
  val deviceTarget: DeviceTarget,
  val decisionLogic: DecisionLogic,
  val distanceProfiles: DistanceProfiles,
  val behaviorSimulation: BehaviorSimulation,
  val fingerFeedback: FingerFeedback,
  val controlsConfig: ControlsConfig,
  val hudLayout: HudLayout,
  val audio: AudioConfig,
  val haptics: HapticsKeysConfig,
  val antiCheatStealth: AntiCheatStealth,
  val safetyCaps: SafetyCaps,
  val calibration: CalibrationConfig,
  val installSteps: List<String>,
  val tuning: TuningRules,
  val rawJson: String,
)

data class AssistStrength(
  val lockStrength: Int,
  val aimLightness: Int,
  val pullMultiplier: Double,
  val safeBandLock: Pair<Int, Int>,
  val safeBandLightness: Pair<Int, Int>,
  val hardCeilingLock: Int,
  val hardCeilingLightness: Int,
)

data class DeviceTarget(
  val model: String,
  val ramGb: Int,
  val lowMemoryMode: Boolean,
  val closeBgBeforeMatch: Boolean,
  val render: RenderTarget,
  val tickMs: Int,
  val thermalGuard: ThermalGuard,
  val ramGuard: RamGuard,
  val stutterCompensation: StutterCompensation,
)

data class RenderTarget(
  val resolution: String,
  val refreshHz: Int,
  val frameMs: Double,
  val dpi: Int,
  val fpsTarget: Int,
  val graphicsQuality: String,
  val antialias: Boolean,
  val shadows: Boolean,
  val reduceResolution: Boolean,
  val highFps: Boolean,
)

data class ThermalGuard(
  val enabled: Boolean,
  val reduceAfterMin: Int,
  val pullReductionPct: Double,
  val stepReductionPct: Double,
)

data class RamGuard(
  val enabled: Boolean,
  val freeRamMinMb: Int,
  val dropSmoothingSteps: Int,
  val pauseOnRamLow: Boolean,
)

data class StutterCompensation(
  val enabled: Boolean,
  val frameDropMs: Int,
  val extraPullPx: Int,
)

data class DecisionLogic(
  val zonePriority: List<String>,
  val preferNeckWhenChestLocked: Boolean,
  val softLockOnNeckAfterMisses: Int,
  val wallCheck: Boolean,
  val ignoreKnocked: Boolean,
  val zoneHysteresisMs: Int,
  val switchPenaltyMs: Int,
  val autoZoneByHeightRatio: Boolean,
)

data class DistanceProfiles(
  val autoSwitch: Boolean,
  val hysteresisPx: Int,
  val near: DistanceProfile,
  val mid: DistanceProfile,
  val far: DistanceProfile,
)

data class DistanceProfile(
  val id: String,
  val labelVi: String,
  val rangeM: Pair<Int, Int>,
  val headAnchorPx: Int,
  val pullPx: Int,
  val rampMs: Int,
  val creepPxS: Double,
  val capMult: Double,
)

data class BehaviorSimulation(
  val curveType: String,
  val frictionMultiplier: Double,
  val curveOnTurnRadius: Double,
  val arcVelocityCap: Int,
  val smoothingSteps: Int,
  val easingIn: String,
  val easingOut: String,
  val releaseReturnPct: Double,
  val settleMs: Int,
  val microDriftEnabled: Boolean,
  val driftAngle: Double,
  val randomJitterFactor: Double,
  val undershootPct: Double,
  val minDelayMs: Int,
  val maxDelayMs: Int,
  val distributionType: String,
  val skipOnReacquire: Boolean,
)

data class FingerFeedback(
  val earlyGuardMs: Int,
  val lateGuardMs: Int,
  val apexWindowMs: Int,
  val fireGateEnabled: Boolean,
  val gateBypassOnSniper: Boolean,
  val debounceMs: Int,
  val releaseRampMs: Int,
  val pressAreaJitterPx: Int,
  val doubleTapMergeMs: Int,
  val timingLearnerEnabled: Boolean,
  val sampleWindowShots: Int,
  val learningRate: Double,
  val maxCorrectionMs: Int,
  val hapticEnabled: Boolean,
  val patternLock: LongArray,
  val intensityLock: Double,
  val patternMiss: LongArray,
  val throttleMs: Int,
  val learnModeFirstNShots: Int,
  val burstLen: Int,
  val burstGapMs: Int,
  val maxShotsPerEngage: Int,
  val convertHoldToTapsOnFar: Boolean,
  val missRecoveryRePullPx: Int,
  val missRecoveryCooldownMs: Int,
  val missRecoveryMaxRetries: Int,
)

data class ControlsConfig(
  val touchResistance: Double,
  val sensScaleNative: String,
  val sensScaleFallback: String,
  val activePreset: String,
  val presets: Map<String, SensitivityPreset>,
  val ingameMenuMap: List<Pair<String, String>>,
)

data class SensitivityPreset(
  val id: String,
  val displayName: String,
  val note: String,
  val s200: SensitivityValues,
  val s100: SensitivityValues,
)

data class SensitivityValues(
  val general: Int,
  val redDot: Int,
  val scope2x: Int,
  val scope4x: Int,
  val awmScope: Int,
  val auxAim: Int,
  val freeLook: Int,
)

data class HudLayout(
  val anchorMap: Map<Int, String>,
  val cluster: String,
  val nodes: List<HudButtonNode>,
  val androidNote: String,
)

data class HudButtonNode(
  val key: String,
  val displayName: String,
  val dx: Double,
  val dy: Double,
  val anchor: Int,
  val scale: Double,
  val alpha: Double,
  val role: String,
)

data class AudioConfig(
  val soundEffect: Double,
  val killSound: Double,
  val gameVoice: Double,
  val music: Double,
)

data class HapticsKeysConfig(
  val key0: Int,
  val key6: Int,
  val others: Int,
)

data class AntiCheatStealth(
  val singleToolOnly: Boolean,
  val forbiddenStack: List<String>,
  val signalStabilization: Double,
  val randomizeCurveSeedPerBurst: Boolean,
  val sessionWarmupShots: Int,
  val occasionalOvershootPct: Double,
  val sensLockMidMatch: Boolean,
  val recalibrateEachSeason: Boolean,
)

data class SafetyCaps(
  val maxStepPx: Double,
  val maxTotalPullPx: Int,
  val minRampMs: Int,
  val tickRangeMs: Pair<Int, Int>,
  val maxPullShareOfScreenPct: Double,
  val minPullPxFloor: Int,
)

data class CalibrationConfig(
  val protocol: String,
  val passThreshold: String,
  val stepPct: Int,
  val log: List<CalibrationEntry>,
)

data class CalibrationEntry(
  val distanceM: Int,
  val zone: String,
  val headHits: String,
  val adjust: String,
)

data class TuningRules(
  val vangKhiXoay: String,
  val keoKhongToi: String,
  val nongSau10p: String,
  val doNotTouch: List<String>,
)
