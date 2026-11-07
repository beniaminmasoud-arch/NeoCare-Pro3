package com.example

import com.example.data.model.ABGInput
import com.example.data.model.AlertLevel
import com.example.data.model.ApgarInput
import com.example.data.model.PhototherapyModality
import com.example.data.model.SilvermanInput
import com.example.data.repository.ClinicalFormulas
import com.example.data.repository.DrugCatalog
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testTfiCalculation() {
        val tfiDol1 = ClinicalFormulas.calculateRecommendedTFI(birthWeightGrams = 900.0, dayOfLife = 1)
        assertEquals(80, tfiDol1)

        val tfiAdjusted = ClinicalFormulas.calculateRecommendedTFI(
            birthWeightGrams = 900.0,
            dayOfLife = 1,
            onPhototherapy = true,
            onRadiantWarmer = true
        )
        assertEquals(110, tfiAdjusted)
    }

    @Test
    fun testGirCalculation() {
        val gir = ClinicalFormulas.calculateGIR(hourlyRateMlHr = 6.25, dextrosePercent = 10.0, weightKg = 1.5)
        assertEquals(6.94, gir, 0.05)

        val rate = ClinicalFormulas.calculateRateFromGIR(desiredGir = 6.944, dextrosePercent = 10.0, weightKg = 1.5)
        assertEquals(6.25, rate, 0.05)
    }

    @Test
    fun testHypoglycemiaBolus() {
        val bolus = ClinicalFormulas.calculateHypoBolusD10W(weightKg = 1.35)
        assertEquals(2.7, bolus, 0.01)
    }

    @Test
    fun testApgarScoring() {
        val normalApgar = ApgarInput(appearance = 2, pulse = 2, grimace = 2, activity = 2, respiration = 2)
        assertEquals(10, normalApgar.totalScore)
        assertEquals(AlertLevel.SAFE, normalApgar.alertLevel)
        assertEquals("Completely pink all over (2)", normalApgar.appearanceLabelEn)
        assertEquals(">= 100 bpm (2)", normalApgar.pulseLabelEn)

        val moderateApgar = ApgarInput(appearance = 1, pulse = 1, grimace = 1, activity = 1, respiration = 1)
        assertEquals(5, moderateApgar.totalScore)
        assertEquals(AlertLevel.WARNING, moderateApgar.alertLevel)

        val depressedApgar = ApgarInput(appearance = 0, pulse = 1, grimace = 0, activity = 0, respiration = 1)
        assertEquals(2, depressedApgar.totalScore)
        assertEquals(AlertLevel.DANGER, depressedApgar.alertLevel)

        val zeroApgar = ApgarInput(appearance = 0, pulse = 0, grimace = 0, activity = 0, respiration = 0)
        assertEquals(0, zeroApgar.totalScore)
        assertEquals(AlertLevel.DANGER, zeroApgar.alertLevel)

        // SpO2 targets
        assertEquals("60% - 65%", normalApgar.getSpO2Target(1))
        assertEquals("80% - 85%", normalApgar.getSpO2Target(5))
        assertEquals("85% - 95%", normalApgar.getSpO2Target(10))

        // NRP action guidance
        assertTrue(depressedApgar.getNrpActionGuidance(1, false).contains("STAT NRP RESUSCITATION"))
        assertTrue(normalApgar.getNrpActionGuidance(1, false).contains("Routine Care"))
    }

    @Test
    fun testSilvermanScoring() {
        val mildSilverman = SilvermanInput(upperChest = 1, lowerChest = 1, xiphoidRetract = 0, naresFlaring = 0, expiratoryGrunt = 0)
        assertEquals(2, mildSilverman.totalScore)
        assertEquals(AlertLevel.SAFE, mildSilverman.alertLevel)

        val severeSilverman = SilvermanInput(upperChest = 2, lowerChest = 2, xiphoidRetract = 2, naresFlaring = 1, expiratoryGrunt = 1)
        assertEquals(8, severeSilverman.totalScore)
        assertEquals(AlertLevel.DANGER, severeSilverman.alertLevel)

        // Test modern 5 signs: nasal flaring, grunting, retractions, breathing pattern, and cyanosis
        val modernFiveSignsMild = SilvermanInput(
            nasalFlaring = 1,
            grunting = 0,
            retractions = 1,
            breathingPattern = 0,
            cyanosis = 0
        )
        assertEquals(2, modernFiveSignsMild.totalScore)
        assertEquals(AlertLevel.SAFE, modernFiveSignsMild.alertLevel)
        assertEquals("Minimal flaring (1)", modernFiveSignsMild.nasalFlaringLabelEn)
        assertEquals("None (0)", modernFiveSignsMild.gruntingLabelEn)

        val modernFiveSignsSevere = SilvermanInput(
            nasalFlaring = 2,
            grunting = 2,
            retractions = 2,
            breathingPattern = 1,
            cyanosis = 1
        )
        assertEquals(8, modernFiveSignsSevere.totalScore)
        assertEquals(AlertLevel.DANGER, modernFiveSignsSevere.alertLevel)
        assertEquals("Marked / continuous flaring (2)", modernFiveSignsSevere.nasalFlaringLabelEn)
        assertEquals("Audible with naked ear (2)", modernFiveSignsSevere.gruntingLabelEn)
        assertEquals("Marked sternal & intercostal retraction (2)", modernFiveSignsSevere.retractionsLabelEn)
        assertEquals("Lag on inspiration (1)", modernFiveSignsSevere.breathingPatternLabelEn)
        assertEquals("Acrocyanosis or relieved with low-flow O2 (1)", modernFiveSignsSevere.cyanosisLabelEn)
    }

    @Test
    fun testBilirubinModalitiesAndExchange() {
        // Safe case: TSB 5.0 at 48h in term infant -> None
        val safeResult = ClinicalFormulas.evaluateBilirubin(
            ageHours = 48.0,
            bilirubinInput = 5.0,
            isUmol = false,
            gestationalWeeks = 39,
            hasRiskFactors = false,
            weightKg = 3.0
        )
        assertEquals(PhototherapyModality.NONE, safeResult.modality)
        assertEquals(AlertLevel.SAFE, safeResult.alertLevel)

        // Single Phototherapy case: TSB 12.5 at 48h -> Single
        val singleResult = ClinicalFormulas.evaluateBilirubin(
            ageHours = 48.0,
            bilirubinInput = 12.5,
            isUmol = false,
            gestationalWeeks = 39,
            hasRiskFactors = false,
            weightKg = 3.0
        )
        assertEquals(PhototherapyModality.SINGLE, singleResult.modality)

        // Critical Exchange Transfusion case: TSB 25.0 -> Exchange
        val exchangeResult = ClinicalFormulas.evaluateBilirubin(
            ageHours = 48.0,
            bilirubinInput = 25.0,
            isUmol = false,
            gestationalWeeks = 39,
            hasRiskFactors = false,
            weightKg = 3.0
        )
        assertEquals(PhototherapyModality.EXCHANGE, exchangeResult.modality)
        assertEquals(AlertLevel.DANGER, exchangeResult.alertLevel)
        assertNotNull(exchangeResult.exchangeProtocolAr)
    }

    @Test
    fun testAirwayEquipmentEstimate() {
        val airway = ClinicalFormulas.calculateAirwayEquipment(weightGrams = 1350.0, gestationalWeeks = 31)
        assertEquals(3.0, airway.ettInternalDiameterMm, 0.01)
        assertEquals(7.0, airway.ettDepthAtLipCm, 0.5)
        assertEquals("Miller 0", airway.laryngoscopeBlade)
    }

    @Test
    fun testABGInterpretation() {
        val abgInput = ABGInput(ph = 7.25, pco2 = 60.0, po2 = 55.0, hco3 = 24.0, fio2Percent = 30.0, mapVentilator = 8.0)
        val result = ClinicalFormulas.interpretABG(abgInput)
        assertTrue(result.primaryDisorderEn.contains("Respiratory Acidosis"))
        assertNotNull(result.oxygenationIndex)
    }
    @Test
    fun testDrugDoseCalculations() {
        val ampicillin = DrugCatalog.drugs.first { it.id == "ampicillin" }
        val dose = ampicillin.calculateDose(weightKg = 1.5, customDosePerKg = 50.0)
        assertEquals(75.0, dose.doseAmount, 0.01)
        assertEquals(0.75, dose.volumeMl, 0.01)
    }

    @Test
    fun testPediatricDoseCappingAdolescent() {
        val paracetamol = DrugCatalog.drugs.first { it.id == "paracetamol" }
        // On a 75 kg adolescent at 15 mg/kg, 75 * 15 = 1125 mg, which exceeds max single dose (1000 mg)
        val cappedDose = paracetamol.calculateDose(weightKg = 75.0, customDosePerKg = 15.0)
        assertTrue(cappedDose.isCappedAtMax)
        assertEquals(1000.0, cappedDose.doseAmount, 0.01)

        // On a 10 kg infant at 15 mg/kg, 10 * 15 = 150 mg (not capped)
        val infantDose = paracetamol.calculateDose(weightKg = 10.0, customDosePerKg = 15.0)
        assertFalse(infantDose.isCappedAtMax)
        assertEquals(150.0, infantDose.doseAmount, 0.01)
    }

    @Test
    fun testPediatricDrugFormulations() {
        val amoxicillin = DrugCatalog.drugs.first { it.id == "amoxicillin" }
        assertTrue(amoxicillin.formulations.isNotEmpty())
        assertEquals("Birth to 18 Years", amoxicillin.ageRangeEn)
        assertTrue(amoxicillin.administrationGuideAr.isNotBlank())
        assertTrue(amoxicillin.ageBands.isNotEmpty())
    }

    @Test
    fun testAntibioticReconstitutionAndPowderDilution() {
        // Ampicillin powder reconstitution check
        val ampicillin = DrugCatalog.drugs.first { it.id == "ampicillin" }
        assertNotNull(ampicillin.reconstitution)
        assertTrue(ampicillin.reconstitution!!.isPowder)
        assertTrue(ampicillin.reconstitution!!.diluentAr.contains("ماء معقم"))
        assertTrue(ampicillin.reconstitution!!.powderVolumeStepsAr.contains("4.8"))

        // Ceftriaxone calcium incompatibility warning
        val ceftriaxone = DrugCatalog.drugs.first { it.id == "ceftriaxone" }
        assertNotNull(ceftriaxone.reconstitution)
        assertTrue(ceftriaxone.reconstitution!!.stabilityAr.contains("كالسيوم"))

        // Vancomycin infusion warning
        val vancomycin = DrugCatalog.drugs.first { it.id == "vancomycin" }
        assertNotNull(vancomycin.reconstitution)
        assertTrue(vancomycin.reconstitution!!.powderVolumeStepsAr.contains("10"))
        assertTrue(vancomycin.reconstitution!!.administrationMethodAr.contains("60 دقيقة"))
    }

    @Test
    fun testDynamicWeightDoseUpdate() {
        val ampicillin = DrugCatalog.drugs.first { it.id == "ampicillin" }
        // Preterm weight: 1.2 kg -> 50 mg/kg = 60 mg, volume = 0.60 mL
        val pretermDose = ampicillin.calculateDose(weightKg = 1.2, customDosePerKg = 50.0)
        assertEquals(60.0, pretermDose.doseAmount, 0.01)
        assertEquals(0.60, pretermDose.volumeMl, 0.01)

        // Child weight: 20.0 kg -> 50 mg/kg = 1000 mg, volume = 10.0 mL
        val childDose = ampicillin.calculateDose(weightKg = 20.0, customDosePerKg = 50.0)
        assertEquals(1000.0, childDose.doseAmount, 0.01)
        assertEquals(10.0, childDose.volumeMl, 0.01)
    }

    @Test
    fun testHollidaySegarMaintenance() {
        // Child 8.0 kg (<= 10 kg: 100 mL/kg/day)
        val infantMaint = ClinicalFormulas.calculateHollidaySegar(8.0)
        assertEquals(800.0, infantMaint.dailyMaintenanceMl, 0.1)
        assertEquals(33.3, infantMaint.hourlyMaintenanceRate, 0.2)

        // Child 14.0 kg (11-20 kg: 1000 + 4*50 = 1200 mL/day)
        val toddlerMaint = ClinicalFormulas.calculateHollidaySegar(14.0)
        assertEquals(1200.0, toddlerMaint.dailyMaintenanceMl, 0.1)
        assertEquals(50.0, toddlerMaint.hourlyMaintenanceRate, 0.2)

        // Child 28.0 kg (> 20 kg: 1500 + 8*20 = 1660 mL/day)
        val olderChildMaint = ClinicalFormulas.calculateHollidaySegar(28.0)
        assertEquals(1660.0, olderChildMaint.dailyMaintenanceMl, 0.1)
        assertEquals(69.2, olderChildMaint.hourlyMaintenanceRate, 0.2)
    }

    @Test
    fun testDehydrationDeficitReplacement() {
        // 10 kg child with moderate dehydration (7%)
        val plan = ClinicalFormulas.calculateDehydrationDeficit(weightKg = 10.0, dehydrationPercent = 7.0)
        assertEquals(700.0, plan.totalDeficitMl, 0.1)
        assertEquals(1000.0, plan.dailyMaintenanceMl, 0.1)
        // First 8 hours: (50% deficit = 350 mL) + (1/3 maintenance = 333.3 mL) = 683.3 mL -> ~85.4 mL/hr
        assertEquals(85.4, plan.first8HourHourlyRate, 0.3)
        // Next 16 hours: (50% deficit = 350 mL) + (2/3 maintenance = 666.7 mL) = 1016.7 mL -> ~63.5 mL/hr
        assertEquals(63.5, plan.next16HourHourlyRate, 0.3)

        // 10 kg child with severe dehydration (10%) -> emergency resuscitation bolus 20 mL/kg = 200 mL
        val severePlan = ClinicalFormulas.calculateDehydrationDeficit(weightKg = 10.0, dehydrationPercent = 10.0)
        assertEquals(200.0, severePlan.resuscitationBolusMl, 0.1)
        assertEquals(AlertLevel.DANGER, severePlan.alertLevel)
    }

    @Test
    fun testSolutionProperties() {
        val d10w = com.example.data.model.FluidSolutionType.D10W
        assertEquals(0.0, d10w.sodiumMeqL, 0.01)
        assertEquals(10.0, d10w.dextrosePercent, 0.01)

        val ns = com.example.data.model.FluidSolutionType.NORMAL_SALINE
        assertEquals(154.0, ns.sodiumMeqL, 0.01)
        assertEquals(0.0, ns.dextrosePercent, 0.01)

        val d5HalfNs = com.example.data.model.FluidSolutionType.D5_HALF_NS
        assertEquals(77.0, d5HalfNs.sodiumMeqL, 0.01)
        assertEquals(20.0, d5HalfNs.potassiumMeqL, 0.01)
        assertEquals(5.0, d5HalfNs.dextrosePercent, 0.01)
    }

    @Test
    fun testGrowthPercentiles() {
        // 12-month old male, 9.6 kg (expected median for 12m), height 75 cm (expected median)
        val growth12m = ClinicalFormulas.calculateGrowthPercentiles(
            ageMonths = 12.0,
            isMale = true,
            weightKg = 9.66,
            heightCm = 75.2
        )
        // Median should yield ~50th percentile
        assertEquals(50.0, growth12m.weightPercentile, 5.0)
        assertEquals(50.0, growth12m.heightPercentile, 5.0)
        assertEquals(AlertLevel.SAFE, growth12m.alertLevel)
        assertTrue(growth12m.weightCategoryEn.contains("Healthy Weight"))

        // Severely underweight infant: 12-month old male at 6.0 kg
        val underweightGrowth = ClinicalFormulas.calculateGrowthPercentiles(
            ageMonths = 12.0,
            isMale = true,
            weightKg = 6.0,
            heightCm = 75.0
        )
        assertTrue(underweightGrowth.weightPercentile < 3.0)
        assertEquals(AlertLevel.DANGER, underweightGrowth.alertLevel)
        assertTrue(underweightGrowth.weightCategoryEn.contains("Severely Underweight"))
    }
}


