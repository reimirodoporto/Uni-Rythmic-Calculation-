package com.example.domain

import androidx.compose.ui.graphics.Color

enum class DomainOrigin(
    val label: String,
    val shortTag: String,
    val glowColor: Color,
    val secondaryGlowColor: Color,
    val creditHeading: String
) {
    MATH(
        label = "MATHEMATICS",
        shortTag = "MATH",
        glowColor = Color(0xFFA78BFA),
        secondaryGlowColor = Color(0xFF6366F1),
        creditHeading = "MATHEMATICAL ORIGIN"
    ),
    PHYSICS(
        label = "PHYSICS",
        shortTag = "PHYS",
        glowColor = Color(0xFFFFAA33),
        secondaryGlowColor = Color(0xFFF59E0B),
        creditHeading = "CLASSICAL PHYSICS ORIGIN"
    ),
    QUANTUM(
        label = "QUANTUM",
        shortTag = "QUANT",
        glowColor = Color(0xFF38BDF8),
        secondaryGlowColor = Color(0xFF06B6D4),
        creditHeading = "QUANTUM MECHANICS ORIGIN"
    ),
    THERMO(
        label = "THERMODYNAMIC",
        shortTag = "THERM",
        glowColor = Color(0xFFFB7185),
        secondaryGlowColor = Color(0xFFF43F5E),
        creditHeading = "THERMODYNAMIC ORIGIN"
    ),
    COSMOLOGY(
        label = "COSMOLOGY",
        shortTag = "COSM",
        glowColor = Color(0xFF34D399),
        secondaryGlowColor = Color(0xFF10B981),
        creditHeading = "COSMOLOGICAL ORIGIN"
    ),
    GENERAL(
        label = "GENERAL",
        shortTag = "GEN",
        glowColor = Color(0xFFEAB308),
        secondaryGlowColor = Color(0xFFC5A059),
        creditHeading = "UNIFIED METROLOGY ORIGIN"
    )
}

data class OriginReferenceLink(
    val label: String,
    val url: String
)

data class FounderCredit(
    val founders: String,
    val originEra: String,
    val attributionSummary: String,
    val mathOriginFormula: String,
    val links: List<OriginReferenceLink>
)

data class TaxonomyFramework(
    val id: String,
    val title: String,
    val subtitle: String,
    val primaryOrigin: DomainOrigin,
    val secondaryOrigin: DomainOrigin? = null,
    val founderCredit: FounderCredit,
    val entries: List<TaxonomyEntry>
)

data class TaxonomyEntry(
    val title: String,
    val detail: String,
    val operationsOrSymbol: String,
    val origin: DomainOrigin,
    val secondaryOrigin: DomainOrigin? = null,
    val founderCredit: FounderCredit,
    val linkedUnitId: String? = null
)

data class CalibrationChallenge(
    val id: String,
    val title: String,
    val prompt: String,
    val hint: String,
    val targetMeasure: MeasureType? = null,
    val targetPrecision: PrecisionMode? = null,
    val targetSector: Int? = null,
    val targetExponent: Int? = null,
    val targetMultiplier: Int? = null,
    val targetUnitId: String? = null
) {
    fun isSolvedBy(
        measureType: MeasureType,
        precisionMode: PrecisionMode,
        sector: Int,
        exponent: Int,
        multiplier: Int,
        unitId: String
    ): Boolean {
        if (targetMeasure != null && measureType != targetMeasure) return false
        if (targetPrecision != null && precisionMode != targetPrecision) return false
        if (targetSector != null && sector != targetSector) return false
        if (targetExponent != null && exponent != targetExponent) return false
        if (targetMultiplier != null && multiplier != targetMultiplier) return false
        if (targetUnitId != null && unitId != targetUnitId) return false
        return true
    }
}

data class TranslatorLanguage(
    val id: String,
    val label: String,
    val isDefault: Boolean = false,
    val requiresPromptFallback: Boolean = false
)

object UniRythmicsCatalog {

