package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.DomainOrigin
import com.example.domain.FounderCredit
import com.example.ui.theme.TabularMonoStyle
import kotlinx.coroutines.delay

@Composable
fun BacklitOriginCard(
    origin: DomainOrigin,
    secondaryOrigin: DomainOrigin? = null,
    founderCredit: FounderCredit,
    modifier: Modifier = Modifier,
    testTag: String = "",
    extraShortcutAction: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current
    var isHolding by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var isRevealed by remember { mutableStateOf(false) }

    LaunchedEffect(isHolding, isRevealed) {
        if (isHolding && !isRevealed) {
            val start = System.currentTimeMillis()
            val durationMs = 5000L
            while (isHolding && !isRevealed) {
                val elapsed = System.currentTimeMillis() - start
                val frac = (elapsed.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                holdProgress = frac
                if (frac >= 1f) {
                    isRevealed = true
                    isHolding = false
                    break
                }
                delay(24L)
            }
        } else if (!isHolding) {
            holdProgress = 0f
        }
    }

    val primaryGlow = origin.glowColor
    val secondaryGlow = secondaryOrigin?.glowColor ?: origin.secondaryGlowColor
    val badgeText = if (secondaryOrigin != null) {
        "${origin.shortTag} + ${secondaryOrigin.shortTag}"
    } else {
        origin.shortTag
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                // Dual-origin backlit aura behind the div
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            primaryGlow.copy(alpha = 0.24f),
                            secondaryGlow.copy(alpha = 0.16f),
                            primaryGlow.copy(alpha = 0.20f)
                        ),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height)
                    ),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            primaryGlow.copy(alpha = 0.72f),
                            secondaryGlow.copy(alpha = 0.65f)
                        )
                    ),
                    topLeft = Offset(0.5.dp.toPx(), 0.5.dp.toPx()),
                    size = Size(size.width - 1.dp.toPx(), size.height - 1.dp.toPx()),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                    style = Stroke(width = if (isHolding || isRevealed) 2.dp.toPx() else 1.2.dp.toPx())
                )
            }
            .pointerInput(founderCredit) {
                detectTapGestures(
                    onPress = {
                        if (!isRevealed) {
                            isHolding = true
                            tryAwaitRelease()
                            isHolding = false
                        }
                    }
                )
            }
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.86f),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top backlit origin strip & 5-second hold hint
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = primaryGlow.copy(alpha = 0.18f),
                        shape = MaterialTheme.shapes.extraSmall,
                        border = BorderStroke(1.dp, primaryGlow.copy(alpha = 0.65f))
                    ) {
                        Text(
                            text = badgeText,
                            style = TabularMonoStyle.copy(fontWeight = FontWeight.Bold),
                            color = primaryGlow,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isRevealed) Icons.Rounded.HistoryEdu else Icons.Rounded.HourglassTop,
                            contentDescription = "hold 5 seconds for origin and founders",
                            tint = if (isHolding || isRevealed) primaryGlow else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = when {
                                isRevealed -> "founders unlocked"
                                isHolding -> "hold ${(5 - (holdProgress * 5).toInt()).coerceAtLeast(1)}s"
                                else -> "hold 5s for founders"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isHolding || isRevealed) primaryGlow else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 5-second charge progress bar while holding
                if (isHolding && !isRevealed) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.extraSmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(holdProgress.coerceIn(0.02f, 1f))
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(listOf(primaryGlow, secondaryGlow))
                                    )
                            )
                        }
                    }
                }

                content()

                // 5-Second Hold Revealed Origin, Founders & Shortcut Links Panel inside the div
                AnimatedVisibility(
                    visible = isRevealed,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(color = primaryGlow.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = origin.creditHeading,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = primaryGlow
                            )
                            MoonGlassLedButton(
                                isOn = true,
                                icon = Icons.Rounded.Close,
                                contentDescription = "close founders credit",
                                accentColor = primaryGlow,
                                onClick = { isRevealed = false }
                            )
                        }

                        Text(
                            text = "founders ${founderCredit.founders}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "era ${founderCredit.originEra}",
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryGlow
                        )
                        Text(
                            text = founderCredit.attributionSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = MaterialTheme.shapes.extraSmall,
                            border = BorderStroke(1.dp, primaryGlow.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = founderCredit.mathOriginFormula,
                                style = TabularMonoStyle,
                                color = primaryGlow,
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState())
                                    .padding(10.dp)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            founderCredit.links.forEach { link ->
                                MoonGlassLedButton(
                                    isOn = true,
                                    icon = Icons.Rounded.OpenInNew,
                                    contentDescription = "open ${link.label}",
                                    symbolText = link.label,
                                    accentColor = primaryGlow,
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                                        context.startActivity(intent)
                                    }
                                )
                            }
                            extraShortcutAction?.invoke()
                        }
                    }
                }
            }
        }
    }
}
