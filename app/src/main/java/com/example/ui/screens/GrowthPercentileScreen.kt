package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertLevel
import com.example.data.model.GrowthPercentileResult
import com.example.data.repository.ClinicalFormulas
import com.example.ui.components.ClinicalCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.UnitInputField
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoCareViewModel
import com.example.ui.viewmodel.WorkstationState
import java.util.Locale

@Composable
fun GrowthPercentileScreen(
    state: WorkstationState,
    viewModel: NeoCareViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAr = state.isArabic
    val patient = state.activePatient

    val ageMonths = state.growthAgeMonthsInput.toDoubleOrNull() ?: 12.0
    val heightCm = state.growthHeightCmInput.toDoubleOrNull() ?: 75.0
    val weightKg = state.weightInputKg.toDoubleOrNull() ?: patient.weightKg
    val isMale = state.growthIsMale

    val growthResult = remember(ageMonths, isMale, weightKg, heightCm) {
        ClinicalFormulas.calculateGrowthPercentiles(
            ageMonths = ageMonths,
            isMale = isMale,
            weightKg = weightKg,
            heightCm = heightCm
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // === 1. HEADER SECTION ===
        item {
            Spacer(modifier = Modifier.height(10.dp))
            SectionHeader(
                title = if (isAr) "حاسبة منحنيات النمو للأطفال (WHO / CDC)" else "Pediatric Growth Percentile Calculator",
                subtitle = if (isAr) "تقييم مؤشرات الوزن والطول وكتلة الجسم (BMI) وفقاً لمعايير منظمة الصحة العالمية" else "Weight, Length/Height & BMI assessment against WHO/CDC growth curves",
                icon = Icons.Default.Timeline
            )

            // Patient Sync Helper Strip
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorder, RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(TealLightContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ChildCare, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = patient.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SlateDark)
                            Text(
                                text = "⚖ ${String.format(Locale.US, "%.2f", weightKg)} kg • ${if (isMale) (if (isAr) "ذكر" else "Boy") else (if (isAr) "أنثى" else "Girl")}",
                                fontSize = 10.5.sp,
                                color = TealPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Quick populate from active patient
                    TextButton(
                        onClick = {
                            val months = when {
                                patient.ageYears > 0 -> patient.ageYears * 12.0
                                patient.ageMonths > 0 -> patient.ageMonths.toDouble()
                                else -> (patient.dayOfLife / 30.0).coerceAtLeast(0.1)
                            }
                            viewModel.updateGrowthAgeMonths(String.format(Locale.US, "%.1f", months))
                            viewModel.updatePatientWeight(patient.weightKg)
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isAr) "مزامنة المريض" else "Sync Patient",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // === 2. INTERACTIVE INPUT CONTROLS ===
        item {
            ClinicalCard(
                title = if (isAr) "مدخلات قياسات نمو الطفل" else "Child Growth Inputs",
                badgeText = if (isMale) (if (isAr) "ولد ♂" else "Boy ♂") else (if (isAr) "بنت ♀" else "Girl ♀"),
                alertLevel = AlertLevel.SAFE
            ) {
                // Gender Selection
                Text(
                    text = if (isAr) "الجنس (Gender):" else "Gender:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateMuted
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isMale,
                        onClick = { if (!isMale) viewModel.toggleGrowthGender() },
                        label = { Text(if (isAr) "ذكر (Boy ♂)" else "Boy ♂", fontSize = 11.sp, fontWeight = if (isMale) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = { Icon(Icons.Default.Male, contentDescription = null, tint = if (isMale) Color.White else CyanAccent, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = !isMale,
                        onClick = { if (isMale) viewModel.toggleGrowthGender() },
                        label = { Text(if (isAr) "أنثى (Girl ♀)" else "Girl ♀", fontSize = 11.sp, fontWeight = if (!isMale) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = { Icon(Icons.Default.Female, contentDescription = null, tint = if (!isMale) Color.White else Color(0xFFDB2777), modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Age, Weight, Height Input Fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UnitInputField(
                        value = state.growthAgeMonthsInput,
                        onValueChange = { viewModel.updateGrowthAgeMonths(it) },
                        label = if (isAr) "العمر بالشهور" else "Age (Months)",
                        unit = if (isAr) "شهر" else "mo",
                        modifier = Modifier.weight(1f),
                        testTag = "growth_age_input"
                    )

                    UnitInputField(
                        value = String.format(Locale.US, "%.2f", weightKg),
                        onValueChange = {
                            val parsed = it.toDoubleOrNull()
                            if (parsed != null && parsed > 0) {
                                viewModel.updatePatientWeight(parsed)
                            }
                        },
                        label = if (isAr) "الوزن بالكيلوجرام" else "Weight",
                        unit = "kg",
                        modifier = Modifier.weight(1f),
                        testTag = "growth_weight_input"
                    )

                    UnitInputField(
                        value = state.growthHeightCmInput,
                        onValueChange = { viewModel.updateGrowthHeightCm(it) },
                        label = if (isAr) "الطول بالسنتيمتر" else "Height / Length",
                        unit = "cm",
                        modifier = Modifier.weight(1f),
                        testTag = "growth_height_input"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Age Quick Chips
                Text(
                    text = if (isAr) "اختيار سريع للعمر النموذجي:" else "Quick Age Milestones:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val milestones = listOf(
                        Pair("0 mo (Birth)", "0.0"),
                        Pair("3 mo", "3.0"),
                        Pair("6 mo", "6.0"),
                        Pair("1 yr (12m)", "12.0"),
                        Pair("2 yr (24m)", "24.0"),
                        Pair("5 yr (60m)", "60.0"),
                        Pair("10 yr (120m)", "120.0")
                    )
                    items(milestones.size) { i ->
                        val (label, valStr) = milestones[i]
                        val isSel = state.growthAgeMonthsInput == valStr
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                viewModel.updateGrowthAgeMonths(valStr)
                                // Auto adjust nominal height/weight for convenience
                                val m = valStr.toDouble()
                                val nominalH = if (m == 0.0) 50.0 else if (m <= 12) 50.0 + (m * 2.1) else 75.0 + ((m - 12) * 0.7)
                                val nominalW = if (m == 0.0) 3.3 else if (m <= 12) 3.3 + (m * 0.5) else 9.5 + ((m - 12) * 0.18)
                                viewModel.updateGrowthHeightCm(String.format(Locale.US, "%.1f", nominalH))
                                viewModel.updatePatientWeight(nominalW)
                            },
                            label = { Text(text = label, fontSize = 10.5.sp) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // === 3. OVERALL ASSESSMENT & CLINICAL ALERT HERO CARD ===
        item {
            val (badgeBg, badgeBorder, badgeColor) = when (growthResult.alertLevel) {
                AlertLevel.SAFE -> Triple(SafeGreenSoft, SafeGreen, SafeGreen)
                AlertLevel.WARNING -> Triple(WarnAmberSoft, WarnAmber, WarnAmber)
                AlertLevel.DANGER -> Triple(DangerRedSoft, DangerRed, DangerRed)
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = badgeBg),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, badgeBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("growth_hero_card")
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
                            text = if (isAr) "تقييم مؤشرات نمو منظمة الصحة العالمية:" else "WHO Growth Assessment:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateDark
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeBorder)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (growthResult.alertLevel == AlertLevel.SAFE) (if (isAr) "نمو طبيعي ✓" else "Normal Growth ✓")
                                else (if (isAr) "انحراف في المنحنى ⚠" else "Percentile Deviation ⚠"),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isAr) growthResult.overallAssessmentAr else growthResult.overallAssessmentEn,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlateDark,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3-Metric Summary Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.9f))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = if (isAr) "الوزن للعمر" else "Weight %ile", fontSize = 10.5.sp, color = SlateMuted)
                            Text(
                                text = "${growthResult.weightPercentile}%",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = getPercentileColor(growthResult.weightPercentile)
                            )
                            Text(text = "Z: ${growthResult.weightZScore}", fontSize = 9.5.sp, color = SlateMuted)
                        }

                        Divider(modifier = Modifier.height(34.dp).width(1.dp), color = SlateBorder)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = if (isAr) "الطول للعمر" else "Height %ile", fontSize = 10.5.sp, color = SlateMuted)
                            Text(
                                text = "${growthResult.heightPercentile}%",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = getPercentileColor(growthResult.heightPercentile)
                            )
                            Text(text = "Z: ${growthResult.heightZScore}", fontSize = 9.5.sp, color = SlateMuted)
                        }

                        Divider(modifier = Modifier.height(34.dp).width(1.dp), color = SlateBorder)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = if (isAr) "كتلة الجسم (BMI)" else "BMI %ile", fontSize = 10.5.sp, color = SlateMuted)
                            Text(
                                text = "${growthResult.bmiPercentile}%",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = getPercentileColor(growthResult.bmiPercentile)
                            )
                            Text(text = "${growthResult.bmi} kg/m²", fontSize = 9.5.sp, color = SlateMuted)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // === 4. DETAILED PERCENTILE CURVE VISUALIZERS ===
        // 1. Weight-for-Age Curve Visualizer
        item {
            GrowthCurveGaugeCard(
                titleAr = "منحنى الوزن بالنسبة للعمر (Weight-for-Age)",
                titleEn = "Weight-for-Age Curve",
                actualValueStr = "${growthResult.weightKg} kg",
                percentile = growthResult.weightPercentile,
                zScore = growthResult.weightZScore,
                categoryText = if (isAr) growthResult.weightCategoryAr else growthResult.weightCategoryEn,
                p3 = growthResult.weightP3,
                p15 = growthResult.weightP15,
                p50 = growthResult.weightP50,
                p85 = growthResult.weightP85,
                p97 = growthResult.weightP97,
                unit = "kg",
                isAr = isAr
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 2. Length/Height-for-Age Curve Visualizer
        item {
            GrowthCurveGaugeCard(
                titleAr = "منحنى الطول بالنسبة للعمر (Length/Height-for-Age)",
                titleEn = "Height-for-Age Curve",
                actualValueStr = "${growthResult.heightCm} cm",
                percentile = growthResult.heightPercentile,
                zScore = growthResult.heightZScore,
                categoryText = if (isAr) growthResult.heightCategoryAr else growthResult.heightCategoryEn,
                p3 = growthResult.heightP3,
                p15 = growthResult.heightP15,
                p50 = growthResult.heightP50,
                p85 = growthResult.heightP85,
                p97 = growthResult.heightP97,
                unit = "cm",
                isAr = isAr
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 3. BMI-for-Age Card
        item {
            ClinicalCard(
                title = if (isAr) "مؤشر كتلة الجسم بالنسبة للعمر (BMI-for-Age)" else "BMI-for-Age Analysis",
                badgeText = "${growthResult.bmi} kg/m²",
                alertLevel = if (growthResult.bmiPercentile < 5.0 || growthResult.bmiPercentile >= 95.0) AlertLevel.WARNING else AlertLevel.SAFE
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isAr) growthResult.bmiCategoryAr else growthResult.bmiCategoryEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = getPercentileColor(growthResult.bmiPercentile)
                        )
                        Text(
                            text = "${if (isAr) "المئين:" else "Percentile:"} ${growthResult.bmiPercentile}% (Z-Score: ${growthResult.bmiZScore})",
                            fontSize = 11.sp,
                            color = SlateMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(getPercentileColor(growthResult.bmiPercentile).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${growthResult.bmiPercentile}th %ile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = getPercentileColor(growthResult.bmiPercentile)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { (growthResult.bmiPercentile / 100.0).toFloat().coerceIn(0f, 1f) },
                    color = getPercentileColor(growthResult.bmiPercentile),
                    trackColor = SlateBorder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "<5% Underweight", fontSize = 9.sp, color = SlateMuted)
                    Text(text = "50% Median", fontSize = 9.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                    Text(text = ">95% Obese", fontSize = 9.sp, color = SlateMuted)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // === 5. ACTION BUTTONS: COPY NOTE & SHARE ===
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val note = if (isAr) {
                            """
                            📊 [تقرير منحنيات نمو الطفل - WHO Standards]
                            المريض: ${patient.name}
                            العمر: ${growthResult.ageMonths} شهر | الجنس: ${if (isMale) "ذكر" else "أنثى"}
                            • الوزن: ${growthResult.weightKg} كجم (المئين: ${growthResult.weightPercentile}% | Z: ${growthResult.weightZScore}) -> ${growthResult.weightCategoryAr}
                            • الطول: ${growthResult.heightCm} سم (المئين: ${growthResult.heightPercentile}% | Z: ${growthResult.heightZScore}) -> ${growthResult.heightCategoryAr}
                            • كتلة الجسم BMI: ${growthResult.bmi} kg/m² (المئين: ${growthResult.bmiPercentile}%) -> ${growthResult.bmiCategoryAr}
                            التقييم العام: ${growthResult.overallAssessmentAr}
                            المصدر: تطبيق NeoCare Pro Master
                            """.trimIndent()
                        } else {
                            """
                            📊 [WHO Pediatric Growth Assessment Report]
                            Patient: ${patient.name}
                            Age: ${growthResult.ageMonths} months | Gender: ${if (isMale) "Male" else "Female"}
                            • Weight: ${growthResult.weightKg} kg (${growthResult.weightPercentile}th %ile | Z: ${growthResult.weightZScore}) -> ${growthResult.weightCategoryEn}
                            • Height: ${growthResult.heightCm} cm (${growthResult.heightPercentile}th %ile | Z: ${growthResult.heightZScore}) -> ${growthResult.heightCategoryEn}
                            • BMI: ${growthResult.bmi} kg/m² (${growthResult.bmiPercentile}th %ile) -> ${growthResult.bmiCategoryEn}
                            Clinical Summary: ${growthResult.overallAssessmentEn}
                            Workstation: NeoCare Pro Master
                            """.trimIndent()
                        }
                        clipboard.setPrimaryClip(ClipData.newPlainText("Growth Report", note))
                        viewModel.showSnackbar(if (isAr) "تم نسخ تقرير منحنيات النمو" else "Growth report copied")
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isAr) "نسخ التقرير" else "Copy Note", fontSize = 11.5.sp)
                }

                Button(
                    onClick = {
                        val note = if (isAr) {
                            """
                            📊 [تقرير منحنيات نمو الطفل - WHO Standards]
                            المريض: ${patient.name}
                            العمر: ${growthResult.ageMonths} شهر | الجنس: ${if (isMale) "ذكر" else "أنثى"}
                            • الوزن: ${growthResult.weightKg} كجم (المئين: ${growthResult.weightPercentile}% | Z: ${growthResult.weightZScore}) -> ${growthResult.weightCategoryAr}
                            • الطول: ${growthResult.heightCm} سم (المئين: ${growthResult.heightPercentile}% | Z: ${growthResult.heightZScore}) -> ${growthResult.heightCategoryAr}
                            • كتلة الجسم BMI: ${growthResult.bmi} kg/m² (المئين: ${growthResult.bmiPercentile}%) -> ${growthResult.bmiCategoryAr}
                            التقييم: ${growthResult.overallAssessmentAr}
                            """.trimIndent()
                        } else {
                            """
                            📊 [WHO Pediatric Growth Assessment Report]
                            Patient: ${patient.name}
                            Age: ${growthResult.ageMonths} months | Gender: ${if (isMale) "Male" else "Female"}
                            • Weight: ${growthResult.weightKg} kg (${growthResult.weightPercentile}th %ile | Z: ${growthResult.weightZScore}) -> ${growthResult.weightCategoryEn}
                            • Height: ${growthResult.heightCm} cm (${growthResult.heightPercentile}th %ile | Z: ${growthResult.heightZScore}) -> ${growthResult.heightCategoryEn}
                            • BMI: ${growthResult.bmi} kg/m² (${growthResult.bmiPercentile}th %ile) -> ${growthResult.bmiCategoryEn}
                            Clinical Summary: ${growthResult.overallAssessmentEn}
                            """.trimIndent()
                        }
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, note)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, if (isAr) "مشاركة تقرير النمو" else "Share Growth Report"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isAr) "مشاركة التقرير" else "Share Report", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// === HELPER COMPOSABLE: INTERACTIVE CURVE GAUGE CARD ===

@Composable
fun GrowthCurveGaugeCard(
    titleAr: String,
    titleEn: String,
    actualValueStr: String,
    percentile: Double,
    zScore: Double,
    categoryText: String,
    p3: Double,
    p15: Double,
    p50: Double,
    p85: Double,
    p97: Double,
    unit: String,
    isAr: Boolean
) {
    ClinicalCard(
        title = if (isAr) titleAr else titleEn,
        badgeText = "$actualValueStr (${percentile}%ile)",
        alertLevel = if (percentile < 3.0 || percentile > 97.0) AlertLevel.DANGER else if (percentile < 15.0 || percentile > 85.0) AlertLevel.WARNING else AlertLevel.SAFE
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = categoryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = getPercentileColor(percentile)
                )
                Text(
                    text = "Z-Score: $zScore • ${if (isAr) "المئين المحسوب:" else "Percentile:"} $percentile%",
                    fontSize = 10.5.sp,
                    color = SlateMuted
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(getPercentileColor(percentile).copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${percentile}th %ile",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = getPercentileColor(percentile)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Custom Visual Percentile Bar with WHO markers (P3, P15, P50, P85, P97)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SlateBg)
                .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Background zones
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(3f).fillMaxHeight().background(DangerRedSoft))
                Box(modifier = Modifier.weight(12f).fillMaxHeight().background(WarnAmberSoft))
                Box(modifier = Modifier.weight(70f).fillMaxHeight().background(SafeGreenSoft.copy(alpha = 0.5f)))
                Box(modifier = Modifier.weight(12f).fillMaxHeight().background(WarnAmberSoft))
                Box(modifier = Modifier.weight(3f).fillMaxHeight().background(DangerRedSoft))
            }

            // Current child cursor needle
            val normProgress = (percentile / 100.0).toFloat().coerceIn(0.02f, 0.98f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val xPos = size.width * normProgress
                // Vertical marker line
                drawLine(
                    color = Color.Black,
                    start = Offset(xPos, 0f),
                    end = Offset(xPos, size.height),
                    strokeWidth = 3.dp.toPx()
                )
                // Marker head
                drawCircle(
                    color = Color.Black,
                    radius = 4.dp.toPx(),
                    center = Offset(xPos, size.height / 2f)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Standard WHO Percentile Reference points strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "3% ($p3$unit)", fontSize = 9.sp, color = DangerRed, fontWeight = FontWeight.Bold)
            Text(text = "15% ($p15$unit)", fontSize = 9.sp, color = WarnAmber)
            Text(text = "50% ($p50$unit)", fontSize = 9.5.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
            Text(text = "85% ($p85$unit)", fontSize = 9.sp, color = WarnAmber)
            Text(text = "97% ($p97$unit)", fontSize = 9.sp, color = DangerRed, fontWeight = FontWeight.Bold)
        }
    }
}

fun getPercentileColor(percentile: Double): Color = when {
    percentile < 3.0 || percentile > 97.0 -> DangerRed
    percentile < 15.0 || percentile > 85.0 -> WarnAmber
    else -> SafeGreen
}
