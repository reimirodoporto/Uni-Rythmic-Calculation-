package com.example.domain

import java.util.Locale
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

enum class MeasureType(val label: String, val shortSymbol: String, val formulaLabel: String) {
    HALF("HALF MEASURE", "1/2", "1/2 = Φ⁻¹ ⇒ 1/1 = 2/Φ"),
    WHOLE("WHOLE MEASURE", "1/1", "1 = Φ⁻¹")
}

enum class PrecisionMode(val label: String, val shortSymbol: String, val basisLabel: String) {
    EXACT("EXACT 1/Φ", "1/Φ", "Φ⁻¹ = (√5 − 1)/2"),
    ROUNDED("ROUNDED 0.618", "0.618", "0.618")
}

enum class ComparisonChannelMode(val label: String, val shortSymbol: String) {
    OFF("OFF", "OFF"),
    NOTABLE_QUERY("CODATA", "SI"),
    SECONDARY_SCALABILITY("DUAL", "2X")
}

enum class DimensionFamily(val label: String, val symbol: String) {
    LENGTH("length", "L"),
    TIME("time", "T"),
    MASS("mass", "M"),
    CURRENT("electric current", "I"),
    TEMPERATURE("temperature", "Θ"),
    AMOUNT("amount of substance", "N"),
    LUMINOSITY("luminous intensity", "J"),
    VELOCITY("velocity", "L*T⁻¹"),
    FREQUENCY("frequency", "T⁻¹"),
    ACTION("action", "M*L²*T⁻¹"),
    CHARGE("electric charge", "I*T"),
    ENTROPY_CONST("thermal entropy", "M*L²*T⁻²*Θ⁻¹"),
    ACCELERATION("acceleration", "L*T⁻²")
}

data class ScientificUnit(
    val id: String,
    val name: String,
    val shortName: String,
    val symbol: String,
    val siValue: Double,
    val siUnitSuffix: String,
    val dimension: DimensionFamily,
    val category: String,
    val description: String,
    val isBenchmark: Boolean = false,
    val origin: DomainOrigin = DomainOrigin.PHYSICS,
    val secondaryOrigin: DomainOrigin? = null,
    val founderCredit: FounderCredit
)

data class BasisCalculationRow(
    val id: String,
    val title: String,
    val measureType: MeasureType,
    val precisionMode: PrecisionMode,
    val basisValue: Double,
    val fullUnit: Double,
    val tenthUnit: Double,
    val shiftedUnit: Double,
    val sectorWeightedGunit: Double
)

data class ArithmeticProof(
    val basisStep: String,
    val measureStep: String,
    val powerShiftStep: String,
    val sectorStep: String,
    val varScaleStep: String,
    val equivalenceSummary: String
)

data class ComparisonEvaluation(
    val primaryScaledValue: Double,
    val comparisonValue: Double,
    val ratioPrimaryToComparison: Double?,
    val log10OrderDiff: Double?,
    val sameDimension: Boolean,
    val summaryText: String
)

object UniRythmicsEngine {

    val PHI: Double = (1.0 + sqrt(5.0)) / 2.0
    val EXACT_PHI_INV: Double = (sqrt(5.0) - 1.0) / 2.0
    const val ROUNDED_PHI_INV: Double = 0.618

    fun computeBasisRow(
        measureType: MeasureType,
        precisionMode: PrecisionMode,
        dodecagonSector: Int,
        nthExponent: Int
    ): BasisCalculationRow {
        val basis = when (precisionMode) {
            PrecisionMode.EXACT -> EXACT_PHI_INV
            PrecisionMode.ROUNDED -> ROUNDED_PHI_INV
        }
        val fullUnit = when (measureType) {
            MeasureType.HALF -> basis * 2.0
            MeasureType.WHOLE -> basis
        }
        val tenthUnit = fullUnit / 10.0
        val safeExp = nthExponent.coerceIn(0, 9)
        val shiftedUnit = fullUnit * 10.0.pow(-safeExp)
        val safeSector = dodecagonSector.coerceIn(1, 12)
        val sectorWeighted = shiftedUnit * (safeSector / 12.0)

        val title = "${measureType.label} ${precisionMode.label}"
        return BasisCalculationRow(
            id = "${measureType.name}_${precisionMode.name}",
            title = title,
            measureType = measureType,
            precisionMode = precisionMode,
            basisValue = basis,
            fullUnit = fullUnit,
            tenthUnit = tenthUnit,
            shiftedUnit = shiftedUnit,
            sectorWeightedGunit = sectorWeighted
        )
    }

    fun computeAllFourMatrixRows(
        dodecagonSector: Int,
        nthExponent: Int
    ): List<BasisCalculationRow> {
        return listOf(
            computeBasisRow(MeasureType.HALF, PrecisionMode.ROUNDED, dodecagonSector, nthExponent),
            computeBasisRow(MeasureType.HALF, PrecisionMode.EXACT, dodecagonSector, nthExponent),
            computeBasisRow(MeasureType.WHOLE, PrecisionMode.ROUNDED, dodecagonSector, nthExponent),
            computeBasisRow(MeasureType.WHOLE, PrecisionMode.EXACT, dodecagonSector, nthExponent)
        )
    }

