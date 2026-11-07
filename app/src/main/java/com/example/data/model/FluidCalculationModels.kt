package com.example.data.model

enum class FluidModuleMode(val labelEn: String, val labelAr: String) {
    HOLLIDAY_SEGAR("Pediatric Maintenance & Deficit", "سوائل الصيانة وتعويض الجفاف (الأطفال والمراهقون)"),
    NEONATAL_NICU("Neonatal TFI & GIR", "سوائل حديثي الولادة ومعدل الجلوكوز (NICU)")
}

enum class DehydrationSeverity(
    val defaultPercent: Double,
    val rangeDescEn: String,
    val rangeDescAr: String,
    val labelEn: String,
    val labelAr: String,
    val alertLevel: AlertLevel
) {
    NONE(0.0, "0%", "0%", "No Dehydration", "طبيعي - لا يوجد جفاف (0%)", AlertLevel.SAFE),
    MILD(4.0, "3% - 5% (30-50 mL/kg)", "3% - 5% (30-50 مل/كجم)", "Mild Dehydration", "جفاف طفيف (3-5%)", AlertLevel.SAFE),
    MODERATE(7.0, "6% - 9% (60-90 mL/kg)", "6% - 9% (60-90 مل/كجم)", "Moderate Dehydration", "جفاف متوسط (6-9%)", AlertLevel.WARNING),
    SEVERE(10.0, "≥ 10% (≥100 mL/kg)", "≥ 10% (≥100 مل/كجم)", "Severe Dehydration / Shock", "جفاف حاد / صدمة نقص الحجم (≥10%)", AlertLevel.DANGER)
}

