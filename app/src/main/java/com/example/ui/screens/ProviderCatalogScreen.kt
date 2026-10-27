package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
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
import com.example.data.TelasetryEngine
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonCyberCyan
import com.example.ui.theme.NeonDangerRed
import com.example.ui.theme.NeonMatrixGreen
import com.example.ui.theme.NeonMatrixGreenDim
import com.example.ui.theme.NeonWarningAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun ProviderCatalogScreen(
    onSelectGameForSim: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CyberSurface)
                    .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = "RGS Veritabanı",
                        tint = NeonCyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SAĞLAYICI TOHUM DÖNGÜSÜ & OYUN KATALOĞU",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonCyberCyan
                    )
                }
                Text(
                    text = "Her sağlayıcının sunucu senkronizasyon dakikaları ve volatilite profilleri aşağıdadır. Herhangi bir oyuna dokunarak anında 500-Spin Ön-Simülasyon başlatabilirsiniz.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        items(TelasetryEngine.providers) { provider ->
            val timeWin = TelasetryEngine.analyzeCurrentTimeWindow(provider)
            val providerGames = TelasetryEngine.games.filter { it.providerCode == provider.code }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberSurface)
                    .border(1.dp, CyberBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = provider.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Baz RTP: %${provider.defaultRtp} • Senkron Dk: ${
                                provider.syncMinutes.joinToString(", ") {
                                    ":${String.format(Locale.US, "%02d", it)}"
                                }
                            }",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyberCyan
                        )
                    }

                    Surface(
                        color = CyberObsidian,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.border(
                            1.dp,
                            if (timeWin.isGoldenWindow) NeonMatrixGreen
                            else if (timeWin.isDeadZoneMinute) NeonDangerRed
                            else NeonWarningAmber,
                            RoundedCornerShape(8.dp)
                        )
                    ) {
                        Text(
                            text = if (timeWin.isGoldenWindow) "AÇIK KORİDOR"
                            else if (timeWin.isDeadZoneMinute) "ÖLÜ DAKİKA"
                            else "YÜK: %${timeWin.serverLoadPct}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (timeWin.isGoldenWindow) NeonMatrixGreen
                            else if (timeWin.isDeadZoneMinute) NeonDangerRed
                            else NeonWarningAmber,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = provider.engineNotes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                providerGames.forEach { game ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberObsidian)
                            .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                            .clickable { onSelectGameForSim(game.title) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("catalog_game_${game.id}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = null,
                                tint = NeonMatrixGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = game.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "RTP: %${game.theoreticalRtp} • Vol: ${game.volatilityTier} • Maks: ${game.maxWinMultiplier}x",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonMatrixGreenDim)
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Simüle Et",
                                tint = NeonMatrixGreen,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SİMÜLE ET",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonMatrixGreen
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
