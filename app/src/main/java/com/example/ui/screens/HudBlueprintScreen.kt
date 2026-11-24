package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FfConfig
import com.example.data.HudButtonNode
import com.example.ui.HudSubMode
import com.example.ui.MainUiState
import com.example.ui.components.KeyValueRow
import com.example.ui.components.MetricTile
import com.example.ui.components.StatusPill
import com.example.ui.components.TacticalSectionCard
import kotlin.math.hypot
import kotlin.math.roundToInt

@Composable
fun HudBlueprintScreen(
  state: MainUiState,
  config: FfConfig,
  onSelectSubMode: (HudSubMode) -> Unit,
  onSelectHudNode: (String) -> Unit,
  onToggleDragZone: () -> Unit,
  onToggleGrid: () -> Unit,
  onToggleDistances: () -> Unit,
  onSelectPracticeProfile: (String) -> Unit,
  onRecordPracticeSwipe: (Float, Long, String) -> Unit,
  onResetPracticeStats: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier.widthIn(max = 720.dp).fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
          SegmentedButton(
            selected = state.hudSubMode == HudSubMode.BLUEPRINT,
            onClick = { onSelectSubMode(HudSubMode.BLUEPRINT) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            modifier = Modifier.testTag("hud_mode_blueprint"),
          ) {
            Text("Bản đồ HUD mẫu")
          }
          SegmentedButton(
            selected = state.hudSubMode == HudSubMode.PRACTICE_PAD,
            onClick = { onSelectSubMode(HudSubMode.PRACTICE_PAD) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            modifier = Modifier.testTag("hud_mode_practice"),
          ) {
            Text("Tập kéo tâm & Rung")
          }
        }
      }

      if (state.hudSubMode == HudSubMode.BLUEPRINT) {
        item {
          HudBlueprintCard(
            state = state,
            config = config,
            onSelectHudNode = onSelectHudNode,
            onToggleDragZone = onToggleDragZone,
            onToggleGrid = onToggleGrid,
            onToggleDistances = onToggleDistances,
          )
        }
      } else {
        item {
          PracticeDragPadCard(
            state = state,
            config = config,
            onSelectProfile = onSelectPracticeProfile,
            onRecordSwipe = onRecordPracticeSwipe,
            onResetStats = onResetPracticeStats,
          )
        }
      }
    }
  }
}

