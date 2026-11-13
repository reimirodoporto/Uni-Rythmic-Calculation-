package com.example.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.PrecisionManufacturing
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RotateLeft
import androidx.compose.material.icons.rounded.RotateRight
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.example.ui.components.BacklitOriginCard
import com.example.ui.components.DodecagonalDialCanvas
import com.example.ui.components.EdisonVacuumGlassChamber
import com.example.ui.components.ElWireEqualsBridge
import com.example.ui.components.FilamentReadoutBox
import com.example.ui.components.HorologicalPanel
import com.example.ui.components.MoonGlassLedButton
import com.example.ui.components.PageInfoEntry
import com.example.ui.components.RolodexMultiplierRack
import com.example.ui.components.UniformPageInfoSection
import com.example.ui.theme.TabularMonoStyle

@Composable
fun WorkbenchScreen(
    uiState: UniRythmicsUiState,
    onSetMeasureType: (MeasureType) -> Unit,
    onSetPrecisionMode: (PrecisionMode) -> Unit,
    onSelectSector: (Int) -> Unit,
    onSelectExponent: (Int) -> Unit,
    onSelectPrimaryUnit: (String) -> Unit,
    onStepMultiplierDigit: (Int, Int) -> Unit,
    onSetMultiplier: (Int) -> Unit,
    onSetComparisonMode: (ComparisonChannelMode) -> Unit,
    onSelectComparisonUnit: (String) -> Unit,
    onComparisonQueryChange: (String) -> Unit,
    onToggleLinkComparisonMultiplier: (Boolean) -> Unit,
    onStepSecondaryDigit: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeRow = UniRythmicsEngine.computeBasisRow(
        measureType = uiState.measureType,
        precisionMode = uiState.precisionMode,
        dodecagonSector = uiState.dodecagonSector,
        nthExponent = uiState.nthExponent
    )
    val primaryUnit = UniRythmicsCatalog.findUnitById(uiState.primaryUnitId)
    val comparisonUnit = UniRythmicsCatalog.findUnitById(uiState.comparisonUnitId)
    val proof = UniRythmicsEngine.buildStepProof(
        activeRow = activeRow,
        dodecagonSector = uiState.dodecagonSector,
        nthExponent = uiState.nthExponent,
        primaryUnit = primaryUnit,
        multiplierInt = uiState.multiplierInt
    )
    val comparisonEval = UniRythmicsEngine.evaluateComparison(
        primaryUnit = primaryUnit,
        primaryMultiplier = uiState.multiplierInt,
        comparisonUnit = comparisonUnit,
        comparisonMultiplier = if (uiState.linkComparisonMultiplier) uiState.multiplierInt else uiState.secondaryMultiplierInt,
        mode = uiState.comparisonMode
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideThreeCol = maxWidth >= 900.dp

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 1220.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "EQUIVALENCE CHAIN",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "1 Gunit = 1 var = var of comparison",
                                style = TabularMonoStyle.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = proof.equivalenceSummary,
                                style = TabularMonoStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    EdisonVacuumGlassChamber(gearRotationAngle = uiState.gearRotationAccumulator) {
                        if (isWideThreeCol) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Stage1GunitColumn(
                                    uiState = uiState,
                                    activeRow = activeRow,
                                    onSetMeasureType = onSetMeasureType,
                                    onSetPrecisionMode = onSetPrecisionMode,
                                    onSelectSector = onSelectSector,
                                    onSelectExponent = onSelectExponent,
                                    modifier = Modifier.weight(1f)
                                )
                                ElWireEqualsBridge(label = "=", vertical = false, modifier = Modifier.padding(top = 120.dp))
                                Stage2VarColumn(
                                    uiState = uiState,
                                    primaryUnit = primaryUnit,
                                    activeRow = activeRow,
                                    onSelectPrimaryUnit = onSelectPrimaryUnit,
                                    onStepMultiplierDigit = onStepMultiplierDigit,
                                    onSetMultiplier = onSetMultiplier,
                                    modifier = Modifier.weight(1f)
                                )
                                ElWireEqualsBridge(label = "=", vertical = false, modifier = Modifier.padding(top = 120.dp))
                                Stage3ComparisonColumn(
                                    uiState = uiState,
                                    comparisonUnit = comparisonUnit,
                                    comparisonEval = comparisonEval,
                                    onSetComparisonMode = onSetComparisonMode,
                                    onSelectComparisonUnit = onSelectComparisonUnit,
                                    onComparisonQueryChange = onComparisonQueryChange,
                                    onToggleLinkComparisonMultiplier = onToggleLinkComparisonMultiplier,
                                    onStepSecondaryDigit = onStepSecondaryDigit,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Stage1GunitColumn(
                                    uiState = uiState,
                                    activeRow = activeRow,
                                    onSetMeasureType = onSetMeasureType,
                                    onSetPrecisionMode = onSetPrecisionMode,
                                    onSelectSector = onSelectSector,
                                    onSelectExponent = onSelectExponent
                                )
                                ElWireEqualsBridge(label = "=", vertical = true)
                                Stage2VarColumn(
                                    uiState = uiState,
                                    primaryUnit = primaryUnit,
                                    activeRow = activeRow,
                                    onSelectPrimaryUnit = onSelectPrimaryUnit,
                                    onStepMultiplierDigit = onStepMultiplierDigit,
                                    onSetMultiplier = onSetMultiplier
                                )
                                ElWireEqualsBridge(label = "=", vertical = true)
                                Stage3ComparisonColumn(
                                    uiState = uiState,
                                    comparisonUnit = comparisonUnit,
                                    comparisonEval = comparisonEval,
                                    onSetComparisonMode = onSetComparisonMode,
                                    onSelectComparisonUnit = onSelectComparisonUnit,
                                    onComparisonQueryChange = onComparisonQueryChange,
                                    onToggleLinkComparisonMultiplier = onToggleLinkComparisonMultiplier,
                                    onStepSecondaryDigit = onStepSecondaryDigit
                                )
                            }
                        }
                    }
                }

                item {
                    HorologicalPanel(
                        title = "ARITHMETIC PROOF BLOCK",
                        badge = "TRANSPARENT CALIBRATION",
                        accentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    proof.basisStep,
                                    proof.measureStep,
                                    proof.powerShiftStep,
                                    proof.sectorStep,
                                    proof.varScaleStep
                                ).forEach { step ->
                                    Text(
                                        text = step,
                                        style = TabularMonoStyle,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    UniformPageInfoSection(
                        sectionTitle = "WORKBENCH OPERATING GUIDE",
                        items = listOf(
                            PageInfoEntry(
                                heading = "GOLDEN BASIS AND MEASURE TOGGLE",
                                plainBody = "select half measure or whole measure then switch between exact phi inverse and rounded 0.618 to observe divergence across powers of ten",
                                mathBlock = "half: 1/2 = (sqrt(5) - 1)/2 => 1/1 = 2/phi\nwhole: 1 = (sqrt(5) - 1)/2"
                            ),
                            PageInfoEntry(
                                heading = "DODECAGON DIAL AND ROLODEX GEARING",
                                plainBody = "tap any sector on the 360 degree twelve sector dial and spin the six digit rolodex drums to link one gunit to si and codata constants",
                                mathBlock = "1 Gunit = full * 10^(-n) * (sector / 12)\n1 var = unit_si * rolodex[000001..999999]"
                            ),
                            PageInfoEntry(
                                heading = "PULL TAB TRANSLATOR ACCESS",
                                plainBody = "pull down the top translator drawer on any page to convert current math or code into lean 4 or other target languages"
                            )
                        )
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun Stage1GunitColumn(
    uiState: UniRythmicsUiState,
    activeRow: com.example.domain.BasisCalculationRow,
    onSetMeasureType: (MeasureType) -> Unit,
    onSetPrecisionMode: (PrecisionMode) -> Unit,
    onSelectSector: (Int) -> Unit,
    onSelectExponent: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    HorologicalPanel(
        title = "STAGE 1 GUNIT DIAL",
        badge = "360 DEG DODECAGON",
        accentColor = MaterialTheme.colorScheme.primary,
        modifier = modifier
    ) {
        DodecagonalDialCanvas(
            selectedSector = uiState.dodecagonSector,
            onSelectSector = onSelectSector
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MoonGlassLedButton(
                isOn = true,
                icon = Icons.Rounded.RotateLeft,
                contentDescription = "step back 30 degrees",
                symbolText = "30",
                accentColor = MaterialTheme.colorScheme.secondary,
                onClick = {
                    val prev = if (uiState.dodecagonSector <= 1) 12 else uiState.dodecagonSector - 1
                    onSelectSector(prev)
                },
                testTag = "sector_prev_btn"
            )
            Text(
                text = "${uiState.dodecagonSector}/12  ${uiState.dodecagonSector * 30}°",
                style = TabularMonoStyle.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            MoonGlassLedButton(
                isOn = true,
                icon = Icons.Rounded.RotateRight,
                contentDescription = "step forward 30 degrees",
                symbolText = "+30",
                accentColor = MaterialTheme.colorScheme.secondary,
                onClick = {
                    val next = if (uiState.dodecagonSector >= 12) 1 else uiState.dodecagonSector + 1
                    onSelectSector(next)
                },
                testTag = "sector_next_btn"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MeasureType.entries.forEach { type ->
                MoonGlassLedButton(
                    isOn = uiState.measureType == type,
                    icon = if (type == MeasureType.HALF) Icons.Rounded.Balance else Icons.Rounded.RadioButtonChecked,
                    contentDescription = type.label,
                    symbolText = type.shortSymbol,
                    accentColor = MaterialTheme.colorScheme.primary,
                    onClick = { onSetMeasureType(type) },
                    testTag = "measure_toggle_${type.name}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PrecisionMode.entries.forEach { mode ->
                MoonGlassLedButton(
                    isOn = uiState.precisionMode == mode,
                    icon = Icons.Rounded.Functions,
                    contentDescription = mode.label,
                    symbolText = mode.shortSymbol,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    onClick = { onSetPrecisionMode(mode) },
                    testTag = "precision_toggle_${mode.name}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Text(
            text = "POWER SHIFT 10⁻ⁿ",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items((0..9).toList()) { exp ->
                val selected = uiState.nthExponent == exp
                val lift by animateDpAsState(if (selected) (-2).dp else 0.dp, tween(200), label = "exp_lift")
                val accent = MaterialTheme.colorScheme.primary
                Surface(
                    modifier = Modifier
                        .offset(y = lift)
                        .minimumInteractiveComponentSize()
                        .shadow(if (selected) 8.dp else 0.dp, MaterialTheme.shapes.extraSmall, ambientColor = accent, spotColor = accent)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .clickable { onSelectExponent(exp) }
                        .testTag("exp_chip_$exp"),
                    color = if (selected) accent.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = MaterialTheme.shapes.extraSmall,
                    border = BorderStroke(
                        if (selected) 1.5.dp else 1.dp,
                        if (selected) accent else Color.Black.copy(alpha = 0.65f)
                    )
                ) {
                    Text(
                        text = if (exp == 0) "10⁰" else "10⁻$exp",
                        style = TabularMonoStyle.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium),
                        color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilamentReadoutBox(
                label = "full 1/1",
                value = UniRythmicsEngine.formatDecimal(activeRow.fullUnit, 6),
                accent = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            FilamentReadoutBox(
                label = "tenth 1/10",
                value = UniRythmicsEngine.formatDecimal(activeRow.tenthUnit, 6),
                accent = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }

        FilamentReadoutBox(
            label = "1 gunit sector ${uiState.dodecagonSector}/12",
            value = UniRythmicsEngine.formatScientificOrDecimal(activeRow.sectorWeightedGunit),
            accent = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Stage2VarColumn(
    uiState: UniRythmicsUiState,
    primaryUnit: com.example.domain.ScientificUnit,
    activeRow: com.example.domain.BasisCalculationRow,
    onSelectPrimaryUnit: (String) -> Unit,
    onStepMultiplierDigit: (Int, Int) -> Unit,
    onSetMultiplier: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val finalTotalSi = primaryUnit.siValue * uiState.multiplierInt.toDouble()
    val finalGunitSpan = activeRow.sectorWeightedGunit * uiState.multiplierInt.toDouble()

    HorologicalPanel(
        title = "STAGE 2 PRIMARY VAR",
        badge = primaryUnit.dimension.label.uppercase(),
        accentColor = MaterialTheme.colorScheme.secondary,
        modifier = modifier
    ) {
        Text(
            text = "PRIMARY UNIT",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(UniRythmicsCatalog.scientificUnits, key = { it.id }) { unit ->
                val selected = unit.id == uiState.primaryUnitId
                MoonGlassLedButton(
                    isOn = selected,
                    icon = Icons.Rounded.Straighten,
                    contentDescription = unit.name,
                    symbolText = unit.shortName,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    onClick = { onSelectPrimaryUnit(unit.id) },
                    testTag = "primary_unit_${unit.id}"
                )
            }
        }

        FilamentReadoutBox(
            label = "${primaryUnit.name} ${primaryUnit.category}",
            value = "${UniRythmicsEngine.formatScientificOrDecimal(primaryUnit.siValue)} ${primaryUnit.siUnitSuffix}",
            accent = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        RolodexMultiplierRack(
            multiplierInt = uiState.multiplierInt,
            onStepDigit = onStepMultiplierDigit,
            onSetMultiplier = onSetMultiplier,
            tagPrefix = "primary_rolodex"
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        FilamentReadoutBox(
            label = "scaled total ${primaryUnit.shortName} * ${UniRythmicsEngine.formatRolodex(uiState.multiplierInt)}",
            value = "${UniRythmicsEngine.formatScientificOrDecimal(finalTotalSi)} ${primaryUnit.siUnitSuffix}",
            accent = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )

        FilamentReadoutBox(
            label = "golden scalar 1 gunit * ${uiState.multiplierInt}",
            value = "${UniRythmicsEngine.formatScientificOrDecimal(finalGunitSpan)} Gunit",
            accent = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Stage3ComparisonColumn(
    uiState: UniRythmicsUiState,
    comparisonUnit: com.example.domain.ScientificUnit,
    comparisonEval: com.example.domain.ComparisonEvaluation?,
    onSetComparisonMode: (ComparisonChannelMode) -> Unit,
    onSelectComparisonUnit: (String) -> Unit,
    onComparisonQueryChange: (String) -> Unit,
    onToggleLinkComparisonMultiplier: (Boolean) -> Unit,
    onStepSecondaryDigit: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    HorologicalPanel(
        title = "STAGE 3 COMPARE VAR",
        badge = uiState.comparisonMode.label,
        accentColor = MaterialTheme.colorScheme.tertiary,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ComparisonChannelMode.entries.forEach { mode ->
                val icon = when (mode) {
                    ComparisonChannelMode.OFF -> Icons.Rounded.PowerSettingsNew
                    ComparisonChannelMode.NOTABLE_QUERY -> Icons.Rounded.Science
                    ComparisonChannelMode.SECONDARY_SCALABILITY -> Icons.Rounded.CompareArrows
                }
                MoonGlassLedButton(
                    isOn = uiState.comparisonMode == mode,
                    icon = icon,
                    contentDescription = mode.label,
                    symbolText = mode.shortSymbol,
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    onClick = { onSetComparisonMode(mode) },
                    testTag = "comparison_mode_${mode.name}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        when (uiState.comparisonMode) {
            ComparisonChannelMode.OFF -> {
                Text(
                    text = "third stage collapsed tap si or 2x moon glass button to compare against codata benchmarks or a second variable",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ComparisonChannelMode.NOTABLE_QUERY -> {
                OutlinedTextField(
                    value = uiState.comparisonQuery,
                    onValueChange = onComparisonQueryChange,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "search codata benchmarks"
                        )
                    },
                    placeholder = { Text("search planck c cesium au") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notable_query_input")
                )

                val matches = UniRythmicsCatalog.benchmarkUnits.filter { item ->
                    val q = uiState.comparisonQuery.trim()
                    q.isEmpty() ||
                        item.name.contains(q, ignoreCase = true) ||
                        item.symbol.contains(q, ignoreCase = true) ||
                        item.category.contains(q, ignoreCase = true)
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(matches, key = { it.id }) { item ->
                        MoonGlassLedButton(
                            isOn = item.id == uiState.comparisonUnitId,
                            icon = Icons.Rounded.PrecisionManufacturing,
                            contentDescription = item.name,
                            symbolText = item.shortName,
                            accentColor = item.origin.glowColor,
                            onClick = { onSelectComparisonUnit(item.id) },
                            testTag = "benchmark_chip_${item.id}"
                        )
                    }
                }

                BacklitOriginCard(
                    origin = comparisonUnit.origin,
                    secondaryOrigin = comparisonUnit.secondaryOrigin,
                    founderCredit = comparisonUnit.founderCredit,
                    testTag = "workbench_benchmark_origin_card"
                ) {
                    FilamentReadoutBox(
                        label = "${comparisonUnit.name} ${comparisonUnit.category}",
                        value = "${UniRythmicsEngine.formatScientificOrDecimal(comparisonUnit.siValue)} ${comparisonUnit.siUnitSuffix}",
                        accent = comparisonUnit.origin.glowColor,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = comparisonUnit.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (comparisonEval != null) {
                    FilamentReadoutBox(
                        label = "ratio scaled var / benchmark",
                        value = comparisonEval.ratioPrimaryToComparison?.let {
                            UniRythmicsEngine.formatScientificOrDecimal(it)
                        } ?: "0",
                        accent = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = comparisonEval.summaryText,
                        style = TabularMonoStyle,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            ComparisonChannelMode.SECONDARY_SCALABILITY -> {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(UniRythmicsCatalog.scientificUnits, key = { it.id }) { unit ->
                        MoonGlassLedButton(
                            isOn = unit.id == uiState.comparisonUnitId,
                            icon = Icons.Rounded.Straighten,
                            contentDescription = unit.name,
                            symbolText = unit.shortName,
                            accentColor = MaterialTheme.colorScheme.tertiary,
                            onClick = { onSelectComparisonUnit(unit.id) },
                            testTag = "secondary_unit_${unit.id}"
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "link primary rolodex multiplier",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    MoonGlassLedButton(
                        isOn = uiState.linkComparisonMultiplier,
                        icon = if (uiState.linkComparisonMultiplier) Icons.Rounded.Link else Icons.Rounded.LinkOff,
                        contentDescription = "link rolodex multiplier",
                        symbolText = if (uiState.linkComparisonMultiplier) "SYNC" else "FREE",
                        accentColor = MaterialTheme.colorScheme.secondary,
                        onClick = { onToggleLinkComparisonMultiplier(!uiState.linkComparisonMultiplier) },
                        testTag = "link_secondary_multiplier_switch"
                    )
                }

                if (!uiState.linkComparisonMultiplier) {
                    RolodexMultiplierRack(
                        multiplierInt = uiState.secondaryMultiplierInt,
                        onStepDigit = onStepSecondaryDigit,
                        onSetMultiplier = { target ->
                            val diff = target - uiState.secondaryMultiplierInt
                            onStepSecondaryDigit(0, diff)
                        },
                        tagPrefix = "secondary_rolodex"
                    )
                }

                if (comparisonEval != null) {
                    FilamentReadoutBox(
                        label = "scaled ${comparisonUnit.name}",
                        value = "${UniRythmicsEngine.formatScientificOrDecimal(comparisonEval.comparisonValue)} ${comparisonUnit.siUnitSuffix}",
                        accent = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    FilamentReadoutBox(
                        label = "side by side ratio",
                        value = comparisonEval.ratioPrimaryToComparison?.let {
                            UniRythmicsEngine.formatScientificOrDecimal(it)
                        } ?: "0",
                        accent = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
