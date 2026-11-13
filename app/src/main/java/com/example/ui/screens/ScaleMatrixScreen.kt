package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.ComparisonChannelMode
import com.example.domain.MeasureType
import com.example.domain.PrecisionMode
import com.example.domain.UniRythmicsCatalog
import com.example.domain.UniRythmicsEngine
import com.example.ui.UniRythmicsUiState
import com.example.ui.components.FilamentReadoutBox
import com.example.ui.components.HorologicalPanel
import com.example.ui.components.PageInfoEntry
import com.example.ui.components.UniformPageInfoSection
import com.example.ui.theme.TabularMonoStyle
import kotlin.math.abs
import kotlin.math.log10

@Composable
fun ScaleMatrixScreen(
    uiState: UniRythmicsUiState,
    onSelectBasisMode: (MeasureType, PrecisionMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val allRows = UniRythmicsEngine.computeAllFourMatrixRows(
        dodecagonSector = uiState.dodecagonSector,
        nthExponent = uiState.nthExponent
    )
    val activeRow = UniRythmicsEngine.computeBasisRow(
        measureType = uiState.measureType,
        precisionMode = uiState.precisionMode,
        dodecagonSector = uiState.dodecagonSector,
        nthExponent = uiState.nthExponent
    )
    val primaryUnit = UniRythmicsCatalog.findUnitById(uiState.primaryUnitId)
    val comparisonUnit = UniRythmicsCatalog.findUnitById(uiState.comparisonUnitId)

    val halfExact = allRows.first { it.measureType == MeasureType.HALF && it.precisionMode == PrecisionMode.EXACT }
    val halfRounded = allRows.first { it.measureType == MeasureType.HALF && it.precisionMode == PrecisionMode.ROUNDED }
    val wholeExact = allRows.first { it.measureType == MeasureType.WHOLE && it.precisionMode == PrecisionMode.EXACT }
    val wholeRounded = allRows.first { it.measureType == MeasureType.WHOLE && it.precisionMode == PrecisionMode.ROUNDED }

    val halfDivergence = abs(halfExact.sectorWeightedGunit - halfRounded.sectorWeightedGunit)
    val wholeDivergence = abs(wholeExact.sectorWeightedGunit - wholeRounded.sectorWeightedGunit)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 900.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HorologicalPanel(
                    title = "FOUR BASIS SCALE MATRIX",
                    badge = "SECTOR ${uiState.dodecagonSector}/12  10⁻${uiState.nthExponent}",
                    accentColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "compare half measure and whole measure across exact phi inverse and rounded 0.618 tap any row to load into workbench",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    allRows.forEach { row ->
                        val isSelected = row.measureType == uiState.measureType && row.precisionMode == uiState.precisionMode
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .clip(MaterialTheme.shapes.small)
                                .clickable { onSelectBasisMode(row.measureType, row.precisionMode) }
                                .testTag("matrix_row_${row.id}"),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLowest
                            },
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = row.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(
                                        imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                        contentDescription = if (isSelected) "active basis" else "tap to activate basis",
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MatrixMetricCell("1/1", UniRythmicsEngine.formatDecimal(row.fullUnit, 7), Modifier.weight(1f))
                                    MatrixMetricCell("1/10", UniRythmicsEngine.formatDecimal(row.tenthUnit, 7), Modifier.weight(1f))
                                    MatrixMetricCell("10⁻${uiState.nthExponent}", UniRythmicsEngine.formatScientificOrDecimal(row.shiftedUnit), Modifier.weight(1f))
                                    MatrixMetricCell("Gunit", UniRythmicsEngine.formatScientificOrDecimal(row.sectorWeightedGunit), Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilamentReadoutBox(
                            label = "half divergence |exact - 0.618|",
                            value = UniRythmicsEngine.formatScientificOrDecimal(halfDivergence),
                            accent = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        FilamentReadoutBox(
                            label = "whole divergence |exact - 0.618|",
                            value = UniRythmicsEngine.formatScientificOrDecimal(wholeDivergence),
                            accent = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                val gunitVal = activeRow.sectorWeightedGunit
                val varScaledVal = primaryUnit.siValue * uiState.multiplierInt
                val compVal = if (uiState.comparisonMode == ComparisonChannelMode.OFF) null else {
                    val m = if (uiState.comparisonMode == ComparisonChannelMode.NOTABLE_QUERY || uiState.linkComparisonMultiplier) {
                        if (uiState.comparisonMode == ComparisonChannelMode.NOTABLE_QUERY) 1 else uiState.multiplierInt
                    } else {
                        uiState.secondaryMultiplierInt
                    }
                    comparisonUnit.siValue * m
                }

                HorologicalPanel(
                    title = "LOGARITHMIC MAGNITUDE DECK",
                    badge = "LOG10 SCALE",
                    accentColor = MaterialTheme.colorScheme.secondary
                ) {
                    Text(
                        text = "visualizes order of magnitude span across one gunit one scaled var and comparison variable",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LogMagnitudeBar(
                        label = "1 gunit ${activeRow.title.lowercase()}",
                        value = gunitVal,
                        unitSuffix = "Gunit",
                        barColor = MaterialTheme.colorScheme.primary
                    )
                    LogMagnitudeBar(
                        label = "1 var * ${UniRythmicsEngine.formatRolodex(uiState.multiplierInt)} ${primaryUnit.shortName}",
                        value = varScaledVal,
                        unitSuffix = primaryUnit.siUnitSuffix,
                        barColor = MaterialTheme.colorScheme.secondary
                    )
                    if (compVal != null) {
                        LogMagnitudeBar(
                            label = "comparison ${comparisonUnit.name}",
                            value = compVal,
                            unitSuffix = comparisonUnit.siUnitSuffix,
                            barColor = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            item {
                UniformPageInfoSection(
                    sectionTitle = "SCALE MATRIX GUIDE",
                    items = listOf(
                        PageInfoEntry(
                            heading = "EXACT VERSUS ROUNDED DRIFT",
                            plainBody = "exact golden conjugate uses infinite radical precision while rounded 0.618 truncates after three decimal digits producing measurable offset at larger multipliers",
                            mathBlock = "delta_half = |2 * ((sqrt(5) - 1)/2) - 2 * 0.618| = 0.0000679775"
                        ),
                        PageInfoEntry(
                            heading = "LOGARITHMIC SPAN METER",
                            plainBody = "log base ten bars compress sixty four orders of magnitude from planck time to astronomical light years onto one comparative panel",
                            mathBlock = "norm = (log10(value) + 46) / 64"
                        )
                    )
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun MatrixMetricCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = TabularMonoStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = TabularMonoStyle.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LogMagnitudeBar(
    label: String,
    value: Double,
    unitSuffix: String,
    barColor: Color
) {
    val fraction = if (value > 0.0) {
        val logV = log10(value)
        ((logV + 46.0) / 64.0).toFloat().coerceIn(0.05f, 1.0f)
    } else {
        0.05f
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${UniRythmicsEngine.formatScientificOrDecimal(value)} $unitSuffix",
                style = TabularMonoStyle.copy(fontWeight = FontWeight.SemiBold),
                color = barColor
            )
        }
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shape = MaterialTheme.shapes.extraSmall,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .background(barColor.copy(alpha = 0.8f))
                )
            }
        }
    }
}
