package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.FfConfig
import com.example.ui.MainUiState
import com.example.ui.components.KeyValueRow
import com.example.ui.components.MetricTile
import com.example.ui.components.StatusPill
import com.example.ui.components.TacticalSectionCard

@Composable
fun CalibrationScreen(
  state: MainUiState,
  config: FfConfig,
  onUpdateScore: (String, Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val near = state.nearHits ?: 0
  val mid = state.midHits ?: 0
  val far = state.farHits ?: 0
  val hasAnyScore = state.nearHits != null || state.midHits != null || state.farHits != null
  val allPassed =
    state.nearHits != null && state.midHits != null && state.farHits != null &&
      near >= 8 && mid >= 8 && far >= 8

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier.widthIn(max = 720.dp).fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // 3-Range Training Ground Calibration
      item {
        TacticalSectionCard(
          title = "Sân tập 3 cự ly (${config.calibration.passThreshold})",
          icon = Icons.Default.TrackChanges,
          accentColor = if (allPassed) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
          trailingBadge =
            when {
              allPassed -> "Đạt chuẩn vào trận"
              hasAnyScore -> "Đang hiệu chỉnh"
              else -> "Chưa ghi nhận"
            },
        ) {
          Text(
            text = "Bắn thử 10 viên mỗi cự ly trong Sân tập. Đạt tối thiểu 8/10 đầu ở cả 3 mốc trước khi vào trận xếp hạng.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          RangeCalibrationStepper(
            title = "Cự ly gần — 8m",
            subtitle = "Pull ${config.distanceProfiles.near.pullPx}px • Ramp ${config.distanceProfiles.near.rampMs}ms",
            score = state.nearHits,
            onScoreChange = { onUpdateScore("near", it) },
            testTagPrefix = "cal_near",
          )
          RangeCalibrationStepper(
            title = "Cự ly trung — 25m",
            subtitle = "Pull ${config.distanceProfiles.mid.pullPx}px • Ramp ${config.distanceProfiles.mid.rampMs}ms",
            score = state.midHits,
            onScoreChange = { onUpdateScore("mid", it) },
            testTagPrefix = "cal_mid",
          )
          RangeCalibrationStepper(
            title = "Cự ly xa — 50m",
            subtitle = "Pull ${config.distanceProfiles.far.pullPx}px • Ramp ${config.distanceProfiles.far.rampMs}ms",
            score = state.farHits,
            onScoreChange = { onUpdateScore("far", it) },
            testTagPrefix = "cal_far",
          )

          // Dynamic diagnosis
          val advice =
            when {
              allPassed -> "Cả 3 cự ly đều đạt ≥ 8/10 đầu. Cấu hình đã sẵn sàng để giữ ổn định trong trận."
              state.nearHits != null && near < 8 ->
                "Cự ly 8m dưới 8/10: nếu kéo hụt đầu, tăng Hồng tâm +${config.calibration.stepPct} ở tab Độ nhạy; giữ nguyên Ngắm phụ."
              state.midHits != null && mid < 8 ->
                "Cự ly 25m dưới 8/10: nếu tâm bị văng khi xoay, đổi preset sang Cân bằng."
              state.farHits != null && far < 8 ->
                "Cự ly 50m dưới 8/10: chuyển giữ nút bắn sang nhấp từng loạt 3 viên (tap) thay vì giữ liên tục."
              else -> "Chạm nút +/- để lưu kết quả từng cự ly (dữ liệu sẽ được kèm theo khi xuất VN.json)."
            }

          Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          ) {
            Text(
              text = advice,
              style = MaterialTheme.typography.bodySmall,
              color =
                if (allPassed) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.secondary,
              modifier = Modifier.fillMaxWidth().padding(12.dp),
            )
          }
        }
      }

      // Assist Strength & Safe Band Inspector
      item {
        val assist = config.assistStrength
        TacticalSectionCard(
          title = "Thông số lực kéo & Dải an toàn",
          icon = Icons.Default.Analytics,
          accentColor = MaterialTheme.colorScheme.secondary,
        ) {
          SafeBandMeter(
            label = "Lock Strength (${assist.lockStrength}) — Dải an toàn ${assist.safeBandLock.first}–${assist.safeBandLock.second}",
            value = assist.lockStrength,
            ceiling = assist.hardCeilingLock,
          )
          SafeBandMeter(
            label = "Aim Lightness (${assist.aimLightness}) — Dải an toàn ${assist.safeBandLightness.first}–${assist.safeBandLightness.second}",
            value = assist.aimLightness,
            ceiling = assist.hardCeilingLightness,
          )
          KeyValueRow("Hệ số kéo (pull_multiplier)", "${assist.pullMultiplier}x")
          KeyValueRow(
            "Độ ưu tiên vùng",
            config.decisionLogic.zonePriority.joinToString(" → "),
          )
          KeyValueRow(
            "Đường cong & Smoothing",
            "${config.behaviorSimulation.curveType} (${config.behaviorSimulation.smoothingSteps} bước)",
          )
          KeyValueRow(
            "Phản xạ mô phỏng",
            "${config.behaviorSimulation.minDelayMs}–${config.behaviorSimulation.maxDelayMs} ms (${config.behaviorSimulation.distributionType})",
          )
          KeyValueRow(
            "Burst Governor",
            "${config.fingerFeedback.burstLen} viên/loạt • Nghỉ ${config.fingerFeedback.burstGapMs}ms • Tối đa ${config.fingerFeedback.maxShotsPerEngage}",
          )
        }
      }

      // Safety Caps & Anti-Cheat Stealth Notes
      item {
        val caps = config.safetyCaps
        TacticalSectionCard(
          title = "Giới hạn an toàn (Safety Caps)",
          icon = Icons.Default.Security,
          accentColor = MaterialTheme.colorScheme.tertiary,
          trailingBadge = if (config.antiCheatStealth.singleToolOnly) "Single-Tool" else "Custom",
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            MetricTile(
              label = "Max Step / Pull",
              value = "${caps.maxStepPx}px / ${caps.maxTotalPullPx}px",
              accentColor = MaterialTheme.colorScheme.primary,
              modifier = Modifier.weight(1f),
            )
            MetricTile(
              label = "Min Ramp / Tick",
              value = "${caps.minRampMs}ms / ${caps.tickRangeMs.first}-${caps.tickRangeMs.second}ms",
              accentColor = MaterialTheme.colorScheme.tertiary,
              modifier = Modifier.weight(1f),
            )
          }
          KeyValueRow("Giới hạn phần trăm màn hình", "${caps.maxPullShareOfScreenPct}% (tối thiểu ${caps.minPullPxFloor}px)")
          KeyValueRow("Ổn định tín hiệu", "${(config.antiCheatStealth.signalStabilization * 100).toInt()}%")
          KeyValueRow("Khóa độ nhạy giữa trận", if (config.antiCheatStealth.sensLockMidMatch) "BẬT" else "TẮT")
          KeyValueRow("Hiệu chỉnh lại mỗi mùa", if (config.antiCheatStealth.recalibrateEachSeason) "BẬT" else "TẮT")
        }
      }
    }
  }
}

