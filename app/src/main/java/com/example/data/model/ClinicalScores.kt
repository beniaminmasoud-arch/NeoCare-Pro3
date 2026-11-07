package com.example.data.model

// --- APGAR Score Models ---
data class ApgarInput(
    val appearance: Int = 2, // 0: Blue/pale, 1: Acrocyanosis, 2: Pink all over
    val pulse: Int = 2,      // 0: Absent, 1: <100 bpm, 2: >=100 bpm
    val grimace: Int = 2,    // 0: Flaccid, 1: Grimace, 2: Cough/sneeze/vigorous cry
    val activity: Int = 2,   // 0: Limp, 1: Some flexion, 2: Active motion
    val respiration: Int = 2 // 0: Absent, 1: Slow/irregular, 2: Good vigorous cry
) {
    val totalScore: Int get() = appearance + pulse + grimace + activity + respiration

    val statusEn: String
        get() = when (totalScore) {
            in 7..10 -> "Normal newborn transition (Reassuring)"
            in 4..6 -> "Moderately abnormal (Requires stimulation, O2 / CPAP)"
            else -> "Severely depressed (Immediate NRP resuscitation required!)"
        }

    val statusAr: String
        get() = when (totalScore) {
            in 7..10 -> "حالة طبيعية ومستقرة (مُطمئنة)"
            in 4..6 -> "هبوط تنفسي/عصبي متوسط (يحتاج تحفيز ودعم تنفسي/CPAP)"
            else -> "هبوط حاد وحرج (يلزم بدء إنعاش وليدي فوري NRP!)"
        }

    val alertLevel: AlertLevel
        get() = when (totalScore) {
            in 7..10 -> AlertLevel.SAFE
            in 4..6 -> AlertLevel.WARNING
            else -> AlertLevel.DANGER
        }
}

// --- Silverman-Anderson Respiratory Score Models ---
data class SilvermanInput(
    val upperChest: Int = 0,     // 0: Synchronized, 1: Lag on inspiration, 2: See-saw breathing
    val lowerChest: Int = 0,     // 0: None, 1: Just visible, 2: Marked retraction
    val xiphoidRetract: Int = 0, // 0: None, 1: Just visible, 2: Marked retraction
    val naresFlaring: Int = 0,   // 0: None, 1: Minimal flaring, 2: Marked flaring
    val expiratoryGrunt: Int = 0 // 0: None, 1: Audible with stethoscope, 2: Audible with naked ear
) {
    val totalScore: Int get() = upperChest + lowerChest + xiphoidRetract + naresFlaring + expiratoryGrunt

    val statusEn: String
        get() = when {
            totalScore == 0 -> "No respiratory distress"
            totalScore in 1..3 -> "Mild respiratory distress (Monitor closely, consider FiO2/HFNC)"
            totalScore in 4..6 -> "Moderate respiratory distress (Start CPAP, assess for surfactant)"
            else -> "Severe respiratory failure (Urgent intubation / invasive ventilation indicated)"
        }

    val statusAr: String
        get() = when {
            totalScore == 0 -> "لا توجد صعوبة في التنفس"
            totalScore in 1..3 -> "صعوبة تنفس طفيفة (متابعة دقيقة، احتمال تدفق أكسجين)"
            totalScore in 4..6 -> "صعوبة تنفس متوسطة (بدء CPAP، تقييم الحاجة لسيرفاكتانت)"
            else -> "فشل تنفسي حاد (يلزم تنبيب حنجري وتنفس صناعي عاجل)"
        }

    val alertLevel: AlertLevel
        get() = when {
            totalScore == 0 -> AlertLevel.SAFE
            totalScore in 1..3 -> AlertLevel.SAFE
            totalScore in 4..6 -> AlertLevel.WARNING
            else -> AlertLevel.DANGER
        }
}

// --- Bilirubin / Phototherapy & Exchange Transfusion Models ---
enum class NeurotoxicityRisk(val labelEn: String, val labelAr: String) {
    STANDARD("No Neurotoxicity Risk Factors", "لا توجد عوامل خطورة عصبية"),
    HIGH_RISK("With Risk Factors (Hemolysis, Sepsis, Asphyxia, Albumin <3)", "مع وجود عوامل خطورة (تكسير دم، تسمم دم، اختناق وليدي)")
}

enum class PhototherapyModality(val titleEn: String, val titleAr: String) {
    NONE("No Phototherapy Required", "لا حاجة لعلاج ضوئي حالياً"),
    SINGLE("Single Phototherapy", "علاج ضوئي أحادي (Single Phototherapy)"),
    DOUBLE("Double / Intensive Phototherapy", "علاج ضوئي ثنائي مكثف (Double Phototherapy)"),
    TRIPLE("Triple / Super-Intensive Phototherapy", "علاج ضوئي ثلاثي فائق الشدة (Triple Phototherapy)"),
    EXCHANGE("Exchange Transfusion Indicated", "تبديل دم كامل عاجل (Exchange Transfusion)")
}

data class BilirubinResult(
    val ageHours: Double,
    val bilirubinValue: Double,
    val isUmol: Boolean,
    val mgDlValue: Double,
    val photoThreshold: Double,
    val exchangeThreshold: Double,
    val modality: PhototherapyModality,
    val diffFromPhoto: Double,       // mg/dL above or below photo threshold
    val diffFromExchange: Double,    // mg/dL distance to exchange threshold
    val riskZoneEn: String,
    val riskZoneAr: String,
    val recommendationEn: String,
    val recommendationAr: String,
    val therapyDetailsEn: List<String>,
    val therapyDetailsAr: List<String>,
    val exchangeProtocolEn: String?,
    val exchangeProtocolAr: String?,
    val ivigProtocolEn: String?,
    val ivigProtocolAr: String?,
    val recheckIntervalHours: Int,
    val stoppingTargetMgDl: Double,
    val alertLevel: AlertLevel
)

enum class AlertLevel {
    SAFE,
    WARNING,
    DANGER
}
