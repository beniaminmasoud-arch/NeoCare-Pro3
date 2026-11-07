package com.example.data.model

data class ABGInput(
    val ph: Double = 7.35,
    val pco2: Double = 40.0,
    val po2: Double = 65.0,
    val hco3: Double = 22.0,
    val be: Double = 0.0,
    val fio2Percent: Double = 21.0,
    val mapVentilator: Double = 8.0 // Mean Airway Pressure in cmH2O
)

data class ABGAnalysisResult(
    val primaryDisorderEn: String,
    val primaryDisorderAr: String,
    val compensationEn: String,
    val compensationAr: String,
    val oxygenationEn: String,
    val oxygenationAr: String,
    val oxygenationIndex: Double?, // (MAP * FiO2) / PaO2
    val pfRatio: Double?, // PaO2 / (FiO2 / 100)
    val ventilatorRecommendationsEn: List<String>,
    val ventilatorRecommendationsAr: List<String>,
    val alertLevel: AlertLevel
)

data class AirwayEquipmentEstimate(
    val ettInternalDiameterMm: Double,
    val ettDepthAtLipCm: Double,
    val laryngoscopeBlade: String,
    val suctionCatheterFr: String,
    val uvcDepthCm: Double,
    val uacDepthLowCm: Double,
    val uacDepthHighCm: Double
)
