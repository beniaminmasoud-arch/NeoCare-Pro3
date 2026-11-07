package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.example.ui.components.PatientSelectorBar
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoCareViewModel
import com.example.ui.viewmodel.WorkstationState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeoCareMainScreen(
    viewModel: NeoCareViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val patients by viewModel.allPatients.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val isAr = state.isArabic
    val layoutDirection = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isAr) "نيو كير برو ماستر" else "NeoCare Pro Master",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = TealPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(TealLightContainer, shape = MaterialTheme.shapes.extraSmall)
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "NICU/PICU",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TealPrimary
                                    )
                                }
                            }
                            Text(
                                text = if (isAr) "محطة الحسابات السريرية لحديثي الولادة والأطفال" else "Pediatric & Neonatal Workstation",
                                fontSize = 10.5.sp,
                                color = SlateMuted
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    actions = {
                        // Global Settings Switch: Dark vs Light Theme
                        IconButton(
                            onClick = { viewModel.toggleTheme() },
                            modifier = Modifier.testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (state.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (state.isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                                tint = if (state.isDarkMode) WarnAmber else SlateDark
                            )
                        }

                        // Clinical Calculation Summary Snippet Share Button
                        IconButton(
                            onClick = { viewModel.setShowShareSummaryDialog(true) },
                            modifier = Modifier.testTag("open_summary_snippet_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Summarize,
                                contentDescription = if (isAr) "ملخص الحسابات ومشاركته" else "Clinical Summary Snippet",
                                tint = TealPrimary
                            )
                        }

                        TextButton(
                            onClick = {
                                viewModel.selectTab(3)
                                viewModel.setScoresSubTab(0)
                            },
                            modifier = Modifier.testTag("quick_apgar_action")
                        ) {
                            Text(
                                text = "APGAR",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                color = TealPrimary
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    val tabs = listOf(
                        Triple(0, if (isAr) "السوائل" else "Fluids", Icons.Default.WaterDrop),
                        Triple(1, if (isAr) "الأدوية" else "Drugs", Icons.Default.Medication),
                        Triple(2, if (isAr) "الصفراء" else "Bilirubin", Icons.Default.LightMode),
                        Triple(3, if (isAr) "المقاييس" else "Scores", Icons.Default.Assessment),
                        Triple(4, if (isAr) "النمو" else "Growth", Icons.Default.Timeline),
                        Triple(5, if (isAr) "المرضى" else "Patients", Icons.Default.Groups)
                    )

                    tabs.forEach { (index, label, icon) ->
                        val selected = state.selectedTab == index
                        NavigationBarItem(
                            selected = selected,
                            onClick = { viewModel.selectTab(index) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (selected) TealPrimary else SlateMuted
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) TealPrimary else SlateMuted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = TealLightContainer
                            ),
                            modifier = Modifier.testTag("nav_tab_$index")
                        )
                    }
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(SlateBg)
            ) {
                // Persistent Active Neonate Bar on top across all calculator tabs
                PatientSelectorBar(
                    patient = state.activePatient,
                    isArabic = isAr,
                    onSwitchPatientClick = { viewModel.selectTab(4) }, // navigate to Patients tab
                    onLanguageToggle = { viewModel.toggleLanguage() }
                )

                // Animated Tab Content Switching
                AnimatedContent(
                    targetState = state.selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_transition",
                    modifier = Modifier.weight(1f)
                ) { targetTab ->
                    when (targetTab) {
                        0 -> FluidsGIRScreen(state = state, viewModel = viewModel)
                        1 -> DrugDosingScreen(state = state, viewModel = viewModel)
                        2 -> BilirubinScreen(state = state, viewModel = viewModel)
                        3 -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Sub-tab Switcher between APGAR, Silverman, and Vent & Airway
                                TabRow(
                                    selectedTabIndex = state.scoresSubTab,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = TealPrimary,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Tab(
                                        selected = state.scoresSubTab == 0,
                                        onClick = { viewModel.setScoresSubTab(0) },
                                        text = {
                                            Text(
                                                text = if (isAr) "أبغار (APGAR)" else "APGAR Score",
                                                fontWeight = if (state.scoresSubTab == 0) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp
                                            )
                                        },
                                        icon = {
                                            Icon(
                                                Icons.Default.Assessment,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        modifier = Modifier.testTag("tab_sub_apgar")
                                    )
                                    Tab(
                                        selected = state.scoresSubTab == 1,
                                        onClick = { viewModel.setScoresSubTab(1) },
                                        text = {
                                            Text(
                                                text = if (isAr) "سيلفرمان (Silverman)" else "Silverman Score",
                                                fontWeight = if (state.scoresSubTab == 1) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp
                                            )
                                        },
                                        icon = {
                                            Icon(
                                                Icons.Default.Air,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        modifier = Modifier.testTag("tab_sub_silverman")
                                    )
                                    Tab(
                                        selected = state.scoresSubTab == 2,
                                        onClick = { viewModel.setScoresSubTab(2) },
                                        text = {
                                            Text(
                                                text = if (isAr) "الغازات والتنفس (Vent)" else "ABG & Airway",
                                                fontWeight = if (state.scoresSubTab == 2) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp
                                            )
                                        },
                                        icon = {
                                            Icon(
                                                Icons.Default.MedicalServices,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        modifier = Modifier.testTag("tab_sub_vent_airway")
                                    )
                                }

                                when (state.scoresSubTab) {
                                    0 -> ApgarScreen(state = state, viewModel = viewModel, modifier = Modifier.weight(1f))
                                    1 -> SilvermanScreen(state = state, viewModel = viewModel, modifier = Modifier.weight(1f))
                                    else -> ScoresAndAirwayScreen(state = state, viewModel = viewModel, modifier = Modifier.weight(1f))
                                 }
                             }
                         }
                         4 -> GrowthPercentileScreen(state = state, viewModel = viewModel)
                         5 -> PatientManagementScreen(state = state, patients = patients, viewModel = viewModel)
                         else -> FluidsGIRScreen(state = state, viewModel = viewModel)
                     }
                 }
             }

             // Calculation Summary Snippet Modal Dialog for Email / Messaging Sharing
             if (state.showShareSummaryDialog) {
                 ClinicalSummarySnippetDialog(
                     state = state,
                     viewModel = viewModel,
                     onDismiss = { viewModel.setShowShareSummaryDialog(false) }
                 )
             }
         }
     }
 }

@Composable
fun ClinicalSummarySnippetDialog(
    state: WorkstationState,
    viewModel: NeoCareViewModel,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isAr = state.isArabic
    val snippetText = remember(state.activePatient, state.weightInputKg, state.customDeficitPercent, state.apgarInput, state.silvermanInput, state.bilirubinInput) {
        viewModel.generateClinicalSummarySnippet()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Summarize, contentDescription = null, tint = TealPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAr) "ملخص الحسابات السريرية (Text Snippet)" else "Calculation Summary Snippet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isAr)
                        "ملخص فوري لجميع الحسابات (أبغار، السوائل، الجرعات، الصفراء) جاهز للإرسال الفوري عبر البريد الإلكتروني أو واتساب أو تطبيقات المراسلة الطبية:"
                    else
                        "Instant formatted calculation snippet (APGAR, fluids, dosing, bilirubin) ready for sharing via email, WhatsApp, or hospital messaging apps:",
                    fontSize = 11.sp,
                    color = SlateMuted
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateBg)
                        .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    androidx.compose.foundation.lazy.LazyColumn {
                        item {
                            Text(
                                text = snippetText,
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = SlateDark,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sendIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, snippetText)
                        type = "text/plain"
                    }
                    val shareChooser = android.content.Intent.createChooser(
                        sendIntent,
                        if (isAr) "إرسال ملخص الحسابات عبر..." else "Share Calculation Snippet via..."
                    )
                    context.startActivity(shareChooser)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = if (isAr) "إرسال ومشاركة" else "Share Snippet", fontSize = 12.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Clinical Summary", snippetText))
                    viewModel.showSnackbar(if (isAr) "تم نسخ الملخص السريري بنجاح" else "Clinical summary snippet copied")
                    onDismiss()
                }
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = if (isAr) "نسخ النص" else "Copy Text", fontSize = 12.sp)
            }
        }
    )
}
