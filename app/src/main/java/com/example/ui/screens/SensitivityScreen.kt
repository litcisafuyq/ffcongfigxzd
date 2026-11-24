package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.FfConfig
import com.example.ui.MainUiState
import com.example.ui.components.KeyValueRow
import com.example.ui.components.StatusPill
import com.example.ui.components.TacticalSectionCard

@Composable
fun SensitivityScreen(
  state: MainUiState,
  config: FfConfig,
  onSelectPreset: (String) -> Unit,
  onToggleScale100: (Boolean) -> Unit,
  onBumpRedDot: () -> Unit,
  onResetRedDot: () -> Unit,
  onCopySensitivity: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val presets = config.controlsConfig.presets.values.toList()
  val activePreset = state.currentPreset
  val eff = state.effectiveSensitivity()
  val maxScale = if (state.useScale100) 100 else 200

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier.widthIn(max = 720.dp).fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Preset Selector & Scale Toggle
      item {
        TacticalSectionCard(
          title = "Bảng độ nhạy Free Fire VN",
          icon = Icons.Default.Tune,
          accentColor = MaterialTheme.colorScheme.primary,
          trailingBadge = if (state.useScale100) "Thang 0–100" else "Thang 0–200",
        ) {
          if (presets.isNotEmpty()) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
              presets.forEachIndexed { idx, preset ->
                SegmentedButton(
                  selected = preset.id == state.activePresetId,
                  onClick = { onSelectPreset(preset.id) },
                  shape = SegmentedButtonDefaults.itemShape(index = idx, count = presets.size),
                  modifier = Modifier.testTag("preset_btn_${preset.id}"),
                ) {
                  Text(preset.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
              }
            }
          }

          if (activePreset != null && activePreset.note.isNotBlank()) {
            Text(
              text = "Ghi chú preset: ${activePreset.note}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.secondary,
            )
          }

          // Scale 0-200 vs 0-100 selector
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            ScaleOptionCard(
              title = "Bản mới (0–200)",
              subtitle = config.controlsConfig.sensScaleNative,
              selected = !state.useScale100,
              onClick = { onToggleScale100(false) },
              modifier = Modifier.weight(1f).testTag("scale_200_btn"),
            )
            ScaleOptionCard(
              title = "Bản cũ (0–100)",
              subtitle = config.controlsConfig.sensScaleFallback,
              selected = state.useScale100,
              onClick = { onToggleScale100(true) },
              modifier = Modifier.weight(1f).testTag("scale_100_btn"),
            )
          }

          if (eff != null) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              SensitivityBarRow(
                label = "Nhìn xung quanh (General)",
                value = eff.general,
                maxScale = maxScale,
                locked = false,
              )
              SensitivityBarRow(
                label =
                  if (state.redDotDelta != 0) "Hồng tâm (Red Dot) [${if (state.redDotDelta > 0) "+${state.redDotDelta}" else state.redDotDelta}]"
                  else "Hồng tâm (Red Dot)",
                value = eff.redDot,
                maxScale = maxScale,
                locked = false,
              )
              SensitivityBarRow(
                label = "Ống ngắm 2x",
                value = eff.scope2x,
                maxScale = maxScale,
                locked = false,
              )
              SensitivityBarRow(
                label = "Ống ngắm 4x",
                value = eff.scope4x,
                maxScale = maxScale,
                locked = false,
              )
              SensitivityBarRow(
                label = "Ống ngắm AWM",
                value = eff.awmScope,
                maxScale = maxScale,
                locked = false,
              )
              SensitivityBarRow(
                label = "Ngắm phụ (Aux Aim — Kịch trần)",
                value = eff.auxAim,
                maxScale = maxScale,
                locked = true,
              )
              SensitivityBarRow(
                label = "Nhìn tự do (Free Look)",
                value = eff.freeLook,
                maxScale = maxScale,
                locked = false,
              )
            }
          }

          Button(
            onClick = onCopySensitivity,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("copy_sensitivity_btn"),
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sao chép toàn bộ bảng độ nhạy")
          }
        }
      }

      // Quick Tuning Controls based on JSON `tuning`
      item {
        TacticalSectionCard(
          title = "Tinh chỉnh nhanh theo tình huống",
          icon = Icons.Default.Tune,
          accentColor = MaterialTheme.colorScheme.secondary,
        ) {
          TuningRuleCard(
            symptom = "Tâm bị văng khi xoay người",
            actionLabel = "Đổi sang Cân bằng",
            recommendation = config.tuning.vangKhiXoay,
            onClick = { onSelectPreset("can_bang") },
            testTag = "tuning_can_bang",
          )
          TuningRuleCard(
            symptom = "Kéo không tới đầu",
            actionLabel = "Hồng tâm +10",
            recommendation = config.tuning.keoKhongToi,
            onClick = onBumpRedDot,
            testTag = "tuning_red_dot_plus10",
          )
          TuningRuleCard(
            symptom = "Máy nóng sau 10 phút / pin yếu",
            actionLabel = "Đổi sang Thuần",
            recommendation = config.tuning.nongSau10p,
            onClick = { onSelectPreset("thuan") },
            testTag = "tuning_thuan",
          )
          if (state.redDotDelta != 0) {
            OutlinedButton(
              onClick = onResetRedDot,
              modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("reset_red_dot_btn"),
            ) {
              Text("Đặt lại bù Hồng tâm (đang +${state.redDotDelta})")
            }
          }
          if (config.tuning.doNotTouch.isNotEmpty()) {
            Text(
              text = "Không được chạm / giữ cố định: ${config.tuning.doNotTouch.joinToString(", ")}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.error,
            )
          }
        }
      }

      // In-game Menu Settings Map
      item {
        TacticalSectionCard(
          title = "Cài đặt trong menu Free Fire VN",
          icon = Icons.Default.DisplaySettings,
          accentColor = MaterialTheme.colorScheme.tertiary,
        ) {
          config.controlsConfig.ingameMenuMap.forEach { (key, value) ->
            KeyValueRow(
              keyLabel = formatMenuKeyVi(key),
              valueText = value,
              valueColor =
                if (value.contains("TẮT", ignoreCase = true) || value.contains("BẬT", ignoreCase = true)) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.onSurface
                },
            )
          }
        }
      }

      // Graphics, Audio & Haptics
      item {
        TacticalSectionCard(
          title = "Đồ họa, Âm thanh & Rung",
          icon = Icons.Default.VolumeUp,
          accentColor = MaterialTheme.colorScheme.primary,
        ) {
          val render = config.deviceTarget.render
          KeyValueRow("Chất lượng đồ họa", "${render.graphicsQuality.uppercase()} • High FPS: ${if (render.highFps) "BẬT" else "TẮT"}")
          KeyValueRow("Hạ độ phân giải / Tắt bóng", "${if (render.reduceResolution) "BẬT" else "TẮT"} / Tắt bóng: ${!render.shadows}")
          KeyValueRow("Chống răng cưa (AA)", if (render.antialias) "BẬT" else "TẮT")
          KeyValueRow(
            "Âm thanh (SFX / Kill / Voice / Nhạc)",
            "${(config.audio.soundEffect * 100).toInt()}% / ${(config.audio.killSound * 100).toInt()}% / ${(config.audio.gameVoice * 100).toInt()}% / ${(config.audio.music * 100).toInt()}%",
          )
          KeyValueRow(
            "Rung phản hồi (key_0 / key_6 / khác)",
            "${config.haptics.key0} / ${config.haptics.key6} / ${config.haptics.others}",
          )
        }
      }
    }
  }
}