@Composable
private fun HudBlueprintCard(
  state: MainUiState,
  config: FfConfig,
  onSelectHudNode: (String) -> Unit,
  onToggleDragZone: () -> Unit,
  onToggleGrid: () -> Unit,
  onToggleDistances: () -> Unit,
) {
  val hud = config.hudLayout
  val selectedNode = hud.nodes.find { it.key == state.selectedHudKey } ?: hud.nodes.firstOrNull()

  Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    TacticalSectionCard(
      title = "Sơ đồ HUD (${hud.cluster})",
      icon = Icons.Default.GridOn,
      accentColor = MaterialTheme.colorScheme.primary,
      trailingBadge = "${hud.nodes.size} nút",
    ) {
      // Toggle chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        FilterChip(
          selected = state.showDragZoneOverlay,
          onClick = onToggleDragZone,
          label = { Text("Chừa mép 15%") },
          modifier = Modifier.testTag("chip_drag_zone"),
        )
        FilterChip(
          selected = state.showHudGrid,
          onClick = onToggleGrid,
          label = { Text("Lưới tọa độ") },
          modifier = Modifier.testTag("chip_hud_grid"),
        )
        FilterChip(
          selected = state.showHudDistances,
          onClick = onToggleDistances,
          label = { Text("Khoảng cách") },
          modifier = Modifier.testTag("chip_hud_dist"),
        )
      }

      // Landscape 16:9 Free Fire HUD Canvas
      BoxWithConstraints(
        modifier =
          Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(MaterialTheme.shapes.medium)
            .background(Color(0xFF0A0F18)),
      ) {
        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val heightPx = with(LocalDensity.current) { maxHeight.toPx() }

        // Coordinate mapping in normalized 16:9 landscape space for clear inspection
        val nodePositions =
          remember(hud.nodes, widthPx, heightPx) {
            hud.nodes.associate { node ->
              node.key to mapHudNodePosition(node, widthPx, heightPx)
            }
          }

        val gridColor = Color(0xFF233044)
        val dragExclusionColor = Color(0xFFFF4D6A)
        val distanceColor = Color(0xFFFFB300)

        Canvas(modifier = Modifier.fillMaxSize()) {
          if (state.showHudGrid) {
            val cols = 8
            val rows = 4
            for (i in 1 until cols) {
              val x = size.width * i / cols
              drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
            }
            for (j in 1 until rows) {
              val y = size.height * j / rows
              drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            }
          }

          // Crosshair center
          val cx = size.width * 0.5f
          val cy = size.height * 0.45f
          drawCircle(
            color = Color.White.copy(alpha = 0.25f),
            radius = 12.dp.toPx(),
            center = Offset(cx, cy),
            style = Stroke(width = 1.5.dp.toPx()),
          )
          drawCircle(
            color = Color(0xFFFF5722),
            radius = 2.5.dp.toPx(),
            center = Offset(cx, cy),
          )

          // 15% right edge exclusion zone per `drag_zone`
          if (state.showDragZoneOverlay) {
            val stripW = size.width * 0.15f
            drawRect(
              color = dragExclusionColor.copy(alpha = 0.14f),
              topLeft = Offset(size.width - stripW, 0f),
              size = Size(stripW, size.height),
            )
            drawLine(
              color = dragExclusionColor.copy(alpha = 0.7f),
              start = Offset(size.width - stripW, 0f),
              end = Offset(size.width - stripW, size.height),
              strokeWidth = 1.5.dp.toPx(),
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
            )
          }

          // Distance guidelines between fire, sight, and aux aim
          if (state.showHudDistances) {
            val fire = nodePositions["hudfire"]
            val sight = nodePositions["hudsight"]
            val aux = nodePositions["hudauxaim"]
            if (fire != null && sight != null) {
              drawLine(
                color = distanceColor.copy(alpha = 0.7f),
                start = fire,
                end = sight,
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
              )
            }
            if (fire != null && aux != null) {
              drawLine(
                color = Color(0xFF2EE59D).copy(alpha = 0.6f),
                start = fire,
                end = aux,
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
              )
            }
          }
        }

        // Interactive button nodes
        hud.nodes.forEach { node ->
          val pos = nodePositions[node.key] ?: Offset(widthPx / 2f, heightPx / 2f)
          val baseSize = (34f * node.scale.toFloat()).dp
          val sizePx = with(LocalDensity.current) { baseSize.toPx() }
          val isSelected = node.key == state.selectedHudKey
          val nodeColor =
            when (node.key) {
              "hudauxaim" -> Color(0xFF2EE59D)
              "hudfire" -> Color(0xFFFF5722)
              "hudsight" -> Color(0xFFFFB300)
              "hudswapweaponquick" -> Color(0xFF9AA5BA)
              else -> Color(0xFF56CCF2)
            }

          Surface(
            modifier =
              Modifier
                .offset {
                  IntOffset(
                    (pos.x - sizePx / 2f).roundToInt(),
                    (pos.y - sizePx / 2f).roundToInt(),
                  )
                }
                .size(baseSize)
                .clip(CircleShape)
                .clickable { onSelectHudNode(node.key) }
                .testTag("hud_node_${node.key}"),
            shape = CircleShape,
            color =
              nodeColor.copy(
                alpha =
                  if (node.alpha < 0.2) 0.18f
                  else if (isSelected) 0.42f
                  else 0.22f,
              ),
            border =
              BorderStroke(
                width = if (isSelected) 2.5.dp else 1.2.dp,
                color = if (isSelected) Color.White else nodeColor,
              ),
          ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
              Text(
                text = shortHudLabel(node.key),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
              )
            }
          }
        }
      }

      if (hud.androidNote.isNotBlank()) {
        Text(
          text = "Ghi chú bố cục: ${hud.androidNote}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    // Selected Node Inspector
    if (selectedNode != null) {
      TacticalSectionCard(
        title = "Chi tiết nút: ${selectedNode.displayName}",
        icon = Icons.Default.CenterFocusStrong,
        accentColor = MaterialTheme.colorScheme.secondary,
        trailingBadge = selectedNode.key,
      ) {
        val anchorName = hud.anchorMap[selectedNode.anchor] ?: "Neo #${selectedNode.anchor}"
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          MetricTile(
            label = "Tọa độ (dx, dy)",
            value = "${selectedNode.dx}, ${selectedNode.dy}",
            accentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
          )
          MetricTile(
            label = "Neo / Scale / Alpha",
            value = "$anchorName • x${selectedNode.scale}",
            subValue = "Alpha ${selectedNode.alpha}",
            accentColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f),
          )
        }
        Text(
          text = "Vai trò: ${selectedNode.role}",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
    }
  }
}