    val translatorLanguages: List<TranslatorLanguage> = listOf(
        TranslatorLanguage("lean4", "LEAN 4 DEFAULT", isDefault = true),
        TranslatorLanguage("kotlin", "KOTLIN COMPOSE"),
        TranslatorLanguage("python", "PYTHON 3"),
        TranslatorLanguage("typescript", "TYPESCRIPT"),
        TranslatorLanguage("rust", "RUST"),
        TranslatorLanguage("cpp", "C++ 20"),
        TranslatorLanguage("html_apk", "HTML TO APK", requiresPromptFallback = true),
        TranslatorLanguage("latex", "MATH BLOCK"),
        TranslatorLanguage("glsl", "GLSL SHADER"),
        TranslatorLanguage("sql", "SQL"),
        TranslatorLanguage("vibe_prompt", "NO CODE PROMPT ONLY", requiresPromptFallback = true)
    )

    private val creditBipm = FounderCredit(
        founders = "bipm codata committee and jean baptiste joseph fourier",
        originEra = "1822 to 2019 si redefinition",
        attributionSummary = "the international bureau of weights and measures anchored seven orthogonal physical dimensions to exact defining constants",
        mathOriginFormula = "[Q] = L^a * M^b * T^c * I^d * Theta^e * N^f * J^g",
        links = listOf(
            OriginReferenceLink("BIPM SI", "https://www.bipm.org/en/measurement-units"),
            OriginReferenceLink("NIST CODATA", "https://physics.nist.gov/cuu/Constants/")
        )
    )

    private val creditPlanck = FounderCredit(
        founders = "max karl ernst ludwig planck",
        originEra = "1899 quantum action paper",
        attributionSummary = "max planck derived natural universal units solely from hbar gravitational constant g and speed of light c",
        mathOriginFormula = "tP = sqrt(hbar * G / c^5) | lP = sqrt(hbar * G / c^3)",
        links = listOf(
            OriginReferenceLink("NIST PLANCK", "https://physics.nist.gov/cgi-bin/cuu/Value?plkt"),
            OriginReferenceLink("WOLFRAM", "https://www.wolframalpha.com/input?i=planck+time")
        )
    )

    private val creditMaxwell = FounderCredit(
        founders = "james clerk maxwell and michael faraday",
        originEra = "1865 dynamical electromagnetic theory",
        attributionSummary = "unified electricity magnetism and light propagation speed c as invariant vacuum field constants",
        mathOriginFormula = "c = 1 / sqrt(mu0 * epsilon0) = 299792458 m/s",
        links = listOf(
            OriginReferenceLink("MACTUTOR", "https://mathshistory.st-andrews.ac.uk/Biographies/Maxwell/"),
            OriginReferenceLink("CODATA C", "https://physics.nist.gov/cgi-bin/cuu/Value?c")
        )
    )

    private val creditBoltzmann = FounderCredit(
        founders = "ludwig boltzmann and sadi carnot",
        originEra = "1877 statistical mechanics",
        attributionSummary = "linked macroscopic thermodynamic temperature to microscopic kinetic energy and microstate entropy",
        mathOriginFormula = "S = kB * ln(W) | kB = 1.380649e-23 J/K",
        links = listOf(
            OriginReferenceLink("MACTUTOR", "https://mathshistory.st-andrews.ac.uk/Biographies/Boltzmann/"),
            OriginReferenceLink("CODATA KB", "https://physics.nist.gov/cgi-bin/cuu/Value?k")
        )
    )

    private val creditNewton = FounderCredit(
        founders = "isaac newton and galileo galilei",
        originEra = "1687 principia mathematica",
        attributionSummary = "established classical kinematics inertial mass and gravitational acceleration laws",
        mathOriginFormula = "F = m * a | g0 = 9.80665 m/s^2",
        links = listOf(
            OriginReferenceLink("MACTUTOR", "https://mathshistory.st-andrews.ac.uk/Biographies/Newton/"),
            OriginReferenceLink("WOLFRAM", "https://www.wolframalpha.com/input?i=standard+gravity")
        )
    )

    private val creditCosmos = FounderCredit(
        founders = "johannes kepler edwin hubble and iau",
        originEra = "1609 to 2012 iau resolution",
        attributionSummary = "defined astronomical distance units anchoring solar orbital radii and light travel per julian year",
        mathOriginFormula = "1 ly = c * 31557600 s | 1 AU = 149597870700 m",
        links = listOf(
            OriginReferenceLink("IAU UNITS", "https://www.iau.org/public/themes/measuring/"),
            OriginReferenceLink("WOLFRAM", "https://www.wolframalpha.com/input?i=light+year+in+meters")
        )
    )

