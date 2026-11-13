package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.TranslatorLanguage
import com.example.domain.UniRythmicsCatalog
import com.example.domain.UniRythmicsEngine
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "GeminiAiViewModel"
private const val DEFAULT_MODEL = "gemini-3.8-flash"

enum class GeminiMode {
    STREAMING_QA,
    STRUCTURED_PROOF
}

data class StructuredMetrologyAnalysis(
    val summary: String,
    val dimensionalBreakdown: String,
    val calibrationSteps: List<String>,
    val precisionNote: String
)

data class TranslatorOutput(
    val targetLanguageLabel: String,
    val plainInstructions: String,
    val mathAndCodeBlock: String,
    val bonusVibePrompt: String,
    val betaNotice: String
)

sealed interface AiResponseState {
    data object Idle : AiResponseState
    data object Loading : AiResponseState
    data class Streaming(val text: String) : AiResponseState
    data class SuccessText(val text: String) : AiResponseState
    data class SuccessStructured(val analysis: StructuredMetrologyAnalysis) : AiResponseState
    data class Error(val message: String) : AiResponseState
}

sealed interface TranslatorState {
    data object Idle : TranslatorState
    data object Loading : TranslatorState
    data class Success(val output: TranslatorOutput) : TranslatorState
    data class Error(val message: String) : TranslatorState
}

data class GeminiLabUiState(
    val promptInput: String = "",
    val mode: GeminiMode = GeminiMode.STREAMING_QA,
    val includeWorkbenchContext: Boolean = true,
    val responseState: AiResponseState = AiResponseState.Idle,
    val isPullDrawerOpen: Boolean = false,
    val isOutputSheetOpen: Boolean = false,
    val translatorInput: String = "",
    val selectedLanguage: TranslatorLanguage = UniRythmicsCatalog.translatorLanguages.first(),
    val translatorState: TranslatorState = TranslatorState.Idle
)

class GeminiAiViewModel : ViewModel() {

    private val systemPrompt = content {
        text(
            """
            You are a metrology, Lean 4 formalization, and code translation assistant for the Uni-Rythmics Horological Metrology and Scaling Workbench.
            Core math rules:
            1 Gunit = 1 var = var of comparison.
            Phi inverse = (sqrt(5) - 1)/2 approx 0.6180339887 vs rounded 0.618.
            Half Measure: 1/2 = basis => 1/1 = 2 * basis, 1/10 = full/10.
            Whole Measure: 1 = basis, 1/10 = full/10.
            10^-n power shift and 360 degree dodecagonal dial (12 sectors x 30 deg).
            1 var is scaled by a 6-digit integer Rolodex multiplier (000001..999999) and compared against CODATA 2022 constants.
            Formatting rules for plain prose: write clean lowercase plain text with NO markdown (#, *, **, _, __, quotes), NO italics, NO emojis, and NO decorative dashes or periods outside math/code blocks.
            """.trimIndent()
        )
    }

    private val streamingModel by lazy {
        Firebase.ai.generativeModel(
            modelName = DEFAULT_MODEL,
            systemInstruction = systemPrompt
        )
    }

    private val metrologySchema = Schema.obj(
        properties = mapOf(
            "summary" to Schema.string(description = "lowercase plain text synthesis without markdown or emojis"),
            "dimensionalBreakdown" to Schema.string(description = "plain text math and dimensional formula block"),
            "calibrationSteps" to Schema.array(
                items = Schema.string(),
                description = "lowercase plain text calibration steps"
            ),
            "precisionNote" to Schema.string(description = "lowercase plain text comparison between exact 1/Phi and rounded 0.618")
        )
    )