@Composable
private fun PracticeDragPadCard(
  state: MainUiState,
  config: FfConfig,
  onSelectProfile: (String) -> Unit,
  onRecordSwipe: (Float, Long, String) -> Unit,
  onResetStats: () -> Unit,
) {
  val profiles = listOf(config.distanceProfiles.near, config.distanceProfiles.mid, config.distanceProfiles.far)
  val activeProfile = profiles.find { it.id == state.practiceProfileId } ?: config.distanceProfiles.near
  val trailPoints = remember { mutableStateListOf<Offset>() }
  var dragStartMs by remember { mutableStateOf(0L) }
  var isDragging by remember { mutableStateOf(false) }

  TacticalSectionCard(
    title = "Tập kéo tâm & phản hồi rung nội bộ",
    icon = Icons.Default.TouchApp,
    accentColor = MaterialTheme.colorScheme.primary,
    trailingBadge = "${state.practiceHeadHits}/${state.practiceAttempts} Head",
  ) {
    Text(
      text = "Bài tập mô phỏng lực kéo ngắn & nhịp rung (${config.fingerFeedback.patternLock.joinToString("-")}ms khi khóa đầu) ngay trong ứng dụng, không can thiệp game.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      profiles.forEach { prof ->
        val selected = prof.id == activeProfile.id
        Surface(
          modifier =
            Modifier
              .weight(1f)
              .heightIn(min = 48.dp)
              .clip(MaterialTheme.shapes.small)
              .clickable { onSelectProfile(prof.id) }
              .testTag("practice_profile_${prof.id}"),
          shape = MaterialTheme.shapes.small,
          color =
            if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            else MaterialTheme.colorScheme.surfaceVariant,
          border =
            BorderStroke(
              1.dp,
              if (selected) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.outlineVariant,
            ),
        ) {
          Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
              text = "${prof.labelVi} (${prof.rangeM.first}–${prof.rangeM.second}m)",
              style = MaterialTheme.typography.labelLarge,
              color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "${prof.pullPx}px • ${prof.rampMs}ms",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }

    // Interactive Drag Pad Canvas
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(270.dp)
          .clip(MaterialTheme.shapes.medium)
          .background(Color(0xFF0A0F18))
          .pointerInput(activeProfile.id) {
            detectDragGestures(
              onDragStart = { startOffset ->
                trailPoints.clear()
                trailPoints.add(startOffset)
                dragStartMs = System.currentTimeMillis()
                isDragging = true
              },
              onDrag = { change, _ ->
                change.consume()
                trailPoints.add(change.position)
              },
              onDragEnd = {
                isDragging = false
                if (trailPoints.size >= 2) {
                  val start = trailPoints.first()
                  val end = trailPoints.last()
                  val dy = (start.y - end.y).coerceAtLeast(0f)
                  val dx = end.x - start.x
                  val distPx = hypot(dx, dy)
                  val durationMs = (System.currentTimeMillis() - dragStartMs).coerceAtLeast(16L)

                  // Map vertical swipe distance against target zones inside the 270dp pad
                  val headTopY = size.height * 0.14f
                  val headBottomY = size.height * 0.30f
                  val neckBottomY = size.height * 0.42f
                  val hitZone =
                    when {
                      end.y in headTopY..headBottomY -> "head"
                      end.y in headBottomY..neckBottomY -> "neck"
                      else -> "chest"
                    }
                  // Convert canvas px to reference config scale
                  val normalizedPull = (distPx / 4.2f)
                  onRecordSwipe(normalizedPull, durationMs, hitZone)
                }
              },
              onDragCancel = { isDragging = false },
            )
          }
          .testTag("practice_drag_canvas"),
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width * 0.5f
        val headY = size.height * 0.22f
        val neckY = size.height * 0.35f
        val chestY = size.height * 0.49f

        // Target silhouette rings (Head / Neck / Chest)
        drawCircle(
          color = Color(0xFF2EE59D).copy(alpha = 0.22f),
          radius = 22.dp.toPx(),
          center = Offset(cx, headY),
        )
        drawCircle(
          color = Color(0xFF2EE59D),
          radius = 22.dp.toPx(),
          center = Offset(cx, headY),
          style = Stroke(width = 2.dp.toPx()),
        )
        drawCircle(
          color = Color(0xFFFFB300).copy(alpha = 0.6f),
          radius = 16.dp.toPx(),
          center = Offset(cx, neckY),
          style = Stroke(width = 1.5.dp.toPx()),
        )
        drawCircle(
          color = Color(0xFF9AA5BA).copy(alpha = 0.45f),
          radius = 24.dp.toPx(),
          center = Offset(cx, chestY),
          style = Stroke(width = 1.5.dp.toPx()),
        )

        // Fire anchor at bottom
        val fireOrigin = Offset(cx, size.height * 0.80f)
        drawCircle(
          color = Color(0xFFFF5722).copy(alpha = 0.22f),
          radius = 28.dp.toPx(),
          center = fireOrigin,
        )
        drawCircle(
          color = Color(0xFFFF5722),
          radius = 28.dp.toPx(),
          center = fireOrigin,
          style = Stroke(width = 2.dp.toPx()),
        )

        // Draw finger swipe Bezier / polyline trail
        if (trailPoints.size > 1) {
          val path = Path().apply {
            moveTo(trailPoints.first().x, trailPoints.first().y)
            for (i in 1 until trailPoints.size) {
              lineTo(trailPoints[i].x, trailPoints[i].y)
            }
          }
          drawPath(
            path = path,
            color = if (isDragging) Color(0xFFFFB300) else Color(0xFF2EE59D),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
          )
        }
      }

      Text(
        text = "ĐẦU (Head)",
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xFF2EE59D),
        modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp),
      )

      Text(
        text = "Kéo từ nút BẮN lên vòng ĐẦU",
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xFFFFB89E),
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp),
      )

      IconButton(
        onClick = {
          trailPoints.clear()
          onResetStats()
        },
        modifier = Modifier.align(Alignment.TopEnd).testTag("reset_practice_btn"),
      ) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = "Đặt lại thống kê tập kéo",
          tint = Color.White,
        )
      }
    }

    val last = state.lastPracticeAttempt
    if (last != null) {
      Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border =
          BorderStroke(
            1.dp,
            if (last.isHeadLock) MaterialTheme.colorScheme.tertiary
            else MaterialTheme.colorScheme.secondary,
          ),
      ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = last.feedbackText,
              style = MaterialTheme.typography.titleSmall,
              color =
                if (last.isHeadLock) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.secondary,
              modifier = Modifier.weight(1f),
            )
            StatusPill(
              text = if (last.isHeadLock) "Rung Lock" else "Rung Miss",
              color = if (last.isHeadLock) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
            )
          }
          KeyValueRow(
            keyLabel = "Mục tiêu ${activeProfile.labelVi} / Thực tế",
            valueText = "${activeProfile.pullPx}px (${activeProfile.rampMs}ms) / ${last.pullPx.toInt()}px (${last.rampMs}ms)",
          )
        }
      }
    }
  }
}