    fun buildStepProof(
        activeRow: BasisCalculationRow,
        dodecagonSector: Int,
        nthExponent: Int,
        primaryUnit: ScientificUnit,
        multiplierInt: Int
    ): ArithmeticProof {
        val safeSector = dodecagonSector.coerceIn(1, 12)
        val safeMultiplier = multiplierInt.coerceIn(1, 999_999)
        val angleDeg = safeSector * 30
        val scaledVarTotal = primaryUnit.siValue * safeMultiplier.toDouble()
        val gunitToVarScale = activeRow.sectorWeightedGunit * safeMultiplier.toDouble()

        val basisStr = if (activeRow.precisionMode == PrecisionMode.EXACT) {
            "1) basis: Φ⁻¹ = (√5 − 1)/2 = ${formatDecimal(activeRow.basisValue, 10)}"
        } else {
            "1) basis: rounded Φ⁻¹ = 0.6180000000"
        }

        val measureStr = if (activeRow.measureType == MeasureType.HALF) {
            "2) half (1/2 = basis): 1/1 = 2 * ${formatDecimal(activeRow.basisValue, 6)} = ${formatDecimal(activeRow.fullUnit, 8)} | 1/10 = ${formatDecimal(activeRow.tenthUnit, 8)}"
        } else {
            "2) whole (1 = basis): 1/1 = ${formatDecimal(activeRow.fullUnit, 8)} | 1/10 = ${formatDecimal(activeRow.tenthUnit, 8)}"
        }

        val powerStr = "3) 10⁻ⁿ shift (n=$nthExponent): ${formatDecimal(activeRow.fullUnit, 6)} * 10⁻$nthExponent = ${formatScientificOrDecimal(activeRow.shiftedUnit)}"
        val sectorStr = "4) sector $safeSector/12 ($angleDeg°): * ($safeSector/12) => 1 Gunit = ${formatScientificOrDecimal(activeRow.sectorWeightedGunit)}"
        val varStr = "5) rolodex x${formatRolodex(safeMultiplier)}: ${primaryUnit.shortName} (${formatScientificOrDecimal(primaryUnit.siValue)} ${primaryUnit.siUnitSuffix}) * $safeMultiplier = ${formatScientificOrDecimal(scaledVarTotal)} ${primaryUnit.siUnitSuffix}"
        val equivStr = "1 Gunit ${formatScientificOrDecimal(activeRow.sectorWeightedGunit)} = ${primaryUnit.shortName} scaled ${formatRolodex(safeMultiplier)} = ${formatScientificOrDecimal(gunitToVarScale)} Gunit"

        return ArithmeticProof(
            basisStep = basisStr,
            measureStep = measureStr,
            powerShiftStep = powerStr,
            sectorStep = sectorStr,
            varScaleStep = varStr,
            equivalenceSummary = equivStr
        )
    }

    fun evaluateComparison(
        primaryUnit: ScientificUnit,
        primaryMultiplier: Int,
        comparisonUnit: ScientificUnit,
        comparisonMultiplier: Int,
        mode: ComparisonChannelMode
    ): ComparisonEvaluation? {
        if (mode == ComparisonChannelMode.OFF) return null
        val pVal = primaryUnit.siValue * primaryMultiplier.coerceIn(1, 999_999)
        val cMult = if (mode == ComparisonChannelMode.NOTABLE_QUERY) 1 else comparisonMultiplier.coerceIn(1, 999_999)
        val cVal = comparisonUnit.siValue * cMult

        val sameDim = primaryUnit.dimension == comparisonUnit.dimension
        val ratio = if (cVal != 0.0) pVal / cVal else null
        val logDiff = if (pVal > 0.0 && cVal > 0.0) log10(pVal / cVal) else null

        val summary = if (sameDim && ratio != null) {
            "1 scaled ${primaryUnit.shortName} = ${formatScientificOrDecimal(ratio)} * ${comparisonUnit.shortName}"
        } else if (ratio != null) {
            "ratio (${primaryUnit.dimension.symbol} / ${comparisonUnit.dimension.symbol}) = ${formatScientificOrDecimal(ratio)}"
        } else {
            "zero comparison denominator"
        }

        return ComparisonEvaluation(
            primaryScaledValue = pVal,
            comparisonValue = cVal,
            ratioPrimaryToComparison = ratio,
            log10OrderDiff = logDiff,
            sameDimension = sameDim,
            summaryText = summary
        )
    }

    fun formatRolodex(value: Int): String {
        return String.format(Locale.US, "%06d", value.coerceIn(1, 999_999))
    }

    fun formatDecimal(value: Double, decimals: Int = 6): String {
        return String.format(Locale.US, "%.${decimals}f", value)
    }

    fun formatScientificOrDecimal(value: Double): String {
        val absVal = abs(value)
        if (absVal == 0.0) return "0.000000"
        return if (absVal >= 1e5 || absVal < 1e-4) {
            String.format(Locale.US, "%.6e", value)
        } else {
            String.format(Locale.US, "%.6f", value)
        }
    }
}
