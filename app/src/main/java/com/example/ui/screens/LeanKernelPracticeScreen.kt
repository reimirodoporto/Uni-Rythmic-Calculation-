package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.SairKernelCatalog
import com.example.ui.KernelVerifyStatus
import com.example.ui.SairKernelUiState
import com.example.ui.components.HorologicalPanel
import com.example.ui.components.MoonGlassLedButton
import com.example.ui.components.PageInfoEntry
import com.example.ui.components.UniformPageInfoSection
import com.example.ui.theme.TabularMonoStyle

@Composable
fun LeanKernelPracticeScreen(
    uiState: SairKernelUiState,
    onSelectProblem: (Int) -> Unit,
    onUpdateDraft: (String, String) -> Unit,
    onLoadVariant: (String, Boolean) -> Unit,
    onApplyFix: (String) -> Unit,
    onSubmitUntilCorrect: (String) -> Unit,
    onSendToTranslator: (String) -> Unit,
    onPushToPlayground: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeProblem = uiState.activeProblem
    val attempt = uiState.attempts[activeProblem.id]
    val draftCode = attempt?.draftCode ?: activeProblem.referenceCode
    val isVerifying = uiState.verifyStatus is KernelVerifyStatus.Verifying &&
        uiState.verifyStatus.problemId == activeProblem.id

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 980.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 7-Problem Selector & Playground Gate Header
            item {
                HorologicalPanel(
                    title = "SAIR LEAN 4 KERNEL PRACTICE",
                    badge = "${uiState.verifiedCount} OF 7 VERIFIED",
                    accentColor = if (uiState.allSevenVerified) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                ) {
                    Text(
                        text = "verify all seven stage one kernel problems til correct before pushing your bundle to the sair web playground",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(SairKernelCatalog.problems, key = { _, p -> p.id }) { idx, prob ->
                            val probAttempt = uiState.attempts[prob.id]
                            val verified = probAttempt?.isVerified == true
                            val selected = idx == uiState.selectedProblemIndex
                            MoonGlassLedButton(
                                isOn = selected || verified,
                                icon = if (verified) Icons.Rounded.CheckCircle else Icons.Rounded.Code,
                                contentDescription = prob.title,
                                symbolText = prob.shortTag,
                                accentColor = if (verified) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                onClick = { onSelectProblem(idx) },
                                testTag = "sair_problem_chip_${prob.id}"
                            )
                        }
                    }

                    // Playground Push Gate Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MoonGlassLedButton(
                            isOn = uiState.allSevenVerified,
                            icon = if (uiState.allSevenVerified) Icons.Rounded.CloudUpload else Icons.Rounded.Lock,
                            contentDescription = "push verified seven problem bundle to sair playground",
                            symbolText = if (uiState.allSevenVerified) "PUSH PLAYGROUND" else "LOCKED ${uiState.verifiedCount}/7",
                            accentColor = MaterialTheme.colorScheme.tertiary,
                            enabled = uiState.allSevenVerified,
                            onClick = onPushToPlayground,
                            testTag = "sair_push_playground_btn",
                            modifier = Modifier.weight(1f)
                        )

                        MoonGlassLedButton(
                            isOn = uiState.allSevenVerified,
                            icon = Icons.Rounded.OpenInBrowser,
                            contentDescription = "open sair lean kernel challenge web app",
                            symbolText = "WEB",
                            accentColor = MaterialTheme.colorScheme.secondary,
                            enabled = uiState.allSevenVerified,
                            onClick = {
                                onPushToPlayground()
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/SAIRcompetition/lean-kernel-challenge")
                                )
                                context.startActivity(intent)
                            },
                            testTag = "sair_open_web_btn"
                        )
                    }
                }
            }

            // Active Problem Editor & Submit Til Correct Engine
            item {
                val isVerified = attempt?.isVerified == true
                HorologicalPanel(
                    title = activeProblem.title,
                    badge = if (isVerified) "PASS ATTEMPTS ${attempt?.attemptCount ?: 0}" else "ATTEMPTS ${attempt?.attemptCount ?: 0}",
                    accentColor = if (isVerified) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                ) {
                    Text(
                        text = activeProblem.objective,
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
                            text = activeProblem.specMathBlock,
                            style = TabularMonoStyle,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(10.dp)
                        )
                    }

                    // Variant Selector (e.g. Mertens Variant A tail-rec vs Variant B odd-step sieve)
                    if (activeProblem.alternateCode != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MoonGlassLedButton(
                                isOn = draftCode == activeProblem.referenceCode,
                                icon = Icons.Rounded.SwapHoriz,
                                contentDescription = "load variant a tail recursive",
                                symbolText = "VARIANT A",
                                accentColor = MaterialTheme.colorScheme.primary,
                                onClick = { onLoadVariant(activeProblem.id, false) },
                                testTag = "sair_variant_a_btn",
                                modifier = Modifier.weight(1f)
                            )
                            MoonGlassLedButton(
                                isOn = draftCode == activeProblem.alternateCode,
                                icon = Icons.Rounded.SwapHoriz,
                                contentDescription = "load variant b odd step sieve",
                                symbolText = "VARIANT B",
                                accentColor = MaterialTheme.colorScheme.secondary,
                                onClick = { onLoadVariant(activeProblem.id, true) },
                                testTag = "sair_variant_b_btn",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = draftCode,
                        onValueChange = { onUpdateDraft(activeProblem.id, it) },
                        label = { Text("lean 4 kernel submission code") },
                        minLines = 8,
                        maxLines = 18,
                        textStyle = TabularMonoStyle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sair_code_editor")
                    )

                    // Action Bar: Submit Til Correct, Translate via Top Pull-Tab, Copy, Reset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MoonGlassLedButton(
                            isOn = !isVerifying,
                            icon = Icons.Rounded.PlayArrow,
                            contentDescription = "submit til correct kernel check",
                            symbolText = if (isVerifying) "CHECKING" else "SUBMIT",
                            accentColor = MaterialTheme.colorScheme.tertiary,
                            enabled = !isVerifying,
                            onClick = { onSubmitUntilCorrect(activeProblem.id) },
                            testTag = "sair_submit_btn",
                            modifier = Modifier.weight(1f)
                        )
                        MoonGlassLedButton(
                            isOn = false,
                            icon = Icons.Rounded.Translate,
                            contentDescription = "send to top pull tab translator",
                            accentColor = MaterialTheme.colorScheme.secondary,
                            onClick = { onSendToTranslator(draftCode) },
                            testTag = "sair_translate_btn"
                        )
                        MoonGlassLedButton(
                            isOn = false,
                            icon = Icons.Rounded.ContentCopy,
                            contentDescription = "copy lean 4 submission",
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = {
                                clipboardManager.setText(AnnotatedString(draftCode))
                            },
                            testTag = "sair_copy_btn"
                        )
                        MoonGlassLedButton(
                            isOn = false,
                            icon = Icons.Rounded.RestartAlt,
                            contentDescription = "reset to reference example",
                            accentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = { onLoadVariant(activeProblem.id, false) },
                            testTag = "sair_reset_btn"
                        )
                    }

                    // Live Verification Feedback Card
                    if (isVerifying) {
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
                                text = "running static kernel gate and gemini proof audit",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else if (attempt != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(
                                1.dp,
                                if (attempt.isVerified) {
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (attempt.isVerified) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                                        contentDescription = "verification status",
                                        tint = if (attempt.isVerified) {
                                            MaterialTheme.colorScheme.tertiary
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        }
                                    )
                                    Text(
                                        text = if (attempt.isVerified) "KERNEL PROOF VERIFIED" else "SUBMIT STATUS",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (attempt.isVerified) {
                                            MaterialTheme.colorScheme.tertiary
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        }
                                    )
                                }

                                Text(
                                    text = attempt.statusNote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (attempt.suggestedFixCode != null) {
                                    MoonGlassLedButton(
                                        isOn = true,
                                        icon = Icons.Rounded.AutoFixHigh,
                                        contentDescription = "apply auto corrected lean 4 fix",
                                        symbolText = "APPLY FIX",
                                        accentColor = MaterialTheme.colorScheme.secondary,
                                        onClick = { onApplyFix(activeProblem.id) },
                                        testTag = "sair_apply_fix_btn"
                                    )
                                }

                                val bonus = attempt.bonusVibePrompt
                                if (!bonus.isNullOrBlank()) {
                                    Text(
                                        text = "BONUS VIBE CODE PROMPT",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = bonus,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Uniform Scroll Info Section Below Instrument
            item {
                UniformPageInfoSection(
                    sectionTitle = "SAIR PLAYGROUND PIPELINE GUIDE",
                    items = listOf(
                        PageInfoEntry(
                            heading = "SEVEN STAGE ONE KERNEL PROBLEMS",
                            plainBody = "practice deck covers fibonacci partition mertens primecount permanent rule 110 and sha256 register loop inside clean namespace submission",
                            mathBlock = "namespace Submission -> def impl -> theorem impl_correct -> end Submission"
                        ),
                        PageInfoEntry(
                            heading = "SUBMIT TIL CORRECT GATE",
                            plainBody = "every submit runs local utf8 and axiom safety checks followed by gemini proof audit only after all seven pass does push playground unlock",
                            mathBlock = "0 sorry | 0 admit | 0 axiom | clean UTF-8 arrows ->"
                        )
                    )
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}