private fun mapHudNodePosition(node: HudButtonNode, width: Float, height: Float): Offset {
  // Position nodes authentically on a 16:9 landscape phone screen matching anchor & cluster notes
  return when (node.key) {
    "hudauxaim" -> Offset(width * 0.90f, height * 0.28f)
    "hudfire" -> Offset(width * 0.72f, height * 0.72f)
    "hudsight" -> Offset(width * 0.90f, height * 0.78f)
    "hudjump" -> Offset(width * 0.90f, height * 0.56f)
    "hudcrouch" -> Offset(width * 0.90f, height * 0.41f)
    "hudcreep" -> Offset(width * 0.80f, height * 0.41f)
    "hudreload" -> Offset(width * 0.62f, height * 0.80f)
    "hudswapweaponquick" -> Offset(width * 0.24f, height * 0.66f)
    else -> {
      val xRatio = (0.85f + (node.dx.toFloat() / 2000f)).coerceIn(0.12f, 0.93f)
      val yRatio = (0.55f + (node.dy.toFloat() / 1000f)).coerceIn(0.15f, 0.88f)
      Offset(width * xRatio, height * yRatio)
    }
  }
}

private fun shortHudLabel(key: String): String =
  when (key) {
    "hudauxaim" -> "Phụ"
    "hudfire" -> "Bắn"
    "hudsight" -> "Ngắm"
    "hudjump" -> "Nhảy"
    "hudcrouch" -> "Ngồi"
    "hudcreep" -> "Trườn"
    "hudreload" -> "Đạn"
    "hudswapweaponquick" -> "Ẩn"
    else -> key.take(4)
  }
