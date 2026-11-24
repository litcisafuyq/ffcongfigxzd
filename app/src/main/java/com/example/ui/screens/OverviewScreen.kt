package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.DeviceTelemetry
import com.example.data.FfConfig
import com.example.ui.MainUiState
import com.example.ui.components.KeyValueRow
import com.example.ui.components.MetricTile
import com.example.ui.components.StatusPill
import com.example.ui.components.TacticalSectionCard

@Composable
fun OverviewScreen(
  state: MainUiState,
  config: FfConfig,
  onImportJson: () -> Unit,
  onExportJson: () -> Unit,
  onResetDefault: () -> Unit,
  onCopyInstallPath: () -> Unit,
  onLaunchGame: () -> Unit,
  onRefreshTelemetry: () -> Unit,
  onToggleMatchTimer: () -> Unit,
  onToggleInstallStep: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val telemetry = state.telemetry
  val completedCount = state.completedInstallSteps.size
  val totalSteps = config.installSteps.size.coerceAtLeast(1)

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier.widthIn(max = 720.dp).fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Hero Banner Card
      item {
        HeroConfigCard(
          config = config,
          state = state,
          onImportJson = onImportJson,
          onExportJson = onExportJson,
          onResetDefault = onResetDefault,
          onCopyInstallPath = onCopyInstallPath,
          onLaunchGame = onLaunchGame,
        )
      }

      // Honest Mechanism & Role Info
      item {
        TacticalSectionCard(
          title = "Cơ chế & Vai trò file JSON",
          icon = Icons.Default.Shield,
          accentColor = MaterialTheme.colorScheme.secondary,
          trailingBadge = if (config.requiresRoot) "Cần Root" else "Không Root",
        ) {
          Text(
            text = config.fileRole.ifBlank { "Bảng số + HUD; game không đọc file lạ, nhập tay theo file này." },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = "Nguyên lý khóa tâm: ${config.mechanism}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
              )
              Text(
                text = "Nguồn cấu hình: ${config.source}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text = "Không sử dụng: ${config.antiCheatStealth.forbiddenStack.joinToString(", ")}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
              )
            }
          }
        }
      }

      // Real Device Telemetry (RAM, Battery/Thermal, Display)
      if (telemetry != null) {
        item {
          DeviceTelemetryCard(
            config = config,
            telemetry = telemetry,
            matchTimerRunning = state.matchTimerRunning,
            matchElapsedSeconds = state.matchElapsedSeconds,
            onRefreshTelemetry = onRefreshTelemetry,
            onToggleMatchTimer = onToggleMatchTimer,
          )
        }
      }

      // Interactive Install Steps Checklist
      item {
        TacticalSectionCard(
          title = "Quy trình áp dụng (${completedCount}/${config.installSteps.size})",
          icon = Icons.Default.CheckCircle,
          accentColor = MaterialTheme.colorScheme.tertiary,
          trailingBadge = if (completedCount == config.installSteps.size && config.installSteps.isNotEmpty()) "Hoàn tất" else "Đang thiết lập",
        ) {
          LinearProgressIndicator(
            progress = { completedCount.toFloat() / totalSteps.toFloat() },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(MaterialTheme.shapes.small),
            color = MaterialTheme.colorScheme.tertiary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
          )
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            config.installSteps.forEachIndexed { index, stepText ->
              val checked = state.completedInstallSteps.contains(index)
              Surface(
                modifier =
                  Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onToggleInstallStep(index) }
                    .testTag("install_step_$index"),
                shape = MaterialTheme.shapes.small,
                color =
                  if (checked) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
                  else MaterialTheme.colorScheme.surfaceVariant,
                border =
                  BorderStroke(
                    1.dp,
                    if (checked) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.outlineVariant,
                  ),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Icon(
                    imageVector =
                      if (checked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (checked) "Đã xong bước ${index + 1}" else "Chưa xong bước ${index + 1}",
                    tint =
                      if (checked) MaterialTheme.colorScheme.tertiary
                      else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                  )
                  Spacer(modifier = Modifier.width(12.dp))
                  Text(
                    text = "${index + 1}. $stepText",
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                      if (checked) MaterialTheme.colorScheme.onSurface
                      else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun HeroConfigCard(
  config: FfConfig,
  state: MainUiState,
  onImportJson: () -> Unit,
  onExportJson: () -> Unit,
  onResetDefault: () -> Unit,
  onCopyInstallPath: () -> Unit,
  onLaunchGame: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
  ) {
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .background(
            Brush.linearGradient(
              colors =
                listOf(
                  MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                  MaterialTheme.colorScheme.surface,
                )
            )
          )
          .padding(16.dp),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top,
        ) {
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = config.game.uppercase(),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
            )
            Text(
              text = config.configId,
              style = MaterialTheme.typography.headlineMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Mục tiêu: ${config.deviceTarget.model} • Build ${config.build}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          StatusPill(
            text = state.currentPreset?.displayName ?: config.controlsConfig.activePreset,
            color = MaterialTheme.colorScheme.primary,
          )
        }

        // File path & package info
        Surface(
          shape = MaterialTheme.shapes.small,
          color = MaterialTheme.colorScheme.background.copy(alpha = 0.65f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Đích lưu (${config.installTool}):",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text = config.installPath.ifBlank { "/storage/emulated/0/Android/data/${config.packageName}/files/VN.json" },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
            IconButton(
              onClick = onCopyInstallPath,
              modifier = Modifier.testTag("copy_install_path_button"),
            ) {
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Sao chép đường dẫn đích",
                tint = MaterialTheme.colorScheme.primary,
              )
            }
          }
        }

        // Primary Actions
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Button(
            onClick = onLaunchGame,
            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("launch_game_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Launch,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text =
                if (state.telemetry?.isFreeFireInstalled == true) "Mở Free Fire VN"
                else "Kiểm tra Game",
              maxLines = 1,
            )
          }

          FilledTonalButton(
            onClick = onExportJson,
            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("export_json_button"),
          ) {
            Icon(
              imageVector = Icons.Default.Download,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Xuất VN.json", maxLines = 1)
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedButton(
            onClick = onImportJson,
            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("import_json_button"),
          ) {
            Icon(
              imageVector = Icons.Default.FolderOpen,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Nạp JSON khác", maxLines = 1)
          }

          if (state.isCustomJsonLoaded || state.redDotDelta != 0) {
            OutlinedButton(
              onClick = onResetDefault,
              modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("reset_default_button"),
            ) {
              Icon(
                imageVector = Icons.Default.RestartAlt,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Khôi phục gốc", maxLines = 1)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DeviceTelemetryCard(
  config: FfConfig,
  telemetry: DeviceTelemetry,
  matchTimerRunning: Boolean,
  matchElapsedSeconds: Int,
  onRefreshTelemetry: () -> Unit,
  onToggleMatchTimer: () -> Unit,
) {
  val minRam = config.deviceTarget.ramGuard.freeRamMinMb
  val ramOk = telemetry.availRamMb >= minRam && !telemetry.isLowMemory
  val ramColor = if (ramOk) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
  val thermalColor =
    if (telemetry.isThermalElevated) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.tertiary

  val thresholdSec = config.deviceTarget.thermalGuard.reduceAfterMin * 60
  val mins = matchElapsedSeconds / 60
  val secs = matchElapsedSeconds % 60
  val formattedTime = "%02d:%02d".format(mins, secs)
  val timerReached = matchElapsedSeconds >= thresholdSec

  TacticalSectionCard(
    title = "Kiểm tra thiết bị thực tế",
    icon = Icons.Default.Memory,
    accentColor = ramColor,
    trailingBadge = if (ramOk) "Sẵn sàng vào trận" else "Cần dọn RAM",
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      MetricTile(
        label = "RAM trống (mục tiêu >${minRam}MB)",
        value = "${telemetry.availRamMb} MB",
        subValue = "Tổng ${telemetry.totalRamMb} MB",
        accentColor = ramColor,
        modifier = Modifier.weight(1f),
      )
      MetricTile(
        label = "Nhiệt độ & Pin",
        value = "${telemetry.batteryTempCelsius}°C • ${telemetry.batteryLevelPct}%",
        subValue = telemetry.thermalLabelVi,
        accentColor = thermalColor,
        modifier = Modifier.weight(1f),
      )
    }

    KeyValueRow(
      keyLabel = "Thiết bị đang chạy",
      valueText = telemetry.deviceModel,
    )
    KeyValueRow(
      keyLabel = "Màn hình hiện tại / Cấu hình",
      valueText =
        "${telemetry.screenWidthPx}x${telemetry.screenHeightPx} @ ${telemetry.refreshHz}Hz / ${config.deviceTarget.render.resolution}",
    )

    // 10-minute Thermal Guard Match Timer
    Surface(
      shape = MaterialTheme.shapes.small,
      color =
        if (timerReached) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.surfaceVariant,
      border =
        BorderStroke(
          1.dp,
          if (timerReached) MaterialTheme.colorScheme.error
          else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Icon(
            imageVector = if (timerReached) Icons.Default.Thermostat else Icons.Default.Timer,
            contentDescription = null,
            tint = if (timerReached) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text =
                if (matchTimerRunning) "Đếm giờ trận (ngưỡng ${config.deviceTarget.thermalGuard.reduceAfterMin}p): $formattedTime"
                else "Hẹn giờ giữ nhiệt trận (${config.deviceTarget.thermalGuard.reduceAfterMin} phút)",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text =
                if (timerReached) "Đã quá ${config.deviceTarget.thermalGuard.reduceAfterMin}p — chuyển Thuần hoặc nghỉ 2 phút"
                else "Tự nhắc hạ lực kéo ${(config.deviceTarget.thermalGuard.pullReductionPct * 100).toInt()}% khi máy nóng",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        IconButton(
          onClick = onToggleMatchTimer,
          modifier = Modifier.testTag("toggle_thermal_timer_button"),
        ) {
          Icon(
            imageVector = if (matchTimerRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = if (matchTimerRunning) "Dừng đếm giờ" else "Bắt đầu đếm giờ trận",
            tint = MaterialTheme.colorScheme.primary,
          )
        }
      }
    }

    OutlinedButton(
      onClick = onRefreshTelemetry,
      modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("refresh_telemetry_button"),
    ) {
      Icon(
        imageVector = Icons.Default.Refresh,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text("Làm mới RAM & Nhiệt độ")
    }
  }
}
