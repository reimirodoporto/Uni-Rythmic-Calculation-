package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.components.MoonGlassLedButton
import com.example.ui.components.PullTabTopTranslatorDrawer
import com.example.ui.components.SettingsApkDialog
import com.example.ui.components.SlideUpTranslatorOutputSheet
import com.example.ui.screens.ChallengesScreen
import com.example.ui.screens.GeminiLabScreen
import com.example.ui.screens.LeanKernelPracticeScreen
import com.example.ui.screens.ScaleMatrixScreen
import com.example.ui.screens.TaxonomiesScreen
import com.example.ui.screens.WorkbenchScreen
import com.example.ui.theme.UniRythmicsTheme
import kotlinx.coroutines.launch

private data class CarouselPageDest(
    val pageIndex: Int,
    val labelRes: Int,
    val icon: ImageVector,
    val testTag: String
)

private val carouselDestinations = listOf(
    CarouselPageDest(
        pageIndex = 0,
        labelRes = R.string.nav_workbench,
        icon = Icons.Rounded.Tune,
        testTag = "nav_workbench"
    ),
    CarouselPageDest(
        pageIndex = 1,
        labelRes = R.string.nav_matrix,
        icon = Icons.Rounded.GridOn,
        testTag = "nav_matrix"
    ),
    CarouselPageDest(
        pageIndex = 2,
        labelRes = R.string.nav_taxonomies,
        icon = Icons.Rounded.Category,
        testTag = "nav_taxonomies"
    ),
    CarouselPageDest(
        pageIndex = 3,
        labelRes = R.string.nav_challenges,
        icon = Icons.Rounded.EmojiEvents,
        testTag = "nav_challenges"
    ),
    CarouselPageDest(
        pageIndex = 4,
        labelRes = R.string.nav_ai_assist,
        icon = Icons.Rounded.AutoAwesome,
        testTag = "nav_ai_assist"
    ),
    CarouselPageDest(
        pageIndex = 5,
        labelRes = R.string.nav_lean_kernel,
        icon = Icons.Rounded.Terminal,
        testTag = "nav_lean_kernel"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniRythmicsApp(
    viewModel: UniRythmicsViewModel = viewModel(),
    geminiViewModel: GeminiAiViewModel = viewModel(),
    sairKernelViewModel: SairKernelViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val geminiState by geminiViewModel.uiState.collectAsStateWithLifecycle()
    val sairKernelState by sairKernelViewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { carouselDestinations.size })
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    fun scrollToPage(index: Int) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(index)
        }
    }

    BackHandler(
        enabled = uiState.isSettingsOpen || geminiState.isOutputSheetOpen || geminiState.isPullDrawerOpen || pagerState.currentPage > 0
    ) {
        when {
            uiState.isSettingsOpen -> viewModel.closeSettings()
            geminiState.isOutputSheetOpen -> geminiViewModel.dismissOutputSheet()
            geminiState.isPullDrawerOpen -> geminiViewModel.closePullDrawer()
            pagerState.currentPage > 0 -> scrollToPage(0)
        }
    }

    UniRythmicsTheme(darkTheme = uiState.isDarkTheme) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val availableWidth = maxWidth
            val useSideRail = availableWidth >= 840.dp
            val currentPageTitle = stringResource(carouselDestinations[pagerState.currentPage].labelRes)

            // Textured space aurora backdrop with 80% deep purple aurora tint layer
            Image(
                painter = painterResource(id = R.drawable.img_space_aurora),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = if (uiState.isDarkTheme) 0.92f else 0.18f,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (uiState.isDarkTheme) {
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xCC1E0938),
                                    Color(0xCC2C0F54),
                                    Color(0xCC120624)
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xE6F4F0FA),
                                    Color(0xE6ECE5F7),
                                    Color(0xE6F5F3EE)
                                )
                            )
                        }
                    )
            )

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "UNIRYTHMICS",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = currentPageTitle,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        },
                        actions = {
                            MoonGlassLedButton(
                                isOn = false,
                                icon = Icons.Rounded.RestartAlt,
                                contentDescription = stringResource(R.string.reset_calibration),
                                accentColor = MaterialTheme.colorScheme.primary,
                                onClick = { viewModel.resetCalibration() },
                                testTag = "reset_calibration_button"
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            MoonGlassLedButton(
                                isOn = uiState.isDarkTheme,
                                icon = if (uiState.isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                                contentDescription = stringResource(R.string.theme_toggle_cd),
                                accentColor = MaterialTheme.colorScheme.secondary,
                                onClick = { viewModel.toggleTheme() },
                                testTag = "theme_toggle_button"
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            MoonGlassLedButton(
                                isOn = uiState.isSettingsOpen,
                                icon = Icons.Rounded.Settings,
                                contentDescription = "open settings and apk downloads",
                                accentColor = MaterialTheme.colorScheme.tertiary,
                                onClick = { viewModel.openSettings(context) },
                                testTag = "settings_button",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                            titleContentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                bottomBar = {
                    if (!useSideRail) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                carouselDestinations.forEach { dest ->
                                    val selected = pagerState.currentPage == dest.pageIndex
                                    val cd = stringResource(dest.labelRes)
                                    MoonGlassLedButton(
                                        isOn = selected,
                                        icon = dest.icon,
                                        contentDescription = cd,
                                        accentColor = if (dest.pageIndex == 4) {
                                            MaterialTheme.colorScheme.secondary
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        },
                                        onClick = { scrollToPage(dest.pageIndex) },
                                        testTag = dest.testTag,
                                        modifier = Modifier.weight(1f).padding(horizontal = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useSideRail) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                carouselDestinations.forEach { dest ->
                                    val selected = pagerState.currentPage == dest.pageIndex
                                    val cd = stringResource(dest.labelRes)
                                    MoonGlassLedButton(
                                        isOn = selected,
                                        icon = dest.icon,
                                        contentDescription = cd,
                                        accentColor = if (dest.pageIndex == 4) {
                                            MaterialTheme.colorScheme.secondary
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        },
                                        onClick = { scrollToPage(dest.pageIndex) },
                                        testTag = dest.testTag
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        // Top Pull-Tab Translator Input Drawer with Lean 4 Default & DB Language Lookup
                        PullTabTopTranslatorDrawer(
                            geminiState = geminiState,
                            onTogglePullDrawer = geminiViewModel::togglePullDrawer,
                            onInputChange = geminiViewModel::updateTranslatorInput,
                            onSelectLanguage = geminiViewModel::selectTranslatorLanguage,
                            onExportWorkbenchLean4 = {
                                geminiViewModel.exportWorkbenchToLean4(uiState)
                            },
                            onRunTranslate = {
                                geminiViewModel.runTranslation(uiState)
                            },
                            onReopenOutput = geminiViewModel::openOutputSheet
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("feature_carousel_pager")
                            ) { page ->
                                when (page) {
                                    0 -> WorkbenchScreen(
                                        uiState = uiState,
                                        onSetMeasureType = viewModel::setMeasureType,
                                        onSetPrecisionMode = viewModel::setPrecisionMode,
                                        onSelectSector = viewModel::setDodecagonSector,
                                        onSelectExponent = viewModel::setNthExponent,
                                        onSelectPrimaryUnit = viewModel::selectPrimaryUnit,
                                        onStepMultiplierDigit = viewModel::stepMultiplierDigit,
                                        onSetMultiplier = viewModel::setMultiplierInt,
                                        onSetComparisonMode = viewModel::setComparisonMode,
                                        onSelectComparisonUnit = viewModel::selectComparisonUnit,
                                        onComparisonQueryChange = viewModel::updateComparisonQuery,
                                        onToggleLinkComparisonMultiplier = viewModel::toggleLinkComparisonMultiplier,
                                        onStepSecondaryDigit = viewModel::stepSecondaryMultiplierDigit
                                    )

                                    1 -> ScaleMatrixScreen(
                                        uiState = uiState,
                                        onSelectBasisMode = { measureType, precisionMode ->
                                            viewModel.setMeasureType(measureType)
                                            viewModel.setPrecisionMode(precisionMode)
                                            scrollToPage(0)
                                        }
                                    )

                                    2 -> TaxonomiesScreen(
                                        uiState = uiState,
                                        onSelectTaxonomy = viewModel::selectTaxonomy,
                                        onSelectOriginFilter = viewModel::selectOriginFilter,
                                        onWolframQueryChange = viewModel::updateWolframQuery,
                                        onRunWolframLookup = viewModel::runWolframLookup,
                                        onLoadIntoPrimary = { unitId ->
                                            viewModel.loadUnitIntoPrimary(unitId)
                                            scrollToPage(0)
                                        },
                                        onLoadIntoComparison = { unitId ->
                                            viewModel.loadUnitIntoComparison(unitId)
                                            scrollToPage(0)
                                        },
                                        onSendToTranslator = { snippet ->
                                            geminiViewModel.loadTextIntoTranslator(snippet)
                                        }
                                    )

                                    3 -> ChallengesScreen(
                                        uiState = uiState,
                                        onSelectChallenge = viewModel::selectChallenge,
                                        onOpenWorkbench = { scrollToPage(0) }
                                    )

                                    4 -> GeminiLabScreen(
                                        geminiState = geminiState,
                                        splitProseAndMath = geminiViewModel::splitProseAndMathBlocks,
                                        onPromptChange = geminiViewModel::updatePromptInput,
                                        onSelectMode = geminiViewModel::selectMode,
                                        onToggleContext = geminiViewModel::toggleIncludeContext,
                                        onSubmit = { overridePrompt ->
                                            geminiViewModel.submitPrompt(uiState, overridePrompt)
                                        },
                                        onCancel = geminiViewModel::cancelGeneration,
                                        onClear = geminiViewModel::clearResponse
                                    )

                                    else -> LeanKernelPracticeScreen(
                                        uiState = sairKernelState,
                                        onSelectProblem = sairKernelViewModel::selectProblem,
                                        onUpdateDraft = sairKernelViewModel::updateDraftCode,
                                        onLoadVariant = sairKernelViewModel::loadReferenceVariant,
                                        onApplyFix = sairKernelViewModel::applySuggestedFix,
                                        onSubmitUntilCorrect = sairKernelViewModel::submitUntilCorrect,
                                        onSendToTranslator = geminiViewModel::loadTextIntoTranslator,
                                        onPushToPlayground = {
                                            val bundle = sairKernelViewModel.buildPlaygroundBundlePayload()
                                            geminiViewModel.showPlaygroundBundleInOutput(
                                                bundleCode = bundle,
                                                verifiedCount = sairKernelState.verifiedCount
                                            )
                                        }
                                    )
                                }
                            }

                            // Slide-Up Translator Output Sheet from below
                            SlideUpTranslatorOutputSheet(
                                geminiState = geminiState,
                                onDismiss = geminiViewModel::dismissOutputSheet,
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }
                }
            }

            if (uiState.isSettingsOpen) {
                SettingsApkDialog(
                    uiState = uiState,
                    onDismiss = viewModel::closeSettings,
                    onToggleTheme = viewModel::toggleTheme,
                    onResetCalibration = viewModel::resetCalibration,
                    onDownloadLatestApk = { viewModel.downloadLatestApk(context) },
                    onRefreshPreviousApks = { viewModel.refreshPreviousApks(context) },
                    onSelectPreviousApk = viewModel::selectPreviousApk,
                    onPromptEmailApk = viewModel::promptEmailForApk,
                    onDismissEmailPrompt = viewModel::dismissPostDownloadEmailPrompt,
                    onRecipientEmailChange = viewModel::updateRecipientEmail
                )
            }
        }
    }
}
