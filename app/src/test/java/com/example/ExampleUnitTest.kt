package com.example

import com.example.data.TelasetryEngine
import com.example.model.VerdictType
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun gameAndUrlDetection_worksCorrectly() {
    val det1 = TelasetryEngine.detectGameAndProvider("https://xslot88.com/play/vs20olympx?aff=991&token=xyz")
    assertEquals("xslot88.com", det1.detectedSiteDomain)
    assertTrue(det1.hasTrackingParams)
    assertEquals("Gates of Olympus 1000", det1.game.title)
    assertEquals("PRAGMATIC", det1.provider.code)

    val det2 = TelasetryEngine.detectGameAndProvider("shining crown")
    assertEquals("Shining Crown", det2.game.title)
    assertEquals("EGT", det2.provider.code)
  }

  @Test
  fun preSimulation500Spins_producesValidReport() {
    val report = TelasetryEngine.run500SpinPreSimulation(
      rawInput = "Sweet Bonanza 1000",
      startingBankroll = 3000.0,
      betSize = 10.0,
      dirtySessionRiskScore = 10,
      seedSalt = 42L
    )
    assertEquals(500, report.totalSpins)
    assertEquals(501, report.equityCurve.size)
    assertTrue(report.ruinProbabilityPct in 0.0..100.0)

    val dirtyReport = TelasetryEngine.run500SpinPreSimulation(
      rawInput = "https://site.com/sweet-bonanza?aff=123",
      startingBankroll = 1000.0,
      betSize = 20.0,
      dirtySessionRiskScore = 80,
      seedSalt = 42L
    )
    assertEquals(VerdictType.RESET_REQUIRED, dirtyReport.verdictType)
  }
}
