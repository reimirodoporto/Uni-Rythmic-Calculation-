package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.UniRythmicsEngine
import com.example.ui.theme.BrassAccent
import com.example.ui.theme.ElWireCyan
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.TabularMonoStyle
import com.example.ui.theme.TungstenAmber
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private val HorologyEase = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

@Composable
fun EdisonVacuumGlassChamber(
    gearRotationAngle: Float,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val animatedAngle by animateFloatAsState(
        targetValue = gearRotationAngle,
        animationSpec = tween(durationMillis = 320, easing = HorologyEase),
        label = "gear_rotation"
    )
    val bg = MaterialTheme.colorScheme.surfaceContainerLowest
    val brassBezel = MaterialTheme.colorScheme.outlineVariant
    val elCyan = MaterialTheme.colorScheme.secondary
    val tungsten = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = bg,
        border = BorderStroke(1.5.dp, brassBezel)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Coupled skeletal sun-and-planet brass gears & Edison glass rim piping
            Canvas(modifier = Modifier.matchParentSize()) {
                val radiusCorner = 22.dp.toPx()

                // Sun & planetary skeletal gears behind darkened glass
                val sunCenter = Offset(size.width * 0.82f, size.height * 0.24f)
                val planetCenter1 = Offset(size.width * 0.65f, size.height * 0.14f)
                val planetCenter2 = Offset(size.width * 0.18f, size.height * 0.78f)

                drawSkeletalGear(
                    center = sunCenter,
                    outerRadius = 74.dp.toPx(),
                    teeth = 18,
                    rotationDeg = animatedAngle,
                    color = BrassAccent.copy(alpha = 0.14f)
                )
                drawSkeletalGear(
                    center = planetCenter1,
                    outerRadius = 46.dp.toPx(),
                    teeth = 12,
                    rotationDeg = -animatedAngle * 1.5f,
                    color = tungsten.copy(alpha = 0.12f)
                )
                drawSkeletalGear(
                    center = planetCenter2,
                    outerRadius = 62.dp.toPx(),
                    teeth = 15,
                    rotationDeg = animatedAngle * 0.618f,
                    color = BrassAccent.copy(alpha = 0.10f)
                )

                // Thin electric-cyan EL-wire piping along inner vacuum-glass edge
                drawRoundRect(
                    color = elCyan.copy(alpha = 0.28f),
                    topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                    size = Size(size.width - 6.dp.toPx(), size.height - 6.dp.toPx()),
                    cornerRadius = CornerRadius(radiusCorner, radiusCorner),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Curved Edison vacuum-glass top-left specular bevel arc
                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent)
                    ),
                    startAngle = 195f,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(10.dp.toPx(), 8.dp.toPx()),
                    size = Size(size.width * 0.45f, 44.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                content = content
            )
        }
    }
}

private fun DrawScope.drawSkeletalGear(
    center: Offset,
    outerRadius: Float,
    teeth: Int,
    rotationDeg: Float,
    color: Color
) {
    val innerRadius = outerRadius * 0.84f
    val hubRadius = outerRadius * 0.28f
    rotate(degrees = rotationDeg, pivot = center) {
        val gearPath = Path()
        val stepAngle = 360.0 / teeth
        for (i in 0 until teeth) {
            val a0 = Math.toRadians(i * stepAngle)
            val a1 = Math.toRadians(i * stepAngle + stepAngle * 0.22)
            val a2 = Math.toRadians(i * stepAngle + stepAngle * 0.55)
            val a3 = Math.toRadians(i * stepAngle + stepAngle * 0.77)

            val p0 = Offset(center.x + (innerRadius * cos(a0)).toFloat(), center.y + (innerRadius * sin(a0)).toFloat())
            val p1 = Offset(center.x + (outerRadius * cos(a1)).toFloat(), center.y + (outerRadius * sin(a1)).toFloat())
            val p2 = Offset(center.x + (outerRadius * cos(a2)).toFloat(), center.y + (outerRadius * sin(a2)).toFloat())
            val p3 = Offset(center.x + (innerRadius * cos(a3)).toFloat(), center.y + (innerRadius * sin(a3)).toFloat())

            if (i == 0) gearPath.moveTo(p0.x, p0.y) else gearPath.lineTo(p0.x, p0.y)
            gearPath.lineTo(p1.x, p1.y)
            gearPath.lineTo(p2.x, p2.y)
            gearPath.lineTo(p3.x, p3.y)
        }
        gearPath.close()

        drawPath(path = gearPath, color = color, style = Stroke(width = 1.4.dp.toPx()))
        drawCircle(color = color, radius = innerRadius * 0.74f, center = center, style = Stroke(width = 1.dp.toPx()))
        drawCircle(color = color, radius = hubRadius, center = center, style = Stroke(width = 1.4.dp.toPx()))

        // 5 skeletal spokes
        for (s in 0 until 5) {
            val spokeAngle = Math.toRadians(s * 72.0)
            drawLine(
                color = color,
                start = Offset(
                    center.x + (hubRadius * cos(spokeAngle)).toFloat(),
                    center.y + (hubRadius * sin(spokeAngle)).toFloat()
                ),
                end = Offset(
                    center.x + (innerRadius * 0.74f * cos(spokeAngle)).toFloat(),
                    center.y + (innerRadius * 0.74f * sin(spokeAngle)).toFloat()
                ),
                strokeWidth = 1.6.dp.toPx()
            )
        }
    }
}

