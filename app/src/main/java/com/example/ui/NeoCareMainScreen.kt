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
import com.example.ui.components.PatientSelectorBar
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoCareViewModel

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
                    )
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
                        Triple(3, if (isAr) "المقاييس" else "Scores & Vent", Icons.Default.Assessment),
                        Triple(4, if (isAr) "المرضى" else "Patients", Icons.Default.Groups)
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
                        3 -> ScoresAndAirwayScreen(state = state, viewModel = viewModel)
                        4 -> PatientManagementScreen(state = state, patients = patients, viewModel = viewModel)
                        else -> FluidsGIRScreen(state = state, viewModel = viewModel)
                    }
                }
            }
        }
    }
}