    private val creditGoldenMath = FounderCredit(
        founders = "euclid of alexandria and leonardo fibonacci",
        originEra = "300 bce elements to 1202 liber abaci",
        attributionSummary = "formalized extreme and mean ratio scaling whose golden conjugate phi inverse drives half and whole measure harmonics",
        mathOriginFormula = "phi^-1 = (sqrt(5) - 1)/2 = 0.6180339887",
        links = listOf(
            OriginReferenceLink("MATHWORLD", "https://mathworld.wolfram.com/GoldenRatio.html"),
            OriginReferenceLink("MACTUTOR", "https://mathshistory.st-andrews.ac.uk/Biographies/Fibonacci/")
        )
    )

    private val creditStevens = FounderCredit(
        founders = "stanley smith stevens",
        originEra = "1946 harvard on the theory of scales",
        attributionSummary = "classified empirical measurement into nominal ordinal interval and ratio structures by permissible group transformations",
        mathOriginFormula = "ratio scale: x' = a * x (a > 0) with invariant true zero",
        links = listOf(
            OriginReferenceLink("STANFORD", "https://plato.stanford.edu/entries/measurement-science/"),
            OriginReferenceLink("WOLFRAM", "https://www.wolframalpha.com/input?i=stevens+scales+of+measurement")
        )
    )

    private val creditTensors = FounderCredit(
        founders = "georg cantor gregorio ricci curbastro and tullio levi civita",
        originEra = "1874 set theory to 1900 tensor calculus",
        attributionSummary = "distinguished discrete versus continuous domains and scalar rank zero vector rank one and multilinear tensor fields",
        mathOriginFormula = "T'_{i1..ip} = sum R_{i1,j1}..R_{ip,jp} * T_{j1..jp}",
        links = listOf(
            OriginReferenceLink("MATHWORLD", "https://mathworld.wolfram.com/Tensor.html"),
            OriginReferenceLink("MACTUTOR", "https://mathshistory.st-andrews.ac.uk/Biographies/Ricci-Curbastro/")
        )
    )

    private val creditGaussMetro = FounderCredit(
        founders = "carl friedrich gauss and bipm jcgm working group",
        originEra = "1809 least squares to gum metrology guide",
        attributionSummary = "separated direct comparison versus indirect derived propagation and quantified random dispersion versus systematic zero bias",
        mathOriginFormula = "u_c^2(y) = sum (df/dx_i)^2 * u^2(x_i)",
        links = listOf(
            OriginReferenceLink("MACTUTOR", "https://mathshistory.st-andrews.ac.uk/Biographies/Gauss/"),
            OriginReferenceLink("BIPM GUM", "https://www.bipm.org/en/committees/jc/jcgm")
        )
    )

    private val creditChem = FounderCredit(
        founders = "amedeo avogadro and iupac commission",
        originEra = "1811 molecular hypothesis to 2019 mole",
        attributionSummary = "fixed exact elementary entity count per mole enabling stoichiometric and thermodynamic bridging",
        mathOriginFormula = "N_A = 6.02214076e23 mol^-1",
        links = listOf(
            OriginReferenceLink("IUPAC", "https://iupac.org/what-we-do/nomenclature/"),
            OriginReferenceLink("CODATA NA", "https://physics.nist.gov/cgi-bin/cuu/Value?na")
        )
    )

