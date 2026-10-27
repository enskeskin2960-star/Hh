package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.data.TermuxScriptProvider
import com.example.model.LogType
import com.example.model.TerminalLogLine
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

private val quickTermuxCommands = listOf(
    "sim gates of olympus 1000",
    "sim sweet bonanza 1000",
    "sim shining crown",
    "sim wanted dead or a wild",
    "ipreset",
    "seed",
    "games",
    "help",
    "clear"
)

@Composable
fun TermuxCliScreen(
    terminalLogs: List<TerminalLogLine>,
    commandInput: String,
    onCommandInputChange: (String) -> Unit,
    onExecuteCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. TERMUX SCRIPT EXPORTER CARD (1-TAP COPY FOR NATIVE TERMUX APP)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CyberSurface)
                .border(1.dp, NeonMatrixGreen.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Termux",
                        tint = NeonMatrixGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SAF TERMUX PYTHON MOTORU (rc30_radar.py)",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonMatrixGreen
                    )
                }

                IconButton(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "rc30_radar.py")
                            putExtra(Intent.EXTRA_TEXT, TermuxScriptProvider.pythonScriptContent)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Termux Kodu Paylaş"))
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Kodu Paylaş",
                        tint = NeonCyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = "Aşağıdaki butonla tek satırlık kurulum kodunu kopyalayıp doğrudan Termux'a yapıştırarak veya alttaki gömülü Termux konsolundan çalıştırabilirsiniz:",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        copyToClipboard(
                            context,
                            "RC30 Termux One-Liner",
                            TermuxScriptProvider.oneLineTermuxCommand,
                            "✅ Termux Tek-Satır Kurulum + Başlatma komutu kopyalandı! Termux'a yapıştırıp ENTER'a basın."
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonMatrixGreen,
                        contentColor = Color(0xFF002111)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("copy_termux_oneliner_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Kopyala",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TEK-SATIR TERMUX KUR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        copyToClipboard(
                            context,
                            "rc30_radar.py",
                            TermuxScriptProvider.pythonScriptContent,
                            "✅ Saf Python 3 kodu (rc30_radar.py) panoya kopyalandı!"
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("copy_python_script_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Python Kopyala",
                        tint = NeonCyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SAF .PY KOPYALA",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyberCyan
                    )
                }
            }
        }

        // 2. QUICK TERMUX COMMAND CHIPS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickTermuxCommands.forEach { cmd ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonMatrixGreenDim)
                        .border(1.dp, NeonMatrixGreen.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .clickable { onExecuteCommand(cmd) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$ $cmd",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonMatrixGreen
                    )
                }
            }
        }

        // 3. LIVE INTERACTIVE TERMUX TERMINAL WINDOW
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CyberObsidian)
                .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
                .testTag("termux_console_output"),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(terminalLogs) { line ->
                val textColor = when (line.type) {
                    LogType.COMMAND -> NeonCyberCyan
                    LogType.INFO -> TextPrimary
                    LogType.SUCCESS -> NeonMatrixGreen
                    LogType.WARNING -> NeonWarningAmber
                    LogType.ERROR -> NeonDangerRed
                    LogType.MATRIX -> NeonMatrixGreen
                }
                Text(
                    text = "[${line.timestamp}] ${line.text}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
        }

        // 4. TERMUX COMMAND PROMPT INPUT
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = commandInput,
                onValueChange = onCommandInputChange,
                placeholder = { Text("u0_a394@termux:~$ sim olympus / ipreset / seed") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = { onExecuteCommand(commandInput) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonMatrixGreen,
                    unfocusedBorderColor = CyberBorder
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("terminal_command_input")
            )

            Button(
                onClick = { onExecuteCommand(commandInput) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonMatrixGreen,
                    contentColor = Color(0xFF002010)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(54.dp)
                    .testTag("terminal_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Komut Gönder"
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String, toastMessage: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    cm?.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show()
}
