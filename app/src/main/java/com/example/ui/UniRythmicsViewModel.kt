package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.ApkDownloadResult
import com.example.domain.ApkExportManager
import com.example.domain.ComparisonChannelMode
import com.example.domain.DomainOrigin
import com.example.domain.MeasureType
import com.example.domain.PrecisionMode
import com.example.domain.StoredApkRecord
import com.example.domain.UniRythmicsCatalog
import com.example.domain.WolframAlphaClient
import com.example.domain.WolframLookupState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.pow

data class UniRythmicsUiState(
    val isDarkTheme: Boolean = true,
    val measureType: MeasureType = MeasureType.HALF,
    val precisionMode: PrecisionMode = PrecisionMode.EXACT,
    val dodecagonSector: Int = 12,
    val nthExponent: Int = 0,
    val primaryUnitId: String = "meter",
    val multiplierInt: Int = 1,
    val comparisonMode: ComparisonChannelMode = ComparisonChannelMode.NOTABLE_QUERY,
    val comparisonUnitId: String = "planck_length",
    val comparisonQuery: String = "",
    val linkComparisonMultiplier: Boolean = true,
    val secondaryMultiplierInt: Int = 1,
    val selectedTaxonomyId: String = "si_base",
    val activeChallengeId: String = UniRythmicsCatalog.challenges.first().id,
    val gearRotationAccumulator: Float = 0f,
    val isSettingsOpen: Boolean = false,
    val isDownloadingApk: Boolean = false,
    val previousApks: List<StoredApkRecord> = emptyList(),
    val selectedPreviousApk: StoredApkRecord? = null,
    val postDownloadEmailPromptApk: StoredApkRecord? = null,
    val apkStatusMessage: String? = null,
    val recipientEmailInput: String = "",
    val selectedOriginFilter: DomainOrigin? = null,
    val wolframQuery: String = "",
    val wolframState: WolframLookupState = WolframLookupState.Idle
)

class UniRythmicsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UniRythmicsUiState())
    val uiState: StateFlow<UniRythmicsUiState> = _uiState.asStateFlow()

    fun toggleTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    fun resetCalibration() {
        _uiState.update { current ->
            UniRythmicsUiState(
                isDarkTheme = current.isDarkTheme,
                gearRotationAccumulator = current.gearRotationAccumulator - 45f,
                isSettingsOpen = current.isSettingsOpen,
                previousApks = current.previousApks,
                selectedPreviousApk = current.selectedPreviousApk,
                recipientEmailInput = current.recipientEmailInput
            )
        }
    }

    fun openSettings(context: Context) {
        _uiState.update { it.copy(isSettingsOpen = true) }
        refreshPreviousApks(context)
    }

    fun closeSettings() {
        _uiState.update {
            it.copy(
                isSettingsOpen = false,
                postDownloadEmailPromptApk = null
            )
        }
    }

    fun refreshPreviousApks(context: Context) {
        val appContext = context.applicationContext
        viewModelScope.launch {
            val list = ApkExportManager.listPreviousApkDownloads(appContext)
            _uiState.update { state ->
                val selected = state.selectedPreviousApk?.let { prev ->
                    list.firstOrNull { it.id == prev.id }
                } ?: list.firstOrNull()
                state.copy(
                    previousApks = list,
                    selectedPreviousApk = selected
                )
            }
        }
    }

    fun downloadLatestApk(context: Context) {
        if (_uiState.value.isDownloadingApk) return
        val appContext = context.applicationContext
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDownloadingApk = true,
                    apkStatusMessage = "writing latest apk directly to storage"
                )
            }
            when (val result = ApkExportManager.downloadLatestApkToStorage(appContext)) {
                is ApkDownloadResult.Success -> {
                    val updatedList = ApkExportManager.listPreviousApkDownloads(appContext)
                    _uiState.update { state ->
                        state.copy(
                            isDownloadingApk = false,
                            previousApks = updatedList,
                            selectedPreviousApk = result.record,
                            postDownloadEmailPromptApk = result.record,
                            apkStatusMessage = result.message
                        )
                    }
                }

                is ApkDownloadResult.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            isDownloadingApk = false,
                            apkStatusMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun selectPreviousApk(record: StoredApkRecord) {
        _uiState.update { it.copy(selectedPreviousApk = record) }
    }

    fun promptEmailForApk(record: StoredApkRecord) {
        _uiState.update { it.copy(postDownloadEmailPromptApk = record) }
    }

    fun dismissPostDownloadEmailPrompt() {
        _uiState.update { it.copy(postDownloadEmailPromptApk = null) }
    }

    fun updateRecipientEmail(email: String) {
        _uiState.update { it.copy(recipientEmailInput = email) }
    }

    fun selectOriginFilter(origin: DomainOrigin?) {
        _uiState.update { it.copy(selectedOriginFilter = origin) }
    }

    fun updateWolframQuery(query: String) {
        _uiState.update { it.copy(wolframQuery = query) }
    }

    fun runWolframLookup(overrideQuery: String? = null) {
        val query = (overrideQuery ?: _uiState.value.wolframQuery).trim()
        if (query.isEmpty()) return
        if (overrideQuery != null) {
            _uiState.update { it.copy(wolframQuery = overrideQuery) }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(wolframState = WolframLookupState.Loading) }
            val result = WolframAlphaClient.queryLookup(query)
            _uiState.update { it.copy(wolframState = result) }
        }
    }

    fun setMeasureType(type: MeasureType) {
        _uiState.update {
            it.copy(
                measureType = type,
                gearRotationAccumulator = it.gearRotationAccumulator + 15f
            )
        }
    }

    fun setPrecisionMode(mode: PrecisionMode) {
        _uiState.update {
            it.copy(
                precisionMode = mode,
                gearRotationAccumulator = it.gearRotationAccumulator + 15f
            )
        }
    }

    fun setDodecagonSector(sector: Int) {
        val safe = sector.coerceIn(1, 12)
        _uiState.update {
            val delta = (safe - it.dodecagonSector) * 30f
            it.copy(
                dodecagonSector = safe,
                gearRotationAccumulator = it.gearRotationAccumulator + if (delta == 0f) 10f else delta
            )
        }
    }

    fun setNthExponent(exponent: Int) {
        val safe = exponent.coerceIn(0, 9)
        _uiState.update {
            it.copy(
                nthExponent = safe,
                gearRotationAccumulator = it.gearRotationAccumulator + 18f
            )
        }
    }

    fun selectPrimaryUnit(unitId: String) {
        _uiState.update {
            it.copy(
                primaryUnitId = unitId,
                gearRotationAccumulator = it.gearRotationAccumulator + 24f
            )
        }
    }

    fun setMultiplierInt(value: Int) {
        val clamped = value.coerceIn(1, 999_999)
        _uiState.update {
            val stepDelta = if (clamped >= it.multiplierInt) 12f else -12f
            it.copy(
                multiplierInt = clamped,
                gearRotationAccumulator = it.gearRotationAccumulator + stepDelta
            )
        }
    }

    fun stepMultiplierDigit(placeFromRight: Int, delta: Int) {
        val power = 10.0.pow(placeFromRight.coerceIn(0, 5)).toInt()
        _uiState.update {
            val next = (it.multiplierInt + delta * power).coerceIn(1, 999_999)
            it.copy(
                multiplierInt = next,
                gearRotationAccumulator = it.gearRotationAccumulator + (delta * 14f)
            )
        }
    }

    fun setComparisonMode(mode: ComparisonChannelMode) {
        _uiState.update { it.copy(comparisonMode = mode) }
    }

    fun selectComparisonUnit(unitId: String) {
        _uiState.update {
            it.copy(
                comparisonUnitId = unitId,
                gearRotationAccumulator = it.gearRotationAccumulator + 12f
            )
        }
    }

    fun updateComparisonQuery(query: String) {
        _uiState.update { it.copy(comparisonQuery = query) }
    }

    fun toggleLinkComparisonMultiplier(linked: Boolean) {
        _uiState.update { it.copy(linkComparisonMultiplier = linked) }
    }

    fun stepSecondaryMultiplierDigit(placeFromRight: Int, delta: Int) {
        val power = 10.0.pow(placeFromRight.coerceIn(0, 5)).toInt()
        _uiState.update {
            val next = (it.secondaryMultiplierInt + delta * power).coerceIn(1, 999_999)
            it.copy(
                secondaryMultiplierInt = next,
                gearRotationAccumulator = it.gearRotationAccumulator + (delta * 10f)
            )
        }
    }

    fun selectTaxonomy(id: String) {
        _uiState.update { it.copy(selectedTaxonomyId = id) }
    }

    fun selectChallenge(id: String) {
        _uiState.update { it.copy(activeChallengeId = id) }
    }

    fun loadUnitIntoPrimary(unitId: String) {
        _uiState.update {
            it.copy(
                primaryUnitId = unitId,
                gearRotationAccumulator = it.gearRotationAccumulator + 24f
            )
        }
    }

    fun loadUnitIntoComparison(unitId: String) {
        _uiState.update {
            it.copy(
                comparisonMode = ComparisonChannelMode.SECONDARY_SCALABILITY,
                comparisonUnitId = unitId,
                gearRotationAccumulator = it.gearRotationAccumulator + 24f
            )
        }
    }
}
