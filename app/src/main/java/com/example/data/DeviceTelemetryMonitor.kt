package com.example.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import kotlin.math.roundToInt

data class DeviceTelemetry(
  val deviceModel: String,
  val availRamMb: Long,
  val totalRamMb: Long,
  val isLowMemory: Boolean,
  val batteryLevelPct: Int,
  val batteryTempCelsius: Float,
  val isCharging: Boolean,
  val thermalLabelVi: String,
  val isThermalElevated: Boolean,
  val screenWidthPx: Int,
  val screenHeightPx: Int,
  val densityDpi: Int,
  val refreshHz: Int,
  val isFreeFireInstalled: Boolean,
)

class DeviceTelemetryMonitor(private val context: Context) {

  fun capture(targetPackage: String): DeviceTelemetry {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memInfo)
    val availRamMb = memInfo.availMem / (1024L * 1024L)
    val totalRamMb = memInfo.totalMem / (1024L * 1024L)

    val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
    val batteryPct = if (level >= 0 && scale > 0) (level * 100) / scale else 0
    val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 300) ?: 300
    val batteryTempC = tempTenths / 10f
    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging =
      status == BatteryManager.BATTERY_STATUS_CHARGING ||
        status == BatteryManager.BATTERY_STATUS_FULL

    val (thermalLabel, thermalElevated) = readThermalStatus(batteryTempC)

    val displayMetrics = context.resources.displayMetrics
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val widthPx: Int
    val heightPx: Int
    val refreshHz: Int
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      val bounds = windowManager.currentWindowMetrics.bounds
      widthPx = bounds.width()
      heightPx = bounds.height()
      @Suppress("DEPRECATION")
      val rate =
        runCatching { context.display?.refreshRate }.getOrNull()
          ?: windowManager.defaultDisplay.refreshRate
      refreshHz = rate.roundToInt().coerceAtLeast(60)
    } else {
      @Suppress("DEPRECATION")
      val display = windowManager.defaultDisplay
      widthPx = displayMetrics.widthPixels
      heightPx = displayMetrics.heightPixels
      refreshHz = display.refreshRate.roundToInt().coerceAtLeast(60)
    }

    val isInstalled =
      runCatching {
        context.packageManager.getPackageInfo(targetPackage, 0)
        true
      }.getOrDefault(false)

    return DeviceTelemetry(
      deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
      availRamMb = availRamMb,
      totalRamMb = totalRamMb,
      isLowMemory = memInfo.lowMemory,
      batteryLevelPct = batteryPct,
      batteryTempCelsius = batteryTempC,
      isCharging = isCharging,
      thermalLabelVi = thermalLabel,
      isThermalElevated = thermalElevated,
      screenWidthPx = minOf(widthPx, heightPx),
      screenHeightPx = maxOf(widthPx, heightPx),
      densityDpi = displayMetrics.densityDpi,
      refreshHz = refreshHz,
      isFreeFireInstalled = isInstalled,
    )
  }

  fun createLaunchIntent(targetPackage: String): Intent? {
    return context.packageManager.getLaunchIntentForPackage(targetPackage)?.apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
  }

  fun playHapticPattern(durationsMs: LongArray, amplitude: Int) {
    if (durationsMs.isEmpty()) return
    val vibrator =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
      } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
      }
    if (!vibrator.hasVibrator()) return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      if (durationsMs.size == 1) {
        vibrator.vibrate(
          VibrationEffect.createOneShot(durationsMs[0].coerceAtLeast(10L), amplitude.coerceIn(1, 255))
        )
      } else {
        // JSON pattern [20, 45, 20] represents pulse-gap-pulse; waveform expects [delay, on, off, on]
        val timings = longArrayOf(0L) + durationsMs
        val amps =
          IntArray(timings.size) { index ->
            if (index == 0) 0 else if (index % 2 == 1) amplitude.coerceIn(1, 255) else 0
          }
        vibrator.vibrate(VibrationEffect.createWaveform(timings, amps, -1))
      }
    } else {
      @Suppress("DEPRECATION")
      vibrator.vibrate(durationsMs.firstOrNull() ?: 25L)
    }
  }

  private fun readThermalStatus(batteryTempC: Float): Pair<String, Boolean> {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
      return when (powerManager.currentThermalStatus) {
        PowerManager.THERMAL_STATUS_NONE -> "Mát / Ổn định" to false
        PowerManager.THERMAL_STATUS_LIGHT -> "Ấm nhẹ" to false
        PowerManager.THERMAL_STATUS_MODERATE -> "Ấm vừa (cân nhắc Thuần)" to true
        PowerManager.THERMAL_STATUS_SEVERE,
        PowerManager.THERMAL_STATUS_CRITICAL,
        PowerManager.THERMAL_STATUS_EMERGENCY,
        PowerManager.THERMAL_STATUS_SHUTDOWN -> "Nóng cao (nên nghỉ 2 phút)" to true
        else -> "Ổn định" to false
      }
    }
    return when {
      batteryTempC >= 41f -> "Nóng cao (${batteryTempC}°C)" to true
      batteryTempC >= 37.5f -> "Ấm nhẹ (${batteryTempC}°C)" to false
      else -> "Mát (${batteryTempC}°C)" to false
    }
  }
}
