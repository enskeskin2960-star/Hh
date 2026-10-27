package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.NetworkIdentityState
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
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun NetworkShieldScreen(
    networkState: NetworkIdentityState,
    onCheckLiveIp: () -> Unit,
    onResetSession: () -> Unit,
    onStartAirplaneCountdown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val riskColor = when {
        networkState.dirtySessionRiskScore >= 60 -> NeonDangerRed
        networkState.dirtySessionRiskScore >= 30 -> NeonWarningAmber
        else -> NeonMatrixGreen
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // 1. STATUS BANNER
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (networkState.dirtySessionRiskScore >= 60) NeonDangerRedDim
                        else NeonMatrixGreenDim
                    )
                    .border(1.5.dp, riskColor, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (networkState.dirtySessionRiskScore >= 60) Icons.Default.Warning else Icons.Default.Shield,
                        contentDescription = "Ağ Durumu",
                        tint = riskColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = networkState.statusBanner,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Son Denetim: ${networkState.lastCheckedTime} • Sıfırlama Sayısı: #${networkState.resetCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // 2. LIVE IP & CARRIER TELEMETRY CARD
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberSurface)
                    .border(1.dp, CyberBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Router,
                            contentDescription = "IP Radarı",
                            tint = NeonCyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CANLI IP & OPERATÖR TELEMETRİSİ",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonCyberCyan
                        )
                    }

                    if (networkState.isCheckingIp) {
                        CircularProgressIndicator(
                            color = NeonMatrixGreen,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = "${networkState.latencyMs} ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonMatrixGreen
                        )
                    }
                }

                // Current vs Previous IP Comparison
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberObsidian)
                            .border(1.dp, NeonMatrixGreen.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "AKTİF DIŞ IP ADRESİ",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = networkState.publicIp,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonMatrixGreen,
                            modifier = Modifier.testTag("current_ip_text")
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberObsidian)
                            .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "ÖNCEKİ IP KAYDI",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = networkState.previousIp ?: "Henüz Yok",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }
                }

                if (networkState.ipChangedAfterReset != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (networkState.ipChangedAfterReset) NeonMatrixGreenDim else NeonDangerRedDim
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (networkState.ipChangedAfterReset) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = "IP Durumu",
                            tint = if (networkState.ipChangedAfterReset) NeonMatrixGreen else NeonWarningAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (networkState.ipChangedAfterReset)
                                "DOĞRULANDI: Baz istasyonu size yeni bir IP atadı. Sağlayıcı oturumu sıfırlandı!"
                            else
                                "IP henüz değişmedi. Mobil şebekede 3-5 saniye Uçak Modu açıp kapatarak yeni IP alabilirsiniz.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }

                NetworkDetailRow("Operatör / ASN", networkState.ispOrg)
                NetworkDetailRow("Bağlantı Modu", networkState.connectionMode)
                NetworkDetailRow("Konum / Bölge", networkState.locationLabel)
                NetworkDetailRow(
                    "Aktif Arayüzler",
                    networkState.activeInterfaces.joinToString(", ").ifEmpty { networkState.localInterfaceIp }
                )

                Button(
                    onClick = onCheckLiveIp,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyberCyan,
                        contentColor = Color(0xFF002026)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("check_ip_button")
                ) {
                    Icon(imageVector = Icons.Default.NetworkCheck, contentDescription = "IP Denetle")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CANLI IP VE AĞ ROTASINI ŞİMDİ DENETLE",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3. SESSION FINGERPRINT, CANVAS/WEBGL HASH & PSEUDO-MAC RESET CARD
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberSurface)
                    .border(1.dp, CyberBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Parmak İzi",
                        tint = NeonMatrixGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OTURUM KİMLİĞİ & PARMAK İZİ SIFIRLAMA",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonMatrixGreen
                    )
                }

                // Dirty Session Risk Bar
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Oturum / Çerez Takip Risk Skoru",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "%${networkState.dirtySessionRiskScore}",
                            style = MaterialTheme.typography.labelLarge,
                            color = riskColor
                        )
                    }
                    LinearProgressIndicator(
                        progress = { networkState.dirtySessionRiskScore / 100f },
                        color = riskColor,
                        trackColor = CyberObsidian,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }

                NetworkDetailRow("Oturum Token (Session ID)", networkState.sessionIdToken)
                NetworkDetailRow("Canvas / WebGL Hash", networkState.canvasWebGlHash)
                NetworkDetailRow("Sanal Arayüz (MAC Spoof)", networkState.pseudoMacFingerprint)
                NetworkDetailRow("Tarayıcı User-Agent", networkState.userAgentSpoof.take(44) + "...")

                Button(
                    onClick = onResetSession,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonMatrixGreen,
                        contentColor = Color(0xFF002011)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("reset_session_button")
                ) {
                    Icon(imageVector = Icons.Default.CleaningServices, contentDescription = "Sıfırla")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OTURUM, ÇEREZ & PARMAK İZİNİ ANINDA SIFIRLA",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 3-Second Airplane Mode IP Reset Helper
                OutlinedButton(
                    onClick = {
                        onStartAirplaneCountdown()
                        try {
                            val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(
                                context,
                                "Bildirim panelinden Uçak Modunu 3 sn açıp kapatın; IP otomatik denetlenecek.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("airplane_mode_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AirplanemodeActive,
                        contentDescription = "Uçak Modu",
                        tint = NeonWarningAmber
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (networkState.airplaneCountdownSec > 0)
                            "IP YENİLENİYOR... (${networkState.airplaneCountdownSec} SN)"
                        else
                            "3 SN UÇAK MODU TETİKLE & YENİ IP DOĞRULA",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonWarningAmber
                    )
                }
            }
        }

        // 4. TECHNICAL REALITY EXPLANATION CARD
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CyberSurfaceElevated)
                    .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "TEKNİK GERÇEK: ANDROID'DE IP VE MAC İZİ NASIL ÇALIŞIR?",
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonWarningAmber
                )
                Text(
                    text = "• 4.5G/5G mobil şebekede fiziksel MAC adresiniz baz istasyonundan öteye (oyun sunucusuna) OSI Katman-2 kuralı gereği asla iletilmez.\n" +
                        "• Sağlayıcılar (Pragmatic, PGSoft, EGT vb.) sizi Dış IP adresiniz, Tarayıcı Canvas/WebGL Hash'iniz, Çerezleriniz ve URL'deki ?token= / ?aff= oturum kimliğinizle tanır.\n" +
                        "• Yukarıdaki sıfırlama modülü yerel izleri temizler ve 3 saniyelik Uçak Modu döngüsüyle CGNAT/Baz İstasyonu IP adresinizin gerçekten değişip değişmediğini canlı doğrular.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun NetworkDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CyberObsidian)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}
