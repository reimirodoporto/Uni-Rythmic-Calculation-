package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.SairKernelCatalog
import com.example.domain.SairKernelProblem
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

private const val KERNEL_TAG = "SairKernelViewModel"
private const val KERNEL_MODEL = "gemini-3.8-flash"

data class ProblemAttemptState(
    val draftCode: String,
    val isVerified: Boolean = false,
    val attemptCount: Int = 0,
    val statusNote: String = "pending submit til correct verification",
    val suggestedFixCode: String? = null,
    val bonusVibePrompt: String? = null
)

sealed interface KernelVerifyStatus {
    data object Idle : KernelVerifyStatus
    data class Verifying(val problemId: String) : KernelVerifyStatus
    data class NeedsRetry(
        val problemId: String,
        val feedback: String,
        val fixedCode: String?,
        val bonusPrompt: String
    ) : KernelVerifyStatus

    data class Passed(
        val problemId: String,
        val feedback: String,
        val bonusPrompt: String
    ) : KernelVerifyStatus
}

data class SairKernelUiState(
    val selectedProblemIndex: Int = 0,
    val attempts: Map<String, ProblemAttemptState> = SairKernelCatalog.problems.associate { problem ->
        problem.id to ProblemAttemptState(draftCode = problem.referenceCode)
    },
    val verifyStatus: KernelVerifyStatus = KernelVerifyStatus.Idle
) {
    val activeProblem: SairKernelProblem
        get() = SairKernelCatalog.problems.getOrElse(selectedProblemIndex) { SairKernelCatalog.problems.first() }

    val verifiedCount: Int
        get() = attempts.values.count { it.isVerified }

    val allSevenVerified: Boolean
        get() = verifiedCount == SairKernelCatalog.problems.size
}

class SairKernelViewModel : ViewModel() {

    private val kernelCheckSchema = Schema.obj(
        properties = mapOf(
            "isCorrect" to Schema.boolean(
                description = "true only if Lean 4 kernel code defines namespace Submission, def impl, theorem impl_correct with 0 sorry/admit/axioms and mathematically valid termination and proof"
            ),
            "plainFeedback" to Schema.string(
                description = "lowercase plain text feedback with NO periods dashes parentheses hashtags asterisks underscores quotes italics or emojis"
            ),
            "correctedLeanCode" to Schema.string(
                description = "clean UTF-8 Lean 4 v4.12 code inside namespace Submission fixing any syntax proof or kernel performance issues"
            ),
            "bonusVibePrompt" to Schema.string(
                description = "lowercase plain text bonus prompt for further Lean 4 kernel replay step reduction without unsafe code"
            )
        )
    )

