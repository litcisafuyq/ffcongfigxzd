package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ConfigParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FFVN Config", appName)
  }

  @Test
  fun `bundled vn_config json parses and exports cleanly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val raw = context.assets.open("vn_config.json").bufferedReader().use { it.readText() }
    val parsed = ConfigParser.parse(raw).getOrThrow()

    assertEquals("VN_A17_NHETAM_V6_FINAL", parsed.configId)
    assertEquals("com.garena.ffvn", parsed.packageName)
    assertEquals(3, parsed.controlsConfig.presets.size)
    assertEquals(8, parsed.hudLayout.nodes.size)
    assertEquals(8, parsed.installSteps.size)
    assertEquals(400, parsed.deviceTarget.ramGuard.freeRamMinMb)

    val exported =
      ConfigParser.buildExportJson(
        baseRawJson = raw,
        activePreset = "can_bang",
        redDotDelta = 10,
        nearHits = 9,
        midHits = 8,
        farHits = 8,
      )
    val reparsed = ConfigParser.parse(exported).getOrThrow()
    assertEquals("can_bang", reparsed.controlsConfig.activePreset)
    assertEquals(165, reparsed.controlsConfig.presets["can_bang"]?.s200?.redDot)
    assertEquals("9/10", reparsed.calibration.log[0].headHits)
    assertTrue(reparsed.antiCheatStealth.singleToolOnly)
  }
}
