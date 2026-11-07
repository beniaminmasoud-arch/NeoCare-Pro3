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

    val appearanceLabelEn: String
        get() = when (appearance) {
            0 -> "Blue / Pale all over (0)"
            1 -> "Acrocyanosis: Body pink, blue hands/feet (1)"
            else -> "Completely pink all over (2)"
        }

    val appearanceLabelAr: String
        get() = when (appearance) {
            0 -> "أزرق / شاحب بالكامل (0)"
            1 -> "زرقة في الأطراف مع جسم وردي (1)"
            else -> "وردي بالكامل بدون زرقة (2)"
        }

    val pulseLabelEn: String
        get() = when (pulse) {
            0 -> "Absent heart rate (0)"
            1 -> "< 100 bpm (1)"
            else -> ">= 100 bpm (2)"
        }

    val pulseLabelAr: String
        get() = when (pulse) {
            0 -> "لا يوجد نبض / توقف القلب (0)"
            1 -> "أقل من 100 د/د (بطء نبض) (1)"
            else -> "100 د/د أو أكثر (طبيعي) (2)"
        }

    val grimaceLabelEn: String
        get() = when (grimace) {
            0 -> "Flaccid / No response (0)"
            1 -> "Grimace / Minor motion (1)"
            else -> "Vigorous cry / Cough / Sneeze (2)"
        }

    val grimaceLabelAr: String
        get() = when (grimace) {
            0 -> "لا توجد استجابة / ارتخاء تام (0)"
            1 -> "تكشيرة خفيفة / حركة طفيفة (1)"
            else -> "بكاء قوي / سعال / عطاس نشط (2)"
        }

    val activityLabelEn: String
        get() = when (activity) {
            0 -> "Limp / Flaccid (0)"
            1 -> "Some flexion of arms and legs (1)"
            else -> "Active motion, well-flexed (2)"
        }

    val activityLabelAr: String
        get() = when (activity) {
            0 -> "مرتخٍ تماماً بلا حركة (0)"
            1 -> "انثناء طفيف في الأطراف (1)"
            else -> "حركة نشطة وثني كامل للأطراف (2)"
        }

    val respirationLabelEn: String
        get() = when (respiration) {
            0 -> "Absent / Apnea (0)"
            1 -> "Slow, irregular, weak cry (1)"
            else -> "Good regular breathing, vigorous cry (2)"
        }

    val respirationLabelAr: String
        get() = when (respiration) {
            0 -> "لا يوجد تنفس / انقطاع تام (0)"
            1 -> "تنفس بطيء، غير منتظم، بكاء خافت (1)"
            else -> "بكاء قوي مستمر وتنفس منتظم (2)"
        }

    fun getSpO2Target(minute: Int): String = when (minute) {
        1 -> "60% - 65%"
        2 -> "65% - 70%"
        3 -> "70% - 75%"
        4 -> "75% - 80%"
        5 -> "80% - 85%"
        10 -> "85% - 95%"
        else -> ">= 90%"
    }

    fun getNrpActionGuidance(minute: Int, isArabic: Boolean): String {
        return if (isArabic) {
            when {
                totalScore in 7..10 ->
                    "✓ رعاية وليدية روتينية: تجفيف وتدفئة، وضع جلد-لجلد مع الأم، مراقبة التنفس، فحص تشبع الأكسجين المستهدف ($minute د: ${getSpO2Target(minute)})."
                totalScore in 4..6 ->
                    "⚠ بروتوكول الهبوط المتوسط (NRP): تدفئة وتجفيف فوري، تعديل وضعية الرأس وشفط الإفرازات (الفم أولاً ثم الأنف)، تحفيز لمسي، تقديم CPAP أو أكسجين ممزوج عبر T-piece، مع مراقبة SpO2 المستهدف (${getSpO2Target(minute)})."
                else ->
                    "🚨 إنعاش وليدي فوري (STAT NRP): بدء تهوية بالضغط الإيجابي (PPV) بقناع 100% O2 فوراً بمعدل 40-60 تنفس/د. فحص النبض بعد 30 ثانية: إذا كان النبض < 60 د/د، ابدأ تدليك الصدر (نسبة 3:1)، جهز أنبوبة حنجرية ETT، قسطرة وريد سري UVC، وحقن إبينفرين 0.02 مجم/كجم."
            }
        } else {
            when {
                totalScore in 7..10 ->
                    "✓ Routine Care: Dry, maintain warmth, skin-to-skin with mother, monitor pre-ductal SpO2 target ($minute min: ${getSpO2Target(minute)})."
                totalScore in 4..6 ->
                    "⚠ Moderate Depression (NRP): Immediate warmth and drying, position airway and suction mouth then nose, tactile stimulation, provide CPAP or blended O2 for labored breathing/cyanosis. Pre-ductal target: ${getSpO2Target(minute)}."
                else ->
                    "🚨 STAT NRP RESUSCITATION: Initiate Positive Pressure Ventilation (PPV) with 100% O2 immediately at 40-60 breaths/min. If HR < 60 bpm after 30s PPV, start chest compressions (3:1 ratio), prepare ETT intubation, emergency UVC line, and Epinephrine (0.02 mg/kg IV)."
            }
        }
    }
}

