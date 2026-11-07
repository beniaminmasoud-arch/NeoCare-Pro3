package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.ClinicalFormulas
import com.example.data.repository.DrugCatalog
import com.example.data.repository.PatientRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

enum class PatientAgeUnit(val labelEn: String, val labelAr: String) {
    DAYS("Days", "أيام"),
    MONTHS("Months", "شهور"),
    YEARS("Years", "سنوات")
}

data class WorkstationState(
    val isArabic: Boolean = true,
    val selectedTab: Int = 0, // 0: Fluids & GIR, 1: Drugs, 2: Scores, 3: ABG & Airway, 4: Patients & SBAR
    val activePatient: NeonatePatient = NeonatePatient(
        id = 1,
        name = "طفل / آدم (Baby Adam)",
        incubatorBed = "حاضنة 4 (NICU-04)",
        gestationalWeeks = 31,
        gestationalDays = 2,
        birthWeightGrams = 1420.0,
        currentWeightGrams = 1350.0,
        dayOfLife = 3,
        ageMonths = 0,
        ageYears = 0,
        diagnosis = "Prematurity (31w), Respiratory Distress Syndrome (RDS), Hypoglycemia risk",
        clinicalNotes = "On CPAP +5, FiO2 25%. Caffeine loading given. TFI target 110 mL/kg/d.",
        isMale = true
    ),
    // Interactive Patient Weight & Age
    val currentAgeValue: Int = 3,
    val currentAgeUnit: PatientAgeUnit = PatientAgeUnit.DAYS,
    // Fluids & GIR state - WEIGHT IN KG AS PRIMARY
    val weightInputKg: String = "1.35",
    val dayOfLifeInput: String = "3",
    val isPhototherapy: Boolean = false,
    val isRadiantWarmer: Boolean = false,
    val dextrosePercentInput: String = "10.0",
    val customTfiInput: String = "110",
    val targetGirInput: String = "6.0",
    // Dedicated Maintenance & Deficit module state
    val fluidModuleMode: FluidModuleMode = FluidModuleMode.HOLLIDAY_SEGAR,
    val dehydrationSeverity: DehydrationSeverity = DehydrationSeverity.MODERATE,
    val customDeficitPercent: String = "7.0",
    val selectedFluidSolution: FluidSolutionType = FluidSolutionType.D5_HALF_NS,
    val resuscitationBolusGivenMl: String = "0.0",
    val ongoingLossesMl: String = "0.0",
    // Drug search state
    val drugSearchQuery: String = "",
    val selectedDrugCategory: DrugCategory? = null,
    // Clinical scores state
    val apgarInput: ApgarInput = ApgarInput(),
    val apgarTimingMinute: Int = 1, // 1 min, 5 min, 10 min
    val silvermanInput: SilvermanInput = SilvermanInput(),
    val bilirubinAgeHours: String = "48",
    val bilirubinInput: String = "10.5",
    val bilirubinIsUmol: Boolean = false,
    val bilirubinRiskFactor: Boolean = false,
    // ABG inputs
    val abgInput: ABGInput = ABGInput(
        ph = 7.28,
        pco2 = 52.0,
        po2 = 58.0,
        hco3 = 23.0,
        be = -2.5,
        fio2Percent = 30.0,
        mapVentilator = 7.5
    ),
    val showPatientDialog: Boolean = false,
    val snackbarMessage: String? = null
)

class NeoCareViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PatientRepository
    val allPatients: StateFlow<List<NeonatePatient>>

    private val _uiState = MutableStateFlow(WorkstationState())
    val uiState: StateFlow<WorkstationState> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PatientRepository(database.patientDao())

        allPatients = repository.allPatients
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Seed initial sample patient if database is empty
        viewModelScope.launch {
            allPatients.collect { list ->
                if (list.isEmpty()) {
                    repository.insertPatient(_uiState.value.activePatient)
                }
            }
        }
    }

    fun toggleLanguage() {
        _uiState.update { it.copy(isArabic = !it.isArabic) }
    }

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun setActivePatient(patient: NeonatePatient) {
        val ageUnit = when {
            patient.ageYears > 0 -> PatientAgeUnit.YEARS
            patient.ageMonths > 0 -> PatientAgeUnit.MONTHS
            else -> PatientAgeUnit.DAYS
        }
        val ageValue = when {
            patient.ageYears > 0 -> patient.ageYears
            patient.ageMonths > 0 -> patient.ageMonths
            else -> patient.dayOfLife
        }
        _uiState.update {
            it.copy(
                activePatient = patient,
                weightInputKg = String.format(Locale.US, "%.2f", patient.weightKg),
                dayOfLifeInput = patient.dayOfLife.toString(),
                currentAgeValue = ageValue,
                currentAgeUnit = ageUnit,
                bilirubinAgeHours = (patient.dayOfLife * 24).toString()
            )
        }
    }

    // --- Interactive Patient Dynamic Weight & Age Control ---
    fun updatePatientWeight(weightKg: Double) {
        val clamped = weightKg.coerceIn(0.4, 120.0)
        val rounded = Math.round(clamped * 100.0) / 100.0
        val grams = rounded * 1000.0
        _uiState.update {
            it.copy(
                activePatient = it.activePatient.copy(currentWeightGrams = grams),
                weightInputKg = String.format(Locale.US, "%.2f", rounded)
            )
        }
    }

    fun adjustPatientWeightBy(deltaKg: Double) {
        val current = _uiState.value.activePatient.weightKg
        updatePatientWeight(current + deltaKg)
    }

    fun updatePatientAge(value: Int, unit: PatientAgeUnit) {
        val safeValue = value.coerceIn(1, 1000)
        val dayOfLife = when (unit) {
            PatientAgeUnit.DAYS -> safeValue
            PatientAgeUnit.MONTHS -> safeValue * 30
            PatientAgeUnit.YEARS -> safeValue * 365
        }
        val ageMonths = when (unit) {
            PatientAgeUnit.DAYS -> safeValue / 30
            PatientAgeUnit.MONTHS -> safeValue
            PatientAgeUnit.YEARS -> safeValue * 12
        }
        val ageYears = when (unit) {
            PatientAgeUnit.DAYS -> safeValue / 365
            PatientAgeUnit.MONTHS -> safeValue / 12
            PatientAgeUnit.YEARS -> safeValue
        }
        _uiState.update {
            it.copy(
                currentAgeValue = safeValue,
                currentAgeUnit = unit,
                dayOfLifeInput = dayOfLife.toString(),
                bilirubinAgeHours = (dayOfLife * 24).toString(),
                activePatient = it.activePatient.copy(
                    dayOfLife = dayOfLife,
                    ageMonths = ageMonths,
                    ageYears = ageYears
                )
            )
        }
    }

    fun quickSetPatientProfile(weightKg: Double, ageValue: Int, ageUnit: PatientAgeUnit, name: String? = null) {
        val grams = weightKg * 1000.0
        val dol = when (ageUnit) {
            PatientAgeUnit.DAYS -> ageValue
            PatientAgeUnit.MONTHS -> ageValue * 30
            PatientAgeUnit.YEARS -> ageValue * 365
        }
        val ageMonths = when (ageUnit) {
            PatientAgeUnit.DAYS -> ageValue / 30
            PatientAgeUnit.MONTHS -> ageValue
            PatientAgeUnit.YEARS -> ageValue * 12
        }
        val ageYears = when (ageUnit) {
            PatientAgeUnit.DAYS -> ageValue / 365
            PatientAgeUnit.MONTHS -> ageValue / 12
            PatientAgeUnit.YEARS -> ageValue
        }
        _uiState.update {
            val updatedPatient = it.activePatient.copy(
                name = name ?: it.activePatient.name,
                currentWeightGrams = grams,
                dayOfLife = dol,
                ageMonths = ageMonths,
                ageYears = ageYears
            )
            it.copy(
                activePatient = updatedPatient,
                weightInputKg = String.format(Locale.US, "%.2f", weightKg),
                currentAgeValue = ageValue,
                currentAgeUnit = ageUnit,
                dayOfLifeInput = dol.toString(),
                bilirubinAgeHours = (dol * 24).toString()
            )
        }
    }

    // --- Fluids & GIR actions ---
    fun updateWeightInputKg(weightKg: String) {
        val parsed = weightKg.toDoubleOrNull()
        if (parsed != null && parsed > 0) {
            updatePatientWeight(parsed)
        } else {
            _uiState.update { it.copy(weightInputKg = weightKg) }
        }
    }

    fun updateDayOfLifeInput(dol: String) {
        val parsed = dol.toIntOrNull()
        if (parsed != null && parsed > 0) {
            updatePatientAge(parsed, _uiState.value.currentAgeUnit)
        } else {
            _uiState.update { it.copy(dayOfLifeInput = dol) }
        }
    }

    fun togglePhototherapy() {
        _uiState.update { it.copy(isPhototherapy = !it.isPhototherapy) }
    }

    fun toggleRadiantWarmer() {
        _uiState.update { it.copy(isRadiantWarmer = !it.isRadiantWarmer) }
    }

    fun updateDextrosePercent(percent: String) {
        _uiState.update { it.copy(dextrosePercentInput = percent) }
    }

    fun updateCustomTfi(tfi: String) {
        _uiState.update { it.copy(customTfiInput = tfi) }
    }

    fun updateTargetGir(gir: String) {
        _uiState.update { it.copy(targetGirInput = gir) }
    }

    // --- Pediatric Maintenance Fluids & Deficit actions ---
    fun setFluidModuleMode(mode: FluidModuleMode) {
        _uiState.update { it.copy(fluidModuleMode = mode) }
    }

    fun setDehydrationSeverity(severity: DehydrationSeverity) {
        _uiState.update {
            it.copy(
                dehydrationSeverity = severity,
                customDeficitPercent = if (severity == DehydrationSeverity.NONE) "0.0" else String.format(Locale.US, "%.1f", severity.defaultPercent)
            )
        }
    }

    fun updateCustomDeficitPercent(percent: String) {
        val parsed = percent.toDoubleOrNull()
        val mappedSeverity = when {
            parsed == null || parsed <= 0.0 -> DehydrationSeverity.NONE
            parsed < 6.0 -> DehydrationSeverity.MILD
            parsed < 10.0 -> DehydrationSeverity.MODERATE
            else -> DehydrationSeverity.SEVERE
        }
        _uiState.update {
            it.copy(
                customDeficitPercent = percent,
                dehydrationSeverity = mappedSeverity
            )
        }
    }

    fun setSelectedFluidSolution(solution: FluidSolutionType) {
        _uiState.update { it.copy(selectedFluidSolution = solution) }
    }

    fun updateResuscitationBolusGivenMl(volume: String) {
        _uiState.update { it.copy(resuscitationBolusGivenMl = volume) }
    }

    fun updateOngoingLossesMl(volume: String) {
        _uiState.update { it.copy(ongoingLossesMl = volume) }
    }

    // --- Drug Dosing actions ---
    fun updateDrugSearch(query: String) {
        _uiState.update { it.copy(drugSearchQuery = query) }
    }

    fun selectDrugCategory(category: DrugCategory?) {
        _uiState.update { it.copy(selectedDrugCategory = category) }
    }

    // --- Scores actions ---
    fun updateApgar(apgar: ApgarInput, minute: Int = _uiState.value.apgarTimingMinute) {
        _uiState.update { it.copy(apgarInput = apgar, apgarTimingMinute = minute) }
    }

    fun updateSilverman(silverman: SilvermanInput) {
        _uiState.update { it.copy(silvermanInput = silverman) }
    }

    fun updateBilirubinAgeHours(hours: String) {
        _uiState.update { it.copy(bilirubinAgeHours = hours) }
    }

    fun updateBilirubinValue(value: String) {
        _uiState.update { it.copy(bilirubinInput = value) }
    }

    fun toggleBilirubinUnit() {
        _uiState.update { it.copy(bilirubinIsUmol = !it.bilirubinIsUmol) }
    }

    fun toggleBilirubinRisk() {
        _uiState.update { it.copy(bilirubinRiskFactor = !it.bilirubinRiskFactor) }
    }

    // --- ABG actions ---
    fun updateABG(abg: ABGInput) {
        _uiState.update { it.copy(abgInput = abg) }
    }

    // --- Patient database actions ---
    fun savePatient(patient: NeonatePatient) {
        viewModelScope.launch {
            if (patient.id == 0L) {
                val newId = repository.insertPatient(patient)
                setActivePatient(patient.copy(id = newId))
            } else {
                repository.updatePatient(patient)
                setActivePatient(patient)
            }
            showSnackbar(if (_uiState.value.isArabic) "تم حفظ بيانات المريض بنجاح" else "Patient record saved successfully")
        }
    }

    fun deletePatient(patient: NeonatePatient) {
        viewModelScope.launch {
            repository.deletePatient(patient)
            showSnackbar(if (_uiState.value.isArabic) "تم حذف سجل المريض" else "Patient record deleted")
        }
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    /**
     * Generate structured SBAR clinical handover report for the active patient
     */
    fun generateSBARReport(): String {
        val p = _uiState.value.activePatient
        val weightKg = p.currentWeightGrams / 1000.0
        val tfi = _uiState.value.customTfiInput.toDoubleOrNull() ?: 110.0
        val dailyVol = weightKg * tfi
        val hourlyRate = dailyVol / 24.0
        val dPct = _uiState.value.dextrosePercentInput.toDoubleOrNull() ?: 10.0
        val gir = ClinicalFormulas.calculateGIR(hourlyRate, dPct, weightKg)
        val airway = ClinicalFormulas.calculateAirwayEquipment(p.currentWeightGrams, p.gestationalWeeks)
        val abgResult = ClinicalFormulas.interpretABG(_uiState.value.abgInput)

        // Bilirubin assessment for active patient
        val biliVal = _uiState.value.bilirubinInput.toDoubleOrNull() ?: 10.5
        val ageHrs = _uiState.value.bilirubinAgeHours.toDoubleOrNull() ?: 48.0
        val biliResult = ClinicalFormulas.evaluateBilirubin(
            ageHours = ageHrs,
            bilirubinInput = biliVal,
            isUmol = _uiState.value.bilirubinIsUmol,
            gestationalWeeks = p.gestationalWeeks,
            hasRiskFactors = _uiState.value.bilirubinRiskFactor,
            weightKg = weightKg
        )

        val isAr = _uiState.value.isArabic
        return if (isAr) {
            """
            === تقرير التسليم السريري الوليدي (SBAR Clinical Handover) ===
            المريض: ${p.name}
            الحاضنة: ${p.incubatorBed}
            العمر الحملي: ${p.gestationalWeeks} أسبوع + ${p.gestationalDays} يوم (CGA: ${p.correctedAgeWeeks}w+${p.correctedAgeDays}d)
            عمر الطفل (DOL): يوم ${p.dayOfLife}
            الوزن عند الولادة: ${String.format(Locale.US, "%.2f", p.birthWeightKg)} كجم (${p.birthWeightGrams.toInt()} جم)
            الوزن الحالي: ${String.format(Locale.US, "%.2f", p.weightKg)} كجم (${p.currentWeightGrams.toInt()} جم) [تغير: ${String.format(Locale.US, "%.1f", p.weightChangePercentage)}%]

            [S] الحالة الراهنة (Situation):
            التشخيص: ${p.diagnosis.ifBlank { "متابعة روتينية في الحضانة" }}

            [B] الخلفية السريرية (Background):
            ${p.clinicalNotes.ifBlank { "ولادة مبكرة مع دعم تنفسي ومتابعة التغذية والسكر والصفراء." }}

            [A] التقييم الحالي والمؤشرات السريرية (Assessment):
            - خطة السوائل (TFI): $tfi مل/كجم/يوم (إجمالي: ${String.format(Locale.US, "%.1f", dailyVol)} مل/يوم)
            - معدل المحلول الوريدي: ${String.format(Locale.US, "%.1f", hourlyRate)} مل/ساعة بنسبة ديكستروز $dPct%
            - معدل ضخ الجلوكوز (GIR): ${String.format(Locale.US, "%.2f", gir)} مجم/كجم/دقيقة
            - قياسات مجرى الهواء: أنبوبة حنجرية ID ${airway.ettInternalDiameterMm} ملم بعمق ${airway.ettDepthAtLipCm} سم عند الشفة
            - قساطر السرة: UVC عمق ${airway.uvcDepthCm} سم | UAC عالي: ${airway.uacDepthHighCm} سم
            - غازات الدم (ABG): pH ${_uiState.value.abgInput.ph}، pCO2 ${_uiState.value.abgInput.pco2}، pO2 ${_uiState.value.abgInput.po2}
              التقييم: ${abgResult.primaryDisorderAr}
            - تقييم الصفراء (Bilirubin): ${biliResult.mgDlValue} مجم/ديسيلتر (عمر: ${biliResult.ageHours.toInt()} ساعة)
              عتبة العلاج الضوئي: ${biliResult.photoThreshold} مجم | عتبة التبديل: ${biliResult.exchangeThreshold} مجم
              القرار العلاجي: ${biliResult.modality.titleAr}

            [R] التوصيات وخطة العمل (Recommendation):
            - خطة الصفراء: ${biliResult.recommendationAr}
            - إعادة قياس الصفراء خلال: ${biliResult.recheckIntervalHours} ساعات (عتبة إيقاف الضوئي: ${biliResult.stoppingTargetMgDl} مجم).
            - استمرار مراقبة السكر والوزن بالكيلوجرام واستقرار العلامات الحيوية.
            ===========================================
            صادر عن: محطة NeoCare Pro Master الوليدية
            """.trimIndent()
        } else {
            """
            === NEONATAL SBAR CLINICAL HANDOVER REPORT ===
            Patient: ${p.name}
            Bed / Incubator: ${p.incubatorBed}
            Gestational Age: ${p.gestationalWeeks}w + ${p.gestationalDays}d (CGA: ${p.correctedAgeWeeks}w+${p.correctedAgeDays}d)
            Day of Life (DOL): Day ${p.dayOfLife}
            Birth Weight: ${String.format(Locale.US, "%.2f", p.birthWeightKg)} kg (${p.birthWeightGrams.toInt()} g)
            Current Weight: ${String.format(Locale.US, "%.2f", p.weightKg)} kg (${p.currentWeightGrams.toInt()} g) [Change: ${String.format(Locale.US, "%.1f", p.weightChangePercentage)}%]

            [S] Situation:
            Diagnosis: ${p.diagnosis.ifBlank { "Routine NICU care and monitoring" }}

            [B] Background:
            ${p.clinicalNotes.ifBlank { "Preterm infant on respiratory support, IV fluid titration, and phototherapy monitoring." }}

            [A] Assessment & Parameters:
            - Fluid Goal (TFI): $tfi mL/kg/day (Total: ${String.format(Locale.US, "%.1f", dailyVol)} mL/day)
            - IV Infusion Rate: ${String.format(Locale.US, "%.1f", hourlyRate)} mL/hr (Dextrose $dPct%)
            - Glucose Infusion Rate (GIR): ${String.format(Locale.US, "%.2f", gir)} mg/kg/min
            - Resuscitation & Airway: ETT ID ${airway.ettInternalDiameterMm} mm at depth ${airway.ettDepthAtLipCm} cm at lip
            - Umbilical Lines: UVC ${airway.uvcDepthCm} cm | UAC high line ${airway.uacDepthHighCm} cm
            - Blood Gas Status: pH ${_uiState.value.abgInput.ph}, pCO2 ${_uiState.value.abgInput.pco2}, pO2 ${_uiState.value.abgInput.po2}
              Interpretation: ${abgResult.primaryDisorderEn}
            - Bilirubin Assessment: ${biliResult.mgDlValue} mg/dL (Age: ${biliResult.ageHours.toInt()}h)
              Photo Threshold: ${biliResult.photoThreshold} mg/dL | Exchange Threshold: ${biliResult.exchangeThreshold} mg/dL
              Therapy Modality: ${biliResult.modality.titleEn}

            [R] Recommendations:
            - Jaundice Action: ${biliResult.recommendationEn}
            - Recheck Bilirubin in: ${biliResult.recheckIntervalHours} hours (Discontinuation threshold: ${biliResult.stoppingTargetMgDl} mg/dL).
            - Maintain continuous cardio-respiratory monitoring, daily weight in kg, and glycemic checks.
            ===========================================
            Generated by: NeoCare Pro Master Clinical Workstation
            """.trimIndent()
        }
    }
}
