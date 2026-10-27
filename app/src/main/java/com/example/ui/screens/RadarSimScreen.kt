package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.VerdictType
import com.example.ui.RadarUiState
import com.example.ui.components.AnimatedRadarSweepHud
import com.example.ui.components.EquityCurveChart
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.NeonCyberCyan
import com.example.ui.theme.NeonDangerRed
import com.example.ui.theme.NeonDangerRedDim
import com.example.ui.theme.NeonMatrixGreen
import com.example.ui.theme.NeonMatrixGreenDim
import com.example.ui.theme.NeonWarningAmber
import com.example.ui.theme.NeonWarningAmberDim
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.roundToInt

private val quickPresets = listOf(
    "Gates of Olympus 1000",
    "Sweet Bonanza 1000",
    "Shining Crown",
    "Fortune Tiger",
    "Wanted Dead or a Wild",
    "Sugar Rush 1000",
    "Big Bass Splash",
    "Aviator"
)

@Composable
fun RadarSimScreen(
    uiState: RadarUiState,
    onQueryChange: (String) -> Unit,
    onBankrollChange: (String) -> Unit,
    onBetSizeChange: (String) -> Unit,
    onSelectPreset: (String) -> Unit,
    onRunSimulation: () -> Unit,
    onQuickResetSession: () -> Unit,
    onSimulateDirtySession: () -> Unit,
    onSwitchToNetworkTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val report = uiState.simulationReport
    val detection = report.detection
    val timeInfo = uiState.currentTimeAnalysis

    val verdictAccent = when (report.verdictType) {
        VerdictType.ENTER_OPTIMAL -> NeonMatrixGreen
        VerdictType.ENTER_HIT_AND_RUN -> NeonWarningAmber
        VerdictType.DO_NOT_ENTER, VerdictType.RESET_REQUIRED -> NeonDangerRed
    }
    val verdictContainerBg = when (report.verdictType) {
        VerdictType.ENTER_OPTIMAL -> NeonMatrixGreenDim
        VerdictType.ENTER_HIT_AND_RUN -> NeonWarningAmberDim
        VerdictType.DO_NOT_ENTER, VerdictType.RESET_REQUIRED -> NeonDangerRedDim
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // 1. TOP TELEMETRY & LIVE SEED CLOCK BAR
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberSurfaceElevated)
                    .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Canlı Saat",
                        tint = NeonCyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = uiState.liveClockUtc3,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Sunucu Yükü: %${timeInfo.serverLoadPct} • ${timeInfo.seedCyclePhase}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (timeInfo.isGoldenWindow) NeonMatrixGreen
                            else if (timeInfo.isDeadZoneMinute) NeonDangerRed
                            else TextSecondary
                        )
                    }
                }

                Surface(
                    color = if (uiState.networkState.dirtySessionRiskScore > 50) NeonDangerRedDim else CyberObsidian,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .border(
                            1.dp,
                            if (uiState.networkState.dirtySessionRiskScore > 50) NeonDangerRed else NeonMatrixGreen,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSwitchToNetworkTab() }
                ) {
                    Text(
                        text = "İZ RİSKİ: %${uiState.networkState.dirtySessionRiskScore}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (uiState.networkState.dirtySessionRiskScore > 50) NeonDangerRed else NeonMatrixGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // 2. GAME / SITE URL / PROVIDER INPUT & PARAMETERS CARD
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberSurface)
                    .border(1.dp, CyberBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "OYUN / SİTE URL / SAĞLAYICI TESPİT MOTORU",
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonCyberCyan
                )

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onQueryChange,
                    label = { Text("Oyun Adı, Sağlayıcı veya Site Linki Yapıştırın") },
                    placeholder = { Text("Örn: https://xslot.com/gates-1000 veya sweet bonanza") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonMatrixGreen,
                        unfocusedBorderColor = CyberBorder,
                        focusedLabelColor = NeonMatrixGreen,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("game_search_input")
                )

                // Quick Game Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickPresets.forEach { preset ->
                        val isSelected = uiState.searchQuery.equals(preset, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonMatrixGreenDim else CyberObsidian)
                                .border(
                                    1.dp,
                                    if (isSelected) NeonMatrixGreen else CyberBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectPreset(preset) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = preset,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) NeonMatrixGreen else TextPrimary
                            )
                        }
                    }
                }

                // Bankroll & Bet Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.bankrollInput,
                        onValueChange = onBankrollChange,
                        label = { Text("Başlangıç Kasa (₺)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyberCyan,
                            unfocusedBorderColor = CyberBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bankroll_input")
                    )

                    OutlinedTextField(
                        value = uiState.betSizeInput,
                        onValueChange = onBetSizeChange,
                        label = { Text("Spin Bahsi (₺)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyberCyan,
                            unfocusedBorderColor = CyberBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bet_size_input")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRunSimulation,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonMatrixGreen,
                            contentColor = Color(0xFF001F10)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp)
                            .testTag("run_simulation_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Simüle Et",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "500-SPİN SİMÜLE ET",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onQuickResetSession,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("quick_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "İz Sıfırla",
                            tint = NeonCyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "İZ SIFIRLA",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonCyberCyan
                        )
                    }
                }
            }
        }

        // 3. UNCOMPROMISING NET VERDICT BANNER + ANIMATED RADAR HUD
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(verdictContainerBg, CyberSurface)
                        )
                    )
                    .border(2.dp, verdictAccent, RoundedCornerShape(18.dp))
                    .padding(16.dp)
                    .testTag("verdict_banner_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (report.verdictType) {
                                    VerdictType.ENTER_OPTIMAL -> Icons.Default.CheckCircle
                                    VerdictType.ENTER_HIT_AND_RUN -> Icons.Default.WarningAmber
                                    VerdictType.DO_NOT_ENTER, VerdictType.RESET_REQUIRED -> Icons.Default.GppBad
                                },
                                contentDescription = "Karar İkonu",
                                tint = verdictAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RC30 NET KARAR RADARI (#${uiState.simulationRunCounter})",
                                style = MaterialTheme.typography.labelSmall,
                                color = verdictAccent
                            )
                        }

                        Text(
                            text = report.verdictTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.testTag("verdict_headline_text")
                        )

                        Text(
                            text = report.verdictSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = verdictAccent
                        )
                    }

                    AnimatedRadarSweepHud(
                        verdictType = report.verdictType,
                        ruinProbabilityPct = report.ruinProbabilityPct,
                        serverLoadPct = timeInfo.serverLoadPct
                    )
                }
            }
        }

        // 4. DETECTED GAME, PROVIDER & SITE SIGNATURE CARD
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CyberSurface)
                    .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = "Tespit",
                            tint = NeonMatrixGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TESPİT EDİLEN OYUN & SAĞLAYICI İMZASI",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonMatrixGreen
                        )
                    }
                    Text(
                        text = "Eşleşme: %${detection.matchConfidencePct}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyberCyan
                    )
                }

                Text(
                    text = "${detection.game.title} • ${detection.provider.name}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )

                if (detection.detectedSiteDomain != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberObsidian)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hedef Site: ${detection.detectedSiteDomain}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeonCyberCyan
                        )
                        Text(
                            text = if (detection.hasTrackingParams) "⚠️ TAKİP TOKENLİ URL" else "✓ TEMİZ DOMAIN",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (detection.hasTrackingParams) NeonDangerRed else NeonMatrixGreen
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetryMiniPill(
                        label = "Teorik RTP",
                        value = "%${detection.game.theoreticalRtp}",
                        accent = NeonCyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMiniPill(
                        label = "Volatilite",
                        value = detection.game.volatilityTier,
                        accent = NeonWarningAmber,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMiniPill(
                        label = "Önerilen Kasa",
                        value = "Min ${detection.game.minRecommendedBankrollBets}x",
                        accent = NeonMatrixGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "RGS / CDN: ${detection.provider.cdnSignature}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = timeInfo.timeVerdictNote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
        }

        // 5. 500-SPIN EQUITY CURVE CHART
        item {
            EquityCurveChart(
                equityCurve = report.equityCurve,
                startingBankroll = report.startingBankroll,
                peakBankroll = report.peakBankroll,
                optimalExitSpin = report.optimalExitSpin,
                verdictType = report.verdictType
            )
        }

        // 6. 6-METRIC SIMULATION TELEMETRY GRID
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetryMiniPill(
                        label = "Kasa Erime İhtimali",
                        value = "%${report.ruinProbabilityPct}",
                        accent = if (report.ruinProbabilityPct >= 50) NeonDangerRed else NeonMatrixGreen,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMiniPill(
                        label = "Simülasyon RTP",
                        value = "%${report.realizedRtpPct}",
                        accent = if (report.realizedRtpPct >= 95) NeonMatrixGreen else NeonWarningAmber,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMiniPill(
                        label = "Net Kapanış (500 Spin)",
                        value = "${if (report.netProfitLoss >= 0) "+" else ""}₺${report.netProfitLoss.roundToInt()}",
                        accent = if (report.netProfitLoss >= 0) NeonMatrixGreen else NeonDangerRed,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetryMiniPill(
                        label = "Bonus / Freespin",
                        value = "${report.bonusHitsCount} Kez",
                        accent = NeonCyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMiniPill(
                        label = "Maksimum Çarpan",
                        value = "${report.peakMultiplierHit}x",
                        accent = NeonMatrixGreen,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMiniPill(
                        label = "Maks Ölü Spin Serisi",
                        value = "${report.longestDeadSpinStreak} Boş Spin",
                        accent = if (report.longestDeadSpinStreak >= 22) NeonDangerRed else NeonWarningAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 7. WHY THIS VERDICT & TACTICAL DIRECTIVES CARD
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CyberSurface)
                    .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "ANALİZ GEREKÇELERİ & TAKTİK EYLEM PLANI",
                    style = MaterialTheme.typography.labelLarge,
                    color = verdictAccent
                )

                report.verdictReasons.forEach { reason ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "▸ ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = verdictAccent
                        )
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberObsidian)
                        .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "UYGULANACAK EYLEM ADIMLARI:",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyberCyan
                        )
                        report.tacticalDirectives.forEach { dir ->
                            Text(
                                text = dir,
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeonMatrixGreen
                            )
                        }
                    }
                }

                // Test button to demonstrate auto-detection of dirty tracking session
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kirli Oturum / Takip Token Engelleme Testi:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    OutlinedButton(
                        onClick = onSimulateDirtySession,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("simulate_dirty_session_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Test",
                            tint = NeonWarningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "KİRLİ İZ TEST ET",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonWarningAmber
                        )
                    }
                }
            }
        }

        // 8. MAJOR SPIN EVENTS LOG (FROM 500-SPIN SIMULATION)
        if (report.majorSpinEvents.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CyberSurface)
                        .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Kritik Spinler",
                            tint = NeonCyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SİMÜLASYON KRİTİK SPİN KIRILIMLARI",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonCyberCyan
                        )
                    }

                    report.majorSpinEvents.forEach { ev ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberObsidian)
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${String.format(Locale.US, "%03d", ev.spinIndex)}. Spin • ${ev.label}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (ev.isBonus) NeonMatrixGreen
                                else if (ev.multiplier == 0.0) NeonDangerRed
                                else NeonCyberCyan
                            )
                            Text(
                                text = "Kasa: ₺${ev.bankrollAfter.roundToInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun TelemetryMiniPill(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CyberObsidian)
            .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accent,
            maxLines = 1
        )
    }
}
