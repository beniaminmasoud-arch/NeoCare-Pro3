package com.example.data.model

enum class DrugCategory(val labelEn: String, val labelAr: String) {
    ANTIPYRETIC_ANALGESIC("Antipyretics & Pain", "خافض حرارة ومسكنات"),
    ANTIBIOTIC("Antibiotics & Anti-infectives", "المضادات الحيوية"),
    RESPIRATORY_ASTHMA("Asthma & Respiratory", "الربو والجهاز التنفسي"),
    GI_NUTRITION("GI & Antiemetics", "الجهاز الهضمي والترجيع"),
    ALLERGY_ANTIHISTAMINE("Allergy & Antihistamines", "الحساسية ومضادات الهيستامين"),
    EMERGENCY("Emergency & Resuscitation", "الإنعاش والطوارئ (NRP/PALS)"),
    NEUROLOGY("Anticonvulsants & Neuro", "التشنجات والأعصاب"),
    CARDIO_DIURETIC("Cardiovascular & Diuretics", "القلب ومدرات البول"),
    STEROID("Steroids & Endocrine", "الكورتيزون والغدد"),
    METABOLIC("Electrolytes & Vitamins", "المعادن والفيتامينات")
}

data class DrugAgeBand(
    val ageGroupEn: String, // e.g. "Neonates (0-28d)", "Infants (1m - 1y)", "Children (1-12y)", "Adolescents (12-18y)"
    val ageGroupAr: String,
    val doseDescriptionEn: String,
    val doseDescriptionAr: String,
    val dosePerKgMin: Double,
    val dosePerKgMax: Double,
    val frequencyEn: String,
    val frequencyAr: String,
    val maxDoseMg: Double? = null
)

data class PediatricFormulation(
    val nameEn: String,
    val nameAr: String,
    val concentrationMgPerMl: Double,
    val route: String // "PO", "IV", "IM", "Inhalation", "PR"
)

data class ReconstitutionGuide(
    val isPowder: Boolean = true,
    val diluentEn: String,
    val diluentAr: String,
    val powderVolumeStepsEn: String,
    val powderVolumeStepsAr: String,
    val administrationMethodEn: String,
    val administrationMethodAr: String,
    val stabilityEn: String,
    val stabilityAr: String
)

data class DrugInfo(
    val id: String,
    val nameEn: String,
    val nameAr: String,
    val category: DrugCategory,
    val ageRangeEn: String = "Birth to 18 Years",
    val ageRangeAr: String = "من حديثي الولادة حتى 18 سنة",
    val doseDescriptionEn: String,
    val doseDescriptionAr: String,
    val dosePerKgMin: Double,
    val dosePerKgMax: Double,
    val doseUnit: String, // e.g., "mg/kg", "mcg/kg", "mL/kg"
    val stockConcentration: Double, // amount per mL, e.g. 100 mg/mL, or 0 for direct volume
    val stockUnit: String, // e.g., "mg/mL", "mcg/mL"
    val defaultRoute: String, // "IV", "PO", "ETT", "IM", "Inhalation", "PR"
    val frequencyEn: String,
    val frequencyAr: String,
    val cautionsEn: String,
    val cautionsAr: String,
    val maxSingleDose: Double? = null, // e.g. 1000 mg
    val maxDailyDose: Double? = null,  // e.g. 4000 mg/day
    val administrationGuideEn: String, // Detailed method of administration
    val administrationGuideAr: String,
    val reconstitution: ReconstitutionGuide? = null, // Reconstitution & diluent details for powders/antibiotics
    val ageBands: List<DrugAgeBand> = emptyList(),
    val formulations: List<PediatricFormulation> = emptyList(),
    val isDirectVolumePerKg: Boolean = false // e.g. Normal saline bolus 10 mL/kg, D10W bolus 2 mL/kg
) {
    /**
     * Calculate single dose in unit and volume in mL for a given weight in kg,
     * applying maximum adult/adolescent caps where appropriate.
     */
    fun calculateDose(weightKg: Double, customDosePerKg: Double? = null): CalculatedDose {
        val selectedDosePerKg = customDosePerKg ?: dosePerKgMin
        val rawDoseUnit = selectedDosePerKg * weightKg

        val isCapped = maxSingleDose != null && rawDoseUnit > maxSingleDose
        val totalDoseUnit = if (isCapped) maxSingleDose!! else rawDoseUnit

        val volumeMl = if (isDirectVolumePerKg) {
            totalDoseUnit
        } else if (stockConcentration > 0) {
            totalDoseUnit / stockConcentration
        } else {
            0.0
        }

        return CalculatedDose(
            doseAmount = totalDoseUnit,
            doseUnit = if (isDirectVolumePerKg) "mL" else doseUnit.replace("/kg", ""),
            volumeMl = volumeMl,
            dosePerKgUsed = selectedDosePerKg,
            isCappedAtMax = isCapped,
            maxSingleDose = maxSingleDose
        )
    }
}

data class CalculatedDose(
    val doseAmount: Double,
    val doseUnit: String,
    val volumeMl: Double,
    val dosePerKgUsed: Double,
    val isCappedAtMax: Boolean = false,
    val maxSingleDose: Double? = null
)
