package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.NetworkShieldManager
import com.example.data.TelasetryEngine
import com.example.model.LogType
import com.example.model.NetworkIdentityState
import com.example.model.SimulationReport
import com.example.model.TerminalLogLine
import com.example.model.TimeWindowAnalysis
import com.example.model.VerdictType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class RadarUiState(
    val selectedTab: Int = 0,
    val searchQuery: String = "Gates of Olympus 1000",
    val bankrollInput: String = "2500",
    val betSizeInput: String = "10",
    val liveClockUtc3: String = "--:--:--",
    val currentTimeAnalysis: TimeWindowAnalysis = TelasetryEngine.analyzeCurrentTimeWindow(TelasetryEngine.providers.first()),
    val simulationReport: SimulationReport = TelasetryEngine.run500SpinPreSimulation(
        rawInput = "Gates of Olympus 1000",
        startingBankroll = 2500.0,
        betSize = 10.0,
        dirtySessionRiskScore = 18
    ),
    val networkState: NetworkIdentityState = NetworkIdentityState(),
    val isSimulating: Boolean = false,
    val simulationRunCounter: Int = 1,
    val terminalLogs: List<TerminalLogLine> = initialTerminalBootLogs(),
    val terminalCommandInput: String = ""
)

private fun initialTerminalBootLogs(): List<TerminalLogLine> {
    val now = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
    return listOf(
        TerminalLogLine(now, "Termux RC30-PRO Kernel v3.4.2-telasetry yüklendi [aarch64-linux-android]", LogType.MATRIX),
        TerminalLogLine(now, "Modüller: [Ön-Simülasyon: AKTİF] [Sağlayıcı Radarı: 10 RGS] [IP/MAC Kalkanı: HAZIR]", LogType.INFO),
        TerminalLogLine(now, "Komut yardımı için 'help' yazın veya aşağıdaki hızlı Termux komutlarına dokunun.", LogType.SUCCESS)
    )
}

class RadarViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RadarUiState())
    val uiState: StateFlow<RadarUiState> = _uiState.asStateFlow()

    init {
        startLiveClockTicker()
    }

    private fun startLiveClockTicker() {
        viewModelScope.launch {
            while (true) {
                val activeProvider = _uiState.value.simulationReport.detection.provider
                val timeAnalysis = TelasetryEngine.analyzeCurrentTimeWindow(activeProvider)
                _uiState.update { state ->
                    state.copy(
                        liveClockUtc3 = timeAnalysis.formattedTimeUtc3,
                        currentTimeAnalysis = timeAnalysis
                    )
                }
                delay(1000L)
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        // Instant live preview detection while typing
        runSimulation(customSalt = System.currentTimeMillis() / 60000L, logToTerminal = false)
    }

    fun updateBankrollInput(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(bankrollInput = filtered) }
    }

    fun updateBetSizeInput(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(betSizeInput = filtered) }
    }

    fun selectQuickPreset(query: String, suggestedBankroll: Int? = null, suggestedBet: Int? = null) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                bankrollInput = suggestedBankroll?.toString() ?: state.bankrollInput,
                betSizeInput = suggestedBet?.toString() ?: state.betSizeInput,
                selectedTab = 0
            )
        }
        runSimulation(customSalt = System.nanoTime(), logToTerminal = true)
    }

    fun runSimulation(customSalt: Long = System.nanoTime(), logToTerminal: Boolean = true) {
        val state = _uiState.value
        val bankroll = state.bankrollInput.toDoubleOrNull()?.coerceAtLeast(10.0) ?: 2500.0
        val bet = state.betSizeInput.toDoubleOrNull()?.coerceAtLeast(0.5) ?: 10.0

        val report = TelasetryEngine.run500SpinPreSimulation(
            rawInput = state.searchQuery,
            startingBankroll = bankroll,
            betSize = bet,
            dirtySessionRiskScore = state.networkState.dirtySessionRiskScore,
            seedSalt = customSalt
        )

        _uiState.update { current ->
            val updatedLogs = if (logToTerminal) {
                val ts = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
                val verdictLogType = when (report.verdictType) {
                    VerdictType.ENTER_OPTIMAL -> LogType.SUCCESS
                    VerdictType.ENTER_HIT_AND_RUN -> LogType.WARNING
                    VerdictType.DO_NOT_ENTER, VerdictType.RESET_REQUIRED -> LogType.ERROR
                }
                current.terminalLogs + listOf(
                    TerminalLogLine(
                        ts,
                        "$ python3 rc30_radar.py --sim \"${report.detection.game.title}\" --kasa ${bankroll.roundToInt()} --bahis $bet",
                        LogType.COMMAND
                    ),
                    TerminalLogLine(
                        ts,
                        "[RGS: ${report.detection.provider.name}] 500-Spin RTP: %${report.realizedRtpPct} | Erime: %${report.ruinProbabilityPct.roundToInt()} | Bonus: ${report.bonusHitsCount}x | Zirve: ₺${report.peakBankroll.roundToInt()}",
                        LogType.INFO
                    ),
                    TerminalLogLine(
                        ts,
                        "=> KARAR: ${report.verdictTitle}",
                        verdictLogType
                    )
                )
            } else {
                current.terminalLogs
            }

            current.copy(
                simulationReport = report,
                currentTimeAnalysis = report.timeAnalysis,
                simulationRunCounter = current.simulationRunCounter + 1,
                terminalLogs = updatedLogs.takeLast(80)
            )
        }
    }

    fun checkLiveIp(context: Context) {
        _uiState.update {
            it.copy(
                networkState = it.networkState.copy(
                    isCheckingIp = true,
                    statusBanner = "Canlı IP ve ASN/Operatör telemetrisi sorgulanıyor..."
                )
            )
        }
        viewModelScope.launch {
            val updatedNet = NetworkShieldManager.inspectLiveNetwork(context, _uiState.value.networkState)
            val ts = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
            _uiState.update { state ->
                state.copy(
                    networkState = updatedNet,
                    terminalLogs = (state.terminalLogs + TerminalLogLine(
                        ts,
                        "[AĞ KALKANI] Canlı IP: ${updatedNet.publicIp} (${updatedNet.ispOrg}) | Ping: ${updatedNet.latencyMs}ms",
                        if (updatedNet.ipChangedAfterReset == true) LogType.SUCCESS else LogType.INFO
                    )).takeLast(80)
                )
            }
            runSimulation(customSalt = System.nanoTime(), logToTerminal = false)
        }
    }

    fun resetSessionAndFingerprint(context: Context) {
        val cleaned = NetworkShieldManager.resetSessionAndFingerprints(_uiState.value.networkState)
        val ts = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        // Also strip tracking parameters from current search query if any
        val cleanQuery = _uiState.value.searchQuery.substringBefore("?")
        _uiState.update { state ->
            state.copy(
                searchQuery = cleanQuery,
                networkState = cleaned,
                terminalLogs = (state.terminalLogs + listOf(
                    TerminalLogLine(ts, "$ rc30 --reset-session --flush-cookies --spoof-canvas", LogType.COMMAND),
                    TerminalLogLine(
                        ts,
                        "[TEMİZLENDİ] Yeni Session: ${cleaned.sessionIdToken} | Canvas: ${cleaned.canvasWebGlHash} | MAC-İzi: ${cleaned.pseudoMacFingerprint}",
                        LogType.SUCCESS
                    )
                )).takeLast(80)
            )
        }
        checkLiveIp(context)
    }

    fun startAirplaneModeIpRefreshTimer(context: Context) {
        viewModelScope.launch {
            for (sec in 3 downTo 1) {
                _uiState.update { state ->
                    state.copy(
                        networkState = state.networkState.copy(
                            airplaneCountdownSec = sec,
                            statusBanner = "✈️ Uçak Modu 3 Saniye Bekleniyor... ($sec sn) — Baz İstasyonu IP Yenilemesi"
                        )
                    )
                }
                delay(1000L)
            }
            _uiState.update { state ->
                state.copy(
                    networkState = state.networkState.copy(
                        airplaneCountdownSec = 0
                    )
                )
            }
            checkLiveIp(context)
        }
    }

    fun simulateDirtySessionForTest() {
        val ts = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        _uiState.update { state ->
            val currentQuery = state.searchQuery.substringBefore("?")
            state.copy(
                searchQuery = "$currentQuery?aff=7741&clickid=tr_99812&token=dirty_ses",
                networkState = state.networkState.copy(
                    dirtySessionRiskScore = 84,
                    statusBanner = "🚨 RİSKLİ OTURUM & TAKİP İZİ TESPİT EDİLDİ (%84 Risk) — Sıfırlama Gerekli!"
                ),
                terminalLogs = (state.terminalLogs + TerminalLogLine(
                    ts,
                    "[UYARI] Kirli oturum çerezi ve ?aff= takip parametresi simüle edildi. Radar otomatik engellemeye geçti!",
                    LogType.ERROR
                )).takeLast(80)
            )
        }
        runSimulation(customSalt = System.nanoTime(), logToTerminal = true)
    }

    fun updateTerminalInput(text: String) {
        _uiState.update { it.copy(terminalCommandInput = text) }
    }

    fun executeTerminalCommand(rawCmd: String, context: Context) {
        val cmd = rawCmd.trim()
        if (cmd.isEmpty()) return
        val ts = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        val lower = cmd.lowercase(Locale.forLanguageTag("tr"))

        val newLines = mutableListOf(
            TerminalLogLine(ts, "u0_a394@termux:~/rc30-v3 $ $cmd", LogType.COMMAND)
        )

        when {
            lower == "help" || lower == "--help" -> {
                newLines.add(TerminalLogLine(ts, "Kullanılabilir RC30 Termux Komutları:", LogType.MATRIX))
                newLines.add(TerminalLogLine(ts, "  sim <oyun/url>   : 500-Spin Monte Carlo ön-simülasyon koşar ve GİR/GİRME kararı verir", LogType.INFO))
                newLines.add(TerminalLogLine(ts, "  ipreset          : Oturum, Canvas/WebGL hash ve çerez izlerini sıfırlar + canlı IP denetler", LogType.INFO))
                newLines.add(TerminalLogLine(ts, "  seed             : Tüm sağlayıcıların anlık saat/dakika tohum (seed) koridorunu listeler", LogType.INFO))
                newLines.add(TerminalLogLine(ts, "  games            : Desteklenen 10 sağlayıcı ve oyun imzalarını listeler", LogType.INFO))
                newLines.add(TerminalLogLine(ts, "  clear            : Terminal ekranını temizler", LogType.INFO))
            }
            lower == "clear" || lower == "cls" -> {
                _uiState.update { it.copy(terminalLogs = emptyList(), terminalCommandInput = "") }
                return
            }
            lower.startsWith("ipreset") || lower.startsWith("reset") -> {
                _uiState.update { it.copy(terminalCommandInput = "") }
                resetSessionAndFingerprint(context)
                return
            }
            lower.startsWith("seed") || lower.startsWith("saat") -> {
                val t = _uiState.value.currentTimeAnalysis
                newLines.add(
                    TerminalLogLine(
                        ts,
                        "[SAAT/TOHUM ANALİZİ] ${t.formattedTimeUtc3} | Yük: %${t.serverLoadPct} | Evre: ${t.seedCyclePhase}",
                        if (t.isGoldenWindow) LogType.SUCCESS else if (t.isDeadZoneMinute) LogType.ERROR else LogType.WARNING
                    )
                )
                newLines.add(TerminalLogLine(ts, t.timeVerdictNote, LogType.INFO))
            }
            lower.startsWith("games") || lower.startsWith("list") -> {
                TelasetryEngine.providers.forEach { p ->
                    newLines.add(
                        TerminalLogLine(
                            ts,
                            "• ${p.name.padEnd(20)} | Baz RTP: %${p.defaultRtp} | Senkron dk: ${p.syncMinutes.joinToString(",")}",
                            LogType.INFO
                        )
                    )
                }
            }
            lower.startsWith("sim ") -> {
                val target = cmd.substringAfter("sim ").trim()
                _uiState.update { it.copy(searchQuery = target, terminalCommandInput = "") }
                runSimulation(customSalt = System.nanoTime(), logToTerminal = true)
                return
            }
            else -> {
                // Treat any other input as a game/URL simulation request!
                _uiState.update { it.copy(searchQuery = cmd, terminalCommandInput = "") }
                runSimulation(customSalt = System.nanoTime(), logToTerminal = true)
                return
            }
        }

        _uiState.update { state ->
            state.copy(
                terminalLogs = (state.terminalLogs + newLines).takeLast(80),
                terminalCommandInput = ""
            )
        }
    }
}