@Composable
private fun RangeCalibrationStepper(
  title: String,
  subtitle: String,
  score: Int?,
  onScoreChange: (Int) -> Unit,
  testTagPrefix: String,
) {
  val current = score ?: 0
  val passed = score != null && score >= 8
  val badgeColor =
    when {
      score == null -> MaterialTheme.colorScheme.onSurfaceVariant
      passed -> MaterialTheme.colorScheme.tertiary
      else -> MaterialTheme.colorScheme.error
    }

  Surface(
    shape = MaterialTheme.shapes.small,
    color = MaterialTheme.colorScheme.surfaceVariant,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
          Spacer(modifier = Modifier.width(8.dp))
          StatusPill(
            text = if (score == null) "__/10" else "$score/10",
            color = badgeColor,
          )
        }
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }

      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        FilledTonalIconButton(
          onClick = { onScoreChange((current - 1).coerceAtLeast(0)) },
          modifier = Modifier.size(48.dp).testTag("${testTagPrefix}_minus"),
        ) {
          Icon(Icons.Default.Remove, contentDescription = "Giảm điểm $title")
        }
        Text(
          text = if (score == null) "--" else "$score",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = badgeColor,
        )
        FilledTonalIconButton(
          onClick = { onScoreChange(((score ?: 7) + 1).coerceAtMost(10)) },
          modifier = Modifier.size(48.dp).testTag("${testTagPrefix}_plus"),
        ) {
          Icon(Icons.Default.Add, contentDescription = "Tăng điểm $title")
        }
      }
    }
  }
}

@Composable
private fun SafeBandMeter(
  label: String,
  value: Int,
  ceiling: Int,
) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
      Text("Trần $ceiling", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    LinearProgressIndicator(
      progress = { (value.toFloat() / ceiling.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f) },
      modifier = Modifier.fillMaxWidth().height(7.dp).clip(MaterialTheme.shapes.small),
      color = MaterialTheme.colorScheme.secondary,
      trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
  }
}
