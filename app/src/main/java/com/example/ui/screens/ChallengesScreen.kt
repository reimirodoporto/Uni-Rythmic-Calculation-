package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.UniRythmicsCatalog
import com.example.domain.UniRythmicsEngine
import com.example.ui.UniRythmicsUiState
import com.example.ui.components.FilamentReadoutBox
import com.example.ui.components.HorologicalPanel
import com.example.ui.components.MoonGlassLedButton
import com.example.ui.components.PageInfoEntry
import com.example.ui.components.UniformPageInfoSection
import com.example.ui.theme.TabularMonoStyle

@Composable
fun ChallengesScreen(
    uiState: UniRythmicsUiState,
    onSelectChallenge: (String) -> Unit,
    onOpenWorkbench: () -> Unit,
    modifier: Modifier = Modifier
) {
    val solvedCount = UniRythmicsCatalog.challenges.count { ch ->
        ch.isSolvedBy(
            measureType = uiState.measureType,
            precisionMode = uiState.precisionMode,
            sector = uiState.dodecagonSector,
            exponent = uiState.nthExponent,
            multiplier = uiState.multiplierInt,
            unitId = uiState.primaryUnitId
        )
    }

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
                    title = "CALIBRATION CHALLENGES",
                    badge = "$solvedCount / ${UniRythmicsCatalog.challenges.size} MATCHED",
                    accentColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "dial the dodecagonal sector half or whole toggles power shift and six digit rolodex multiplier on the workbench to match each metrology target",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val activeUnit = UniRythmicsCatalog.findUnitById(uiState.primaryUnitId)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilamentReadoutBox(
                            label = "live mode",
                            value = "${uiState.measureType.shortSymbol} ${uiState.precisionMode.shortSymbol}",
                            accent = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        FilamentReadoutBox(
                            label = "dial and rolodex",
                            value = "S${uiState.dodecagonSector} 10^-${uiState.nthExponent} x${UniRythmicsEngine.formatRolodex(uiState.multiplierInt)} ${activeUnit.shortName}",
                            accent = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    MoonGlassLedButton(
                        isOn = true,
                        icon = Icons.Rounded.Tune,
                        contentDescription = "open workbench to dial calibration",
                        symbolText = "DIAL WORKBENCH",
                        accentColor = MaterialTheme.colorScheme.primary,
                        onClick = onOpenWorkbench,
                        testTag = "challenge_open_workbench_btn",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            items(UniRythmicsCatalog.challenges, key = { it.id }) { challenge ->
                val solved = challenge.isSolvedBy(
                    measureType = uiState.measureType,
                    precisionMode = uiState.precisionMode,
                    sector = uiState.dodecagonSector,
                    exponent = uiState.nthExponent,
                    multiplier = uiState.multiplierInt,
                    unitId = uiState.primaryUnitId
                )
                val selected = challenge.id == uiState.activeChallengeId

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { onSelectChallenge(challenge.id) }
                        .testTag("challenge_card_${challenge.id}"),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(
                        1.dp,
                        when {
                            solved -> MaterialTheme.colorScheme.tertiary
                            selected -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outlineVariant
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = if (solved) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = if (solved) "challenge matched" else "challenge pending",
                            tint = if (solved) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(22.dp)
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = challenge.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (solved) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = challenge.prompt,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "hint ${challenge.hint}",
                                style = TabularMonoStyle,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            item {
                UniformPageInfoSection(
                    sectionTitle = "CHALLENGE VERIFICATION GUIDE",
                    items = listOf(
                        PageInfoEntry(
                            heading = "LIVE TELEMETRY MATCHING",
                            plainBody = "every calibration state updates in real time across pages when you dial the exact combination the emerald led indicator illuminates automatically",
                            mathBlock = "sector(k) => angle = k * 30 deg | rolodex => 000001..999999"
                        ),
                        PageInfoEntry(
                            heading = "LEAN 4 PROOF EXPORT",
                            plainBody = "after matching any target open the top pull tab and tap lean 4 sync to export your calibrated parameters into verifiable lean 4 code"
                        )
                    )
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}
