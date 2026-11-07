package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoCareViewModel
import com.example.ui.viewmodel.PatientAgeUnit
import com.example.ui.viewmodel.WorkstationState
import java.util.Locale

data class QuickPatientPreset(
    val titleAr: String,
    val titleEn: String,
    val weightKg: Double,
    val ageValue: Int,
    val ageUnit: PatientAgeUnit,
    val subtitleAr: String,
    val subtitleEn: String
)

/**
 * Interactive Live Patient Weight & Age Adjuster Panel
 * Allows adjusting weight in kg and age directly,
 * and immediately recalculates all fluid, dosing, and scoring outputs.
 */
@Composable
fun InteractivePatientWeightAgeAdjuster(
    state: WorkstationState,
    viewModel: NeoCareViewModel,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    val patient = state.activePatient
    var isExpanded by remember { mutableStateOf(false) }

    val presets = remember {
        listOf(
            QuickPatientPreset("خديج NICU", "Preterm NICU", 1.20, 3, PatientAgeUnit.DAYS, "1.2 كجم / 3 أيام", "1.2 kg / 3 days"),
            QuickPatientPreset("وليد مكتمل", "Term Newborn", 3.20, 5, PatientAgeUnit.DAYS, "3.2 كجم / 5 أيام", "3.2 kg / 5 days"),
            QuickPatientPreset("رضيع 6 أشهر", "Infant 6m", 7.50, 6, PatientAgeUnit.MONTHS, "7.5 كجم / 6 شهور", "7.5 kg / 6 months"),
            QuickPatientPreset("طفل دارج 2س", "Toddler 2y", 12.0, 2, PatientAgeUnit.YEARS, "12 كجم / سنتين", "12 kg / 2 years"),
            QuickPatientPreset("طفل 6 سنوات", "Child 6y", 20.0, 6, PatientAgeUnit.YEARS, "20 كجم / 6 سنوات", "20 kg / 6 years"),
            QuickPatientPreset("مراهق 14 سنة", "Adolescent 14y", 50.0, 14, PatientAgeUnit.YEARS, "50 كجم / 14 سنة", "50 kg / 14 years")
        )
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, TealPrimary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Summary & Expand Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TealLightContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ChildCare,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isArabic) "بيانات الطفل الحالية: " else "Active Child: ",
                                fontSize = 12.sp,
                                color = SlateMuted
                            )
                            Text(
                                text = patient.name,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateDark
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⚖ ${String.format(Locale.US, "%.2f", patient.weightKg)} ${if (isArabic) "كجم" else "kg"}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TealPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "•  👶 ${if (isArabic) patient.ageDisplayAr else patient.ageDisplayEn}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CyanAccent
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TealLightContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isArabic) "تعديل فوري ⚙" else "Live Edit ⚙",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Quick Stepper Row (Always visible for rapid bedside titration)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlateBg)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isArabic) "تعديل الوزن سريعاً:" else "Quick Weight Adjust:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateDark
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilledTonalButton(
                        onClick = { viewModel.adjustPatientWeightBy(-1.0) },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("-1 kg", fontSize = 10.5.sp)
                    }
                    FilledTonalButton(
                        onClick = { viewModel.adjustPatientWeightBy(-0.1) },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("-0.1", fontSize = 10.5.sp)
                    }
                    FilledTonalButton(
                        onClick = { viewModel.adjustPatientWeightBy(0.1) },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("+0.1", fontSize = 10.5.sp)
                    }
                    FilledTonalButton(
                        onClick = { viewModel.adjustPatientWeightBy(1.0) },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("+1 kg", fontSize = 10.5.sp)
                    }
                }
            }

            // Expanded Precise Inputs & Age Unit Selector
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    HorizontalDivider(color = SlateBorder, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Editable Weight in kg
                        OutlinedTextField(
                            value = state.weightInputKg,
                            onValueChange = { viewModel.updateWeightInputKg(it) },
                            label = { Text(if (isArabic) "الوزن الدقيق (كجم)" else "Exact Weight (kg)", fontSize = 10.sp) },
                            suffix = { Text("kg", fontSize = 11.sp, color = TealPrimary, fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.1f)
                        )

                        // Editable Age Value
                        OutlinedTextField(
                            value = state.currentAgeValue.toString(),
                            onValueChange = { input ->
                                val v = input.filter { it.isDigit() }.toIntOrNull()
                                if (v != null) {
                                    viewModel.updatePatientAge(v, state.currentAgeUnit)
                                }
                            },
                            label = { Text(if (isArabic) "قيمة العمر" else "Age Value", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(0.9f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Age Unit Chips (Days / Months / Years)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isArabic) "وحدة العمر:" else "Age Unit:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateMuted
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            PatientAgeUnit.values().forEach { unit ->
                                FilterChip(
                                    selected = state.currentAgeUnit == unit,
                                    onClick = { viewModel.updatePatientAge(state.currentAgeValue, unit) },
                                    label = {
                                        Text(
                                            text = if (isArabic) unit.labelAr else unit.labelEn,
                                            fontSize = 10.5.sp,
                                            fontWeight = if (state.currentAgeUnit == unit) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // One-Tap Presets for Common Pediatric Stages
                    Text(
                        text = if (isArabic) "أعمار وأوزان شائعة سريعة (ضغطة واحدة):" else "Quick Stage Presets (1-Tap):",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateMuted
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(presets) { preset ->
                            SuggestionChip(
                                onClick = {
                                    viewModel.quickSetPatientProfile(
                                        weightKg = preset.weightKg,
                                        ageValue = preset.ageValue,
                                        ageUnit = preset.ageUnit,
                                        name = if (isArabic) preset.titleAr else preset.titleEn
                                    )
                                },
                                label = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = if (isArabic) preset.titleAr else preset.titleEn, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(text = if (isArabic) preset.subtitleAr else preset.subtitleEn, fontSize = 9.sp, color = SlateMuted)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