    private val structuredModel by lazy {
        Firebase.ai.generativeModel(
            modelName = DEFAULT_MODEL,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = metrologySchema
            },
            systemInstruction = systemPrompt
        )
    }

    private val translatorSchema = Schema.obj(
        properties = mapOf(
            "plainInstructions" to Schema.string(
                description = "clear non ascii plain text instructions in lowercase with no markdown hashtags asterisks underscores dashes quotes parentheses or emojis"
            ),
            "mathAndCodeBlock" to Schema.string(
                description = "auto corrected Lean 4 default or translated target language code and math equations in plain block text"
            ),
            "bonusVibePrompt" to Schema.string(
                description = "always included bonus vibe code prompt in plain lowercase text for html to apk conversion unclear code or java injection malware precaution"
            )
        )
    )

    private val translatorModel by lazy {
        Firebase.ai.generativeModel(
            modelName = DEFAULT_MODEL,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = translatorSchema
            },
            systemInstruction = systemPrompt
        )
    }

    private val _uiState = MutableStateFlow(GeminiLabUiState())
    val uiState: StateFlow<GeminiLabUiState> = _uiState.asStateFlow()

    private var activeJob: Job? = null
    private var translatorJob: Job? = null

    fun togglePullDrawer() {
        _uiState.update { it.copy(isPullDrawerOpen = !it.isPullDrawerOpen) }
    }

    fun closePullDrawer() {
        _uiState.update { it.copy(isPullDrawerOpen = false) }
    }

    fun dismissOutputSheet() {
        _uiState.update { it.copy(isOutputSheetOpen = false) }
    }

    fun openOutputSheet() {
        _uiState.update { it.copy(isOutputSheetOpen = true) }
    }

    fun updateTranslatorInput(text: String) {
        _uiState.update { it.copy(translatorInput = text) }
    }

    fun selectTranslatorLanguage(lang: TranslatorLanguage) {
        _uiState.update { it.copy(selectedLanguage = lang) }
    }

    fun exportWorkbenchToLean4(workbenchState: UniRythmicsUiState) {
        val row = UniRythmicsEngine.computeBasisRow(
            measureType = workbenchState.measureType,
            precisionMode = workbenchState.precisionMode,
            dodecagonSector = workbenchState.dodecagonSector,
            nthExponent = workbenchState.nthExponent
        )
        val unit = UniRythmicsCatalog.findUnitById(workbenchState.primaryUnitId)
        val compUnit = UniRythmicsCatalog.findUnitById(workbenchState.comparisonUnitId)

        val leanDraft = """
            import Mathlib.Data.Real.Sqrt

            namespace UniRythmics.Calibration

            noncomputable def phiInv : Real := (Real.sqrt 5 - 1) / 2
            noncomputable def gunitFull : Real := ${if (row.measureType.name == "HALF") "2 * phiInv" else "phiInv"}
            noncomputable def gunitShifted : Real := gunitFull * (10 : Real) ^ (-${workbenchState.nthExponent} : Int)
            noncomputable def gunitSector : Real := gunitShifted * (${workbenchState.dodecagonSector} / 12)

            def rolodexMultiplier : Nat := ${workbenchState.multiplierInt}
            noncomputable def primarySiBase : Real := ${unit.siValue}
            noncomputable def primaryScaledTotal : Real := primarySiBase * (rolodexMultiplier : Real)
            noncomputable def comparisonSiBase : Real := ${compUnit.siValue}

            end UniRythmics.Calibration
        """.trimIndent()

        val plainExplain = "auto corrected lean 4 calibration snapshot exported from current workbench state at sector ${workbenchState.dodecagonSector} and rolodex ${UniRythmicsEngine.formatRolodex(workbenchState.multiplierInt)} beta notice always check your code in third party test"
        val bonusPrompt = "create a verified standalone lean 4 module using mathlib real sqrt that proves gunitSector stays positive across sectors 1 to 12 and checks dimensional ratio against ${compUnit.shortName} without unsafe external calls"

        _uiState.update {
            it.copy(
                selectedLanguage = UniRythmicsCatalog.translatorLanguages.first(),
                translatorInput = leanDraft,
                isOutputSheetOpen = true,
                translatorState = TranslatorState.Success(
                    TranslatorOutput(
                        targetLanguageLabel = "LEAN 4 DEFAULT",
                        plainInstructions = plainExplain,
                        mathAndCodeBlock = leanDraft,
                        bonusVibePrompt = bonusPrompt,
                        betaNotice = "beta notice always check your code in third party test"
                    )
                )
            )
        }
    }

    fun loadTextIntoTranslator(leanCode: String) {
        _uiState.update {
            it.copy(
                selectedLanguage = UniRythmicsCatalog.translatorLanguages.first(),
                translatorInput = leanCode,
                isPullDrawerOpen = true
            )
        }
    }

    fun showPlaygroundBundleInOutput(bundleCode: String, verifiedCount: Int) {
        _uiState.update {
            it.copy(
                selectedLanguage = UniRythmicsCatalog.translatorLanguages.first(),
                translatorInput = bundleCode,
                isOutputSheetOpen = true,
                translatorState = TranslatorState.Success(
                    TranslatorOutput(
                        targetLanguageLabel = "SAIR PLAYGROUND BUNDLE $verifiedCount OF 7",
                        plainInstructions = "all seven lean 4 kernel challenge problems verified ready to paste into sair web playground or copy via share button",
                        mathAndCodeBlock = bundleCode,
                        bonusVibePrompt = "run lake build and replay kernel instruction benchmark inside sair lean4 playground without external unsafe axioms or sorry tactics",
                        betaNotice = "beta notice always check your code in third party test"
                    )
                )
            )
        }
    }

    fun runTranslation(workbenchState: UniRythmicsUiState) {
        val raw = _uiState.value.translatorInput.trim()
        if (raw.isEmpty()) return
        val lang = _uiState.value.selectedLanguage

        translatorJob?.cancel()
        translatorJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isOutputSheetOpen = true,
                    translatorState = TranslatorState.Loading
                )
            }
            try {
                val prompt = buildTranslatorPrompt(raw, lang, workbenchState)
                val response = withTimeout(60_000L) {
                    translatorModel.generateContent(prompt)
                }
                val jsonText = response.text
                if (jsonText.isNullOrBlank()) {
                    _uiState.update {
                        it.copy(translatorState = TranslatorState.Error("gemini returned an empty translation"))
                    }
                    return@launch
                }
                val json = JSONObject(jsonText)
                val instructions = sanitizePlainProse(json.optString("plainInstructions", ""))
                val codeBlock = json.optString("mathAndCodeBlock", "")
                val bonusPrompt = sanitizePlainProse(json.optString("bonusVibePrompt", ""))

                _uiState.update {
                    it.copy(
                        translatorState = TranslatorState.Success(
                            TranslatorOutput(
                                targetLanguageLabel = lang.label,
                                plainInstructions = instructions,
                                mathAndCodeBlock = codeBlock,
                                bonusVibePrompt = bonusPrompt,
                                betaNotice = "beta notice always check your code in third party test"
                            )
                        )
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Translator request timed out", e)
                _uiState.update {
                    it.copy(translatorState = TranslatorState.Error("translation timed out after 60 seconds"))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Translator request failed", e)
                _uiState.update {
                    it.copy(
                        translatorState = TranslatorState.Error(
                            sanitizePlainProse(e.localizedMessage ?: "translation request failed")
                        )
                    )
                }
            }
        }
    }

    fun updatePromptInput(text: String) {
        _uiState.update { it.copy(promptInput = text) }
    }

    fun selectMode(mode: GeminiMode) {
        _uiState.update { it.copy(mode = mode) }
    }

    fun toggleIncludeContext(enabled: Boolean) {
        _uiState.update { it.copy(includeWorkbenchContext = enabled) }
    }

    fun cancelGeneration() {
        activeJob?.cancel()
        activeJob = null
        _uiState.update { state ->
            val current = state.responseState
            val next = if (current is AiResponseState.Streaming && current.text.isNotBlank()) {
                AiResponseState.SuccessText(current.text)
            } else {
                AiResponseState.Idle
            }
            state.copy(responseState = next)
        }
    }

    fun clearResponse() {
        activeJob?.cancel()
        activeJob = null
        _uiState.update { it.copy(responseState = AiResponseState.Idle) }
    }

    fun submitPrompt(workbenchState: UniRythmicsUiState, overridePrompt: String? = null) {
        val rawPrompt = (overridePrompt ?: _uiState.value.promptInput).trim()
        if (rawPrompt.isEmpty()) return

        if (overridePrompt != null) {
            _uiState.update { it.copy(promptInput = overridePrompt) }
        }

        val fullPrompt = buildFullPrompt(rawPrompt, workbenchState, _uiState.value.includeWorkbenchContext)
        val selectedMode = _uiState.value.mode

        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            _uiState.update { it.copy(responseState = AiResponseState.Loading) }
            try {
                when (selectedMode) {
                    GeminiMode.STREAMING_QA -> runStreaming(fullPrompt)
                    GeminiMode.STRUCTURED_PROOF -> runStructured(fullPrompt)
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Gemini AI request timed out", e)
                _uiState.update {
                    it.copy(
                        responseState = AiResponseState.Error(
                            "request timed out after 60 seconds please try again"
                        )
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Gemini AI request failed", e)
                _uiState.update {
                    it.copy(
                        responseState = AiResponseState.Error(
                            sanitizePlainProse(e.localizedMessage ?: "gemini ai request failed")
                        )
                    )
                }
            }
        }
    }

    private suspend fun runStreaming(prompt: String) {
        val accumulated = StringBuilder()
        withTimeout(60_000L) {
            streamingModel.generateContentStream(prompt).collect { chunk ->
                val chunkText = chunk.text.orEmpty()
                if (chunkText.isNotEmpty()) {
                    accumulated.append(chunkText)
                    _uiState.update {
                        it.copy(responseState = AiResponseState.Streaming(accumulated.toString()))
                    }
                }
            }
        }
        val finalResult = accumulated.toString()
        if (finalResult.isNotBlank()) {
            _uiState.update { it.copy(responseState = AiResponseState.SuccessText(finalResult)) }
        } else {
            _uiState.update {
                it.copy(responseState = AiResponseState.Error("gemini returned an empty response"))
            }
        }
    }

    private suspend fun runStructured(prompt: String) {
        val response = withTimeout(60_000L) {
            structuredModel.generateContent(prompt)
        }
        val jsonText = response.text
        if (jsonText.isNullOrBlank()) {
            _uiState.update {
                it.copy(responseState = AiResponseState.Error("gemini returned an empty structured payload"))
            }
            return
        }

        val json = JSONObject(jsonText)
        val summary = sanitizePlainProse(json.optString("summary", ""))
        val dimensionalBreakdown = json.optString("dimensionalBreakdown", "")
        val precisionNote = sanitizePlainProse(json.optString("precisionNote", ""))
        val stepsArray = json.optJSONArray("calibrationSteps")
        val steps = if (stepsArray != null) {
            (0 until stepsArray.length()).mapNotNull { idx ->
                sanitizePlainProse(stepsArray.optString(idx)).takeIf { it.isNotBlank() }
            }
        } else {
            emptyList()
        }

        _uiState.update {
            it.copy(
                responseState = AiResponseState.SuccessStructured(
                    StructuredMetrologyAnalysis(
                        summary = summary,
                        dimensionalBreakdown = dimensionalBreakdown,
                        calibrationSteps = steps,
                        precisionNote = precisionNote
                    )
                )
            )
        }
    }

    private fun buildTranslatorPrompt(
        input: String,
        targetLang: TranslatorLanguage,
        state: UniRythmicsUiState
    ): String {
        val row = UniRythmicsEngine.computeBasisRow(
            measureType = state.measureType,
            precisionMode = state.precisionMode,
            dodecagonSector = state.dodecagonSector,
            nthExponent = state.nthExponent
        )
        return """
            Task: Translate and auto-correct the user input into ${targetLang.label}.
            Rules:
            1. If target is LEAN 4 DEFAULT, auto-correct the input for valid Lean 4 v4.12.0 syntax.
            2. If target is HTML TO APK or NO CODE PROMPT ONLY or no coding language applies cleanly, output a practical conversion/prompt design in bonusVibePrompt and plainInstructions, plus safe starter structure in mathAndCodeBlock.
            3. Always include a bonus vibe code prompt in bonusVibePrompt for clarity or Java injection malware safety precautions.
            4. In plainInstructions and bonusVibePrompt: use ONLY lowercase plain text free of markdown symbols (#, *, **, _, __, quotes, parentheses, dashes, periods, emojis).
            5. Keep math equations and source code inside mathAndCodeBlock only.
            Current Uni-Rythmics reference: 1 Gunit = ${UniRythmicsEngine.formatScientificOrDecimal(row.sectorWeightedGunit)}, sector ${state.dodecagonSector}/12, rolodex ${state.multiplierInt}.
            User Input:
            $input
        """.trimIndent()
    }

    private fun buildFullPrompt(
        userPrompt: String,
        state: UniRythmicsUiState,
        includeContext: Boolean
    ): String {
        if (!includeContext) return userPrompt
        val row = UniRythmicsEngine.computeBasisRow(
            measureType = state.measureType,
            precisionMode = state.precisionMode,
            dodecagonSector = state.dodecagonSector,
            nthExponent = state.nthExponent
        )
        val primaryUnit = UniRythmicsCatalog.findUnitById(state.primaryUnitId)
        val compUnit = UniRythmicsCatalog.findUnitById(state.comparisonUnitId)

        val contextBlock = """
            Current Uni-Rythmics Workbench Calibration:
            1 Gunit = ${UniRythmicsEngine.formatScientificOrDecimal(row.sectorWeightedGunit)} (${row.title}, sector ${state.dodecagonSector}/12, 10^-${state.nthExponent})
            1 var = ${primaryUnit.name} x ${UniRythmicsEngine.formatRolodex(state.multiplierInt)} = ${UniRythmicsEngine.formatScientificOrDecimal(primaryUnit.siValue * state.multiplierInt)} ${primaryUnit.siUnitSuffix}
            var of comparison = ${compUnit.name} (${UniRythmicsEngine.formatScientificOrDecimal(compUnit.siValue)} ${compUnit.siUnitSuffix})
        """.trimIndent()
        return "$contextBlock\n\nQuestion:\n$userPrompt"
    }

    fun sanitizePlainProse(raw: String): String {
        return raw
            .replace(Regex("[#*\"_()\\[\\]{}<>~`•—–\\-.]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .lowercase()
    }

    fun splitProseAndMathBlocks(raw: String): List<Pair<Boolean, String>> {
        val result = mutableListOf<Pair<Boolean, String>>()
        val regex = Regex("```[a-zA-Z0-9_+-]*\\n?([\\s\\S]*?)```")
        var lastIdx = 0
        for (match in regex.findAll(raw)) {
            if (match.range.first > lastIdx) {
                val prose = sanitizePlainProse(raw.substring(lastIdx, match.range.first))
                if (prose.isNotBlank()) result.add(false to prose)
            }
            val code = match.groupValues[1].trim()
            if (code.isNotBlank()) result.add(true to code)
            lastIdx = match.range.last + 1
        }
        if (lastIdx < raw.length) {
            val tail = sanitizePlainProse(raw.substring(lastIdx))
            if (tail.isNotBlank()) result.add(false to tail)
        }
        return result
    }
}
