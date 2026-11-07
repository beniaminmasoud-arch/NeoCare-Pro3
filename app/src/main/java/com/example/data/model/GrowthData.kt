package com.example.data.model

data class GrowthPercentileResult(
    val ageMonths: Double,
    val genderIsMale: Boolean,
    val weightKg: Double,
    val heightCm: Double,
    val bmi: Double,
    // Percentile values (0 - 100)
    val weightPercentile: Double,
    val heightPercentile: Double,
    val bmiPercentile: Double,
    val weightZScore: Double,
    val heightZScore: Double,
    val bmiZScore: Double,
    // Clinical categorizations
    val weightCategoryEn: String,
    val weightCategoryAr: String,
    val heightCategoryEn: String,
    val heightCategoryAr: String,
    val bmiCategoryEn: String,
    val bmiCategoryAr: String,
    val overallAssessmentEn: String,
    val overallAssessmentAr: String,
    val alertLevel: AlertLevel,
    // Standard WHO/CDC curve reference points for this age & gender
    val weightP3: Double,
    val weightP15: Double,
    val weightP50: Double,
    val weightP85: Double,
    val weightP97: Double,
    val heightP3: Double,
    val heightP15: Double,
    val heightP50: Double,
    val heightP85: Double,
    val heightP97: Double
)
