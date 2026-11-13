package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Input
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.DomainOrigin
import com.example.domain.FounderCredit
import com.example.domain.OriginReferenceLink
import com.example.domain.UniRythmicsCatalog
import com.example.domain.UniRythmicsEngine
import com.example.domain.WolframLookupState
import com.example.ui.UniRythmicsUiState
import com.example.ui.components.BacklitOriginCard
import com.example.ui.components.HorologicalPanel
import com.example.ui.components.MoonGlassLedButton
import com.example.ui.components.PageInfoEntry
import com.example.ui.components.UniformPageInfoSection
import com.example.ui.theme.TabularMonoStyle

@Composable
fun TaxonomiesScreen(
    uiState: UniRythmicsUiState,
    onSelectTaxonomy: (String) -> Unit,
    onSelectOriginFilter: (DomainOrigin?) -> Unit,
    onWolframQueryChange: (String) -> Unit,
    onRunWolframLookup: (String?) -> Unit,
    onLoadIntoPrimary: (String) -> Unit,
    onLoadIntoComparison: (String) -> Unit,
    onSendToTranslator: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeOrigin = uiState.selectedOriginFilter

    val visibleTaxonomies = UniRythmicsCatalog.taxonomies.filter { fw ->
        activeOrigin == null ||
            fw.primaryOrigin == activeOrigin ||
            fw.secondaryOrigin == activeOrigin ||
            fw.entries.any { it.origin == activeOrigin || it.secondaryOrigin == activeOrigin }
    }
    val activeTaxonomy = visibleTaxonomies.firstOrNull { it.id == uiState.selectedTaxonomyId }
        ?: visibleTaxonomies.firstOrNull()
        ?: UniRythmicsCatalog.taxonomies.first()

    val filteredUnits = UniRythmicsCatalog.scientificUnits.filter { unit ->
        val matchesOrigin = activeOrigin == null ||
            unit.origin == activeOrigin ||
            unit.secondaryOrigin == activeOrigin
        val q = uiState.wolframQuery.trim()
        val matchesQuery = q.isEmpty() ||
            unit.name.contains(q, ignoreCase = true) ||
            unit.shortName.contains(q, ignoreCase = true) ||
            unit.category.contains(q, ignoreCase = true) ||
            unit.description.contains(q, ignoreCase = true)
        matchesOrigin && matchesQuery
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 940.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Domain Origin Segregation Bar (backlit in color of origin)
            item {
                HorologicalPanel(
                    title = "ORIGIN SEGREGATION",
                    badge = activeOrigin?.label ?: "ALL DOMAINS",
                    accentColor = activeOrigin?.glowColor ?: MaterialTheme.colorScheme.secondary
                ) {
                    Text(
                        text = "filter scientific data and lookup constants by origin domain hold any backlit card for five seconds to reveal founders and links",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            MoonGlassLedButton(
                                isOn = activeOrigin == null,
                                icon = Icons.Rounded.AutoAwesome,
                                contentDescription = "show all domains",
                                symbolText = "ALL",
                                accentColor = MaterialTheme.colorScheme.secondary,
                                onClick = { onSelectOriginFilter(null) },
                                testTag = "origin_filter_all"
                            )
                        }
                        items(DomainOrigin.entries, key = { it.name }) { origin ->
                            MoonGlassLedButton(
                                isOn = activeOrigin == origin,
                                icon = Icons.Rounded.Science,
                                contentDescription = origin.label,
                                symbolText = origin.shortTag,
                                accentColor = origin.glowColor,
                                onClick = { onSelectOriginFilter(origin) },
                                testTag = "origin_filter_${origin.name}"
                            )
                        }
                    }
                }
            }

            // Wolfram|Alpha API & Scientific Lookup DB Panel
            item {
                HorologicalPanel(
                    title = "WOLFRAM ALPHA LOOKUP DB",
                    badge = "COMPUTATIONAL API",
                    accentColor = MaterialTheme.colorScheme.primary
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.wolframQuery,
                            onValueChange = onWolframQueryChange,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = "search wolfram alpha and local lookup db"
                                )
                            },
                            placeholder = { Text("query planck constant phi ratio boltzmann") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("wolfram_lookup_input")
                        )
                        MoonGlassLedButton(
                            isOn = uiState.wolframQuery.isNotBlank(),
                            icon = Icons.Rounded.Science,
                            contentDescription = "query wolfram alpha api",
                            symbolText = "QUERY",
                            accentColor = MaterialTheme.colorScheme.primary,
                            enabled = uiState.wolframQuery.isNotBlank(),
                            onClick = { onRunWolframLookup(null) },
                            testTag = "wolfram_query_btn"
                        )
                    }

                    // Quick Wolfram|Alpha Domain Presets
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val presets = listOf(
                            "golden ratio conjugate" to DomainOrigin.MATH,
                            "planck constant" to DomainOrigin.QUANTUM,
                            "boltzmann constant" to DomainOrigin.THERMO,
                            "speed of light" to DomainOrigin.PHYSICS,
                            "parsec in meters" to DomainOrigin.COSMOLOGY
                        )
                        items(presets, key = { it.first }) { (q, origin) ->
                            MoonGlassLedButton(
                                isOn = uiState.wolframQuery.equals(q, ignoreCase = true),
                                icon = Icons.Rounded.AutoAwesome,
                                contentDescription = q,
                                symbolText = origin.shortTag,
                                accentColor = origin.glowColor,
                                onClick = { onRunWolframLookup(q) },
                                testTag = "wolfram_preset_${origin.name}"
                            )
                        }
                    }

                    // Live Wolfram|Alpha API Result State
                    when (val wState = uiState.wolframState) {
                        WolframLookupState.Idle -> {
                            Text(
                                text = "enter a formula or physical constant and tap query for wolfram alpha computational pods or browse backlit constants below",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        WolframLookupState.Loading -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "querying wolfram alpha computational engine",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        is WolframLookupState.MissingKey -> {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                shape = MaterialTheme.shapes.small,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "WOLFRAM APP ID NEEDED FOR LIVE API",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "add wolfram app id in the ai studio secrets panel for live api pods or open wolfram alpha web directly below",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    MoonGlassLedButton(
                                        isOn = true,
                                        icon = Icons.Rounded.OpenInNew,
                                        contentDescription = "open wolfram alpha web lookup",
                                        symbolText = "OPEN WOLFRAM",
                                        accentColor = MaterialTheme.colorScheme.secondary,
                                        onClick = {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(wState.webUrl)))
                                        }
                                    )
                                }
                            }
                        }

                        is WolframLookupState.Error -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ErrorOutline,
                                    contentDescription = "wolfram error",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = wState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1f)
                                )
                                MoonGlassLedButton(
                                    isOn = true,
                                    icon = Icons.Rounded.OpenInNew,
                                    contentDescription = "open in wolfram web",
                                    symbolText = "WEB",
                                    accentColor = MaterialTheme.colorScheme.secondary,
                                    onClick = {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(wState.webUrl)))
                                    }
                                )
                            }
                        }

                        is WolframLookupState.Success -> {
                            wState.pods.forEach { pod ->
                                val podCredit = FounderCredit(
                                    founders = "stephen wolfram and wolfram research computational knowledge team",
                                    originEra = "wolfram alpha v2 knowledge engine",
                                    attributionSummary = "live symbolic and numerical pod computed for ${wState.query.lowercase()}",
                                    mathOriginFormula = pod.plainText,
                                    links = listOf(
                                        OriginReferenceLink("WOLFRAM WEB", wState.webUrl)
                                    )
                                )
                                BacklitOriginCard(
                                    origin = pod.origin,
                                    secondaryOrigin = wState.secondaryOrigin,
                                    founderCredit = podCredit,
                                    extraShortcutAction = {
                                        MoonGlassLedButton(
                                            isOn = true,
                                            icon = Icons.Rounded.Translate,
                                            contentDescription = "send pod to translator",
                                            symbolText = "TRANSLATE",
                                            accentColor = pod.origin.glowColor,
                                            onClick = { onSendToTranslator(pod.plainText) }
                                        )
                                    }
                                ) {
                                    Text(
                                        text = pod.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = pod.origin.glowColor
                                    )
                                    Text(
                                        text = pod.plainText,
                                        style = TabularMonoStyle,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Local Segregated Backlit Lookup DB Cards
                    if (filteredUnits.isNotEmpty()) {
                        Text(
                            text = "ORIGIN BACKLIT LOOKUP ENTRIES ${filteredUnits.size}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        filteredUnits.take(6).forEach { unit ->
                            BacklitOriginCard(
                                origin = unit.origin,
                                secondaryOrigin = unit.secondaryOrigin,
                                founderCredit = unit.founderCredit,
                                testTag = "lookup_unit_card_${unit.id}",
                                extraShortcutAction = {
                                    MoonGlassLedButton(
                                        isOn = true,
                                        icon = Icons.Rounded.Input,
                                        contentDescription = "load into 1 var",
                                        symbolText = "1 VAR",
                                        accentColor = MaterialTheme.colorScheme.secondary,
                                        onClick = { onLoadIntoPrimary(unit.id) }
                                    )
                                }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = unit.name.uppercase(),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = unit.origin.glowColor
                                    )
                                    Text(
                                        text = "${UniRythmicsEngine.formatScientificOrDecimal(unit.siValue)} ${unit.siUnitSuffix}",
                                        style = TabularMonoStyle.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = unit.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MoonGlassLedButton(
                                        isOn = true,
                                        icon = Icons.Rounded.Input,
                                        contentDescription = "load into primary variable",
                                        symbolText = "1 VAR",
                                        accentColor = MaterialTheme.colorScheme.secondary,
                                        onClick = { onLoadIntoPrimary(unit.id) },
                                        testTag = "lookup_load_primary_${unit.id}",
                                        modifier = Modifier.weight(1f)
                                    )
                                    MoonGlassLedButton(
                                        isOn = true,
                                        icon = Icons.Rounded.CompareArrows,
                                        contentDescription = "load into comparison variable",
                                        symbolText = "COMP",
                                        accentColor = MaterialTheme.colorScheme.tertiary,
                                        onClick = { onLoadIntoComparison(unit.id) },
                                        testTag = "lookup_load_comp_${unit.id}",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Categorizing Scientific Data Framework Selector
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(visibleTaxonomies, key = { it.id }) { framework ->
                        MoonGlassLedButton(
                            isOn = framework.id == activeTaxonomy.id,
                            icon = Icons.Rounded.Category,
                            contentDescription = framework.title,
                            symbolText = framework.title.split(" ").take(2).joinToString(" "),
                            accentColor = framework.primaryOrigin.glowColor,
                            onClick = { onSelectTaxonomy(framework.id) },
                            testTag = "taxonomy_tab_${framework.id}"
                        )
                    }
                }
            }

            // Active Scientific Data Framework & Entries (every entry backlit by origin color + 5s hold)
            item {
                HorologicalPanel(
                    title = activeTaxonomy.title,
                    badge = "${activeTaxonomy.entries.size} CLASSIFICATIONS",
                    accentColor = activeTaxonomy.primaryOrigin.glowColor
                ) {
                    Text(
                        text = activeTaxonomy.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val visibleEntries = activeTaxonomy.entries.filter { entry ->
                        activeOrigin == null ||
                            entry.origin == activeOrigin ||
                            entry.secondaryOrigin == activeOrigin
                    }

                    visibleEntries.forEach { entry ->
                        BacklitOriginCard(
                            origin = entry.origin,
                            secondaryOrigin = entry.secondaryOrigin,
                            founderCredit = entry.founderCredit,
                            extraShortcutAction = {
                                MoonGlassLedButton(
                                    isOn = true,
                                    icon = Icons.Rounded.Translate,
                                    contentDescription = "translate origin formula",
                                    symbolText = "LEAN 4",
                                    accentColor = entry.origin.glowColor,
                                    onClick = { onSendToTranslator(entry.founderCredit.mathOriginFormula) }
                                )
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = entry.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = entry.origin.glowColor,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = entry.operationsOrSymbol,
                                    style = TabularMonoStyle.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = entry.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (entry.linkedUnitId != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MoonGlassLedButton(
                                        isOn = true,
                                        icon = Icons.Rounded.Input,
                                        contentDescription = "load into primary variable",
                                        symbolText = "1 VAR",
                                        accentColor = MaterialTheme.colorScheme.secondary,
                                        onClick = { onLoadIntoPrimary(entry.linkedUnitId) },
                                        testTag = "load_primary_${entry.linkedUnitId}",
                                        modifier = Modifier.weight(1f)
                                    )
                                    MoonGlassLedButton(
                                        isOn = true,
                                        icon = Icons.Rounded.CompareArrows,
                                        contentDescription = "load into comparison variable",
                                        symbolText = "COMP",
                                        accentColor = MaterialTheme.colorScheme.tertiary,
                                        onClick = { onLoadIntoComparison(entry.linkedUnitId) },
                                        testTag = "load_comp_${entry.linkedUnitId}",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                UniformPageInfoSection(
                    sectionTitle = "CATEGORIZING SCIENTIFIC DATA GUIDE",
                    items = listOf(
                        PageInfoEntry(
                            heading = "FIVE SECOND ORIGIN CREDIT HOLD",
                            plainBody = "press and hold any backlit math physics quantum thermo or cosmology card for five seconds to expand founder attribution origin formulas and shortcut links",
                            mathBlock = "violet MATH | amber PHYSICS | cyan QUANTUM | rose THERMO | emerald COSMOS"
                        ),
                        PageInfoEntry(
                            heading = "MULTI FRAMEWORK TAXONOMIES",
                            plainBody = "measurements group simultaneously by stevens permissible transforms si dimensional bases mathematical tensor rank direct versus indirect error and disciplinary domain",
                            mathBlock = "tP = sqrt(hbar * G / c^5) | [Q] = L^a * M^b * T^c * I^d * Theta^e * N^f * J^g"
                        )
                    )
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}
