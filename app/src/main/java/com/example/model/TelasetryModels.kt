package com.example.model

enum class VerdictType {
    ENTER_OPTIMAL,
    ENTER_HIT_AND_RUN,
    DO_NOT_ENTER,
    RESET_REQUIRED
}

data class ProviderProfile(
    val code: String,
    val name: String,
    val cdnSignature: String,
    val defaultRtp: Double,
    val volatilityIndex: Int, // 1..10
    val deadStreakFactor: Double,
    val syncMinutes: List<Int>,
    val peakHoursUtc3: List<Int>,
    val safeHoursUtc3: List<Int>,
    val engineNotes: String
)

data class GameSignature(
    val id: String,
    val title: String,
    val providerCode: String,
    val providerName: String,
    val theoreticalRtp: Double,
    val volatilityTier: String,
    val volatilityScore: Int, // 1..10
    val hitFrequencyPct: Double,
    val bonusFrequencySpins: Int,
    val maxWinMultiplier: Int,
    val minRecommendedBankrollBets: Int,
    val keywords: List<String>,
    val urlSlugs: List<String>
)

data class TimeWindowAnalysis(
    val formattedTimeUtc3: String,
    val hour: Int,
    val minute: Int,
    val second: Int,
    val serverLoadPct: Int,
    val seedCyclePhase: String,
    val rtpCorridorOffset: Double,
    val isGoldenWindow: Boolean,
    val isDeadZoneMinute: Boolean,
    val timeVerdictNote: String
)

data class SpinEvent(
    val spinIndex: Int,
    val bet: Double,
    val multiplier: Double,
    val payout: Double,
    val bankrollAfter: Double,
    val isBonus: Boolean,
    val label: String
)

data class DetectionResult(
    val rawInput: String,
    val detectedSiteDomain: String?,
    val hasTrackingParams: Boolean,
    val trackingTokensFound: List<String>,
    val game: GameSignature,
    val provider: ProviderProfile,
    val matchConfidencePct: Int,
    val isCustomInferred: Boolean
)

data class SimulationReport(
    val detection: DetectionResult,
    val timeAnalysis: TimeWindowAnalysis,
    val totalSpins: Int,
    val startingBankroll: Double,
    val betSize: Double,
    val finalBankroll: Double,
    val peakBankroll: Double,
    val lowestBankroll: Double,
    val netProfitLoss: Double,
    val realizedRtpPct: Double,
    val ruinProbabilityPct: Double,
    val profitProbabilityPct: Double,
    val maxDrawdownPct: Double,
    val longestDeadSpinStreak: Int,
    val bonusHitsCount: Int,
    val peakMultiplierHit: Double,
    val optimalExitSpin: Int,
    val optimalTargetBankroll: Double,
    val equityCurve: List<Float>,
    val majorSpinEvents: List<SpinEvent>,
    val verdictType: VerdictType,
    val verdictTitle: String,
    val verdictSubtitle: String,
    val verdictReasons: List<String>,
    val tacticalDirectives: List<String>
)

data class NetworkIdentityState(
    val publicIp: String = "Taranıyor...",
    val previousIp: String? = null,
    val ispOrg: String = "Analiz Ediliyor",
    val locationLabel: String = "TR / Mobil Şebeke",
    val connectionMode: String = "4.5G / 5G",
    val localInterfaceIp: String = "10.0.0.1",
    val activeInterfaces: List<String> = emptyList(),
    val pseudoMacFingerprint: String = "A4:7B:9E:12:C0:4F",
    val canvasWebGlHash: String = "0x8F92A1E4C7B093D2",
    val sessionIdToken: String = "RC30-SES-90418A",
    val userAgentSpoof: String = "Mozilla/5.0 (Linux; Android 15; Pixel 9 Pro) Chrome/131.0.0.0",
    val dirtySessionRiskScore: Int = 18,
    val ipChangedAfterReset: Boolean? = null,
    val latencyMs: Int = 34,
    val lastCheckedTime: String = "--:--:--",
    val resetCount: Int = 0,
    val statusBanner: String = "Ağ & Oturum Kalkanı Aktif",
    val isCheckingIp: Boolean = false,
    val airplaneCountdownSec: Int = 0
)

data class TerminalLogLine(
    val timestamp: String,
    val text: String,
    val type: LogType = LogType.INFO
)

enum class LogType {
    COMMAND,
    INFO,
    SUCCESS,
    WARNING,
    ERROR,
    MATRIX
}