    // CODATA 2022 & SI Base Units Registry with DomainOrigin & FounderCredit
    val scientificUnits: List<ScientificUnit> = listOf(
        ScientificUnit(
            id = "meter",
            name = "1 meter",
            shortName = "m",
            symbol = "m",
            siValue = 1.0,
            siUnitSuffix = "m",
            dimension = DimensionFamily.LENGTH,
            category = "si base quantities",
            description = "si base unit of length defined by fixing vacuum light speed c",
            origin = DomainOrigin.PHYSICS,
            secondaryOrigin = DomainOrigin.GENERAL,
            founderCredit = creditBipm
        ),
        ScientificUnit(
            id = "second",
            name = "1 second",
            shortName = "s",
            symbol = "s",
            siValue = 1.0,
            siUnitSuffix = "s",
            dimension = DimensionFamily.TIME,
            category = "si base quantities",
            description = "si base unit of time anchored to cesium 133 hyperfine ticks",
            origin = DomainOrigin.QUANTUM,
            secondaryOrigin = DomainOrigin.PHYSICS,
            founderCredit = creditBipm
        ),
        ScientificUnit(
            id = "kilogram",
            name = "1 kilogram",
            shortName = "kg",
            symbol = "kg",
            siValue = 1.0,
            siUnitSuffix = "kg",
            dimension = DimensionFamily.MASS,
            category = "si base quantities",
            description = "si base unit of mass defined via the planck constant h",
            origin = DomainOrigin.PHYSICS,
            secondaryOrigin = DomainOrigin.QUANTUM,
            founderCredit = creditBipm
        ),
        ScientificUnit(
            id = "ampere",
            name = "1 ampere",
            shortName = "A",
            symbol = "A",
            siValue = 1.0,
            siUnitSuffix = "A",
            dimension = DimensionFamily.CURRENT,
            category = "si base quantities",
            description = "si base unit of electric current defined via elementary charge e",
            origin = DomainOrigin.PHYSICS,
            founderCredit = creditMaxwell
        ),
        ScientificUnit(
            id = "kelvin",
            name = "1 kelvin",
            shortName = "K",
            symbol = "K",
            siValue = 1.0,
            siUnitSuffix = "K",
            dimension = DimensionFamily.TEMPERATURE,
            category = "si base quantities",
            description = "si base unit of thermodynamic temperature defined via boltzmann constant",
            origin = DomainOrigin.THERMO,
            founderCredit = creditBoltzmann
        ),
        ScientificUnit(
            id = "mole",
            name = "1 mole",
            shortName = "mol",
            symbol = "mol",
            siValue = 1.0,
            siUnitSuffix = "mol",
            dimension = DimensionFamily.AMOUNT,
            category = "si base quantities",
            description = "si base unit of amount of substance anchored to avogadro number",
            origin = DomainOrigin.GENERAL,
            secondaryOrigin = DomainOrigin.THERMO,
            founderCredit = creditChem
        ),
        ScientificUnit(
            id = "candela",
            name = "1 candela",
            shortName = "cd",
            symbol = "cd",
            siValue = 1.0,
            siUnitSuffix = "cd",
            dimension = DimensionFamily.LUMINOSITY,
            category = "si base quantities",
            description = "si base unit of luminous intensity in a given direction",
            origin = DomainOrigin.PHYSICS,
            founderCredit = creditBipm
        ),
        ScientificUnit(
            id = "planck_time",
            name = "1 planck time",
            shortName = "tP",
            symbol = "tP",
            siValue = 5.391247e-44,
            siUnitSuffix = "s",
            dimension = DimensionFamily.TIME,
            category = "planck quantum units",
            description = "smallest quantum temporal interval derived from hbar g and c",
            isBenchmark = true,
            origin = DomainOrigin.QUANTUM,
            secondaryOrigin = DomainOrigin.COSMOLOGY,
            founderCredit = creditPlanck
        ),
        ScientificUnit(
            id = "planck_length",
            name = "1 planck length",
            shortName = "lP",
            symbol = "lP",
            siValue = 1.616255e-35,
            siUnitSuffix = "m",
            dimension = DimensionFamily.LENGTH,
            category = "planck quantum units",
            description = "quantum spatial cell derived from hbar g and speed of light c",
            isBenchmark = true,
            origin = DomainOrigin.QUANTUM,
            secondaryOrigin = DomainOrigin.MATH,
            founderCredit = creditPlanck
        ),
        ScientificUnit(
            id = "planck_mass",
            name = "1 planck mass",
            shortName = "mP",
            symbol = "mP",
            siValue = 2.176434e-8,
            siUnitSuffix = "kg",
            dimension = DimensionFamily.MASS,
            category = "planck quantum units",
            description = "quantum gravitational mass threshold from hbar c and g",
            isBenchmark = true,
            origin = DomainOrigin.QUANTUM,
            secondaryOrigin = DomainOrigin.PHYSICS,
            founderCredit = creditPlanck
        ),
        ScientificUnit(
            id = "planck_temp",
            name = "1 planck temp",
            shortName = "TP",
            symbol = "TP",
            siValue = 1.416784e32,
            siUnitSuffix = "K",
            dimension = DimensionFamily.TEMPERATURE,
            category = "planck quantum units",
            description = "maximum quantum thermal scale from planck mass and boltzmann constant",
            isBenchmark = true,
            origin = DomainOrigin.THERMO,
            secondaryOrigin = DomainOrigin.QUANTUM,
            founderCredit = creditPlanck
        ),
        ScientificUnit(
            id = "speed_of_light",
            name = "light speed c",
            shortName = "c",
            symbol = "c",
            siValue = 299_792_458.0,
            siUnitSuffix = "m/s",
            dimension = DimensionFamily.VELOCITY,
            category = "codata 2022 constants",
            description = "exact vacuum speed of light in meters per second",
            isBenchmark = true,
            origin = DomainOrigin.PHYSICS,
            secondaryOrigin = DomainOrigin.COSMOLOGY,
            founderCredit = creditMaxwell
        ),
        ScientificUnit(
            id = "cesium_hyperfine",
            name = "cesium 133 tick",
            shortName = "dvCs",
            symbol = "Hz",
            siValue = 9_192_631_770.0,
            siUnitSuffix = "Hz",
            dimension = DimensionFamily.FREQUENCY,
            category = "codata 2022 constants",
            description = "unperturbed ground state hyperfine transition frequency of cesium 133",
            isBenchmark = true,
            origin = DomainOrigin.QUANTUM,
            founderCredit = creditBipm
        ),
        ScientificUnit(
            id = "planck_constant",
            name = "planck action h",
            shortName = "h",
            symbol = "Js",
            siValue = 6.62607015e-34,
            siUnitSuffix = "Js",
            dimension = DimensionFamily.ACTION,
            category = "codata 2022 constants",
            description = "quantum of electromagnetic action in joule seconds",
            isBenchmark = true,
            origin = DomainOrigin.QUANTUM,
            founderCredit = creditPlanck
        ),
        ScientificUnit(
            id = "elementary_charge",
            name = "proton charge e",
            shortName = "e",
            symbol = "C",
            siValue = 1.602176634e-19,
            siUnitSuffix = "C",
            dimension = DimensionFamily.CHARGE,
            category = "codata 2022 constants",
            description = "exact electric charge carried by a single proton in coulombs",
            isBenchmark = true,
            origin = DomainOrigin.QUANTUM,
            secondaryOrigin = DomainOrigin.PHYSICS,
            founderCredit = creditMaxwell
        ),
        ScientificUnit(
            id = "boltzmann_constant",
            name = "boltzmann kB",
            shortName = "kB",
            symbol = "J/K",
            siValue = 1.380649e-23,
            siUnitSuffix = "J/K",
            dimension = DimensionFamily.ENTROPY_CONST,
            category = "codata 2022 constants",
            description = "relates average relative thermal energy to thermodynamic temperature",
            isBenchmark = true,
            origin = DomainOrigin.THERMO,
            founderCredit = creditBoltzmann
        ),
        ScientificUnit(
            id = "standard_gravity",
            name = "earth gravity g0",
            shortName = "g0",
            symbol = "m/s2",
            siValue = 9.80665,
            siUnitSuffix = "m/s2",
            dimension = DimensionFamily.ACCELERATION,
            category = "kinematic benchmarks",
            description = "nominal gravitational acceleration at earth surface",
            isBenchmark = true,
            origin = DomainOrigin.PHYSICS,
            founderCredit = creditNewton
        ),
        ScientificUnit(
            id = "astronomical_unit",
            name = "astronomical au",
            shortName = "AU",
            symbol = "m",
            siValue = 1.495978707e11,
            siUnitSuffix = "m",
            dimension = DimensionFamily.LENGTH,
            category = "astrophysical benchmarks",
            description = "mean earth sun reference distance in meters",
            isBenchmark = true,
            origin = DomainOrigin.COSMOLOGY,
            founderCredit = creditCosmos
        ),
        ScientificUnit(
            id = "light_year",
            name = "light year ly",
            shortName = "ly",
            symbol = "m",
            siValue = 9.4607304725808e15,
            siUnitSuffix = "m",
            dimension = DimensionFamily.LENGTH,
            category = "astrophysical benchmarks",
            description = "distance light travels across one julian year",
            isBenchmark = true,
            origin = DomainOrigin.COSMOLOGY,
            founderCredit = creditCosmos
        ),
        ScientificUnit(
            id = "millisecond",
            name = "1 millisecond",
            shortName = "ms",
            symbol = "ms",
            siValue = 1.0e-3,
            siUnitSuffix = "s",
            dimension = DimensionFamily.TIME,
            category = "sub unit benchmarks",
            description = "one thousandth of a second horological beat",
            isBenchmark = true,
            origin = DomainOrigin.GENERAL,
            founderCredit = creditGoldenMath
        ),
        ScientificUnit(
            id = "nanometer",
            name = "1 nanometer",
            shortName = "nm",
            symbol = "nm",
            siValue = 1.0e-9,
            siUnitSuffix = "m",
            dimension = DimensionFamily.LENGTH,
            category = "sub unit benchmarks",
            description = "one billionth of a meter atomic optical scale",
            isBenchmark = true,
            origin = DomainOrigin.QUANTUM,
            founderCredit = creditPlanck
        )
    )

