package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.AlertLevel
import com.example.data.model.ApgarInput
import com.example.data.model.PhototherapyModality
import com.example.data.model.SilvermanInput
import com.example.data.repository.ClinicalFormulas
import com.example.ui.components.ClinicalCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.UnitInputField
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoCareViewModel
import com.example.ui.viewmodel.WorkstationState

@Composable
fun ClinicalScoresScreen(
    state: WorkstationState,
    viewModel: NeoCareViewModel,
    modifier: Modifier = Modifier
) {
    val isAr = state.isArabic
    val apgar = state.apgarInput
    val silverman = state.silvermanInput

    // Bilirubin calculation with active patient weight in kg
    val biliVal = state.bilirubinInput.toDoubleOrNull() ?: 10.5
    val ageHrs = state.bilirubinAgeHours.toDoubleOrNull() ?: 48.0
    val biliResult = remember(
        ageHrs,
        biliVal,
        state.bilirubinIsUmol,
        state.activePatient.gestationalWeeks,
        state.bilirubinRiskFactor,
        state.activePatient.weightKg
    ) {
        ClinicalFormulas.evaluateBilirubin(
            ageHours = ageHrs,
            bilirubinInput = biliVal,
            isUmol = state.bilirubinIsUmol,
            gestationalWeeks = state.activePatient.gestationalWeeks,
            hasRiskFactors = state.bilirubinRiskFactor,
            weightKg = state.activePatient.weightKg
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // === 1. BILIRUBIN / PHOTOTHERAPY & EXCHANGE TRANSFUSION (FEATURED AT TOP) ===
        item {
            SectionHeader(
                title = if (isAr) "تقييم الصفراء والعلاج الضوئي (AAP 2022)" else "Hyperbilirubinemia & Phototherapy Risk (AAP 2022)",
                subtitle = if (isAr) "تحديد نوع العلاج (أحادي، ثنائي، ثلاثي، تبديل دم) بناءً على النتيجة" else "Determine exact modality (Single, Double, Triple, Exchange Transfusion)",
                icon = Icons.Default.LightMode
            )

            ClinicalCard(
                title = if (isAr) "بيانات وقياس الصفراء (TSB)" else "Bilirubin Inputs & Status",
                badgeText = "${biliResult.mgDlValue} mg/dL",
                alertLevel = biliResult.alertLevel
            ) {
                // Inputs: Age in hours, Bilirubin value, Unit toggle, Risk Factor toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    UnitInputField(
                        value = state.bilirubinAgeHours,
                        onValueChange = { viewModel.updateBilirubinAgeHours(it) },
                        label = if (isAr) "عمر الرضيع" else "Postnatal Age",
                        unit = if (isAr) "ساعة" else "hrs",
                        modifier = Modifier.weight(1f),
                        testTag = "bili_age_hours"
                    )

                    UnitInputField(
                        value = state.bilirubinInput,
                        onValueChange = { viewModel.updateBilirubinValue(it) },
                        label = if (isAr) "نسبة الصفراء (TSB)" else "Total Bilirubin",
                        unit = if (state.bilirubinIsUmol) "µmol/L" else "mg/dL",
                        modifier = Modifier.weight(1f),
                        testTag = "bili_value_input"
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.bilirubinIsUmol,
                        onClick = { viewModel.toggleBilirubinUnit() },
                        label = {
                            Text(
                                text = if (state.bilirubinIsUmol) "الوحدة: µmol/L" else "Unit: mg/dL",
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = state.bilirubinRiskFactor,
                        onClick = { viewModel.toggleBilirubinRisk() },
                        label = {
                            Text(
                                text = if (isAr) "عوامل خطورة (تكسير/تسمم)" else "Neuro Risk Factors (+)",
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier.weight(1.3f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Threshold Comparisons Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateBg)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = if (isAr) "عتبة الضوئي" else "Photo Threshold", fontSize = 10.5.sp, color = SlateMuted)
                        Text(text = "${biliResult.photoThreshold} mg/dL", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = WarnAmber)
                    }

                    Divider(modifier = Modifier.height(28.dp).width(1.dp), color = SlateBorder)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = if (isAr) "عتبة تبديل الدم" else "Exchange Threshold", fontSize = 10.5.sp, color = SlateMuted)
                        Text(text = "${biliResult.exchangeThreshold} mg/dL", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = DangerRed)
                    }

                    Divider(modifier = Modifier.height(28.dp).width(1.dp), color = SlateBorder)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = if (isAr) "وزن الطفل" else "Weight", fontSize = 10.5.sp, color = SlateMuted)
                        Text(text = "${String.format("%.2f", state.activePatient.weightKg)} kg", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = TealPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // === DEDICATED PHOTOTHERAPY & TREATMENT MODALITY DECISION CARD ===
            val modalityBg = when (biliResult.modality) {
                PhototherapyModality.NONE -> SafeGreenSoft
                PhototherapyModality.SINGLE -> WarnAmberSoft
                PhototherapyModality.DOUBLE -> Color(0xFFFFEDD5)
                PhototherapyModality.TRIPLE -> DangerRedSoft
                PhototherapyModality.EXCHANGE -> Color(0xFFFFE4E6)
            }

            val modalityBorder = when (biliResult.modality) {
                PhototherapyModality.NONE -> SafeGreen
                PhototherapyModality.SINGLE -> WarnAmber
                PhototherapyModality.DOUBLE -> Color(0xFFEA580C)
                PhototherapyModality.TRIPLE -> DangerRed
                PhototherapyModality.EXCHANGE -> Color(0xFFBE123C)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = modalityBg),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, modalityBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isAr) "القرار العلاجي الموصى به:" else "Prescribed Treatment Modality:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateMuted
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(modalityBorder)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isAr) biliResult.modality.titleAr else biliResult.modality.titleEn,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isAr) biliResult.riskZoneAr else biliResult.riskZoneEn,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = modalityBorder
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isAr) biliResult.recommendationAr else biliResult.recommendationEn,
                        fontSize = 12.sp,
                        color = SlateDark,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Metrics Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.8f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = if (isAr) "الفارق عن عتبة الضوئي" else "Diff from Photo", fontSize = 10.sp, color = SlateMuted)
                            Text(
                                text = "${if (biliResult.diffFromPhoto > 0) "+" else ""}${biliResult.diffFromPhoto} mg/dL",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (biliResult.diffFromPhoto > 0) WarnAmber else SafeGreen
                            )
                        }

                        Divider(modifier = Modifier.height(24.dp).width(1.dp), color = SlateBorder)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = if (isAr) "المسافة حتى تبديل الدم" else "Dist to Exchange", fontSize = 10.sp, color = SlateMuted)
                            Text(
                                text = "${biliResult.diffFromExchange} mg/dL",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (biliResult.diffFromExchange <= 2.0) DangerRed else SlateDark
                            )
                        }

                        Divider(modifier = Modifier.height(24.dp).width(1.dp), color = SlateBorder)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = if (isAr) "إعادة الفحص بعد" else "Recheck In", fontSize = 10.sp, color = SlateMuted)
                            Text(
                                text = if (isAr) "${biliResult.recheckIntervalHours} ساعات" else "${biliResult.recheckIntervalHours} hrs",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = TealPrimary
                            )
                        }
                    }

                    // Detailed Treatment Guidance Points
                    val details = if (isAr) biliResult.therapyDetailsAr else biliResult.therapyDetailsEn
                    if (details.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isAr) "التعليمات الفنية للعلاج المتبع:" else "Technical Protocol & Nursing Instructions:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = SlateDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        details.forEach { item ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(text = "• ", color = modalityBorder, fontWeight = FontWeight.Bold)
                                Text(text = item, fontSize = 11.sp, color = SlateDark, lineHeight = 16.sp)
                            }
                        }
                    }

                    // Exchange Transfusion Protocol box (if indicated or standby)
                    val exchText = if (isAr) biliResult.exchangeProtocolAr else biliResult.exchangeProtocolEn
                    if (exchText != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEE2E2))
                                .border(1.dp, DangerRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Bloodtype, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isAr) "بروتوكول تبديل الدم (Exchange Transfusion Protocol):" else "Exchange Transfusion Protocol:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = DangerRed
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = exchText, fontSize = 10.5.sp, color = Color(0xFF7F1D1D), lineHeight = 15.sp)
                            }
                        }
                    }

                    // IVIG Protocol box (if applicable)
                    val ivigText = if (isAr) biliResult.ivigProtocolAr else biliResult.ivigProtocolEn
                    if (ivigText != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyanLightContainer)
                                .border(1.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Medication, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isAr) "بروتوكول الجلوبيولين المناعي (IVIG):" else "IVIG Administration Guide:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = CyanAccent
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = ivigText, fontSize = 10.5.sp, color = Color(0xFF0C4A6E), lineHeight = 15.sp)
                            }
                        }
                    }

                    // Stopping Criteria
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isAr)
                            "✓ معيار إيقاف الضوئي: إيقاف العلاج عند هبوط الصفراء إلى ${biliResult.stoppingTargetMgDl} مجم/ديسيلتر أو أقل، وفحص ارتداد الصفراء (Rebound) بعد 12-24 ساعة."
                        else
                            "✓ Discontinuation: Stop phototherapy when TSB falls to ${biliResult.stoppingTargetMgDl} mg/dL or lower; recheck rebound TSB in 12-24 hours.",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // === 2. APGAR SCORE CALCULATOR ===
        item {
            SectionHeader(
                title = if (isAr) "مقياس أبغار للوليد (APGAR Score)" else "APGAR Score Calculator",
                subtitle = if (isAr) "تقييم تكيف الوليد في الدقيقة 1 و 5 و 10 بعد الولادة" else "Newborn transition assessment at 1, 5, and 10 minutes",
                icon = Icons.Default.Assessment
            )

            // Timing selector chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1, 5, 10).forEach { minute ->
                    FilterChip(
                        selected = state.apgarTimingMinute == minute,
                        onClick = { viewModel.updateApgar(apgar, minute) },
                        label = {
                            Text(
                                text = if (isAr) "الدقيقة $minute" else "Min $minute",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            ClinicalCard(
                title = if (isAr) "نتيجة مقياس أبغار (${apgar.totalScore}/10)" else "APGAR Result (${apgar.totalScore}/10)",
                badgeText = "${apgar.totalScore} / 10",
                alertLevel = apgar.alertLevel
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (apgar.alertLevel) {
                                AlertLevel.SAFE -> SafeGreenSoft
                                AlertLevel.WARNING -> WarnAmberSoft
                                AlertLevel.DANGER -> DangerRedSoft
                            }
                        )
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = if (isAr) apgar.statusAr else apgar.statusEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = when (apgar.alertLevel) {
                                AlertLevel.SAFE -> SafeGreen
                                AlertLevel.WARNING -> WarnAmber
                                AlertLevel.DANGER -> DangerRed
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ScoreSelectorRow(
                    label = if (isAr) "1. المظهر ولون الجلد (Appearance)" else "1. Appearance (Skin Color)",
                    options = if (isAr) listOf("أزرق / شاحب (0)", "زرقة بالأطراف (1)", "وردي بالكامل (2)")
                    else listOf("Blue/Pale (0)", "Acrocyanosis (1)", "Pink All Over (2)"),
                    selectedIndex = apgar.appearance,
                    onSelect = { viewModel.updateApgar(apgar.copy(appearance = it)) }
                )

                ScoreSelectorRow(
                    label = if (isAr) "2. النبض ومعدل ضربات القلب (Pulse)" else "2. Pulse (Heart Rate)",
                    options = if (isAr) listOf("لا يوجد نبض (0)", "< 100 د/د (1)", ">= 100 د/د (2)")
                    else listOf("Absent (0)", "< 100 bpm (1)", ">= 100 bpm (2)"),
                    selectedIndex = apgar.pulse,
                    onSelect = { viewModel.updateApgar(apgar.copy(pulse = it)) }
                )

                ScoreSelectorRow(
                    label = if (isAr) "3. استجابة المنعكسات (Grimace)" else "3. Grimace (Reflex Irritability)",
                    options = if (isAr) listOf("لا استجابة (0)", "تكشيرة خفيفة (1)", "بكاء قوي/سعال (2)")
                    else listOf("No response (0)", "Grimace (1)", "Cry/Cough/Sneeze (2)"),
                    selectedIndex = apgar.grimace,
                    onSelect = { viewModel.updateApgar(apgar.copy(grimace = it)) }
                )

                ScoreSelectorRow(
                    label = if (isAr) "4. النشاط والتوتر العضلي (Activity)" else "4. Activity (Muscle Tone)",
                    options = if (isAr) listOf("ارتخاء رخو (0)", "انثناء طفيف (1)", "حركة نشطة (2)")
                    else listOf("Limp / Flaccid (0)", "Some flexion (1)", "Active motion (2)"),
                    selectedIndex = apgar.activity,
                    onSelect = { viewModel.updateApgar(apgar.copy(activity = it)) }
                )

                ScoreSelectorRow(
                    label = if (isAr) "5. التنفس والجهد التنفسي (Respiration)" else "5. Respiration (Effort)",
                    options = if (isAr) listOf("لا يوجد تنفس (0)", "بطيء/غير منتظم (1)", "بكاء قوي مستمر (2)")
                    else listOf("Absent (0)", "Slow / Irregular (1)", "Good strong cry (2)"),
                    selectedIndex = apgar.respiration,
                    onSelect = { viewModel.updateApgar(apgar.copy(respiration = it)) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // === 3. SILVERMAN-ANDERSON RESPIRATORY SCORE ===
        item {
            SectionHeader(
                title = if (isAr) "مقياس سيلفرمان لضيق التنفس (Silverman-Anderson)" else "Silverman-Anderson Retraction Score",
                subtitle = if (isAr) "تقييم درجة الضائقة التنفسية وسحب العضلات لدى الخدج" else "Respiratory distress severity and retraction assessment in neonates",
                icon = Icons.Default.Air
            )

            ClinicalCard(
                title = if (isAr) "درجة سيلفرمان (${silverman.totalScore}/10)" else "Silverman Score (${silverman.totalScore}/10)",
                badgeText = "${silverman.totalScore} / 10",
                alertLevel = silverman.alertLevel
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (silverman.alertLevel) {
                                AlertLevel.SAFE -> if (silverman.totalScore == 0) TealLightContainer else SafeGreenSoft
                                AlertLevel.WARNING -> WarnAmberSoft
                                AlertLevel.DANGER -> DangerRedSoft
                            }
                        )
                        .padding(10.dp)
                ) {
                    Text(
                        text = if (isAr) silverman.statusAr else silverman.statusEn,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = when (silverman.alertLevel) {
                            AlertLevel.SAFE -> if (silverman.totalScore == 0) TealPrimary else SafeGreen
                            AlertLevel.WARNING -> WarnAmber
                            AlertLevel.DANGER -> DangerRed
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                ScoreSelectorRow(
                    label = if (isAr) "1. حركة أعلى الصدر (Upper Chest)" else "1. Upper Chest Movement",
                    options = if (isAr) listOf("متزامن مع البطن (0)", "تأخر بالشهيق (1)", "تأرجح سيسو (2)")
                    else listOf("Synchronized (0)", "Lag on inspir. (1)", "See-saw (2)"),
                    selectedIndex = silverman.upperChest,
                    onSelect = { viewModel.updateSilverman(silverman.copy(upperChest = it)) }
                )

                ScoreSelectorRow(
                    label = if (isAr) "2. سحب أسفل الصدر (Lower Chest)" else "2. Lower Chest Retraction",
                    options = if (isAr) listOf("لا يوجد (0)", "مرئي قليلاً (1)", "شديد وواضح (2)")
                    else listOf("None (0)", "Just visible (1)", "Marked (2)"),
                    selectedIndex = silverman.lowerChest,
                    onSelect = { viewModel.updateSilverman(silverman.copy(lowerChest = it)) }
                )

                ScoreSelectorRow(
                    label = if (isAr) "3. سحب عظم القص (Xiphoid Retraction)" else "3. Xiphoid Retraction",
                    options = if (isAr) listOf("لا يوجد (0)", "مرئي قليلاً (1)", "شديد وواضح (2)")
                    else listOf("None (0)", "Just visible (1)", "Marked (2)"),
                    selectedIndex = silverman.xiphoidRetract,
                    onSelect = { viewModel.updateSilverman(silverman.copy(xiphoidRetract = it)) }
                )

                ScoreSelectorRow(
                    label = if (isAr) "4. اتساع فتحات الأنف (Nares Dilation)" else "4. Nares Flaring",
                    options = if (isAr) listOf("لا يوجد (0)", "خفيف (1)", "واضح وملحوظ (2)")
                    else listOf("None (0)", "Minimal (1)", "Marked (2)"),
                    selectedIndex = silverman.naresFlaring,
                    onSelect = { viewModel.updateSilverman(silverman.copy(naresFlaring = it)) }
                )

                ScoreSelectorRow(
                    label = if (isAr) "5. التأوه الزفيري (Expiratory Grunt)" else "5. Expiratory Grunt",
                    options = if (isAr) listOf("لا يوجد (0)", "يُسمع بالسماعة (1)", "يُسمع بالأذن المجردة (2)")
                    else listOf("None (0)", "Stethoscope only (1)", "Audible naked ear (2)"),
                    selectedIndex = silverman.expiratoryGrunt,
                    onSelect = { viewModel.updateSilverman(silverman.copy(expiratoryGrunt = it)) }
                )
            }
        }
    }
}

@Composable
fun ScoreSelectorRow(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = SlateDark
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = selectedIndex == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) TealPrimary else SlateBg)
                        .border(
                            0.8.dp,
                            if (isSelected) TealPrimary else SlateBorder,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { onSelect(index) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else SlateDark,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
