package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppTab
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.components.StatusPill
import com.example.ui.screens.CalibrationScreen
import com.example.ui.screens.HudBlueprintScreen
import com.example.ui.screens.OverviewScreen
import com.example.ui.screens.SensitivityScreen
import com.example.ui.theme.FfvnConfigTheme

class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      FfvnConfigTheme(darkTheme = true) {
        FfvnConfigApp(viewModel = viewModel)
      }
    }
  }
}

private data class TabItem(
  val tab: AppTab,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String,
)

private val appTabs =
  listOf(
    TabItem(AppTab.OVERVIEW, "Tổng quan", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_overview"),
    TabItem(AppTab.SENSITIVITY, "Độ nhạy", Icons.Filled.Tune, Icons.Outlined.Tune, "nav_sensitivity"),
    TabItem(AppTab.HUD, "Bản đồ HUD", Icons.Filled.GridOn, Icons.Outlined.GridOn, "nav_hud"),
    TabItem(AppTab.CALIBRATION, "Sân tập", Icons.Filled.TrackChanges, Icons.Outlined.TrackChanges, "nav_calibration"),
  )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FfvnConfigApp(viewModel: MainViewModel) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  var currentTab by rememberSaveable { mutableStateOf(AppTab.OVERVIEW) }

  BackHandler(enabled = currentTab != AppTab.OVERVIEW) {
    currentTab = AppTab.OVERVIEW
  }

  LaunchedEffect(Unit) {
    viewModel.events.collect { msg ->
      snackbarHostState.showSnackbar(msg)
    }
  }

  val importLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      if (uri != null) {
        runCatching {
          context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        }.onSuccess { text ->
          if (!text.isNullOrBlank()) {
            viewModel.importJson(text)
          } else {
            viewModel.notifyMessage("Tệp JSON trống hoặc không đọc được")
          }
        }.onFailure { err ->
          viewModel.notifyMessage("Không mở được tệp: ${err.message}")
        }
      }
    }

  val exportLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
      if (uri != null) {
        runCatching {
          val jsonOutput = viewModel.buildExportableJson()
          context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(jsonOutput) }
        }.onSuccess {
          viewModel.notifyMessage("Đã xuất VN.json thành công")
        }.onFailure { err ->
          viewModel.notifyMessage("Lỗi xuất tệp: ${err.message}")
        }
      }
    }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useRail = maxWidth >= 600.dp

    Scaffold(
      modifier = Modifier.fillMaxSize(),
      contentWindowInsets = WindowInsets.safeDrawing,
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      topBar = {
        TopAppBar(
          title = {
            Text(
              text = "FFVN Config",
              style = MaterialTheme.typography.titleLarge,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          },
          actions = {
            uiState.currentPreset?.let { preset ->
              StatusPill(
                text = preset.displayName,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp),
              )
            }
          },
          colors =
            TopAppBarDefaults.topAppBarColors(
              containerColor = MaterialTheme.colorScheme.background,
              titleContentColor = MaterialTheme.colorScheme.onBackground,
            ),
        )
      },
      bottomBar = {
        if (!useRail) {
          NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
          ) {
            appTabs.forEach { item ->
              val selected = currentTab == item.tab
              NavigationBarItem(
                selected = selected,
                onClick = { currentTab = item.tab },
                icon = {
                  Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.label,
                  )
                },
                label = { Text(item.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                modifier = Modifier.testTag(item.testTag),
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        if (useRail) {
          NavigationRail(
            modifier = Modifier.fillMaxHeight(),
            containerColor = MaterialTheme.colorScheme.surface,
          ) {
            appTabs.forEach { item ->
              val selected = currentTab == item.tab
              NavigationRailItem(
                selected = selected,
                onClick = { currentTab = item.tab },
                icon = {
                  Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.label,
                  )
                },
                label = { Text(item.label, maxLines = 1) },
                modifier = Modifier.testTag(item.testTag),
              )
            }
          }
        }

        MainContentArea(
          uiState = uiState,
          currentTab = currentTab,
          viewModel = viewModel,
          onImportJson = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
          onExportJson = { exportLauncher.launch("VN.json") },
          onCopyInstallPath = {
            val path =
              uiState.config?.installPath?.ifBlank {
                "/storage/emulated/0/Android/data/com.garena.ffvn/files/VN.json"
              } ?: "/storage/emulated/0/Android/data/com.garena.ffvn/files/VN.json"
            clipboardManager.setText(AnnotatedString(path))
            viewModel.notifyMessage("Đã sao chép đường dẫn VN.json")
          },
          onLaunchGame = {
            val launchIntent = viewModel.createLaunchFreeFireIntent()
            if (launchIntent != null) {
              context.startActivity(launchIntent)
            } else {
              viewModel.notifyMessage("Chưa phát hiện gói com.garena.ffvn trên thiết bị")
            }
          },
          onCopySensitivity = {
            val summary = viewModel.buildFormattedSensitivitySummary()
            if (summary.isNotBlank()) {
              clipboardManager.setText(AnnotatedString(summary))
              viewModel.notifyMessage("Đã sao chép bảng độ nhạy vào khay nhớ tạm")
            }
          },
          modifier = Modifier.weight(1f).fillMaxSize(),
        )
      }
    }
  }
}

@Composable
private fun MainContentArea(
  uiState: MainUiState,
  currentTab: AppTab,
  viewModel: MainViewModel,
  onImportJson: () -> Unit,
  onExportJson: () -> Unit,
  onCopyInstallPath: () -> Unit,
  onLaunchGame: () -> Unit,
  onCopySensitivity: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val config = uiState.config
  if (uiState.isLoading || config == null) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
      if (uiState.errorMessage != null) {
        Text(
          text = uiState.errorMessage,
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodyLarge,
          modifier = Modifier.padding(24.dp),
        )
      } else {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
      }
    }
    return
  }

  when (currentTab) {
    AppTab.OVERVIEW ->
      OverviewScreen(
        state = uiState,
        config = config,
        onImportJson = onImportJson,
        onExportJson = onExportJson,
        onResetDefault = viewModel::resetToDefaultJson,
        onCopyInstallPath = onCopyInstallPath,
        onLaunchGame = onLaunchGame,
        onRefreshTelemetry = viewModel::refreshTelemetry,
        onToggleMatchTimer = viewModel::toggleMatchThermalTimer,
        onToggleInstallStep = viewModel::toggleInstallStep,
        modifier = modifier,
      )
    AppTab.SENSITIVITY ->
      SensitivityScreen(
        state = uiState,
        config = config,
        onSelectPreset = viewModel::selectPreset,
        onToggleScale100 = viewModel::toggleScale100,
        onBumpRedDot = viewModel::bumpRedDotPlus10,
        onResetRedDot = viewModel::resetRedDotOffset,
        onCopySensitivity = onCopySensitivity,
        modifier = modifier,
      )
    AppTab.HUD ->
      HudBlueprintScreen(
        state = uiState,
        config = config,
        onSelectSubMode = viewModel::setHudSubMode,
        onSelectHudNode = viewModel::selectHudNode,
        onToggleDragZone = viewModel::toggleDragZoneOverlay,
        onToggleGrid = viewModel::toggleHudGrid,
        onToggleDistances = viewModel::toggleHudDistances,
        onSelectPracticeProfile = viewModel::selectPracticeProfile,
        onRecordPracticeSwipe = viewModel::recordPracticeSwipe,
        onResetPracticeStats = viewModel::resetPracticeStats,
        modifier = modifier,
      )
    AppTab.CALIBRATION ->
      CalibrationScreen(
        state = uiState,
        config = config,
        onUpdateScore = viewModel::setCalibrationScore,
        modifier = modifier,
      )
  }
}