@Composable
fun HorologicalPanel(
    title: String,
    badge: String,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = accentColor
                )
                Surface(
                    color = accentColor.copy(alpha = 0.14f),
                    shape = MaterialTheme.shapes.extraSmall,
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = badge.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            content()
        }
    }
}

@Composable
fun ElWireEqualsBridge(
    label: String = "=",
    vertical: Boolean = true,
    modifier: Modifier = Modifier
) {
    val cyan = MaterialTheme.colorScheme.secondary
    if (vertical) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, cyan.copy(alpha = 0.65f))
                        )
                    )
            )
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.extraSmall,
                border = BorderStroke(1.dp, cyan.copy(alpha = 0.7f)),
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                Text(
                    text = label,
                    style = TabularMonoStyle.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = cyan,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(cyan.copy(alpha = 0.65f), Color.Transparent)
                        )
                    )
            )
        }
    } else {
        Column(
            modifier = modifier.padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.extraSmall,
                border = BorderStroke(1.dp, cyan.copy(alpha = 0.7f))
            ) {
                Text(
                    text = label,
                    style = TabularMonoStyle.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = cyan,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun DodecagonalDialCanvas(
    selectedSector: Int,
    onSelectSector: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tungsten = MaterialTheme.colorScheme.primary
    val elCyan = MaterialTheme.colorScheme.secondary
    val surfaceLow = MaterialTheme.colorScheme.surfaceContainerLowest
    val outline = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val animatedSectorAngle by animateFloatAsState(
        targetValue = (selectedSector * 30f),
        animationSpec = tween(durationMillis = 260, easing = HorologyEase),
        label = "dodecagon_sector_angle"
    )
    val textMeasurer = rememberTextMeasurer()
    val monoLabelStyle = TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        fontFeatureSettings = "tnum"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .pointerInput(selectedSector) {
                detectTapGestures { tap ->
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val dx = tap.x - cx
                    val dy = tap.y - cy
                    if (dx * dx + dy * dy > 16f) {
                        var deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())) + 90.0
                        if (deg <= 0.0) deg += 360.0
                        val sector = ((deg / 30.0).roundToInt()).let { if (it <= 0) 12 else if (it > 12) it - 12 else it }
                        onSelectSector(sector)
                    }
                }
            }
            .testTag("dodecagonal_dial_canvas")
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerR = min(size.width, size.height) * 0.40f
        val innerR = outerR * 0.618f

        // Recessed vacuum dial well
        drawCircle(color = surfaceLow, radius = outerR * 1.12f, center = center)
        drawCircle(
            color = outline.copy(alpha = 0.45f),
            radius = outerR * 1.12f,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        // EL-wire cyan sweep arc showing active 30° × sector progress
        drawArc(
            color = elCyan.copy(alpha = 0.7f),
            startAngle = -90f,
            sweepAngle = animatedSectorAngle,
            useCenter = false,
            topLeft = Offset(center.x - outerR * 1.04f, center.y - outerR * 1.04f),
            size = Size(outerR * 2.08f, outerR * 2.08f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Inner Φ⁻¹ golden harmonic circle
        drawCircle(
            color = tungsten.copy(alpha = 0.25f),
            radius = innerR,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        val vertices = Array(12) { idx ->
            val sectorNum = idx + 1
            val rad = Math.toRadians(sectorNum * 30.0 - 90.0)
            Offset(
                x = center.x + (outerR * cos(rad)).toFloat(),
                y = center.y + (outerR * sin(rad)).toFloat()
            )
        }

        // Dodecagon perimeter & radial spokes
        val poly = Path().apply {
            moveTo(vertices[0].x, vertices[0].y)
            for (i in 1 until 12) lineTo(vertices[i].x, vertices[i].y)
            close()
        }
        drawPath(poly, color = outline.copy(alpha = 0.65f), style = Stroke(width = 1.2.dp.toPx()))

        for (i in 0 until 12) {
            val sNum = i + 1
            val isActive = sNum <= selectedSector
            drawLine(
                color = if (isActive) tungsten.copy(alpha = 0.35f) else outline.copy(alpha = 0.22f),
                start = center,
                end = vertices[i],
                strokeWidth = 1.dp.toPx()
            )
        }

        // Filament indicator hand pointing to selected sector
        val handRad = Math.toRadians(animatedSectorAngle.toDouble() - 90.0)
        val handTip = Offset(
            x = center.x + (outerR * 0.92f * cos(handRad)).toFloat(),
            y = center.y + (outerR * 0.92f * sin(handRad)).toFloat()
        )
        drawLine(
            brush = Brush.linearGradient(listOf(elCyan, tungsten), start = center, end = handTip),
            start = center,
            end = handTip,
            strokeWidth = 2.6.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 12 sector nodes & angle labels
        for (i in 0 until 12) {
            val sNum = i + 1
            val isSelected = sNum == selectedSector
            val v = vertices[i]
            if (isSelected) {
                drawCircle(color = tungsten.copy(alpha = 0.30f), radius = 10.dp.toPx(), center = v)
            }
            drawCircle(
                color = if (isSelected) tungsten else if (sNum < selectedSector) elCyan else surfaceLow,
                radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                center = v
            )
            drawCircle(
                color = if (isSelected) tungsten else outline,
                radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                center = v,
                style = Stroke(width = 1.dp.toPx())
            )

            val rad = Math.toRadians(sNum * 30.0 - 90.0)
            val labelR = outerR * 1.22f
            val lx = center.x + (labelR * cos(rad)).toFloat()
            val ly = center.y + (labelR * sin(rad)).toFloat()
            val labelResult = textMeasurer.measure(
                text = "$sNum",
                style = monoLabelStyle.copy(
                    color = if (isSelected) tungsten else onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            )
            drawText(
                labelResult,
                topLeft = Offset(lx - labelResult.size.width / 2f, ly - labelResult.size.height / 2f)
            )
        }

        // Center incandescent hub
        drawCircle(color = surfaceLow, radius = 28.dp.toPx(), center = center)
        drawCircle(color = tungsten.copy(alpha = 0.7f), radius = 28.dp.toPx(), center = center, style = Stroke(width = 1.5.dp.toPx()))
        val hubText = textMeasurer.measure(
            text = "${selectedSector * 30}°",
            style = monoLabelStyle.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onSurface)
        )
        drawText(
            hubText,
            topLeft = Offset(center.x - hubText.size.width / 2f, center.y - hubText.size.height / 2f)
        )
    }
}

@Composable
fun RolodexMultiplierRack(
    multiplierInt: Int,
    onStepDigit: (placeFromRight: Int, delta: Int) -> Unit,
    onSetMultiplier: (Int) -> Unit,
    tagPrefix: String = "rolodex",
    modifier: Modifier = Modifier
) {
    val formatted = UniRythmicsEngine.formatRolodex(multiplierInt)
    val tungsten = MaterialTheme.colorScheme.primary
    val surfaceLowest = MaterialTheme.colorScheme.surfaceContainerLowest
    val outline = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SCALAR ROLODEX MULTIPLIER",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "×$formatted",
                style = TabularMonoStyle.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = tungsten
            )
        }

        // 6 mechanical watch-style rolodex drums
        Surface(
            color = surfaceLowest,
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "×",
                    style = TabularMonoStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = tungsten,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                for (index in 0 until 6) {
                    val placeFromRight = 5 - index
                    val digitChar = formatted[index]
                    RolodexDrumWheel(
                        digit = digitChar,
                        onIncrement = { onStepDigit(placeFromRight, +1) },
                        onDecrement = { onStepDigit(placeFromRight, -1) },
                        testTagBase = "${tagPrefix}_digit_$placeFromRight",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick calibration multiplier presets in moon-glass LED style
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(1, 10, 12, 60, 100, 1000).forEach { preset ->
                val active = multiplierInt == preset
                val lift by animateDpAsState(if (active) (-2).dp else 0.dp, tween(200), label = "preset_lift")
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .offset(y = lift)
                        .minimumInteractiveComponentSize()
                        .shadow(if (active) 8.dp else 0.dp, MaterialTheme.shapes.extraSmall, ambientColor = tungsten, spotColor = tungsten)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .clickable { onSetMultiplier(preset) }
                        .testTag("${tagPrefix}_preset_$preset"),
                    color = if (active) tungsten.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = MaterialTheme.shapes.extraSmall,
                    border = BorderStroke(
                        if (active) 1.5.dp else 1.dp,
                        if (active) tungsten else Color.Black.copy(alpha = 0.65f)
                    )
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = "×$preset",
                            style = TabularMonoStyle.copy(
                                fontSize = 10.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (active) tungsten else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RolodexDrumWheel(
    digit: Char,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    testTagBase: String,
    modifier: Modifier = Modifier
) {
    val tungsten = MaterialTheme.colorScheme.primary
    val drumSurface = MaterialTheme.colorScheme.surfaceContainerHigh
    val drumShadow = MaterialTheme.colorScheme.surfaceContainerLowest
    val outline = MaterialTheme.colorScheme.outline

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .clickable(onClick = onIncrement)
                .testTag("${testTagBase}_up"),
            color = drumSurface,
            shape = MaterialTheme.shapes.extraSmall,
            border = BorderStroke(0.5.dp, outline.copy(alpha = 0.5f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowUp,
                    contentDescription = "Increment digit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Cylindrical watch drum window with center rib
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(drumShadow, drumSurface, drumShadow)
                    )
                )
                .clickable(onClick = onIncrement),
            contentAlignment = Alignment.Center
        ) {
            // Hairline center rib
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(outline.copy(alpha = 0.35f))
            )

            AnimatedContent(
                targetState = digit,
                transitionSpec = {
                    (slideInVertically(animationSpec = tween(150, easing = HorologyEase)) { it / 2 } + fadeIn(tween(120)))
                        .togetherWith(slideOutVertically(animationSpec = tween(150, easing = HorologyEase)) { -it / 2 } + fadeOut(tween(120)))
                },
                label = "rolodex_digit_anim"
            ) { targetDigit ->
                Text(
                    text = targetDigit.toString(),
                    style = TabularMonoStyle.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = tungsten
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .clickable(onClick = onDecrement)
                .testTag("${testTagBase}_down"),
            color = drumSurface,
            shape = MaterialTheme.shapes.extraSmall,
            border = BorderStroke(0.5.dp, outline.copy(alpha = 0.5f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Decrement digit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun FilamentReadoutBox(
    label: String,
    value: String,
    accent: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = TabularMonoStyle.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = accent
            )
        }
    }
}

/**
 * 2050 Moon-Base Neomorphic Frosted-Glass EL-Wire LED Button.
 * When ON: floats upward with neon frosted outline glow and high-gloss marble glass sheen.
 * When OFF: sits recessed in engraved shadow contour with dim frosted glow.
 */
@Composable
fun MoonGlassLedButton(
    isOn: Boolean,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    accentColor: Color = MaterialTheme.colorScheme.secondary,
    symbolText: String? = null,
    enabled: Boolean = true,
    testTag: String = "",
    modifier: Modifier = Modifier
) {
    val effectiveOn = isOn && enabled
    val hoverLift by animateDpAsState(
        targetValue = if (effectiveOn) (-2).dp else 0.dp,
        animationSpec = tween(220, easing = HorologyEase),
        label = "moon_hover_lift"
    )
    val shadowElev by animateDpAsState(
        targetValue = if (effectiveOn) 10.dp else 1.dp,
        animationSpec = tween(220, easing = HorologyEase),
        label = "moon_shadow_elev"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (effectiveOn) 1f else if (enabled) 0.24f else 0.08f,
        animationSpec = tween(220, easing = HorologyEase),
        label = "moon_glow_alpha"
    )
    val iconTint by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            effectiveOn -> accentColor
            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f)
        },
        animationSpec = tween(220),
        label = "moon_icon_tint"
    )

    val shape = RoundedCornerShape(14.dp)
    val baseDark = MaterialTheme.colorScheme.surfaceContainerLowest
    val baseGlass = MaterialTheme.colorScheme.surfaceContainerHigh

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .offset(y = hoverLift)
            .shadow(
                elevation = shadowElev,
                shape = shape,
                ambientColor = if (effectiveOn) accentColor else Color.Black,
                spotColor = if (effectiveOn) accentColor else Color.Black
            )
            .clip(shape)
            .background(
                if (effectiveOn) {
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.18f),
                            baseGlass.copy(alpha = 0.92f),
                            accentColor.copy(alpha = 0.20f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.48f),
                            baseDark,
                            baseGlass.copy(alpha = 0.42f)
                        )
                    )
                }
            )
            .drawBehind {
                // Underneath EL-wire radial glow beneath frosted engraved symbol
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.36f * glowAlpha),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.minDimension * 0.76f
                    ),
                    center = center,
                    radius = size.minDimension * 0.76f
                )

                // Engraved shadow edge contour (when OFF) or bright EL-wire edge rim (when ON)
                drawRoundRect(
                    color = if (effectiveOn) {
                        accentColor.copy(alpha = 0.88f)
                    } else {
                        Color.Black.copy(alpha = 0.68f)
                    },
                    topLeft = Offset(1.dp.toPx(), 1.dp.toPx()),
                    size = Size(size.width - 2.dp.toPx(), size.height - 2.dp.toPx()),
                    cornerRadius = CornerRadius(13.dp.toPx(), 13.dp.toPx()),
                    style = Stroke(width = if (effectiveOn) 1.5.dp.toPx() else 1.2.dp.toPx())
                )

                // Frosted highlight rim on top marble bevel
                drawLine(
                    color = Color.White.copy(alpha = if (effectiveOn) 0.34f else 0.08f),
                    start = Offset(10.dp.toPx(), 1.5.dp.toPx()),
                    end = Offset(size.width - 10.dp.toPx(), 1.5.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .clickable(enabled = enabled, onClick = onClick)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(19.dp)
            )
            if (symbolText != null) {
                Text(
                    text = symbolText,
                    style = TabularMonoStyle.copy(
                        fontSize = 11.sp,
                        fontWeight = if (effectiveOn) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = iconTint,
                    maxLines = 1
                )
            }
        }
    }
}

data class PageInfoEntry(
    val heading: String,
    val plainBody: String,
    val mathBlock: String? = null
)

@Composable
fun UniformPageInfoSection(
    sectionTitle: String,
    items: List<PageInfoEntry>,
    modifier: Modifier = Modifier
) {
    HorologicalPanel(
        title = sectionTitle.uppercase(),
        badge = "UNIFORM FIELD GUIDE",
        accentColor = MaterialTheme.colorScheme.secondary,
        modifier = modifier
    ) {
        items.forEach { entry ->
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
                    Text(
                        text = entry.heading.uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = entry.plainBody.lowercase(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (entry.mathBlock != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = MaterialTheme.shapes.extraSmall,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = entry.mathBlock,
                                style = TabularMonoStyle.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            shape = MaterialTheme.shapes.extraSmall,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "beta notice always check your code in third party test",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

