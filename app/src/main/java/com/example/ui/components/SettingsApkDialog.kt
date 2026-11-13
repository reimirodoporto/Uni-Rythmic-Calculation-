package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.BuildConfig
import com.example.domain.ApkExportManager
import com.example.domain.StoredApkRecord
import com.example.ui.UniRythmicsUiState
import com.example.ui.theme.TabularMonoStyle

@Composable
fun SettingsApkDialog(
    uiState: UniRythmicsUiState,
    onDismiss: () -> Unit,
    onToggleTheme: () -> Unit,
    onResetCalibration: () -> Unit,
    onDownloadLatestApk: () -> Unit,
    onRefreshPreviousApks: () -> Unit,
    onSelectPreviousApk: (StoredApkRecord) -> Unit,
    onPromptEmailApk: (StoredApkRecord) -> Unit,
    onDismissEmailPrompt: () -> Unit,
    onRecipientEmailChange: (String) -> Unit
) {
    val context = LocalContext.current
    var prevMenuExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.65f)),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp)
                .testTag("settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SETTINGS",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    MoonGlassLedButton(
                        isOn = true,
                        icon = Icons.Rounded.Close,
                        contentDescription = "close settings",
                        accentColor = MaterialTheme.colorScheme.error,
                        onClick = onDismiss,
                        testTag = "settings_close_button"
                    )
                }

                // System Appearance & Calibration Controls
                HorologicalPanel(
                    title = "INSTRUMENT CONTROLS",
                    badge = "SYSTEM",
                    accentColor = MaterialTheme.colorScheme.primary
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MoonGlassLedButton(
                            isOn = uiState.isDarkTheme,
                            icon = if (uiState.isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                            contentDescription = "toggle theme",
                            symbolText = if (uiState.isDarkTheme) "DARK" else "LIGHT",
                            accentColor = MaterialTheme.colorScheme.secondary,
                            onClick = onToggleTheme,
                            testTag = "settings_theme_btn",
                            modifier = Modifier.weight(1f)
                        )
                        MoonGlassLedButton(
                            isOn = false,
                            icon = Icons.Rounded.RestartAlt,
                            contentDescription = "reset workbench calibration",
                            symbolText = "RESET",
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = onResetCalibration,
                            testTag = "settings_reset_btn",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Download Latest APK Directly to Storage + Previous Versions Dropdown
                HorologicalPanel(
                    title = "APK STORAGE EXPORT",
                    badge = "V${BuildConfig.VERSION_NAME} B${BuildConfig.VERSION_CODE}",
                    accentColor = MaterialTheme.colorScheme.secondary
                ) {
                    Text(
                        text = "download the latest compiled apk build directly to device downloads storage or select a previous saved version below",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "package: ${BuildConfig.APPLICATION_ID}\nversion: ${BuildConfig.VERSION_NAME} (code ${BuildConfig.VERSION_CODE})",
                            style = TabularMonoStyle,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    MoonGlassLedButton(
                        isOn = !uiState.isDownloadingApk,
                        icon = Icons.Rounded.Download,
                        contentDescription = "download latest apk directly to storage",
                        symbolText = if (uiState.isDownloadingApk) "SAVING" else "DOWNLOAD APK",
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        enabled = !uiState.isDownloadingApk,
                        onClick = onDownloadLatestApk,
                        testTag = "settings_download_apk_button",
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (uiState.isDownloadingApk) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "exporting apk package to device downloads",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else if (uiState.apkStatusMessage != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            shape = MaterialTheme.shapes.extraSmall,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = uiState.apkStatusMessage,
                                style = TabularMonoStyle,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // Previous Version Downloads Dropdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PREVIOUS DOWNLOADS",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box {
                                MoonGlassLedButton(
                                    isOn = prevMenuExpanded || uiState.previousApks.isNotEmpty(),
                                    icon = Icons.Rounded.History,
                                    contentDescription = "previous version downloads dropdown",
                                    symbolText = "PREV ${uiState.previousApks.size}",
                                    accentColor = MaterialTheme.colorScheme.secondary,
                                    onClick = { prevMenuExpanded = true },
                                    testTag = "settings_prev_apk_dropdown"
                                )
                                DropdownMenu(
                                    expanded = prevMenuExpanded,
                                    onDismissRequest = { prevMenuExpanded = false }
                                ) {
                                    if (uiState.previousApks.isEmpty()) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "no previous downloads yet",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            },
                                            onClick = { prevMenuExpanded = false }
                                        )
                                    } else {
                                        uiState.previousApks.forEach { apk ->
                                            DropdownMenuItem(
                                                text = {
                                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                        Text(
                                                            text = apk.versionLabel.uppercase(),
                                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                        Text(
                                                            text = "${apk.fileName} | ${ApkExportManager.formatBytes(apk.sizeBytes)} | ${ApkExportManager.formatTimestamp(apk.timestampMs)}",
                                                            style = TabularMonoStyle,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    onSelectPreviousApk(apk)
                                                    prevMenuExpanded = false
                                                },
                                                modifier = Modifier.testTag("prev_apk_item_${apk.id}")
                                            )
                                        }
                                    }
                                }
                            }

                            MoonGlassLedButton(
                                isOn = false,
                                icon = Icons.Rounded.Refresh,
                                contentDescription = "refresh stored apks",
                                accentColor = MaterialTheme.colorScheme.primary,
                                onClick = onRefreshPreviousApks,
                                testTag = "settings_refresh_apks_btn"
                            )
                        }
                    }

                    val selectedPrev = uiState.selectedPreviousApk
                    if (selectedPrev != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "SELECTED ARCHIVE",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "file: ${selectedPrev.fileName}\nver: ${selectedPrev.versionLabel} | size: ${ApkExportManager.formatBytes(selectedPrev.sizeBytes)}\nsha256: ${selectedPrev.sha256Short}\npath: ${selectedPrev.storageLocationLabel}",
                                    style = TabularMonoStyle,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                MoonGlassLedButton(
                                    isOn = true,
                                    icon = Icons.Rounded.Email,
                                    contentDescription = "send selected apk to email",
                                    symbolText = "SEND EMAIL",
                                    accentColor = MaterialTheme.colorScheme.tertiary,
                                    onClick = { onPromptEmailApk(selectedPrev) },
                                    testTag = "settings_email_selected_apk_btn",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Post-Download Confirmation Prompt asking if user also wants to send APK to email
    val emailRecord = uiState.postDownloadEmailPromptApk
    if (emailRecord != null) {
        Dialog(onDismissRequest = onDismissEmailPrompt) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_download_email_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "APK SAVED TO STORAGE",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "download complete would you also like to send this apk to email",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${emailRecord.fileName}\n${ApkExportManager.formatBytes(emailRecord.sizeBytes)} | ${emailRecord.storageLocationLabel}",
                            style = TabularMonoStyle,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = uiState.recipientEmailInput,
                        onValueChange = onRecipientEmailChange,
                        label = { Text("optional recipient email address") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_recipient_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MoonGlassLedButton(
                            isOn = false,
                            icon = Icons.Rounded.Close,
                            contentDescription = "skip sending email",
                            symbolText = "NO THANKS",
                            accentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = onDismissEmailPrompt,
                            testTag = "email_prompt_skip_btn",
                            modifier = Modifier.weight(1f)
                        )
                        MoonGlassLedButton(
                            isOn = true,
                            icon = Icons.Rounded.Email,
                            contentDescription = "send apk to email",
                            symbolText = "SEND EMAIL",
                            accentColor = MaterialTheme.colorScheme.tertiary,
                            onClick = {
                                val intent = ApkExportManager.buildEmailApkIntent(
                                    context = context,
                                    record = emailRecord,
                                    recipientEmail = uiState.recipientEmailInput
                                )
                                context.startActivity(intent)
                                onDismissEmailPrompt()
                            },
                            testTag = "email_prompt_send_btn",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