@Composable
private fun ScaleOptionCard(
  title: String,
  subtitle: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.heightIn(min = 48.dp).clip(MaterialTheme.shapes.small).clickable(onClick = onClick),
    shape = MaterialTheme.shapes.small,
    color =
      if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
      else MaterialTheme.colorScheme.surfaceVariant,
    border =
      BorderStroke(
        1.dp,
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
      ),
  ) {
    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun SensitivityBarRow(
  label: String,
  value: Int,
  maxScale: Int,
  locked: Boolean,
) {
  val fraction = (value.toFloat() / maxScale.toFloat()).coerceIn(0f, 1f)
  val barColor = if (locked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary

  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Text(
          text = label,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
        if (locked) {
          Spacer(modifier = Modifier.width(6.dp))
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Giữ nguyên kịch trần",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(14.dp),
          )
        }
      }
      Text(
        text = "$value / $maxScale",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = barColor,
      )
    }
    LinearProgressIndicator(
      progress = { fraction },
      modifier = Modifier.fillMaxWidth().height(7.dp).clip(MaterialTheme.shapes.small),
      color = barColor,
      trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
  }
}

@Composable
private fun TuningRuleCard(
  symptom: String,
  actionLabel: String,
  recommendation: String,
  onClick: () -> Unit,
  testTag: String,
) {
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
      Column(modifier = Modifier.weight(1f).padding(end = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(symptom, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        Text(recommendation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.heightIn(min = 48.dp).testTag(testTag),
      ) {
        if (actionLabel.contains("+")) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
        }
        Text(actionLabel, maxLines = 1)
      }
    }
  }
}

private fun formatMenuKeyVi(key: String): String =
  when (key) {
    "aim_assist" -> "Trợ ngắm (Aim Assist)"
    "aux_aim" -> "Ngắm phụ (Aux Aim)"
    "aim_precision" -> "Độ chính xác ngắm"
    "fire_mode" -> "Chế độ bắn"
    "left_fire" -> "Nút bắn trái"
    "hit_hint" -> "Báo trúng đạn"
    "show_player_outline" -> "Viền nhân vật"
    "hide_teammate_name" -> "Ẩn tên đồng đội"
    "enemy_fire_hint" -> "Cảnh báo hướng đạn địch"
    "minimap" -> "Bản đồ nhỏ"
    "stacked_kill_mode" -> "Hiển thị hạ gục xếp chồng"
    "drag_zone" -> "Vùng kéo tâm"
    else -> key
  }