    val benchmarkUnits: List<ScientificUnit> = scientificUnits.filter { it.isBenchmark }

    fun findUnitById(id: String): ScientificUnit {
        return scientificUnits.firstOrNull { it.id == id } ?: scientificUnits.first()
    }

    val taxonomies: List<TaxonomyFramework> = listOf(
        TaxonomyFramework(
            id = "stevens_scales",
            title = "STEVENS SCALES OF MEASUREMENT",
            subtitle = "permissible mathematical transforms separating categorical rank interval and ratio measurements",
            primaryOrigin = DomainOrigin.MATH,
            secondaryOrigin = DomainOrigin.GENERAL,
            founderCredit = creditStevens,
            entries = listOf(
                TaxonomyEntry(
                    title = "NOMINAL SCALE",
                    detail = "qualitative equivalence classes and labels without intrinsic numeric order",
                    operationsOrSymbol = "= !=",
                    origin = DomainOrigin.GENERAL,
                    founderCredit = creditStevens
                ),
                TaxonomyEntry(
                    title = "ORDINAL SCALE",
                    detail = "monotone rank ordered categories without standardized interval spacing",
                    operationsOrSymbol = "< >",
                    origin = DomainOrigin.MATH,
                    founderCredit = creditStevens
                ),
                TaxonomyEntry(
                    title = "INTERVAL SCALE",
                    detail = "equal additive differences anchored to an arbitrary non physical zero",
                    operationsOrSymbol = "+ -",
                    origin = DomainOrigin.MATH,
                    founderCredit = creditStevens
                ),
                TaxonomyEntry(
                    title = "RATIO SCALE",
                    detail = "absolute true zero origin permitting golden ratio multiplication and dimensional scaling",
                    operationsOrSymbol = "* /",
                    origin = DomainOrigin.MATH,
                    secondaryOrigin = DomainOrigin.PHYSICS,
                    founderCredit = creditGoldenMath,
                    linkedUnitId = "meter"
                )
            )
        ),
        TaxonomyFramework(
            id = "si_base",
            title = "SI DIMENSIONAL FRAMEWORK",
            subtitle = "seven base quantities and derived dimensional products governing codata 2022 physics",
            primaryOrigin = DomainOrigin.PHYSICS,
            secondaryOrigin = DomainOrigin.QUANTUM,
            founderCredit = creditBipm,
            entries = listOf(
                TaxonomyEntry("LENGTH L", "meter fixed via vacuum speed of light c", "m", DomainOrigin.PHYSICS, founderCredit = creditBipm, linkedUnitId = "meter"),
                TaxonomyEntry("TIME T", "second fixed via cesium 133 hyperfine frequency", "s", DomainOrigin.QUANTUM, DomainOrigin.PHYSICS, founderCredit = creditBipm, linkedUnitId = "second"),
                TaxonomyEntry("MASS M", "kilogram fixed via planck quantum constant h", "kg", DomainOrigin.PHYSICS, DomainOrigin.QUANTUM, founderCredit = creditBipm, linkedUnitId = "kilogram"),
                TaxonomyEntry("CURRENT I", "ampere fixed via elementary proton charge e", "A", DomainOrigin.PHYSICS, founderCredit = creditMaxwell, linkedUnitId = "ampere"),
                TaxonomyEntry("TEMPERATURE THETA", "kelvin fixed via boltzmann entropy constant kB", "K", DomainOrigin.THERMO, founderCredit = creditBoltzmann, linkedUnitId = "kelvin"),
                TaxonomyEntry("AMOUNT N", "mole fixed via avogadro entity count", "mol", DomainOrigin.GENERAL, DomainOrigin.THERMO, founderCredit = creditChem, linkedUnitId = "mole"),
                TaxonomyEntry("LUMINOSITY J", "candela fixed via 540 terahertz luminous efficacy", "cd", DomainOrigin.PHYSICS, founderCredit = creditBipm, linkedUnitId = "candela")
            )
        ),
        TaxonomyFramework(
            id = "math_properties",
            title = "MATHEMATICAL DATA PROPERTIES",
            subtitle = "qualitative versus quantitative continuous versus discrete and scalar vector tensor rank",
            primaryOrigin = DomainOrigin.MATH,
            secondaryOrigin = DomainOrigin.PHYSICS,
            founderCredit = creditTensors,
            entries = listOf(
                TaxonomyEntry("QUALITATIVE DISCRETE", "countable nominal attributes classified by membership", "SET", DomainOrigin.MATH, founderCredit = creditTensors),
                TaxonomyEntry("QUANTITATIVE CONTINUOUS", "real valued continuum supporting limits and arbitrary subdivision", "REAL", DomainOrigin.MATH, founderCredit = creditGoldenMath),
                TaxonomyEntry("SCALAR RANK 0", "directionless magnitude invariant under frame rotation", "RANK 0", DomainOrigin.PHYSICS, DomainOrigin.MATH, founderCredit = creditTensors, linkedUnitId = "kilogram"),
                TaxonomyEntry("VECTOR RANK 1", "magnitude plus spatial orientation obeying transformation laws", "RANK 1", DomainOrigin.PHYSICS, DomainOrigin.MATH, founderCredit = creditMaxwell, linkedUnitId = "speed_of_light"),
                TaxonomyEntry("TENSOR RANK 2 PLUS", "multilinear array mapping stress curvature and spacetime metric", "RANK 2+", DomainOrigin.MATH, DomainOrigin.COSMOLOGY, founderCredit = creditTensors)
            )
        ),
        TaxonomyFramework(
            id = "direct_indirect",
            title = "DIRECT AND INDIRECT METROLOGY",
            subtitle = "primary artifact comparison versus derived law coupling and error budget separation",
            primaryOrigin = DomainOrigin.GENERAL,
            secondaryOrigin = DomainOrigin.MATH,
            founderCredit = creditGaussMetro,
            entries = listOf(
                TaxonomyEntry("DIRECT COMPARISON", "read directly against a calibrated standard reference scale", "DIRECT", DomainOrigin.GENERAL, founderCredit = creditGaussMetro, linkedUnitId = "meter"),
                TaxonomyEntry("INDIRECT DERIVED", "computed through physical equations relating multiple base inputs", "f(X)", DomainOrigin.PHYSICS, DomainOrigin.MATH, founderCredit = creditGaussMetro, linkedUnitId = "planck_constant"),
                TaxonomyEntry("PRECISION SIGMA", "random repeatability spread across independent trials", "SIGMA", DomainOrigin.MATH, founderCredit = creditGaussMetro),
                TaxonomyEntry("SYSTEMATIC BIAS", "consistent zero offset removed through reference calibration", "DELTA", DomainOrigin.GENERAL, founderCredit = creditGaussMetro)
            )
        ),
        TaxonomyFramework(
            id = "disciplinary_domains",
            title = "DISCIPLINARY SCIENTIFIC DOMAINS",
            subtitle = "segregated measurement lenses spanning mechanical em quantum thermal chemical biological psychometric and cosmic",
            primaryOrigin = DomainOrigin.QUANTUM,
            secondaryOrigin = DomainOrigin.COSMOLOGY,
            founderCredit = creditPlanck,
            entries = listOf(
                TaxonomyEntry("MECHANICAL KINEMATICS", "force inertia acceleration and orbital dynamics", "MECH", DomainOrigin.PHYSICS, founderCredit = creditNewton, linkedUnitId = "standard_gravity"),
                TaxonomyEntry("ELECTROMAGNETIC FIELD", "charge potential flux and light speed c propagation", "EM", DomainOrigin.PHYSICS, founderCredit = creditMaxwell, linkedUnitId = "speed_of_light"),
                TaxonomyEntry("THERMODYNAMIC ENTROPY", "heat capacity thermal energy and microstate disorder", "THERM", DomainOrigin.THERMO, founderCredit = creditBoltzmann, linkedUnitId = "boltzmann_constant"),
                TaxonomyEntry("QUANTUM PLANCK SCALES", "discrete action hbar planck length time and atomic ticks", "QUANT", DomainOrigin.QUANTUM, founderCredit = creditPlanck, linkedUnitId = "planck_time"),
                TaxonomyEntry("CHEMICAL STOICHIOMETRY", "molar concentration avogadro counts and bond energies", "CHEM", DomainOrigin.THERMO, DomainOrigin.GENERAL, founderCredit = creditChem, linkedUnitId = "mole"),
                TaxonomyEntry("BIOLOGICAL PSYCHOMETRIC", "logarithmic sensory response thresholds and bio signals", "PSYCH", DomainOrigin.GENERAL, DomainOrigin.MATH, founderCredit = creditStevens),
                TaxonomyEntry("COSMOLOGICAL ASTROPHYSICS", "astronomical unit parsec light year and cosmic expansion", "COSM", DomainOrigin.COSMOLOGY, founderCredit = creditCosmos, linkedUnitId = "light_year")
            )
        )
    )