enum class FluidSolutionType(
    val nameEn: String,
    val nameAr: String,
    val sodiumMeqL: Double,
    val potassiumMeqL: Double,
    val dextrosePercent: Double,
    val osmolarityMasmL: Int,
    val recommendedWeightMinKg: Double,
    val recommendedWeightMaxKg: Double,
    val primaryUseEn: String,
    val primaryUseAr: String,
    val cautionEn: String,
    val cautionAr: String
) {
    D10W(
        nameEn = "D10W (10% Dextrose in Water)",
        nameAr = "ديكستروز 10% في ماء (D10W)",
        sodiumMeqL = 0.0,
        potassiumMeqL = 0.0,
        dextrosePercent = 10.0,
        osmolarityMasmL = 505,
        recommendedWeightMinKg = 0.4,
        recommendedWeightMaxKg = 5.0,
        primaryUseEn = "Neonatal maintenance, acute hypoglycemia bolus (2 mL/kg) and infusion, parenteral base for neonates.",
        primaryUseAr = "سوائل الصيانة لحديثي الولادة، جرعة إنقاذ هبوط السكر (2 مل/كجم)، قاعدة المحاليل الوريدية للخدج.",
        cautionEn = "Contains NO electrolytes (0 mEq Na). NEVER use as rapid hydration bolus in older children due to water intoxication risk.",
        cautionAr = "خالٍ تماماً من الصوديوم والأملاح. يُحظر إعطاؤه كدفعة سريعة لإنعاش الجفاف لدى الأطفال لتجنب وذمة الدماغ."
    ),
    NORMAL_SALINE(
        nameEn = "0.9% Normal Saline (0.9% NaCl)",
        nameAr = "محلول ملحي نظامي 0.9% (Normal Saline)",
        sodiumMeqL = 154.0,
        potassiumMeqL = 0.0,
        dextrosePercent = 0.0,
        osmolarityMasmL = 308,
        recommendedWeightMinKg = 0.5,
        recommendedWeightMaxKg = 120.0,
        primaryUseEn = "Isotonic resuscitation bolus (10-20 mL/kg over 20-30 min) for shock, severe dehydration, and isonatremic replacement.",
        primaryUseAr = "دفعات الإنعاش السريعة (10-20 مل/كجم على 20-30 دقيقة) لحالات الصدمة وهبوط التروية والجفاف الحاد.",
        cautionEn = "Contains NO glucose (risk of hypoglycemia if used alone as maintenance in small infants without dextrose monitoring).",
        cautionAr = "لا يحتوي على جلوكوز؛ خطر هبوط السكر إذا استُخدم منفرداً كسائل صيانة لفترة طويلة لدى صغار الأطفال."
    ),
    D5_HALF_NS(
        nameEn = "D5 0.45% NS (+ 20 mEq/L KCl)",
        nameAr = "ديكستروز 5% بنصف ملحي 0.45% (+ بوتاسيوم)",
        sodiumMeqL = 77.0,
        potassiumMeqL = 20.0,
        dextrosePercent = 5.0,
        osmolarityMasmL = 407,
        recommendedWeightMinKg = 3.5,
        recommendedWeightMaxKg = 80.0,
        primaryUseEn = "Standard pediatric maintenance solution for infants >1 month and children up to 18 years.",
        primaryUseAr = "محلول الصيانة القياسي المعتمد للأطفال من عمر شهر حتى 18 سنة لتوفير الصوديوم والجلوكوز اليومي.",
        cautionEn = "Add KCl (20 mEq/L) ONLY after verifying active urine output and stable renal function.",
        cautionAr = "تُضاف أمبولات البوتاسيوم (20 mEq/L) فقط بعد التأكد من نزول البول واستقرار وظائف الكلى."
    ),
    D5_NS(
        nameEn = "D5 0.9% Normal Saline",
        nameAr = "ديكستروز 5% بمحلول ملحي نظامي 0.9% (D5 NS)",
        sodiumMeqL = 154.0,
        potassiumMeqL = 0.0,
        dextrosePercent = 5.0,
        osmolarityMasmL = 560,
        recommendedWeightMinKg = 3.0,
        recommendedWeightMaxKg = 100.0,
        primaryUseEn = "Isotonic maintenance in post-operative, CNS trauma, meningitis, or risk of hyponatremia/SIADH.",
        primaryUseAr = "صيانة متساوية التوتر بعد العمليات الجراحية، إصابات الرأس والجهاز العصبي، وحالات خطورة نقص الصوديوم.",
        cautionEn = "Hypertonic osmolarity in vitro; monitor serum sodium levels to prevent rapid hypernatremia.",
        cautionAr = "عالي التناضح في العبوة؛ مراقبة صوديوم الدم لتجنب حدوث فرط صوديوم الدم."
    ),
    D5_QUARTER_NS(
        nameEn = "D5 0.2% Normal Saline",
        nameAr = "ديكستروز 5% بربع ملحي 0.2% (D5 0.2% NS)",
        sodiumMeqL = 34.0,
        potassiumMeqL = 0.0,
        dextrosePercent = 5.0,
        osmolarityMasmL = 321,
        recommendedWeightMinKg = 2.5,
        recommendedWeightMaxKg = 15.0,
        primaryUseEn = "Slow replacement protocol in hypernatremic dehydration, or early transition in young infants.",
        primaryUseAr = "بروتوكول التعويض البطيء في حالات الجفاف مرتفع الصوديوم (Hypernatremic) لحماية المخ من التورم.",
        cautionEn = "Hypotonic sodium; high risk of acute hyponatremic encephalopathy if overinfused rapidly.",
        cautionAr = "منخفض الصوديوم؛ خطر حدوث وذمة مخية بنقص الصوديوم إذا تم ضخه بسرعة وبكميات زائدة."
    ),
    RINGERS_LACTATE(
        nameEn = "Ringer's Lactate (Hartmann's)",
        nameAr = "رينجر لاكتات (Ringer's Lactate)",
        sodiumMeqL = 130.0,
        potassiumMeqL = 4.0,
        dextrosePercent = 0.0,
        osmolarityMasmL = 273,
        recommendedWeightMinKg = 2.0,
        recommendedWeightMaxKg = 120.0,
        primaryUseEn = "Balanced crystalloid for burn resuscitation, trauma, intraoperative fluid replacement, and metabolic acidosis.",
        primaryUseAr = "محلول متوازن لإنعاش الحروق، الصدمات الجراحية، تعويض السوائل أثناء العمليات، ومعادلة حموضة الدم.",
        cautionEn = "Contains potassium (4 mEq/L) and calcium; avoid mixing with Ceftriaxone or in severe hyperkalemic renal failure.",
        cautionAr = "يحتوي على كالسيوم وبوتاسيوم؛ يُمنع خلطه مع السيفترياكسون في نفس الخط أو في حالات الفشل الكلوي الحاد."
    )
}

data class HollidaySegarResult(
    val weightKg: Double,
    val dailyMaintenanceMl: Double,
    val hourlyMaintenanceRate: Double,
    val shift8HourMl: Double,
    val rule421DescEn: String,
    val rule421DescAr: String
)

data class DehydrationDeficitPlan(
    val weightKg: Double,
    val dehydrationPercent: Double,
    val totalDeficitMl: Double,
    val resuscitationBolusMl: Double,
    val netRemainingDeficitMl: Double,
    val dailyMaintenanceMl: Double,
    val total24HourFluidMl: Double,
    // First 8 hours (50% deficit + 1/3 maintenance)
    val first8HourTotalMl: Double,
    val first8HourHourlyRate: Double,
    // Next 16 hours (50% deficit + 2/3 maintenance)
    val next16HourTotalMl: Double,
    val next16HourHourlyRate: Double,
    val ongoingLossesMl: Double,
    val alertLevel: AlertLevel,
    val recommendationsEn: List<String>,
    val recommendationsAr: List<String>
)