// --- Silverman-Anderson Respiratory Score Models ---
data class SilvermanInput(
    val nasalFlaring: Int = 0,     // 0: None, 1: Minimal flaring, 2: Marked flaring
    val grunting: Int = 0,         // 0: None, 1: Stethoscope only, 2: Naked ear
    val retractions: Int = 0,      // 0: None, 1: Just visible / mild, 2: Marked intercostal/xiphoid
    val breathingPattern: Int = 0, // 0: Synchronized, 1: Lag on inspiration, 2: See-saw / paradoxical
    val cyanosis: Int = 0,         // 0: None / Pink in room air, 1: Relieved by O2, 2: Central cyanosis
    // Backwards compatibility properties for legacy code
    val upperChest: Int = 0,
    val lowerChest: Int = 0,
    val xiphoidRetract: Int = 0,
    val naresFlaring: Int = 0,
    val expiratoryGrunt: Int = 0
) {
    val totalScore: Int
        get() {
            // If legacy parameters were set (non-zero) and modern parameters are 0, use legacy
            val legacySum = upperChest + lowerChest + xiphoidRetract + naresFlaring + expiratoryGrunt
            val modernSum = nasalFlaring + grunting + retractions + breathingPattern + cyanosis
            return if (modernSum > 0 || (legacySum == 0 && modernSum == 0)) {
                modernSum + (if (upperChest > 0 && breathingPattern == 0) upperChest else 0) + (if (xiphoidRetract > 0) xiphoidRetract else 0)
            } else {
                legacySum
            }
        }

    val statusEn: String
        get() = when {
            totalScore == 0 -> "Normal breathing (No respiratory distress)"
            totalScore in 1..3 -> "Mild respiratory distress (Low-flow O2 / cannula, close monitoring)"
            totalScore in 4..6 -> "Moderate respiratory distress (Initiate Nasal CPAP 5-7 cmH2O, assess surfactant)"
            else -> "Severe respiratory failure (Urgent intubation / mechanical ventilation indicated)"
        }

    val statusAr: String
        get() = when {
            totalScore == 0 -> "تنفس طبيعي ومستقر (لا توجد ضائقة تنفسية)"
            totalScore in 1..3 -> "ضائقة تنفسية طفيفة (أكسجين رطب خفيف، مراقبة مستمرة)"
            totalScore in 4..6 -> "ضائقة تنفسية متوسطة (بدء CPAP أنفي 5-7 سمH2O، تقييم السيرفاكتانت)"
            else -> "فشل تنفسي حاد وشيك (يلزم تنبيب حنجري ETT وتنفس صناعي ميكانيكي عاجل)"
        }

    val alertLevel: AlertLevel
        get() = when {
            totalScore == 0 -> AlertLevel.SAFE
            totalScore in 1..3 -> AlertLevel.SAFE
            totalScore in 4..6 -> AlertLevel.WARNING
            else -> AlertLevel.DANGER
        }

    val nasalFlaringLabelEn: String
        get() = when (nasalFlaring) {
            0 -> "None (0)"
            1 -> "Minimal flaring (1)"
            else -> "Marked / continuous flaring (2)"
        }

    val nasalFlaringLabelAr: String
        get() = when (nasalFlaring) {
            0 -> "لا يوجد (0)"
            1 -> "اتساع طفيف في الأنف (1)"
            else -> "اتساع واضح ومستمر في فتحات الأنف (2)"
        }

    val gruntingLabelEn: String
        get() = when (grunting) {
            0 -> "None (0)"
            1 -> "Audible with stethoscope only (1)"
            else -> "Audible with naked ear (2)"
        }

    val gruntingLabelAr: String
        get() = when (grunting) {
            0 -> "لا يوجد (0)"
            1 -> "يُسمع بالسماعة الطبية فقط (1)"
            else -> "يُسمع بالأذن المجردة بدون سماعة (2)"
        }

    val retractionsLabelEn: String
        get() = when (retractions) {
            0 -> "None (0)"
            1 -> "Just visible / mild intercostal/xiphoid (1)"
            else -> "Marked sternal & intercostal retraction (2)"
        }

    val retractionsLabelAr: String
        get() = when (retractions) {
            0 -> "لا يوجد (0)"
            1 -> "سحب طفيف بين الأضلاع أو تحت القص (1)"
            else -> "سحب صدري وقصي عميق وشديد (2)"
        }

    val breathingPatternLabelEn: String
        get() = when (breathingPattern) {
            0 -> "Synchronized chest & abdominal movement (0)"
            1 -> "Lag on inspiration (1)"
            else -> "See-saw / paradoxical breathing (2)"
        }

    val breathingPatternLabelAr: String
        get() = when (breathingPattern) {
            0 -> "حركة متزامنة ومنتظمة بين الصدر والبطن (0)"
            1 -> "تأخر أو تباطؤ في الشهيق (1)"
            else -> "تأرجح متناقض سيسو (See-saw) (2)"
        }

    val cyanosisLabelEn: String
        get() = when (cyanosis) {
            0 -> "None / Completely pink in room air (0)"
            1 -> "Acrocyanosis or relieved with low-flow O2 (1)"
            else -> "Central cyanosis / refractory on O2 (2)"
        }

    val cyanosisLabelAr: String
        get() = when (cyanosis) {
            0 -> "لا توجد زرقة / وردي بهواء الغرفة (0)"
            1 -> "زرقة بالأطراف أو تستجيب للأكسجين (1)"
            else -> "زرقة مركزية واضحة أو مستمرة مع الأكسجين (2)"
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