    val challenges: List<CalibrationChallenge> = listOf(
        CalibrationChallenge(
            id = "ch_half_exact_home",
            title = "HALF MEASURE EXACT BASELINE",
            prompt = "configure 1 gunit to half measure in exact phi inverse mode at sector 12 and exponent 0",
            hint = "select half measure exact phi inverse sector 12 and power 10^0",
            targetMeasure = MeasureType.HALF,
            targetPrecision = PrecisionMode.EXACT,
            targetSector = 12,
            targetExponent = 0
        ),
        CalibrationChallenge(
            id = "ch_tenth_rounded",
            title = "ONE TENTH ROUNDED HARMONIC",
            prompt = "switch to rounded 0.618 in whole measure and set exponent n equals 1 at sector 12",
            hint = "toggle whole measure rounded 0.618 sector 12 and power 10^-1",
            targetMeasure = MeasureType.WHOLE,
            targetPrecision = PrecisionMode.ROUNDED,
            targetSector = 12,
            targetExponent = 1
        ),
        CalibrationChallenge(
            id = "ch_quarter_dial",
            title = "DODECAGONAL QUARTER SWEEP",
            prompt = "sweep the 360 degree dial to sector 3 in half measure exact mode at exponent 0",
            hint = "tap vertex 3 at 90 degrees on the dodecagonal dial",
            targetMeasure = MeasureType.HALF,
            targetPrecision = PrecisionMode.EXACT,
            targetSector = 3,
            targetExponent = 0
        ),
        CalibrationChallenge(
            id = "ch_rolodex_12",
            title = "DODECAGONAL ROLODEX LOCK",
            prompt = "load 1 meter into 1 var and step the six digit rolodex multiplier to 000012",
            hint = "select 1 meter and tap preset x12 or dial tens and ones",
            targetUnitId = "meter",
            targetMultiplier = 12
        ),
        CalibrationChallenge(
            id = "ch_planck_harmonic",
            title = "PLANCK TIME SUB HARMONIC",
            prompt = "load 1 planck time into 1 var set exponent 3 and dial rolodex to 000100",
            hint = "load 1 planck time from the list set 10^-3 and dial 000100",
            targetUnitId = "planck_time",
            targetExponent = 3,
            targetMultiplier = 100
        ),
        CalibrationChallenge(
            id = "ch_exact_vs_rounded",
            title = "GOLDEN MINUTE CALIBRATION",
            prompt = "set half measure exact at sector 6 with 1 second and rolodex 000060",
            hint = "half measure exact sector 6 at 180 degrees 1 second and x60",
            targetMeasure = MeasureType.HALF,
            targetPrecision = PrecisionMode.EXACT,
            targetSector = 6,
            targetUnitId = "second",
            targetMultiplier = 60
        )
    )
}
