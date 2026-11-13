package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Stream
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.AiResponseState
import com.example.ui.GeminiLabUiState
import com.example.ui.GeminiMode
import com.example.ui.components.HorologicalPanel
import com.example.ui.components.MoonGlassLedButton
import com.example.ui.components.PageInfoEntry
import com.example.ui.components.UniformPageInfoSection
import com.example.ui.theme.TabularMonoStyle

private data class MetrologyPromptPreset(
    val id: String,
    val label: String,
    val prompt: String
)

private val metrologyPromptPresets = listOf(
    MetrologyPromptPreset(
        id = "half_vs_whole",
        label = "PHI",
        prompt = "Explain why Half Measure (1/2 = 1/Phi => 1/1 = 2/Phi) and Whole Measure (1 = 1/Phi) diverge from rounded 0.618 across 10^-n shifts."
    ),
    MetrologyPromptPreset(
        id = "planck_ratio",
        label = "PLANCK",
        prompt = "Show how the current 1 var and Rolodex multiplier compare against CODATA 2022 Planck time (5.391247e-44 s) and Cesium-133 frequency."
    ),
    MetrologyPromptPreset(
        id = "dodecagon_sector",
        label = "30°",
        prompt = "How does sweeping a 360-degree dodecagonal dial across 12 x 30° sectors map geometric phase to 1 Gunit scaling?"
    ),
    MetrologyPromptPreset(
        id = "stevens_scale",
        label = "RATIO",
        prompt = "Why does Uni-Rythmics scaling require Stevens Ratio Scale (with true zero) rather than Interval or Ordinal scales?"
    )
)

