package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.RadarViewModel
import com.example.ui.screens.NetworkShieldScreen
import com.example.ui.screens.ProviderCatalogScreen
import com.example.ui.screens.RadarSimScreen
import com.example.ui.screens.TermuxCliScreen
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyberCyan
import com.example.ui.theme.NeonMatrixGreen
import com.example.ui.theme.NeonMatrixGreenDim
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Rc30RadarApp()
            }
        }
    }
}

@Composable
fun Rc30RadarApp(
    viewModel: RadarViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.checkLiveIp(context)
    }

    if (uiState.selectedTab != 0) {
        BackHandler {
            viewModel.selectTab(0)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CyberObsidian,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Rc30TopHeader(
                currentIp = uiState.networkState.publicIp,
                onTerminalBadgeClick = { viewModel.selectTab(2) }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CyberSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = { Icon(Icons.Default.Radar, contentDescription = "Radar") },
                    label = { Text(stringResource(R.string.tab_radar), style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonMatrixGreen,
                        selectedTextColor = NeonMatrixGreen,
                        indicatorColor = NeonMatrixGreenDim,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_radar")
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = { Icon(Icons.Default.Security, contentDescription = "IP & Oturum") },
                    label = { Text(stringResource(R.string.tab_network), style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyberCyan,
                        selectedTextColor = NeonCyberCyan,
                        indicatorColor = NeonMatrixGreenDim,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_network")
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = { Icon(Icons.Default.Terminal, contentDescription = "Termux CLI") },
                    label = { Text(stringResource(R.string.tab_termux), style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonMatrixGreen,
                        selectedTextColor = NeonMatrixGreen,
                        indicatorColor = NeonMatrixGreenDim,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_termux")
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = { Icon(Icons.Default.Storage, contentDescription = "Sağlayıcılar") },
                    label = { Text(stringResource(R.string.tab_catalog), style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyberCyan,
                        selectedTextColor = NeonCyberCyan,
                        indicatorColor = NeonMatrixGreenDim,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_catalog")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.selectedTab) {
                0 -> RadarSimScreen(
                    uiState = uiState,
                    onQueryChange = viewModel::updateSearchQuery,
                    onBankrollChange = viewModel::updateBankrollInput,
                    onBetSizeChange = viewModel::updateBetSizeInput,
                    onSelectPreset = { preset -> viewModel.selectQuickPreset(preset) },
                    onRunSimulation = { viewModel.runSimulation() },
                    onQuickResetSession = { viewModel.resetSessionAndFingerprint(context) },
                    onSimulateDirtySession = { viewModel.simulateDirtySessionForTest() },
                    onSwitchToNetworkTab = { viewModel.selectTab(1) }
                )

                1 -> NetworkShieldScreen(
                    networkState = uiState.networkState,
                    onCheckLiveIp = { viewModel.checkLiveIp(context) },
                    onResetSession = { viewModel.resetSessionAndFingerprint(context) },
                    onStartAirplaneCountdown = { viewModel.startAirplaneModeIpRefreshTimer(context) }
                )

                2 -> TermuxCliScreen(
                    terminalLogs = uiState.terminalLogs,
                    commandInput = uiState.terminalCommandInput,
                    onCommandInputChange = viewModel::updateTerminalInput,
                    onExecuteCommand = { cmd -> viewModel.executeTerminalCommand(cmd, context) }
                )

                3 -> ProviderCatalogScreen(
                    onSelectGameForSim = { gameTitle ->
                        viewModel.selectQuickPreset(gameTitle)
                    }
                )
            }
        }
    }
}

@Composable
private fun Rc30TopHeader(
    currentIp: String,
    onTerminalBadgeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(NeonMatrixGreenDim)
                    .border(1.dp, NeonMatrixGreen, RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "RC30 Logo",
                    tint = NeonMatrixGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = stringResource(R.string.radar_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "IP: $currentIp",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyberCyan
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(CyberObsidian)
                .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 9.dp, vertical = 5.dp)
        ) {
            Text(
                text = "TERMUX V3.4",
                style = MaterialTheme.typography.labelSmall,
                color = NeonMatrixGreen
            )
        }
    }
}
