package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.ClinicalFormulas
import com.example.ui.components.ClinicalCard
import com.example.ui.components.InteractivePatientWeightAgeAdjuster
import com.example.ui.components.SectionHeader
import com.example.ui.components.UnitInputField
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoCareViewModel
import com.example.ui.viewmodel.WorkstationState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluidsGIRScreen(
    state: WorkstationState,
    viewModel: NeoCareViewModel,
    modifier: Modifier = Modifier
) {
    val isAr = state.isArabic
    val weightKg = state.weightInputKg.toDoubleOrNull() ?: state.activePatient.weightKg
    val dol = state.dayOfLifeInput.toIntOrNull() ?: state.activePatient.dayOfLife

    // === PEDIATRIC HOLLIDAY-SEGAR & DEFICIT CALCULATIONS ===
    val hollidaySegar = remember(weightKg) {
        ClinicalFormulas.calculateHollidaySegar(weightKg)
    }

    val deficitPercent = state.customDeficitPercent.toDoubleOrNull() ?: state.dehydrationSeverity.defaultPercent
    val bolusGiven = state.resuscitationBolusGivenMl.toDoubleOrNull() ?: 0.0
    val ongoingLoss = state.ongoingLossesMl.toDoubleOrNull() ?: 0.0

    val deficitPlan = remember(weightKg, deficitPercent, bolusGiven, ongoingLoss) {
        ClinicalFormulas.calculateDehydrationDeficit(
            weightKg = weightKg,
            dehydrationPercent = deficitPercent,
            bolusGivenMl = bolusGiven,
            ongoingLossesMl = ongoingLoss
        )
    }

    // === NEONATAL TFI & GIR CALCULATIONS ===
    val recTfi = ClinicalFormulas.calculateRecommendedTFI(
        birthWeightGrams = state.activePatient.birthWeightGrams,
        dayOfLife = dol,
        onPhototherapy = state.isPhototherapy,
        onRadiantWarmer = state.isRadiantWarmer
    )
    val currentTfi = state.customTfiInput.toDoubleOrNull() ?: recTfi.toDouble()
    val dailyNeonatalVolume = ClinicalFormulas.calculateDailyFluidVolume(weightKg, currentTfi)
    val hourlyNeonatalIvRate = ClinicalFormulas.calculateHourlyRate(dailyNeonatalVolume)

    val dextrosePct = state.dextrosePercentInput.toDoubleOrNull() ?: 10.0
    val calculatedGir = ClinicalFormulas.calculateGIR(hourlyNeonatalIvRate, dextrosePct, weightKg)

    val desiredGir = state.targetGirInput.toDoubleOrNull() ?: 6.0
    val neededRateForTargetGir = ClinicalFormulas.calculateRateFromGIR(desiredGir, dextrosePct, weightKg)
    val neededDextroseForCurrentRate = ClinicalFormulas.calculateDextrosePercentFromGIR(desiredGir, hourlyNeonatalIvRate, weightKg)

    val hypoBolusMl = ClinicalFormulas.calculateHypoBolusD10W(weightKg)
    val naDailyMeq = weightKg * 3.0
    val kDailyMeq = weightKg * 1.5
    val caGluconateDailyMl = weightKg * 1.5

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        // === 1. INTERACTIVE LIVE PATIENT WEIGHT & AGE ADJUSTER ===
        item {
            Spacer(modifier = Modifier.height(10.dp))
            InteractivePatientWeightAgeAdjuster(
                state = state,
                viewModel = viewModel,
                isArabic = isAr
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // === 2. CLINICAL MODULE MODE TABS ===
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = SlateBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FluidModuleMode.values().forEach { mode ->
                        val isSelected = state.fluidModuleMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) TealPrimary else Color.Transparent)
                                .clickable { viewModel.setFluidModuleMode(mode) }
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isAr) mode.labelAr else mode.labelEn,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else SlateDark,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // =========================================================================
        // === TAB 1: PEDIATRIC HOLLIDAY-SEGAR MAINTENANCE & DEFICIT REPLACEMENT ===
        // =========================================================================
        if (state.fluidModuleMode == FluidModuleMode.HOLLIDAY_SEGAR) {
            // --- Section 1: Holliday-Segar Maintenance Fluid ---
            item {
                SectionHeader(
                    title = if (isAr) "سوائل الصيانة اليومية (Holliday-Segar Formula)" else "Daily Maintenance Fluids (Holliday-Segar)",
                    subtitle = if (isAr) "حساب السوائل اليومية ومعدل الضخ بالساعة بناءً على وزن الطفل بالكيلوجرام" else "24-hr volume & hourly IV rate based on child weight in kg",
                    icon = Icons.Default.WaterDrop
                )

                ClinicalCard(
                    title = if (isAr) "معدل سوائل الصيانة لوزن ${String.format(Locale.US, "%.1f", weightKg)} كجم" else "Maintenance Rate for ${String.format(Locale.US, "%.1f", weightKg)} kg",
                    badgeText = "${String.format(Locale.US, "%.1f", hollidaySegar.hourlyMaintenanceRate)} mL/hr",
                    alertLevel = AlertLevel.SAFE
                ) {
                    // Summary strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateBg)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = if (isAr) "الإجمالي لـ 24 ساعة" else "24-hr Total", fontSize = 10.5.sp, color = SlateMuted)
                            Text(text = "${hollidaySegar.dailyMaintenanceMl} mL", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateDark)
                            Text(text = "mL / day", fontSize = 9.5.sp, color = SlateMuted)
                        }

                        Divider(modifier = Modifier.height(34.dp).width(1.dp), color = SlateBorder)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = if (isAr) "معدل الضخ بالساعة" else "Hourly Rate", fontSize = 10.5.sp, color = SlateMuted)
                            Text(text = "${hollidaySegar.hourlyMaintenanceRate}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TealPrimary)
                            Text(text = "mL / hr", fontSize = 9.5.sp, color = TealPrimary)
                        }

                        Divider(modifier = Modifier.height(34.dp).width(1.dp), color = SlateBorder)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = if (isAr) "لكل شفت (8 ساعات)" else "8-hr Shift", fontSize = 10.5.sp, color = SlateMuted)
                            Text(text = "${hollidaySegar.shift8HourMl} mL", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CyanAccent)
                            Text(text = "mL / 8h", fontSize = 9.5.sp, color = SlateMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Calculation Breakdown Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TealLightContainer.copy(alpha = 0.5f))
                            .border(0.8.dp, TealPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Functions, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isAr) "تفاصيل حساب هوليداي-سيجار (قاعدة 100/50/20 وقاعدة 4-2-1):" else "Formula Calculation Breakdown (100/50/20 & 4-2-1 Rule):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = TealPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isAr) hollidaySegar.rule421DescAr else hollidaySegar.rule421DescEn,
                                fontSize = 11.sp,
                                color = SlateDark,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isAr)
                                    "• أول 10 كجم: 100 مل/كجم/يوم (4 مل/كجم/ساعة)\n• ثاني 10 كجم (11-20 كجم): 50 مل/كجم/يوم (2 مل/كجم/ساعة)\n• فوق 20 كجم: 20 مل/كجم/يوم (1 مل/كجم/ساعة)"
                                else
                                    "• First 10 kg: 100 mL/kg/day (4 mL/kg/hr)\n• 11 - 20 kg: + 50 mL/kg/day (+ 2 mL/kg/hr)\n• Above 20 kg: + 20 mL/kg/day (+ 1 mL/kg/hr)",
                                fontSize = 10.sp,
                                color = SlateMuted,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // --- Section 2: Dehydration Deficit Replacement ---
            item {
                SectionHeader(
                    title = if (isAr) "تعويض عجز الجفاف (Dehydration Deficit & Replacement)" else "Dehydration Deficit Replacement",
                    subtitle = if (isAr) "تقييم درجة الجفاف، جرعة إنعاش الصدمة، وجدول التعويض على مدار 24 ساعة" else "Degree of dehydration, shock boluses, and 24-hr replacement plan",
                    icon = Icons.Default.LocalHospital
                )

                ClinicalCard(
                    title = if (isAr) "بيانات ودرجة الجفاف السريري" else "Dehydration Assessment",
                    badgeText = "${deficitPlan.dehydrationPercent}% Deficit",
                    alertLevel = deficitPlan.alertLevel
                ) {
                    // Severity selector chips
                    Text(
                        text = if (isAr) "اختر درجة الجفاف السريرية:" else "Clinical Dehydration Severity:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateMuted
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DehydrationSeverity.values().forEach { sev ->
                            val isSel = state.dehydrationSeverity == sev
                            val chipBg = when {
                                !isSel -> SlateBg
                                sev == DehydrationSeverity.SEVERE -> DangerRed
                                sev == DehydrationSeverity.MODERATE -> WarnAmber
                                else -> TealPrimary
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(chipBg)
                                    .border(0.8.dp, if (isSel) chipBg else SlateBorder, RoundedCornerShape(6.dp))
                                    .clickable { viewModel.setDehydrationSeverity(sev) }
                                    .padding(vertical = 6.dp, horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isAr) sev.labelAr else sev.labelEn,
                                    fontSize = 9.5.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.White else SlateDark,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UnitInputField(
                            value = state.customDeficitPercent,
                            onValueChange = { viewModel.updateCustomDeficitPercent(it) },
                            label = if (isAr) "نسبة العجز المئوية (%)" else "Deficit %",
                            unit = "%",
                            modifier = Modifier.weight(1f),
                            testTag = "deficit_percent_input"
                        )

                        UnitInputField(
                            value = state.ongoingLossesMl,
                            onValueChange = { viewModel.updateOngoingLossesMl(it) },
                            label = if (isAr) "فقدان مستمر (قيء/إسهال)" else "Ongoing Losses",
                            unit = "mL",
                            modifier = Modifier.weight(1f),
                            testTag = "ongoing_losses_input"
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Resuscitation Bolus Emergency Box (if severe or indicated)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (deficitPlan.alertLevel == AlertLevel.DANGER) DangerRedSoft else WarnAmberSoft)
                            .border(
                                1.dp,
                                if (deficitPlan.alertLevel == AlertLevel.DANGER) DangerRed.copy(alpha = 0.4f) else WarnAmber.copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = if (deficitPlan.alertLevel == AlertLevel.DANGER) DangerRed else WarnAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isAr) "دفعة إنعاش الصدمة (Resuscitation Bolus):" else "Resuscitation Bolus (0.9% NS):",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = if (deficitPlan.alertLevel == AlertLevel.DANGER) DangerRed else Color(0xFF78350F)
                                    )
                                }

                                Text(
                                    text = "${deficitPlan.resuscitationBolusMl} mL NS",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = if (deficitPlan.alertLevel == AlertLevel.DANGER) DangerRed else Color(0xFF78350F)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (isAr)
                                    "جرعة الإنعاش: 20 مل/كجم من محلول ملحي 0.9% (Normal Saline) وريدياً سريعاً على 20-30 دقيقة لحالات الصدمة وسوء التروية."
                                else
                                    "Resuscitation Dose: 20 mL/kg of 0.9% Normal Saline IV push over 20-30 min for poor perfusion / hypovolemic shock.",
                                fontSize = 10.5.sp,
                                color = SlateDark,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isAr) "هل تم إعطاء الدفعة الإنعاشية؟" else "Bolus administered?",
                                    fontSize = 10.5.sp,
                                    color = SlateMuted
                                )

                                val bolusActive = bolusGiven > 0.0
                                FilledTonalButton(
                                    onClick = {
                                        if (bolusActive) {
                                            viewModel.updateResuscitationBolusGivenMl("0.0")
                                        } else {
                                            viewModel.updateResuscitationBolusGivenMl(deficitPlan.resuscitationBolusMl.toString())
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = if (bolusActive)
                                            (if (isAr) "تم خصم ${bolusGiven.toInt()} مل ✓" else "Deducted ${bolusGiven.toInt()} mL ✓")
                                        else
                                            (if (isAr) "خصم الدفعة من العجز" else "Deduct Bolus"),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 24-Hour Rehydration Replacement Protocol Schedule
                    Text(
                        text = if (isAr) "جدول وخطة التعويض الوريدي على مدار 24 ساعة:" else "24-Hour Rehydration Delivery Schedule:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SlateDark
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // First 8 Hours
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyanLightContainer)
                                .border(1.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (isAr) "أول 8 ساعات (1st 8h)" else "First 8 Hours",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = CyanAccent
                                )
                                Text(
                                    text = if (isAr) "50% من العجز + 1/3 الصيانة" else "50% Deficit + 1/3 Maint",
                                    fontSize = 9.sp,
                                    color = SlateMuted
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${deficitPlan.first8HourHourlyRate} mL/hr",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = SlateDark
                                )
                                Text(
                                    text = if (isAr) "الإجمالي: ${deficitPlan.first8HourTotalMl.toInt()} مل" else "Total: ${deficitPlan.first8HourTotalMl.toInt()} mL",
                                    fontSize = 10.sp,
                                    color = SlateMuted
                                )
                            }
                        }

                        // Next 16 Hours
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TealLightContainer)
                                .border(1.dp, TealPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (isAr) "الـ 16 ساعة التالية" else "Next 16 Hours",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = TealPrimary
                                )
                                Text(
                                    text = if (isAr) "50% من العجز + 2/3 الصيانة" else "50% Deficit + 2/3 Maint",
                                    fontSize = 9.sp,
                                    color = SlateMuted
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${deficitPlan.next16HourHourlyRate} mL/hr",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = SlateDark
                                )
                                Text(
                                    text = if (isAr) "الإجمالي: ${deficitPlan.next16HourTotalMl.toInt()} مل" else "Total: ${deficitPlan.next16HourTotalMl.toInt()} mL",
                                    fontSize = 10.sp,
                                    color = SlateMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Clinical recommendations
                    val recs = if (isAr) deficitPlan.recommendationsAr else deficitPlan.recommendationsEn
                    recs.forEach { tip ->
                        Row(modifier = Modifier.padding(vertical = 1.5.dp)) {
                            Text(text = "• ", color = TealPrimary, fontWeight = FontWeight.Bold)
                            Text(text = tip, fontSize = 10.5.sp, color = SlateDark, lineHeight = 15.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // --- Section 3: Dedicated Solution Chooser based on child's weight in kilograms ---
            item {
                SectionHeader(
                    title = if (isAr) "اختيار المحلول الوريدي المناسب لوزن الطفل" else "IV Solution Chooser by Child Weight",
                    subtitle = if (isAr) "مقارنة المحاليل (D10W، سالين 0.9%، D5 0.45% NS) ونسب الصوديوم والبوتاسيوم" else "Compare D10W, Normal Saline 0.9%, D5 0.45% NS with electrolyte content",
                    icon = Icons.Default.Science
                )

                ClinicalCard(
                    title = if (isAr) "دليل اختيار المحاليل السريرية" else "Clinical Solution Selection",
                    badgeText = state.selectedFluidSolution.name,
                    alertLevel = AlertLevel.SAFE
                ) {
                    Text(
                        text = if (isAr) "اختر المحلول لمطابقته مع وزن الطفل الحالي (${String.format(Locale.US, "%.1f", weightKg)} كجم):" else "Select IV solution to evaluate suitability for ${String.format(Locale.US, "%.1f", weightKg)} kg:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateMuted
                    )

                    // Material 3 Dropdown for selecting IV solution type (D10W, Normal Saline, D5 0.45% NS, etc.)
                    var dropdownExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("solution_dropdown_box")
                    ) {
                        OutlinedTextField(
                            value = if (isAr) state.selectedFluidSolution.nameAr else state.selectedFluidSolution.nameEn,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isAr) "نوع المحلول الوريدي (IV Solution Type)" else "IV Solution Type", fontSize = 11.5.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            FluidSolutionType.values().forEach { sol ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = if (isAr) sol.nameAr else sol.nameEn,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = SlateDark
                                            )
                                            Text(
                                                text = "${sol.osmolarityMasmL} mOsm/L • Na: ${sol.sodiumMeqL.toInt()} mEq/L • Dextrose: ${sol.dextrosePercent}%",
                                                fontSize = 10.sp,
                                                color = SlateMuted
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.setSelectedFluidSolution(sol)
                                        dropdownExpanded = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.WaterDrop,
                                            contentDescription = null,
                                            tint = if (state.selectedFluidSolution == sol) TealPrimary else SlateMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Horizontal Solution selector quick chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(FluidSolutionType.values()) { sol ->
                            val isSel = state.selectedFluidSolution == sol
                            FilterChip(
                                selected = isSel,
                                onClick = { viewModel.setSelectedFluidSolution(sol) },
                                label = {
                                    Text(
                                        text = sol.nameEn.substringBefore(" ("),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Active Solution Evaluation Card
                    val activeSol = state.selectedFluidSolution
                    val isWeightSuitable = weightKg in activeSol.recommendedWeightMinKg..activeSol.recommendedWeightMaxKg

                    // Total sodium delivery in mEq per 24 hours at the calculated maintenance volume
                    val dailySodiumMeq = (hollidaySegar.dailyMaintenanceMl / 1000.0) * activeSol.sodiumMeqL
                    val sodiumPerKgDay = dailySodiumMeq / weightKg
                    val dailyGlucoseGrams = (hollidaySegar.dailyMaintenanceMl / 100.0) * activeSol.dextrosePercent

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isWeightSuitable) TealLightContainer.copy(alpha = 0.4f) else WarnAmberSoft)
                            .border(1.dp, if (isWeightSuitable) TealPrimary.copy(alpha = 0.4f) else WarnAmber, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isAr) activeSol.nameAr else activeSol.nameEn,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = SlateDark
                                    )
                                    Text(
                                        text = "${activeSol.osmolarityMasmL} mOsm/L • Na: ${activeSol.sodiumMeqL.toInt()} mEq/L • K: ${activeSol.potassiumMeqL.toInt()} mEq/L • Dextrose: ${activeSol.dextrosePercent}%",
                                        fontSize = 10.sp,
                                        color = SlateMuted
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isWeightSuitable) SafeGreen else WarnAmber)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (isWeightSuitable) (if (isAr) "ملائم للوزن ✓" else "Suitable ✓") else (if (isAr) "تحذير وزن ⚠" else "Caution ⚠"),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Electrolyte delivery grid
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.9f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = if (isAr) "صوديوم يومي (Na)" else "Daily Sodium", fontSize = 10.sp, color = SlateMuted)
                                    Text(text = "${String.format(Locale.US, "%.1f", dailySodiumMeq)} mEq", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = TealPrimary)
                                    Text(text = "(${String.format(Locale.US, "%.1f", sodiumPerKgDay)} mEq/kg/d)", fontSize = 9.5.sp, color = SlateMuted)
                                }

                                Divider(modifier = Modifier.height(26.dp).width(1.dp), color = SlateBorder)

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = if (isAr) "بوتاسيوم يومي (K)" else "Daily Potassium", fontSize = 10.sp, color = SlateMuted)
                                    Text(
                                        text = if (activeSol.potassiumMeqL > 0) "${String.format(Locale.US, "%.1f", (hollidaySegar.dailyMaintenanceMl / 1000.0) * activeSol.potassiumMeqL)} mEq" else "0 mEq",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = if (activeSol.potassiumMeqL > 0) SafeGreen else SlateMuted
                                    )
                                    Text(text = if (activeSol.potassiumMeqL > 0) "Added KCl" else "Needs KCl additive", fontSize = 9.sp, color = SlateMuted)
                                }

                                Divider(modifier = Modifier.height(26.dp).width(1.dp), color = SlateBorder)

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = if (isAr) "جلوكوز يومي" else "Daily Dextrose", fontSize = 10.sp, color = SlateMuted)
                                    Text(text = "${String.format(Locale.US, "%.1f", dailyGlucoseGrams)} g", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = CyanAccent)
                                    Text(text = "${String.format(Locale.US, "%.0f", dailyGlucoseGrams * 3.4)} kcal", fontSize = 9.5.sp, color = SlateMuted)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Primary use description
                            Text(
                                text = if (isAr) "🎯 الاستخدام السريري المعتمد:" else "🎯 Clinical Indications:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateDark
                            )
                            Text(
                                text = if (isAr) activeSol.primaryUseAr else activeSol.primaryUseEn,
                                fontSize = 10.5.sp,
                                color = SlateDark,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Caution
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(WarnAmberSoft)
                                    .padding(6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Text(text = "⚠ ", fontSize = 11.sp, color = WarnAmber)
                                    Text(
                                        text = if (isAr) activeSol.cautionAr else activeSol.cautionEn,
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF78350F),
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // === TAB 2: NEONATAL NICU TFI & GIR CALCULATOR ===========================
        // =========================================================================
        else {
            // Section: Neonatal TFI and IV Infusion Rate
            item {
                SectionHeader(
                    title = if (isAr) "خطة السوائل الوريدية لحديثي الولادة (TFI)" else "Total Fluid Intake & Infusion Rate (NICU)",
                    subtitle = if (isAr) "المقترح لعمر الوليد وأوزان الخدج مع تعويض التدفئة والعلاج الضوئي" else "Day of life fluid goal and insensible loss adjustment",
                    icon = Icons.Default.LocalDrink
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.isPhototherapy,
                        onClick = { viewModel.togglePhototherapy() },
                        label = {
                            Text(
                                text = if (isAr) "علاج ضوئي (+15 مل/كجم)" else "Phototherapy (+15)",
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = state.isRadiantWarmer,
                        onClick = { viewModel.toggleRadiantWarmer() },
                        label = {
                            Text(
                                text = if (isAr) "تدفئة مشعة (+15 مل/كجم)" else "Radiant Warmer (+15)",
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ClinicalCard(
                    title = if (isAr) "حساب معدل الضخ الوريدي لحديثي الولادة" else "Neonatal IV Infusion Rate",
                    icon = Icons.Default.LocalDrink,
                    badgeText = "${String.format(Locale.US, "%.1f", hourlyNeonatalIvRate)} mL/hr",
                    alertLevel = AlertLevel.SAFE
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UnitInputField(
                            value = state.customTfiInput,
                            onValueChange = { viewModel.updateCustomTfi(it) },
                            label = if (isAr) "معدل السوائل المستهدف (TFI)" else "Target TFI",
                            unit = "mL/kg/d",
                            helperText = if (isAr) "المقترح لعمر $dol يوم: $recTfi مل" else "Guideline: $recTfi mL/kg/d",
                            modifier = Modifier.weight(1.2f),
                            testTag = "custom_tfi_input"
                        )

                        Button(
                            onClick = { viewModel.updateCustomTfi(recTfi.toString()) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealLightContainer, contentColor = TealPrimary),
                            modifier = Modifier.weight(0.8f)
                        ) {
                            Text(text = if (isAr) "استخدام المقترح" else "Use Guideline", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateBg)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = if (isAr) "الإجمالي اليومي" else "Total Daily Vol", fontSize = 11.sp, color = SlateMuted)
                            Text(text = "${String.format(Locale.US, "%.1f", dailyNeonatalVolume)} mL", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateDark)
                        }

                        Divider(modifier = Modifier.height(28.dp).width(1.dp), color = SlateBorder)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = if (isAr) "معدل الضخ بالساعة" else "Hourly IV Rate", fontSize = 11.sp, color = SlateMuted)
                            Text(text = "${String.format(Locale.US, "%.2f", hourlyNeonatalIvRate)} mL/hr", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TealPrimary)
                        }

                        Divider(modifier = Modifier.height(28.dp).width(1.dp), color = SlateBorder)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = if (isAr) "كل 8 ساعات" else "Per 8-hr Shift", fontSize = 11.sp, color = SlateMuted)
                            Text(text = "${String.format(Locale.US, "%.1f", dailyNeonatalVolume / 3.0)} mL", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateDark)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Section: Glucose Infusion Rate (GIR)
            item {
                val girAlert = when {
                    calculatedGir < 4.0 -> AlertLevel.WARNING
                    calculatedGir in 4.0..8.0 -> AlertLevel.SAFE
                    calculatedGir in 8.0..12.0 -> AlertLevel.SAFE
                    else -> AlertLevel.DANGER
                }

                ClinicalCard(
                    title = if (isAr) "معدل ضخ الجلوكوز (Glucose Infusion Rate - GIR)" else "Glucose Infusion Rate (GIR)",
                    icon = Icons.Default.Bolt,
                    badgeText = "${String.format(Locale.US, "%.2f", calculatedGir)} mg/kg/min",
                    alertLevel = girAlert
                ) {
                    Text(
                        text = if (isAr)
                            "المعادلة السريرية: (معدل الضخ مل/س × نسبة الديكستروز) ÷ (6 × الوزن كجم)"
                        else
                            "Formula: (IV Rate mL/hr × Dextrose %) / (6 × Weight kg)",
                        fontSize = 11.sp,
                        color = SlateMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        UnitInputField(
                            value = state.dextrosePercentInput,
                            onValueChange = { viewModel.updateDextrosePercent(it) },
                            label = if (isAr) "تركيز الديكستروز" else "Dextrose %",
                            unit = "%",
                            helperText = "e.g., D10W = 10%",
                            modifier = Modifier.weight(1f),
                            testTag = "dextrose_percent_input"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (girAlert == AlertLevel.SAFE) TealLightContainer else WarnAmberSoft)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = if (isAr) "GIR الناتج" else "Resulting GIR", fontSize = 11.sp, color = TealOnContainer)
                                Text(
                                    text = String.format(Locale.US, "%.2f", calculatedGir),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (girAlert == AlertLevel.SAFE) TealPrimary else WarnAmber
                                )
                                Text(text = "mg/kg/min", fontSize = 10.sp, color = TealOnContainer)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isAr)
                            "النطاق المثالي: 4 - 8 مجم/كجم/دقيقة للوليد الكامل، 6 - 8 للخدج. تجنب الهبوط < 4 لمنع تلف خلايا المخ."
                        else
                            "Target Range: 4 - 8 mg/kg/min (Term), 6 - 8 mg/kg/min (Preterm). Avoid < 4 to prevent neurological injury.",
                        fontSize = 11.sp,
                        color = SlateDark,
                        fontWeight = FontWeight.Medium
                    )

                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = SlateBorder)

                    // Reverse GIR Titrator
                    Text(
                        text = if (isAr) "الحاسبة العكسية (لتحقيق GIR معين):" else "Reverse Titration (To achieve target GIR):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UnitInputField(
                            value = state.targetGirInput,
                            onValueChange = { viewModel.updateTargetGir(it) },
                            label = if (isAr) "GIR المستهدف" else "Desired GIR",
                            unit = "mg/kg/min",
                            modifier = Modifier.weight(1f),
                            testTag = "target_gir_input"
                        )

                        Column(modifier = Modifier.weight(1.3f)) {
                            Text(
                                text = if (isAr) "المعدل المطلوب (مع D$dextrosePct%):" else "Needed Rate (at D$dextrosePct%):",
                                fontSize = 10.sp,
                                color = SlateMuted
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.1f", neededRateForTargetGir)} mL/hr",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = CyanAccent
                            )
                            Text(
                                text = if (isAr) "أو تركيز Dextrose: ${String.format(Locale.US, "%.1f", neededDextroseForCurrentRate)}%" else "Or Dextrose %: ${String.format(Locale.US, "%.1f", neededDextroseForCurrentRate)}%",
                                fontSize = 10.sp,
                                color = SlateMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Section: Acute Hypoglycemia Bolus
            item {
                ClinicalCard(
                    title = if (isAr) "بروتوكول هبوط السكر الحاد (Hypoglycemia STAT Bolus)" else "Acute Hypoglycemia Protocol (STAT Bolus)",
                    icon = Icons.Default.Warning,
                    badgeText = "${String.format(Locale.US, "%.1f", hypoBolusMl)} mL D10W",
                    alertLevel = AlertLevel.WARNING
                ) {
                    Text(
                        text = if (isAr)
                            "في حال كان قياس سكر الدم < 45-50 مجم/ديسيلتر مع أعراض:"
                        else
                            "For symptomatic neonatal blood glucose < 45-50 mg/dL:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateDark
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(WarnAmberSoft)
                            .border(1.dp, WarnAmber.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isAr)
                                    "جرعة الإنقاذ السريعة: 2 مل/كجم من محلول D10W وريدياً ببطء على مدى 5 إلى 10 دقائق."
                                else
                                    "Rescue Push: 2 mL/kg of D10W IV push slowly over 5 - 10 minutes.",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF78350F)
                            )
                            Text(
                                text = if (isAr)
                                    "حجم الجرعة لوزن ${String.format(Locale.US, "%.2f", weightKg)} كجم = ${String.format(Locale.US, "%.1f", hypoBolusMl)} مل من D10W (تعطي 200 مجم/كجم جلوكوز)."
                                else
                                    "Exact Dose for ${String.format(Locale.US, "%.2f", weightKg)} kg = ${String.format(Locale.US, "%.1f", hypoBolusMl)} mL of D10W (provides 200 mg/kg dextrose).",
                                fontSize = 11.sp,
                                color = Color(0xFF78350F)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isAr)
                                    "تحذير: لا تستخدم مطلقاً D25W أو D50W في حديثي الولادة. أعد قياس السكر بعد 20-30 دقيقة."
                                else
                                    "Caution: Never use D25W or D50W in newborns! Recheck glucose in 20-30 minutes.",
                                fontSize = 10.sp,
                                color = DangerRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Section: Electrolyte Requirements
            item {
                ClinicalCard(
                    title = if (isAr) "الاحتياجات اليومية من الأملاح (Electrolytes)" else "Daily Electrolyte Maintenance Requirements",
                    icon = Icons.Default.Science,
                    badgeText = if (isAr) "ابتداءً من DOL 2-3" else "From DOL 2-3",
                    alertLevel = AlertLevel.SAFE
                ) {
                    Text(
                        text = if (isAr)
                            "تُضاف للمحاليل الوريدية بعد التأكد من نزول البول واستقرار وظائف الكلى:"
                        else
                            "Added to IV maintenance after adequate urine output is established:",
                        fontSize = 11.sp,
                        color = SlateMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ElectrolyteItem(
                            name = if (isAr) "صوديوم (Na)" else "Sodium (Na)",
                            formula = "2 - 4 mEq/kg/d",
                            total = "${String.format(Locale.US, "%.1f", naDailyMeq)} mEq/d",
                            detail = "${String.format(Locale.US, "%.1f", naDailyMeq / 0.154)} mL NS 0.9%",
                            modifier = Modifier.weight(1f)
                        )

                        ElectrolyteItem(
                            name = if (isAr) "بوتاسيوم (K)" else "Potassium (K)",
                            formula = "1 - 2 mEq/kg/d",
                            total = "${String.format(Locale.US, "%.1f", kDailyMeq)} mEq/d",
                            detail = "${String.format(Locale.US, "%.1f", kDailyMeq / 2.0)} mL KCl (2mEq/mL)",
                            modifier = Modifier.weight(1f)
                        )

                        ElectrolyteItem(
                            name = if (isAr) "كالسيوم 10%" else "Ca Gluconate",
                            formula = "100-200 mg/kg/d",
                            total = "${String.format(Locale.US, "%.1f", caGluconateDailyMl)} mL/d",
                            detail = "10% solution",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ElectrolyteItem(
    name: String,
    formula: String,
    total: String,
    detail: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SlateBg)
            .border(0.8.dp, SlateBorder, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(text = name, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = TealPrimary)
            Text(text = formula, fontSize = 9.sp, color = SlateMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = total, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SlateDark)
            Text(text = detail, fontSize = 9.5.sp, color = CyanAccent)
        }
    }
}