    private val verifierModel by lazy {
        Firebase.ai.generativeModel(
            modelName = KERNEL_MODEL,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = kernelCheckSchema
            },
            systemInstruction = content {
                text(
                    """
                    You are a strict Lean 4 kernel verifier for the SAIR Lean Kernel Challenge Stage 1 (7 problems: fib, partition, mertens, primecount, permanent, ca-rule110, sha256).
                    Verify Lean 4 syntax, namespace Submission, def impl, theorem impl_correct, termination, and absence of sorry/admit/unsafe/axioms.
                    Formatting rule: plainFeedback and bonusVibePrompt MUST be strictly lowercase plain text with NO periods, NO dashes, NO parentheses, NO markdown (#, *, **, _, __, quotes), NO italics, and NO emojis.
                    """.trimIndent()
                )
            }
        )
    }

    private val _uiState = MutableStateFlow(SairKernelUiState())
    val uiState: StateFlow<SairKernelUiState> = _uiState.asStateFlow()

    private var verifyJob: Job? = null

    fun selectProblem(index: Int) {
        val safe = index.coerceIn(0, SairKernelCatalog.problems.lastIndex)
        _uiState.update { it.copy(selectedProblemIndex = safe) }
    }

    fun updateDraftCode(problemId: String, newCode: String) {
        _uiState.update { state ->
            val current = state.attempts[problemId] ?: return@update state
            val updated = current.copy(
                draftCode = newCode,
                isVerified = false,
                statusNote = "edited awaiting submit til correct check"
            )
            state.copy(attempts = state.attempts + (problemId to updated))
        }
    }

    fun loadReferenceVariant(problemId: String, useAlternate: Boolean) {
        val problem = SairKernelCatalog.problems.firstOrNull { it.id == problemId } ?: return
        val code = if (useAlternate && problem.alternateCode != null) {
            problem.alternateCode
        } else {
            problem.referenceCode
        }
        _uiState.update { state ->
            val current = state.attempts[problemId] ?: ProblemAttemptState(code)
            val updated = current.copy(
                draftCode = code,
                isVerified = false,
                suggestedFixCode = null,
                statusNote = "loaded reference example ready to verify"
            )
            state.copy(attempts = state.attempts + (problemId to updated))
        }
    }

    fun applySuggestedFix(problemId: String) {
        _uiState.update { state ->
            val current = state.attempts[problemId] ?: return@update state
            val fix = current.suggestedFixCode ?: return@update state
            val updated = current.copy(
                draftCode = fix,
                isVerified = false,
                statusNote = "applied auto fix tap submit to verify"
            )
            state.copy(attempts = state.attempts + (problemId to updated))
        }
    }

    fun submitUntilCorrect(problemId: String) {
        val problem = SairKernelCatalog.problems.firstOrNull { it.id == problemId } ?: return
        val currentAttempt = _uiState.value.attempts[problemId] ?: return
        val draft = currentAttempt.draftCode

        // Step 1: Deterministic local Lean 4 kernel sanity gate
        val sanity = SairKernelCatalog.runStaticKernelSanityCheck(draft)
        if (!sanity.passed) {
            val cleaned = SairKernelCatalog.cleanMojibake(draft)
            val msg = sanitizeProse(sanity.issues.joinToString(" "))
            val bonus = "clean utf8 symbols and ensure namespace Submission wraps def impl and theorem impl_correct with zero sorry tactics"
            _uiState.update { state ->
                val updated = currentAttempt.copy(
                    isVerified = false,
                    attemptCount = currentAttempt.attemptCount + 1,
                    statusNote = msg,
                    suggestedFixCode = if (cleaned != draft) cleaned else currentAttempt.suggestedFixCode,
                    bonusVibePrompt = bonus
                )
                state.copy(
                    attempts = state.attempts + (problemId to updated),
                    verifyStatus = KernelVerifyStatus.NeedsRetry(
                        problemId = problemId,
                        feedback = msg,
                        fixedCode = if (cleaned != draft) cleaned else null,
                        bonusPrompt = bonus
                    )
                )
            }
            return
        }

        // Step 2: Gemini 3.8 Flash formal kernel audit
        verifyJob?.cancel()
        verifyJob = viewModelScope.launch {
            _uiState.update {
                it.copy(verifyStatus = KernelVerifyStatus.Verifying(problemId))
            }
            try {
                val prompt = """
                    Audit this Lean 4 submission for SAIR Lean Kernel Challenge Problem ${problem.number}: ${problem.title}.
                    Specification requirement:
                    ${problem.specMathBlock}

                    Submission Code:
                    $draft
                """.trimIndent()

                val response = withTimeout(60_000L) {
                    verifierModel.generateContent(prompt)
                }
                val jsonText = response.text
                if (jsonText.isNullOrBlank()) {
                    markRetry(problemId, "gemini verifier returned empty response retry submit", null, "")
                    return@launch
                }

                val json = JSONObject(jsonText)
                val isCorrect = json.optBoolean("isCorrect", false)
                val feedback = sanitizeProse(json.optString("plainFeedback", "verification checked"))
                val correctedCode = json.optString("correctedLeanCode", draft).trim()
                val bonusPrompt = sanitizeProse(json.optString("bonusVibePrompt", ""))

                if (isCorrect) {
                    _uiState.update { state ->
                        val prev = state.attempts[problemId] ?: currentAttempt
                        val updated = prev.copy(
                            isVerified = true,
                            attemptCount = prev.attemptCount + 1,
                            statusNote = feedback,
                            suggestedFixCode = null,
                            bonusVibePrompt = bonusPrompt
                        )
                        state.copy(
                            attempts = state.attempts + (problemId to updated),
                            verifyStatus = KernelVerifyStatus.Passed(
                                problemId = problemId,
                                feedback = feedback,
                                bonusPrompt = bonusPrompt
                            )
                        )
                    }
                } else {
                    markRetry(
                        problemId = problemId,
                        feedback = feedback,
                        fixedCode = correctedCode.takeIf { it.isNotBlank() && it != draft },
                        bonusPrompt = bonusPrompt
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(KERNEL_TAG, "Kernel verify timed out", e)
                markRetry(problemId, "verification timed out after 60 seconds retry submit", null, "")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(KERNEL_TAG, "Kernel verify failed", e)
                markRetry(
                    problemId = problemId,
                    feedback = sanitizeProse(e.localizedMessage ?: "verification failed retry submit"),
                    fixedCode = null,
                    bonusPrompt = ""
                )
            }
        }
    }

    private fun markRetry(
        problemId: String,
        feedback: String,
        fixedCode: String?,
        bonusPrompt: String
    ) {
        _uiState.update { state ->
            val prev = state.attempts[problemId] ?: return@update state
            val updated = prev.copy(
                isVerified = false,
                attemptCount = prev.attemptCount + 1,
                statusNote = feedback,
                suggestedFixCode = fixedCode,
                bonusVibePrompt = bonusPrompt.ifBlank { null }
            )
            state.copy(
                attempts = state.attempts + (problemId to updated),
                verifyStatus = KernelVerifyStatus.NeedsRetry(
                    problemId = problemId,
                    feedback = feedback,
                    fixedCode = fixedCode,
                    bonusPrompt = bonusPrompt
                )
            )
        }
    }

    fun buildPlaygroundBundlePayload(): String {
        val sb = StringBuilder()
        sb.appendLine("-- SAIR LEAN 4 KERNEL CHALLENGE 7 PROBLEM VERIFIED PLAYGROUND BUNDLE")
        SairKernelCatalog.problems.forEach { problem ->
            val attempt = _uiState.value.attempts[problem.id]
            val status = if (attempt?.isVerified == true) "VERIFIED" else "PENDING"
            sb.appendLine("\n-- [${problem.number}/7] ${problem.title} [$status]")
            sb.appendLine(attempt?.draftCode ?: problem.referenceCode)
        }
        return sb.toString().trim()
    }

    private fun sanitizeProse(raw: String): String {
        return raw
            .replace(Regex("[#*\"_()\\[\\]{}<>~`•—–\\-.]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .lowercase()
    }
}
