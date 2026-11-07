package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.MedicalServices
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
import com.example.data.model.ABGInput
import com.example.data.model.AlertLevel
import com.example.data.repository.ClinicalFormulas
import com.example.ui.components.ClinicalCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.UnitInputField
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoCareViewModel
import com.example.ui.viewmodel.WorkstationState

@Composable
fun ABGVentScreen(
    state: WorkstationState,
    viewModel: NeoCareViewModel,
    modifier: Modifier = Modifier
) {
    val isAr = state.isArabic
    val abg = state.abgInput

    // ABG interpretation
    val abgResult = remember(abg) {
        ClinicalFormulas.interpretABG(abg)
    }

    // Airway equipment sizing
    val airway = remember(state.activePatient.currentWeightGrams, state.activePatient.gestationalWeeks) {
        ClinicalFormulas.calculateAirwayEquipment(
            weightGrams = state.activePatient.currentWeightGrams,
            gestationalWeeks = state.activePatient.gestationalWeeks
        )
    }

    // Local temporary string states for editing
    var phStr by remember(abg.ph) { mutableStateOf(abg.ph.toString()) }
    var pco2Str by remember(abg.pco2) { mutableStateOf(abg.pco2.toString()) }
    var po2Str by remember(abg.po2) { mutableStateOf(abg.po2.toString()) }
    var hco3Str by remember(abg.hco3) { mutableStateOf(abg.hco3.toString()) }
    var fio2Str by remember(abg.fio2Percent) { mutableStateOf(abg.fio2Percent.toString()) }
    var mapStr by remember(abg.mapVentilator) { mutableStateOf(abg.mapVentilator.toString()) }

    fun commitABG() {
        viewModel.updateABG(
            ABGInput(
                ph = phStr.toDoubleOrNull() ?: 7.35,
                pco2 = pco2Str.toDoubleOrNull() ?: 40.0,
                po2 = po2Str.toDoubleOrNull() ?: 65.0,
                hco3 = hco3Str.toDoubleOrNull() ?: 22.0,
                fio2Percent = fio2Str.toDoubleOrNull() ?: 21.0,
                mapVentilator = mapStr.toDoubleOrNull() ?: 8.0
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // === 1. AIRWAY & RESUSCITATION EQUIPMENT SIZING ===
        item {
            SectionHeader(
                title = if (isAr) "قياسات مجرى الهواء وقساطر السرة" else "Airway & Umbilical Catheter Sizing",
                subtitle = if (isAr) "مخصصة لوزن ${state.activePatient.name} (${String.format("%.2f", state.activePatient.weightKg)} كجم - ${state.activePatient.gestationalWeeks} أسبوع)"
                else "Customized for ${state.activePatient.name} (${String.format("%.2f", state.activePatient.weightKg)} kg, ${state.activePatient.gestationalWeeks}w)",
                icon = Icons.Default.MedicalServices
            )

            ClinicalCard(
                title = if (isAr) "مواصفات الأنبوبة الحنجرية (ETT) والمنظار" else "Endotracheal Tube (ETT) & Equipment",
                badgeText = "ETT ${airway.ettInternalDiameterMm} mm",
                alertLevel = AlertLevel.SAFE
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AirwayStatBox(
                        title = if (isAr) "قطر الأنبوبة (ID)" else "ETT Size (ID)",
                        value = "${airway.ettInternalDiameterMm} mm",
                        sub = if (isAr) "غير مقيدة (Uncuffed)" else "Uncuffed",
                        modifier = Modifier.weight(1f)
                    )

                    AirwayStatBox(
                        title = if (isAr) "عمق الشفة (Lip)" else "Lip-to-Tip Depth",
                        value = "${airway.ettDepthAtLipCm} cm",
                        sub = if (isAr) "الوزن + 6 سم" else "NRP formula",
                        highlight = true,
                        modifier = Modifier.weight(1f)
                    )

                    AirwayStatBox(
                        title = if (isAr) "شفرة المنظار" else "Laryngoscope",
                        value = airway.laryngoscopeBlade,
                        sub = "Straight blade",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Umbilical Lines & Suction
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AirwayStatBox(
                        title = if (isAr) "قسطرة وريد السرة (UVC)" else "UVC Depth",
                        value = "${airway.uvcDepthCm} cm",
                        sub = "IVC / RA junction",
                        modifier = Modifier.weight(1f)
                    )

                    AirwayStatBox(
                        title = if (isAr) "قسطرة شريان السرة العالية" else "UAC Depth (High)",
                        value = "${airway.uacDepthHighCm} cm",
                        sub = "T6 - T8 vertebral",
                        modifier = Modifier.weight(1f)
                    )

                    AirwayStatBox(
                        title = if (isAr) "قسطرة الشفط" else "Suction Catheter",
                        value = airway.suctionCatheterFr,
                        sub = "French size",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // === 2. ARTERIAL BLOOD GAS (ABG) INTERPRETER ===
        item {
            SectionHeader(
                title = if (isAr) "مفسر غازات الدم الشرياني (ABG Interpreter)" else "Arterial Blood Gas (ABG) Interpreter",
                subtitle = if (isAr) "تحليل التوازن الحمضي القلوي ومؤشر الأكسجة (OI) وتعديلات التنفس الصناعي" else "Acid-base balance, Oxygenation Index (OI), and ventilator adjustments",
                icon = Icons.Default.Bloodtype
            )

            // Input Fields Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UnitInputField(
                    value = phStr,
                    onValueChange = {
                        phStr = it
                        commitABG()
                    },
                    label = "pH",
                    helperText = "7.35 - 7.45",
                    modifier = Modifier.weight(1f),
                    testTag = "abg_ph_input"
                )

                UnitInputField(
                    value = pco2Str,
                    onValueChange = {
                        pco2Str = it
                        commitABG()
                    },
                    label = "pCO2",
                    unit = "mmHg",
                    helperText = "35 - 45",
                    modifier = Modifier.weight(1f),
                    testTag = "abg_pco2_input"
                )

                UnitInputField(
                    value = po2Str,
                    onValueChange = {
                        po2Str = it
                        commitABG()
                    },
                    label = "pO2",
                    unit = "mmHg",
                    helperText = "50 - 70",
                    modifier = Modifier.weight(1f),
                    testTag = "abg_po2_input"
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UnitInputField(
                    value = hco3Str,
                    onValueChange = {
                        hco3Str = it
                        commitABG()
                    },
                    label = "HCO3-",
                    unit = "mEq/L",
                    helperText = "20 - 26",
                    modifier = Modifier.weight(1f),
                    testTag = "abg_hco3_input"
                )

                UnitInputField(
                    value = fio2Str,
                    onValueChange = {
                        fio2Str = it
                        commitABG()
                    },
                    label = "FiO2",
                    unit = "%",
                    helperText = "21 - 100%",
                    modifier = Modifier.weight(1f),
                    testTag = "abg_fio2_input"
                )

                UnitInputField(
                    value = mapStr,
                    onValueChange = {
                        mapStr = it
                        commitABG()
                    },
                    label = "MAP",
                    unit = "cmH2O",
                    helperText = "Mean Airway Press",
                    modifier = Modifier.weight(1f),
                    testTag = "abg_map_input"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ABG Analysis Output Card
            ClinicalCard(
                title = if (isAr) "التشخيص السريري لغازات الدم" else "ABG Clinical Diagnostic Output",
                badgeText = if (isAr) abgResult.primaryDisorderAr else abgResult.primaryDisorderEn,
                alertLevel = abgResult.alertLevel
            ) {
                // Primary Diagnosis Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (abgResult.alertLevel) {
                                AlertLevel.SAFE -> SafeGreenSoft
                                AlertLevel.WARNING -> WarnAmberSoft
                                AlertLevel.DANGER -> DangerRedSoft
                            }
                        )
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = if (isAr) abgResult.primaryDisorderAr else abgResult.primaryDisorderEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = when (abgResult.alertLevel) {
                                AlertLevel.SAFE -> SafeGreen
                                AlertLevel.WARNING -> WarnAmber
                                AlertLevel.DANGER -> DangerRed
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isAr) "التعويض: ${abgResult.compensationAr}" else "Compensation: ${abgResult.compensationEn}",
                            fontSize = 11.5.sp,
                            color = SlateDark
                        )
                        Text(
                            text = if (isAr) "الأكسجة: ${abgResult.oxygenationAr}" else "Oxygenation: ${abgResult.oxygenationEn}",
                            fontSize = 11.5.sp,
                            color = SlateDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Indices: Oxygenation Index (OI) and P/F ratio
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateBg)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Oxygenation Index (OI)", fontSize = 10.5.sp, color = SlateMuted)
                        Text(
                            text = if (abgResult.oxygenationIndex != null) "${abgResult.oxygenationIndex}" else "--",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (abgResult.oxygenationIndex != null && abgResult.oxygenationIndex >= 25.0) DangerRed else TealPrimary
                        )
                        Text(text = if (isAr) "<15 خفيف، >25 حرج" else "<15 mild, >25 severe", fontSize = 9.sp, color = SlateMuted)
                    }

                    Divider(modifier = Modifier.height(28.dp).width(1.dp), color = SlateBorder)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "PaO2 / FiO2 Ratio", fontSize = 10.5.sp, color = SlateMuted)
                        Text(
                            text = if (abgResult.pfRatio != null) "${abgResult.pfRatio.toInt()}" else "--",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SlateDark
                        )
                        Text(text = if (isAr) ">300 طبيعي، <200 ضائقة" else ">300 normal, <200 ARDS", fontSize = 9.sp, color = SlateMuted)
                    }
                }

                // Ventilator Recommendations
                val recs = if (isAr) abgResult.ventilatorRecommendationsAr else abgResult.ventilatorRecommendationsEn
                if (recs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isAr) "توصيات سريرية لضبط جهاز التنفس الصناعي:" else "Bedside Ventilator Management Tips:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = CyanAccent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    recs.forEach { tip ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(text = "• ", color = TealPrimary, fontWeight = FontWeight.Bold)
                            Text(text = tip, fontSize = 11.sp, color = SlateDark)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AirwayStatBox(
    title: String,
    value: String,
    sub: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (highlight) TealLightContainer else SlateBg)
            .border(
                0.8.dp,
                if (highlight) TealPrimary.copy(alpha = 0.5f) else SlateBorder,
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Column {
            Text(text = title, fontSize = 10.sp, color = if (highlight) TealOnContainer else SlateMuted, fontWeight = FontWeight.SemiBold)
            Text(text = value, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = if (highlight) TealPrimary else SlateDark)
            Text(text = sub, fontSize = 9.sp, color = if (highlight) TealPrimary else SlateMuted)
        }
    }
}
