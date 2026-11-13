package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Translate
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.TranslatorLanguage
import com.example.domain.UniRythmicsCatalog
import com.example.ui.GeminiLabUiState
import com.example.ui.TranslatorState
import com.example.ui.theme.TabularMonoStyle

@Composable
fun PullTabTopTranslatorDrawer(
    geminiState: GeminiLabUiState,
    onTogglePullDrawer: () -> Unit,
    onInputChange: (String) -> Unit,
    onSelectLanguage: (TranslatorLanguage) -> Unit,
    onExportWorkbenchLean4: () -> Unit,
    onRunTranslate: () -> Unit,
    onReopenOutput: () -> Unit,
    modifier: Modifier = Modifier
) {
    var langMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pull-Down Input Panel anchored at top
        AnimatedVisibility(
            visible = geminiState.isPullDrawerOpen,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header row with Language Lookup Dropdown in TOP RIGHT corner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CODE AND MATH TRANSLATOR",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Box {
                            MoonGlassLedButton(
                                isOn = langMenuExpanded,
                                icon = Icons.Rounded.Language,
                                contentDescription = "Select target language",
                                symbolText = geminiState.selectedLanguage.label,
                                accentColor = MaterialTheme.colorScheme.secondary,
                                onClick = { langMenuExpanded = true },
                                testTag = "translator_language_dropdown"
                            )
                            DropdownMenu(
                                expanded = langMenuExpanded,
                                onDismissRequest = { langMenuExpanded = false }
                            ) {
                                UniRythmicsCatalog.translatorLanguages.forEach { lang ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = lang.label,
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        },
                                        onClick = {
                                            onSelectLanguage(lang)
                                            langMenuExpanded = false
                                        },
                                        modifier = Modifier.testTag("lang_option_${lang.id}")
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = geminiState.translatorInput,
                        onValueChange = onInputChange,
                        label = { Text("enter math expression code or app conversion idea") },
                        minLines = 2,
                        maxLines = 5,
                        textStyle = TabularMonoStyle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("translator_input_field")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MoonGlassLedButton(
                            isOn = true,
                            icon = Icons.Rounded.AutoFixHigh,
                            contentDescription = "Auto correct and translate",
                            symbolText = "TRANSLATE",
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = onRunTranslate,
                            testTag = "translator_run_button",
                            modifier = Modifier.weight(1f)
                        )
                        MoonGlassLedButton(
                            isOn = false,
                            icon = Icons.Rounded.Code,
                            contentDescription = "Export workbench to Lean 4",
                            symbolText = "LEAN 4 SYNC",
                            accentColor = MaterialTheme.colorScheme.secondary,
                            onClick = onExportWorkbenchLean4,
                            testTag = "translator_export_lean4_button",
                            modifier = Modifier.weight(1f)
                        )
                        if (geminiState.translatorState is TranslatorState.Success && !geminiState.isOutputSheetOpen) {
                            MoonGlassLedButton(
                                isOn = true,
                                icon = Icons.Rounded.Translate,
                                contentDescription = "Show output sheet",
                                accentColor = MaterialTheme.colorScheme.tertiary,
                                onClick = onReopenOutput,
                                testTag = "translator_show_output_button"
                            )
                        }
                    }
                }
            }
        }

        // Tactile Pull Tab Handle centered at top edge
        MoonGlassLedButton(
            isOn = geminiState.isPullDrawerOpen,
            icon = if (geminiState.isPullDrawerOpen) Icons.Rounded.ExpandLess else Icons.Rounded.DragHandle,
            contentDescription = "Pull tab translator input",
            symbolText = if (geminiState.isPullDrawerOpen) "CLOSE INPUT" else "TRANSLATOR PULL TAB",
            accentColor = MaterialTheme.colorScheme.secondary,
            onClick = onTogglePullDrawer,
            testTag = "translator_pull_tab",
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
        )
    }
}

@Composable
fun SlideUpTranslatorOutputSheet(
    geminiState: GeminiLabUiState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = geminiState.isOutputSheetOpen,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.65f)),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRANSLATOR OUTPUT",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    MoonGlassLedButton(
                        isOn = true,
                        icon = Icons.Rounded.Close,
                        contentDescription = "Close output sheet",
                        accentColor = MaterialTheme.colorScheme.error,
                        onClick = onDismiss,
                        testTag = "translator_close_output_button"
                    )
                }

                when (val state = geminiState.translatorState) {
                    TranslatorState.Idle -> {
                        Text(
                            text = "enter math or code in the top pull tab to translate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TranslatorState.Loading -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "translating and auto correcting code",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    is TranslatorState.Error -> {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    is TranslatorState.Success -> {
                        val out = state.output

                        Text(
                            text = "TARGET ${out.targetLanguageLabel}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Plain text instructions (no markdown, no ascii clutter, no italics)
                        Text(
                            text = "PLAIN TEXT INSTRUCTIONS",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = out.plainInstructions,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Math / Code always in plain-text monospace block code for easy separation
                        Text(
                            text = "MATH AND CODE BLOCK",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = out.mathAndCodeBlock,
                                style = TabularMonoStyle,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState())
                                    .padding(12.dp)
                            )
                        }

                        // Always adds a bonus vibe code prompt
                        Text(
                            text = "BONUS VIBE CODE PROMPT",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = out.bonusVibePrompt,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Mandatory beta notice
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = MaterialTheme.shapes.extraSmall,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = out.betaNotice,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