@Composable
fun GeminiLabScreen(
    geminiState: GeminiLabUiState,
    splitProseAndMath: (String) -> List<Pair<Boolean, String>>,
    onPromptChange: (String) -> Unit,
    onSelectMode: (GeminiMode) -> Unit,
    onToggleContext: (Boolean) -> Unit,
    onSubmit: (String?) -> Unit,
    onCancel: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBusy = geminiState.responseState is AiResponseState.Loading ||
        geminiState.responseState is AiResponseState.Streaming

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 900.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                HorologicalPanel(
                    title = "GEMINI METROLOGY LAB",
                    badge = "GEMINI 3.8 FLASH",
                    accentColor = MaterialTheme.colorScheme.secondary
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MoonGlassLedButton(
                            isOn = geminiState.mode == GeminiMode.STREAMING_QA,
                            icon = Icons.Rounded.Stream,
                            contentDescription = "streaming mode",
                            symbolText = "STREAM",
                            accentColor = MaterialTheme.colorScheme.secondary,
                            onClick = { onSelectMode(GeminiMode.STREAMING_QA) },
                            testTag = "gemini_mode_streaming",
                            modifier = Modifier.weight(1f)
                        )
                        MoonGlassLedButton(
                            isOn = geminiState.mode == GeminiMode.STRUCTURED_PROOF,
                            icon = Icons.Rounded.DataObject,
                            contentDescription = "structured proof mode",
                            symbolText = "SCHEMA",
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = { onSelectMode(GeminiMode.STRUCTURED_PROOF) },
                            testTag = "gemini_mode_structured",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LIVE WORKBENCH CONTEXT",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "attach gunit sector power shift rolodex and comparison state",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        MoonGlassLedButton(
                            isOn = geminiState.includeWorkbenchContext,
                            icon = if (geminiState.includeWorkbenchContext) Icons.Rounded.Link else Icons.Rounded.LinkOff,
                            contentDescription = "toggle live workbench context",
                            symbolText = if (geminiState.includeWorkbenchContext) "ON" else "OFF",
                            accentColor = MaterialTheme.colorScheme.tertiary,
                            onClick = { onToggleContext(!geminiState.includeWorkbenchContext) },
                            testTag = "gemini_context_switch"
                        )
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(metrologyPromptPresets, key = { it.id }) { preset ->
                            MoonGlassLedButton(
                                isOn = false,
                                icon = Icons.Rounded.Psychology,
                                contentDescription = preset.prompt,
                                symbolText = preset.label,
                                accentColor = MaterialTheme.colorScheme.primary,
                                enabled = !isBusy,
                                onClick = { onSubmit(preset.prompt) },
                                testTag = "gemini_preset_${preset.id}"
                            )
                        }
                    }

                    OutlinedTextField(
                        value = geminiState.promptInput,
                        onValueChange = onPromptChange,
                        label = { Text("enter metrology question or proof request") },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gemini_prompt_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isBusy) {
                            MoonGlassLedButton(
                                isOn = true,
                                icon = Icons.Rounded.Stop,
                                contentDescription = "stop generation",
                                symbolText = "STOP",
                                accentColor = MaterialTheme.colorScheme.error,
                                onClick = onCancel,
                                testTag = "gemini_cancel_button",
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            MoonGlassLedButton(
                                isOn = geminiState.promptInput.isNotBlank(),
                                icon = Icons.AutoMirrored.Rounded.Send,
                                contentDescription = "send prompt to gemini",
                                symbolText = if (geminiState.mode == GeminiMode.STREAMING_QA) "ANALYZE" else "PROOF",
                                accentColor = MaterialTheme.colorScheme.secondary,
                                enabled = geminiState.promptInput.isNotBlank(),
                                onClick = { onSubmit(null) },
                                testTag = "gemini_send_button",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (geminiState.responseState !is AiResponseState.Idle) {
                            MoonGlassLedButton(
                                isOn = false,
                                icon = Icons.Rounded.Close,
                                contentDescription = "clear response",
                                symbolText = "CLEAR",
                                accentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                onClick = onClear,
                                testTag = "gemini_clear_button"
                            )
                        }
                    }
                }
            }

            when (val state = geminiState.responseState) {
                AiResponseState.Idle -> {
                    item {
                        HorologicalPanel(
                            title = "STANDBY TELEMETRY",
                            badge = "IDLE",
                            accentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = "standby",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "select a quick icon preset or enter any metrology prompt above",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                AiResponseState.Loading -> {
                    item {
                        HorologicalPanel(
                            title = "ATTESTING CONNECTION",
                            badge = "APP CHECK",
                            accentColor = MaterialTheme.colorScheme.secondary
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.5.dp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "querying gemini via firebase ai logic",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                is AiResponseState.Streaming -> {
                    item {
                        HorologicalPanel(
                            title = "LIVE STREAM",
                            badge = "STREAMING",
                            accentColor = MaterialTheme.colorScheme.secondary
                        ) {
                            ProseAndMathBlockRenderer(segments = splitProseAndMath(state.text))
                        }
                    }
                }

                is AiResponseState.SuccessText -> {
                    item {
                        HorologicalPanel(
                            title = "GEMINI RESPONSE",
                            badge = "COMPLETE",
                            accentColor = MaterialTheme.colorScheme.tertiary
                        ) {
                            ProseAndMathBlockRenderer(segments = splitProseAndMath(state.text))
                        }
                    }
                }

                is AiResponseState.SuccessStructured -> {
                    item {
                        val analysis = state.analysis
                        HorologicalPanel(
                            title = "STRUCTURED BREAKDOWN",
                            badge = "JSON SCHEMA",
                            accentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Verified,
                                    contentDescription = "verified precision note",
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    text = analysis.precisionNote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }

                            Text(
                                text = "SYNTHESIS",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = analysis.summary,
                                style = MaterialTheme.typography.bodyMedium
                            )

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
                                    text = analysis.dimensionalBreakdown,
                                    style = TabularMonoStyle,
                                    modifier = Modifier
                                        .horizontalScroll(rememberScrollState())
                                        .padding(12.dp)
                                )
                            }

                            if (analysis.calibrationSteps.isNotEmpty()) {
                                Text(
                                    text = "CALIBRATION STEPS",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                analysis.calibrationSteps.forEachIndexed { idx, step ->
                                    Text(
                                        text = "${idx + 1} $step",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                is AiResponseState.Error -> {
                    item {
                        HorologicalPanel(
                            title = "REQUEST ERROR",
                            badge = "ERROR",
                            accentColor = MaterialTheme.colorScheme.error
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ErrorOutline,
                                    contentDescription = "error",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            MoonGlassLedButton(
                                isOn = true,
                                icon = Icons.Rounded.Refresh,
                                contentDescription = "retry request",
                                symbolText = "RETRY",
                                accentColor = MaterialTheme.colorScheme.primary,
                                onClick = { onSubmit(null) },
                                testTag = "gemini_retry_button"
                            )
                        }
                    }
                }
            }

            item {
                UniformPageInfoSection(
                    sectionTitle = "GEMINI LAB GUIDE",
                    items = listOf(
                        PageInfoEntry(
                            heading = "SEPARATED PROSE AND MATH",
                            plainBody = "plain text commentary appears in lowercase sans serif while equations and lean 4 snippets isolate cleanly inside monospace code blocks",
                            mathBlock = "def gunit (n : Nat) : Real := ((Real.sqrt 5 - 1) / 2) * (10 : Real) ^ (-n : Int)"
                        ),
                        PageInfoEntry(
                            heading = "TOP PULL TAB TRANSLATOR",
                            plainBody = "use the pull tab drawer at the top for dedicated lean 4 auto correction or multi language conversion with automatic bonus vibe prompts"
                        )
                    )
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun ProseAndMathBlockRenderer(segments: List<Pair<Boolean, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        segments.forEach { (isCodeOrMath, content) ->
            if (isCodeOrMath) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = content,
                        style = TabularMonoStyle,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(12.dp)
                    )
                }
            } else {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
