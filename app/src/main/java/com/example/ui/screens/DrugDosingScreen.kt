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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertLevel
import com.example.data.model.DrugCategory
import com.example.data.model.DrugInfo
import com.example.data.model.PediatricFormulation
import com.example.data.model.ReconstitutionGuide
import com.example.data.repository.DrugCatalog
import com.example.ui.components.ClinicalCard
import com.example.ui.components.InteractivePatientWeightAgeAdjuster
import com.example.ui.components.QuickPatientPreset
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoCareViewModel
import com.example.ui.viewmodel.PatientAgeUnit
import com.example.ui.viewmodel.WorkstationState
import java.util.Locale

enum class TargetAgeFilter(val labelEn: String, val labelAr: String) {
    ALL("All Ages (0-18y)", "جميع الأعمار (0-18 سنة)"),
    NEONATE("Neonates (0-28d)", "حديثي الولادة (0-28 يوم)"),
    INFANT_CHILD("Infants & Children (1m-12y)", "الرضع والأطفال (شهر-12س)"),
    ADOLESCENT("Adolescents (12-18y)", "المراهقون (12-18 سنة)")
}

@Composable
fun DrugDosingScreen(
    state: WorkstationState,
    viewModel: NeoCareViewModel,
    modifier: Modifier = Modifier
) {
    val isAr = state.isArabic
    val weightKg = state.activePatient.weightKg
    val isNeonate = state.activePatient.isNeonate
    var selectedAgeFilter by remember { mutableStateOf(TargetAgeFilter.ALL) }

    val filteredDrugs = remember(state.drugSearchQuery, state.selectedDrugCategory, selectedAgeFilter) {
        DrugCatalog.drugs.filter { drug ->
            val matchesCategory = state.selectedDrugCategory == null || drug.category == state.selectedDrugCategory
            val matchesSearch = state.drugSearchQuery.isBlank() ||
                    drug.nameEn.contains(state.drugSearchQuery, ignoreCase = true) ||
                    drug.nameAr.contains(state.drugSearchQuery, ignoreCase = true) ||
                    drug.category.labelEn.contains(state.drugSearchQuery, ignoreCase = true) ||
                    drug.category.labelAr.contains(state.drugSearchQuery, ignoreCase = true) ||
                    drug.administrationGuideAr.contains(state.drugSearchQuery, ignoreCase = true) ||
                    drug.administrationGuideEn.contains(state.drugSearchQuery, ignoreCase = true) ||
                    (drug.reconstitution?.diluentAr?.contains(state.drugSearchQuery, ignoreCase = true) == true) ||
                    (drug.reconstitution?.powderVolumeStepsAr?.contains(state.drugSearchQuery, ignoreCase = true) == true)

            val matchesAge = when (selectedAgeFilter) {
                TargetAgeFilter.ALL -> true
                TargetAgeFilter.NEONATE -> drug.ageBands.any { it.ageGroupEn.contains("Neonate", ignoreCase = true) } ||
                        drug.ageRangeEn.contains("Birth", ignoreCase = true) || drug.ageRangeAr.contains("حديثي الولادة")
                TargetAgeFilter.INFANT_CHILD -> drug.ageBands.any { it.ageGroupEn.contains("Infant", ignoreCase = true) || it.ageGroupEn.contains("Child", ignoreCase = true) } ||
                        drug.ageRangeEn.contains("18", ignoreCase = true) || drug.ageRangeEn.contains("Child", ignoreCase = true)
                TargetAgeFilter.ADOLESCENT -> drug.ageBands.any { it.ageGroupEn.contains("Adolescent", ignoreCase = true) } ||
                        drug.ageRangeEn.contains("18", ignoreCase = true)
            }

            matchesCategory && matchesSearch && matchesAge
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // === SEARCH WORKSTATION HEADER ===
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Medication,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isAr) "دليل وبحث أدوية ومضادات الأطفال والولدان" else "Pediatric & Neonatal Drug Workstation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                        Text(
                            text = if (isAr) "الجرعات، حل البودرة، المحاليل، وطرق الإعطاء (0-18 سنة)" else "Dosing, Reconstitution, Diluents & Routes (0-18y)",
                            fontSize = 11.sp,
                            color = SlateMuted
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TealLightContainer)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${filteredDrugs.size} ${if (isAr) "دواء" else "drugs"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ==============================================================
            // === INTERACTIVE PATIENT WEIGHT & AGE ADJUSTER (REQUESTED) ====
            // ==============================================================
            InteractivePatientWeightAgeAdjuster(
                state = state,
                viewModel = viewModel,
                isArabic = isAr
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Prominent Search Input Box
            OutlinedTextField(
                value = state.drugSearchQuery,
                onValueChange = { viewModel.updateDrugSearch(it) },
                placeholder = {
                    Text(
                        text = if (isAr) "ابحث عن دواء (أمبيسيلين، سيفوتاكسيم، سيفترياكسون، فانكومايسين، أوجمنتين)..."
                        else "Search medication (Ampicillin, Cefotaxime, Ceftriaxone, Vancomycin, Augmentin)...",
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TealPrimary)
                },
                trailingIcon = if (state.drugSearchQuery.isNotBlank()) {
                    {
                        IconButton(onClick = { viewModel.updateDrugSearch("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = SlateMuted)
                        }
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = SlateBorder,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("drug_search_input")
            )

            // Age Band Filter Chips (0 to 18 Years highlight)
            Text(
                text = if (isAr) "تصفية حسب الفئة العمرية:" else "Filter by Age Bracket:",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlateMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(TargetAgeFilter.values()) { ageFilter ->
                    FilterChip(
                        selected = selectedAgeFilter == ageFilter,
                        onClick = { selectedAgeFilter = ageFilter },
                        label = {
                            Text(
                                text = if (isAr) ageFilter.labelAr else ageFilter.labelEn,
                                fontSize = 11.sp,
                                fontWeight = if (selectedAgeFilter == ageFilter) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Quick Search Suggestion Tags
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val tags = if (isAr) {
                    listOf("أمبيسيلين", "سيفوتاكسيم", "سيفترياكسون", "فانكومايسين", "أوجمنتين", "باراسيتامول", "سالبوتامول", "كافيين", "فينوباربيتال", "أدرينالين", "IVIG")
                } else {
                    listOf("Ampicillin", "Cefotaxime", "Ceftriaxone", "Vancomycin", "Augmentin", "Paracetamol", "Salbutamol", "Caffeine", "Phenobarbital", "Epinephrine", "IVIG")
                }
                items(tags) { tag ->
                    AssistChip(
                        onClick = { viewModel.updateDrugSearch(tag) },
                        label = { Text(text = tag, fontSize = 10.5.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = SlateBg,
                            labelColor = TealPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(0.6.dp, SlateBorder)
                    )
                }
            }

            // Category Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedDrugCategory == null,
                        onClick = { viewModel.selectDrugCategory(null) },
                        label = {
                            Text(text = if (isAr) "جميع الأقسام" else "All Categories", fontSize = 11.5.sp)
                        }
                    )
                }

                items(DrugCategory.values()) { category ->
                    FilterChip(
                        selected = state.selectedDrugCategory == category,
                        onClick = { viewModel.selectDrugCategory(if (state.selectedDrugCategory == category) null else category) },
                        label = {
                            Text(text = if (isAr) category.labelAr else category.labelEn, fontSize = 11.sp)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }

        // List of Drugs
        if (filteredDrugs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = SlateMuted, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isAr) "لا توجد نتائج مطابقة لبحثك: '${state.drugSearchQuery}'" else "No medications found matching '${state.drugSearchQuery}'",
                            color = SlateMuted,
                            fontSize = 13.sp
                        )
                        TextButton(onClick = {
                            viewModel.updateDrugSearch("")
                            viewModel.selectDrugCategory(null)
                            selectedAgeFilter = TargetAgeFilter.ALL
                        }) {
                            Text(text = if (isAr) "عرض جميع الأدوية" else "Show All Drugs", color = TealPrimary)
                        }
                    }
                }
            }
        } else {
            items(filteredDrugs, key = { it.id }) { drug ->
                InteractiveDrugDoseCard(
                    drug = drug,
                    weightKg = weightKg,
                    isNeonate = isNeonate,
                    patientAgeDisplay = if (isAr) state.activePatient.ageDisplayAr else state.activePatient.ageDisplayEn,
                    isArabic = isAr
                )
            }
        }
    }
}

@Composable
fun InteractiveDrugDoseCard(
    drug: DrugInfo,
    weightKg: Double,
    isNeonate: Boolean,
    patientAgeDisplay: String,
    isArabic: Boolean
) {
    var showCustomDose by remember { mutableStateOf(false) }
    var showAgeBands by remember { mutableStateOf(false) }
    var showReconstitutionDetails by remember { mutableStateOf(drug.reconstitution != null) }
    var customDoseInput by remember(drug) { mutableStateOf(drug.dosePerKgMin.toString()) }

    // Selected Formulation for volume calculation
    var selectedFormulation by remember(drug) {
        mutableStateOf<PediatricFormulation?>(drug.formulations.firstOrNull())
    }

    val activeDosePerKg = if (showCustomDose) {
        customDoseInput.toDoubleOrNull() ?: drug.dosePerKgMin
    } else {
        drug.dosePerKgMin
    }

    val baseCalc = remember(drug, weightKg, activeDosePerKg) {
        drug.calculateDose(weightKg, customDosePerKg = activeDosePerKg)
    }

    // Recalculate volume if a specific pediatric formulation is selected
    val finalVolumeMl = remember(baseCalc, selectedFormulation) {
        if (selectedFormulation != null && selectedFormulation!!.concentrationMgPerMl > 0) {
            baseCalc.doseAmount / selectedFormulation!!.concentrationMgPerMl
        } else {
            baseCalc.volumeMl
        }
    }

    val isEmergency = drug.category == DrugCategory.EMERGENCY

    // Critical Neonatal Contraindication Check (e.g. Ceftriaxone in neonates <= 28 days)
    val isContraindicatedInNeonate = isNeonate && drug.id == "ceftriaxone"

    ClinicalCard(
        title = if (isArabic) "${drug.nameAr} (${drug.nameEn})" else "${drug.nameEn} - ${drug.nameAr}",
        icon = Icons.Default.Medication,
        badgeText = if (finalVolumeMl > 0) "${String.format(Locale.US, "%.2f", finalVolumeMl)} mL" else "${String.format(Locale.US, "%.2f", baseCalc.doseAmount)} ${baseCalc.doseUnit}",
        alertLevel = if (isContraindicatedInNeonate || isEmergency) AlertLevel.DANGER else AlertLevel.SAFE
    ) {
        // Neonatal Contraindication Emergency Warning (AAP & Pediatric Safety Alert)
        if (isContraindicatedInNeonate) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DangerRedSoft)
                    .border(1.2.dp, DangerRed, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Contraindicated",
                        tint = DangerRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isArabic) "🚫 تحذير خطير: ممنوع منعاً باتاً في حديثي الولادة (<= 28 يوماً)"
                            else "🚫 CONTRAINDICATED in Neonates (<= 28 Days of Life)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = DangerRed
                        )
                        Text(
                            text = if (isArabic)
                                "الطفل الحالي عمره ($patientAgeDisplay). يزيح السيفترياكسون البيليروبين عن الألبومين مسبباً اليرقان النووي (Kernicterus) والترسب المراري. البديل الآمن المعتمد لحديثي الولادة هو: سيفوتاكسيم (Cefotaxime)."
                            else
                                "Current patient is ($patientAgeDisplay). Ceftriaxone displaces bilirubin from albumin causing kernicterus & biliary sludging. Safe first-line neonatal alternative: Cefotaxime.",
                            fontSize = 11.sp,
                            color = SlateDark,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Age Range Badge & Max Single Dose
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(TealLightContainer)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "👶 ${if (isArabic) drug.ageRangeAr else drug.ageRangeEn}",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TealPrimary
                )
            }

            if (drug.maxSingleDose != null) {
                Text(
                    text = "${if (isArabic) "الحد الأقصى للجرعة:" else "Max single:"} ${drug.maxSingleDose.toInt()} mg",
                    fontSize = 10.5.sp,
                    color = SlateMuted
                )
            }
        }

        // Dosage description
        Text(
            text = if (isArabic) drug.doseDescriptionAr else drug.doseDescriptionEn,
            fontWeight = FontWeight.Bold,
            fontSize = 12.5.sp,
            color = SlateDark
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Computed Dose Box (Live updated based on patient weight)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (isEmergency || isContraindicatedInNeonate) DangerRedSoft else SlateBg)
                .border(
                    0.8.dp,
                    if (isEmergency || isContraindicatedInNeonate) DangerRed.copy(alpha = 0.3f) else SlateBorder,
                    RoundedCornerShape(8.dp)
                )
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isArabic) "الجرعة المحسوبة لوزن (${String.format(Locale.US, "%.2f", weightKg)} كجم):"
                    else "Calculated Dose (${String.format(Locale.US, "%.2f", weightKg)} kg):",
                    fontSize = 11.sp,
                    color = if (isEmergency) DangerRed else SlateMuted
                )
                Text(
                    text = "${String.format(Locale.US, "%.2f", baseCalc.doseAmount)} ${baseCalc.doseUnit}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isEmergency) DangerRed else TealPrimary
                )
                if (baseCalc.isCappedAtMax) {
                    Text(
                        text = if (isArabic) "⚠️ محددة عند الحد الأقصى الآمن" else "⚠️ Capped at safe adult/max dose",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarnAmber
                    )
                }
            }

            if (finalVolumeMl > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isArabic) "حجم السحب بالسرنجة:" else "Draw-up Volume:",
                        fontSize = 11.sp,
                        color = if (isEmergency) DangerRed else SlateMuted
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.2f", finalVolumeMl)} mL",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isEmergency) DangerRed else CyanAccent
                    )
                    if (selectedFormulation != null) {
                        Text(
                            text = if (isArabic) selectedFormulation!!.nameAr else selectedFormulation!!.nameEn,
                            fontSize = 9.5.sp,
                            color = SlateMuted
                        )
                    }
                }
            }
        }

        // Available Formulations Selector Chips (if drug has multiple formulations)
        if (drug.formulations.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isArabic) "اختر التركيز أو الشكل الدوائي المتوفر لديك:" else "Available Formulations / Syrups:",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlateDark
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(drug.formulations) { formulation ->
                    val isSelected = selectedFormulation == formulation
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFormulation = formulation },
                        label = {
                            Text(
                                text = if (isArabic) formulation.nameAr else formulation.nameEn,
                                fontSize = 10.sp
                            )
                        }
                    )
                }
            }
        }

        // ==============================================================
        // === RECONSTITUTION & DILUTION GUIDE (USER EXPLICIT REQUEST) ==
        // ==============================================================
        if (drug.reconstitution != null) {
            val recon = drug.reconstitution
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CyanLightContainer.copy(alpha = 0.45f)),
                border = androidx.compose.foundation.BorderStroke(0.8.dp, CyanAccent.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Reconstitution Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showReconstitutionDetails = !showReconstitutionDetails },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Science,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "🧪 تحضير وحل الدواء البودرة وطريقة الإعطاء" else "🧪 Powder Reconstitution & Administration Guide",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                        }
                        Icon(
                            if (showReconstitutionDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // 1. Diluent (بيتحل في إيه)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = if (isArabic) "💧 بيتحل في إيه (المحلول): " else "💧 Diluent: ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                        Text(
                            text = if (isArabic) recon.diluentAr else recon.diluentEn,
                            fontSize = 11.sp,
                            color = TealPrimaryDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    AnimatedVisibility(visible = showReconstitutionDetails) {
                        Column(modifier = Modifier.padding(top = 6.dp)) {
                            // 2. Powder Volume Steps (بيتحل في قد إيه لو بودرة)
                            Text(
                                text = if (isArabic) "📏 كمية المحلول للحل (بيتحل في قد إيه لو بودرة):" else "📏 Reconstitution Volume & Dilution Steps:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(0.5.dp, SlateBorder, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = if (isArabic) recon.powderVolumeStepsAr else recon.powderVolumeStepsEn,
                                    fontSize = 10.5.sp,
                                    color = SlateDark,
                                    lineHeight = 15.sp
                                )
                            }

                            // 3. Administration Route & Rate (بيتاخد إزاي)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isArabic) "💉 بيتاخد إزاي (طريقة الإعطاء والتسريب):" else "💉 Administration Method & Infusion Rate:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(0.5.dp, SlateBorder, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = if (isArabic) recon.administrationMethodAr else recon.administrationMethodEn,
                                    fontSize = 10.5.sp,
                                    color = SlateDark,
                                    lineHeight = 15.sp
                                )
                            }

                            // 4. Stability After Reconstitution (فترة الصلاحية بعد الحل)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    text = if (isArabic) "⏳ الصلاحية بعد الحل: " else "⏳ Stability: ",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateDark
                                )
                                Text(
                                    text = if (isArabic) recon.stabilityAr else recon.stabilityEn,
                                    fontSize = 10.5.sp,
                                    color = SlateMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Route & Frequency
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${if (isArabic) "طريق الإعطاء:" else "Route:"} ${selectedFormulation?.route ?: drug.defaultRoute}",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlateDark
            )

            Text(
                text = "${if (isArabic) "التكرار:" else "Freq:"} ${if (isArabic) drug.frequencyAr else drug.frequencyEn}",
                fontSize = 11.sp,
                color = SlateMuted
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Method of Administration Guide Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(TealLightContainer.copy(alpha = 0.6f))
                .border(0.6.dp, TealPrimary.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Vaccines,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isArabic) "طريقة وأسلوب الأخذ والتحضير:" else "Method of Administration & Guide:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (isArabic) drug.administrationGuideAr else drug.administrationGuideEn,
                    fontSize = 11.sp,
                    color = SlateDark,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Clinical Cautions Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(6.dp)
        ) {
            Text(
                text = "⚠ ${if (isArabic) drug.cautionsAr else drug.cautionsEn}",
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Age Bands Breakdown Expander (if available)
        if (drug.ageBands.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAgeBands = !showAgeBands }
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = TealPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isArabic) "تفاصيل الجرعات حسب الفئات العمرية (${drug.ageBands.size})" else "Age Bracket Dose Guidelines (${drug.ageBands.size})",
                        fontSize = 11.sp,
                        color = TealPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    if (showAgeBands) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = showAgeBands) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateBg)
                        .border(0.6.dp, SlateBorder, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    drug.ageBands.forEach { band ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "• ${if (isArabic) band.ageGroupAr else band.ageGroupEn}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TealPrimary
                            )
                            Text(
                                text = if (isArabic) "${band.doseDescriptionAr} (${band.frequencyAr})" else "${band.doseDescriptionEn} (${band.frequencyEn})",
                                fontSize = 10.5.sp,
                                color = SlateDark
                            )
                        }
                    }
                }
            }
        }

        // Custom Dose Simulator Expander
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCustomDose = !showCustomDose }
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = TealPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isArabic) "تعديل الجرعة لكل كجم يدوياً (معايرة خاصة)" else "Custom Dose Titrator",
                    fontSize = 11.sp,
                    color = TealPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                if (showCustomDose) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = TealPrimary,
                modifier = Modifier.size(16.dp)
            )
        }

        AnimatedVisibility(visible = showCustomDose) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TealLightContainer.copy(alpha = 0.5f))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customDoseInput,
                        onValueChange = { customDoseInput = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text(text = if (isArabic) "الجرعة لكل كجم" else "Dose / kg", fontSize = 10.sp) },
                        suffix = { Text(text = drug.doseUnit, fontSize = 10.sp, color = TealPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) "الحجم الناتج:" else "Resulting Vol:",
                            fontSize = 10.sp,
                            color = SlateMuted
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.2f", finalVolumeMl)} mL",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                        Text(
                            text = "(${String.format(Locale.US, "%.2f", baseCalc.doseAmount)} ${baseCalc.doseUnit})",
                            fontSize = 10.sp,
                            color = SlateDark
                        )
                    }
                }
            }
        }
    }
}
