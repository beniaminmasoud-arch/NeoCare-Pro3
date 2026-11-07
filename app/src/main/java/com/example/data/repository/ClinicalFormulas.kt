package com.example.data.repository

import com.example.data.model.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object ClinicalFormulas {

    /**
     * Recommended baseline Total Fluid Intake (TFI) in mL/kg/day
     * based on Day of Life and birth weight in grams or kilograms.
     */
    fun calculateRecommendedTFI(
        birthWeightGrams: Double,
        dayOfLife: Int,
        onPhototherapy: Boolean = false,
        onRadiantWarmer: Boolean = false
    ): Int {
        val dol = max(1, min(dayOfLife, 7))
        val baseTfi = when {
            birthWeightGrams < 1000.0 -> {
                when (dol) {
                    1 -> 80
                    2 -> 100
                    3 -> 120
                    4 -> 135
                    5 -> 145
                    6 -> 150
                    else -> 150
                }
            }
            birthWeightGrams <= 1500.0 -> {
                when (dol) {
                    1 -> 70
                    2 -> 90
                    3 -> 110
                    4 -> 125
                    5 -> 140
                    6 -> 150
                    else -> 150
                }
            }
            else -> {
                when (dol) {
                    1 -> 60
                    2 -> 80
                    3 -> 100
                    4 -> 120
                    5 -> 135
                    6 -> 145
                    else -> 150
                }
            }
        }

        var adjusted = baseTfi
        if (onPhototherapy) adjusted += 15
        if (onRadiantWarmer) adjusted += 15
        return adjusted
    }

    fun calculateDailyFluidVolume(weightKg: Double, tfiMlKgDay: Double): Double {
        return weightKg * tfiMlKgDay
    }

    fun calculateHourlyRate(dailyVolumeMl: Double): Double {
        return dailyVolumeMl / 24.0
    }

    fun calculateGIR(hourlyRateMlHr: Double, dextrosePercent: Double, weightKg: Double): Double {
        if (weightKg <= 0) return 0.0
        return (hourlyRateMlHr * dextrosePercent) / (6.0 * weightKg)
    }

    fun calculateRateFromGIR(desiredGir: Double, dextrosePercent: Double, weightKg: Double): Double {
        if (dextrosePercent <= 0) return 0.0
        return (desiredGir * 6.0 * weightKg) / dextrosePercent
    }

    fun calculateDextrosePercentFromGIR(desiredGir: Double, hourlyRateMlHr: Double, weightKg: Double): Double {
        if (hourlyRateMlHr <= 0) return 0.0
        return (desiredGir * 6.0 * weightKg) / hourlyRateMlHr
    }

    fun calculateHypoBolusD10W(weightKg: Double): Double {
        return weightKg * 2.0
    }

    fun calculateAirwayEquipment(weightGrams: Double, gestationalWeeks: Int): AirwayEquipmentEstimate {
        val weightKg = weightGrams / 1000.0

        val ettId = when {
            weightGrams < 1000 || gestationalWeeks < 28 -> 2.5
            weightGrams <= 2000 || gestationalWeeks <= 34 -> 3.0
            weightGrams <= 3000 || gestationalWeeks <= 38 -> 3.5
            else -> 3.5
        }

        val lipDepthFormula = weightKg + 6.0
        val ettDepth = when {
            gestationalWeeks in 23..24 -> 5.5
            gestationalWeeks in 25..26 -> 6.0
            gestationalWeeks in 27..29 -> 6.5
            gestationalWeeks in 30..32 -> 7.0
            gestationalWeeks in 33..34 -> 7.5
            gestationalWeeks in 35..37 -> 8.0
            gestationalWeeks >= 38 -> max(8.5, lipDepthFormula)
            else -> lipDepthFormula
        }

        val blade = when {
            weightGrams < 1000 -> "Miller 00"
            weightGrams <= 2500 -> "Miller 0"
            else -> "Miller 1"
        }

        val suction = when (ettId) {
            2.5 -> "5 - 6 Fr"
            3.0 -> "6 - 8 Fr"
            else -> "8 - 10 Fr"
        }

        val uvc = (weightKg * 1.5) + 5.5
        val uacLow = (weightKg * 0.8) + 7.0
        val uacHigh = (weightKg * 3.0) + 9.0

        return AirwayEquipmentEstimate(
            ettInternalDiameterMm = ettId,
            ettDepthAtLipCm = (ettDepth * 10.0).roundToInt() / 10.0,
            laryngoscopeBlade = blade,
            suctionCatheterFr = suction,
            uvcDepthCm = (uvc * 10.0).roundToInt() / 10.0,
            uacDepthLowCm = (uacLow * 10.0).roundToInt() / 10.0,
            uacDepthHighCm = (uacHigh * 10.0).roundToInt() / 10.0
        )
    }

    /**
     * Comprehensive AAP 2022 Hyperbilirubinemia Evaluator:
     * Determines whether therapy is:
     * - None (below threshold)
     * - Single Phototherapy (at threshold)
     * - Double Intensive Phototherapy (>=2.5 mg/dL above threshold)
     * - Triple Super-Intensive Phototherapy (within 2 mg/dL of exchange transfusion)
     * - Exchange Transfusion (at or above exchange transfusion threshold)
     * Also generates exchange volume (mL), IVIG indications, and step-by-step nursing & medical instructions.
     */
    fun evaluateBilirubin(
        ageHours: Double,
        bilirubinInput: Double,
        isUmol: Boolean,
        gestationalWeeks: Int,
        hasRiskFactors: Boolean,
        weightKg: Double = 2.5
    ): BilirubinResult {
        // Convert to mg/dL if input in umol/L (1 mg/dL = 17.1 umol/L)
        val mgDl = if (isUmol) bilirubinInput / 17.1 else bilirubinInput
        val hours = max(12.0, min(ageHours, 168.0))

        // Baseline phototherapy threshold estimate by gestational age & risk
        val baseAt24h = when {
            gestationalWeeks >= 38 && !hasRiskFactors -> 9.0
            gestationalWeeks >= 38 && hasRiskFactors -> 7.5
            gestationalWeeks in 35..37 && !hasRiskFactors -> 7.5
            gestationalWeeks in 35..37 && hasRiskFactors -> 6.0
            else -> 5.0 // <35 weeks
        }

        val baseAt96h = when {
            gestationalWeeks >= 38 && !hasRiskFactors -> 18.0
            gestationalWeeks >= 38 && hasRiskFactors -> 16.0
            gestationalWeeks in 35..37 && !hasRiskFactors -> 16.0
            gestationalWeeks in 35..37 && hasRiskFactors -> 13.5
            else -> 12.0 // <35 weeks
        }

        // Interpolation between 24h and 96h
        val photoThreshold = if (hours <= 24.0) {
            val ratio = hours / 24.0
            baseAt24h * ratio
        } else if (hours < 96.0) {
            val ratio = (hours - 24.0) / (96.0 - 24.0)
            baseAt24h + (baseAt96h - baseAt24h) * ratio
        } else {
            baseAt96h
        }

        val exchangeThreshold = photoThreshold + 4.5

        val diffFromPhoto = (mgDl - photoThreshold)
        val diffFromExchange = (exchangeThreshold - mgDl)

        // Classify modality based on bilirubin level relative to thresholds
        val modality: PhototherapyModality
        val alertLevel: AlertLevel
        val riskZoneEn: String
        val riskZoneAr: String
        val recEn: String
        val recAr: String
        val therapyDetailsEn = mutableListOf<String>()
        val therapyDetailsAr = mutableListOf<String>()
        var exchangeProtocolEn: String? = null
        var exchangeProtocolAr: String? = null
        var ivigProtocolEn: String? = null
        var ivigProtocolAr: String? = null
        val recheckIntervalHours: Int

        // Exchange volume calculation
        val doubleVolumeExchangeMl = if (gestationalWeeks >= 37) {
            (weightKg * 160.0).roundToInt()
        } else {
            (weightKg * 180.0).roundToInt()
        }

        when {
            // Case 1: Exchange Transfusion
            mgDl >= exchangeThreshold -> {
                modality = PhototherapyModality.EXCHANGE
                alertLevel = AlertLevel.DANGER
                recheckIntervalHours = 2
                riskZoneEn = "CRITICAL: Bilirubin at/above Exchange Transfusion Threshold"
                riskZoneAr = "حرج جداً: نسبة الصفراء تجاوزت عتبة تبديل الدم الكامل (Exchange Transfusion)"
                recEn = "Emergency: Prepare for Double-Volume Exchange Transfusion (DVET). In parallel, initiate Triple Intensive Phototherapy immediately with maximum irradiance."
                recAr = "طوارئ قصوى: تحضير فوري لتبديل الدم الكامل (Double-Volume Exchange Transfusion) مع تشغيل علاج ضوئي ثلاثي فائق الكثافة فوراً أثناء تجهيز الدم."

                therapyDetailsAr.add("العلاج الفوري: تشغيل علاج ضوئي ثلاثي (جهازان علويان + مرتبة ألياف ضوئية Biliblanket تحت الظهر) بأقصى شدة إشعاعية (>35 µW/cm²/nm).")
                therapyDetailsAr.add("تجهيز دم للتبديل: نوع الدم O Rh-negative متوافق مع الأم والرضيع، طازج (<5 أيام)، مفلتر من كرات الدم البيضاء وخالي من CMV.")
                therapyDetailsAr.add("حجم الدم التبادلي المحسوب لوزن ${String.format("%.2f", weightKg)} كجم = $doubleVolumeExchangeMl مل (Double Volume: ${if (gestationalWeeks >= 37) "160" else "180"} مل/كجم).")
                therapyDetailsAr.add("مراقبة الكالسيوم: حقن 1 مل جلوكونات الكالسيوم 10% وريدياً ببطء بعد كل 100 مل دم متبادل للوقاية من نقص الكالسيوم الناتج عن السترات.")

                therapyDetailsEn.add("Immediate Mode: Triple intensive phototherapy (2 overhead LED banks + Biliblanket underneath) with maximum irradiance (>35 µW/cm²/nm).")
                therapyDetailsEn.add("Crossmatch Blood: O Rh-negative blood compatible with infant & mother, fresh (<5 days), irradiated, leukodepleted.")
                therapyDetailsEn.add("Calculated Double Exchange Volume for ${String.format("%.2f", weightKg)} kg = $doubleVolumeExchangeMl mL.")
                therapyDetailsEn.add("Calcium Monitoring: Administer 1 mL of 10% Calcium Gluconate IV slow push after every 100 mL of citrated blood exchanged.")

                exchangeProtocolAr = "بروتوكول تبديل الدم: تثبيت خط وريد سري UVC مركزي، سحب وضخ دفعات صغيرة (5-10 مل/دقيقة)، مراقبة العلامات الحيوية المستمرة، فحص سكر الدم والكالسيوم والغازات بعد الإجراء."
                exchangeProtocolEn = "Exchange Protocol: Secure central UVC line, perform small aliquots push/pull (5-10 mL/cycle), continuous ECG/BP/SpO2 monitoring, post-procedure TSB, glucose, and electrolytes."

                ivigProtocolAr = "الجلوبيولين المناعي (IVIG): في حال كان السبب تكسير دم مناعي (عدم توافق ABO أو Rh مع إيجابية اختبار كومبس)، إعطاء IVIG بجرعة 0.5 - 1 جم/كجم وريدياً على ساعتين للمساعدة في خفض الحاجة للتبديل."
                ivigProtocolEn = "IVIG Protocol: If hemolytic disease (ABO/Rh incompatibility with positive DAT/Coombs), give IVIG 0.5 - 1.0 g/kg IV over 2 hours to avoid exchange transfusion."
            }

            // Case 2: Triple / Super-Intensive Phototherapy (within 2 mg/dL of exchange)
            mgDl >= (exchangeThreshold - 2.0) -> {
                modality = PhototherapyModality.TRIPLE
                alertLevel = AlertLevel.DANGER
                recheckIntervalHours = 3
                riskZoneEn = "VERY HIGH: Within 2 mg/dL of Exchange Transfusion Level"
                riskZoneAr = "مرتفع وحرج: الصفراء على بعد أقل من 2 مجم من مستوى تبديل الدم"
                recEn = "Start Triple Super-Intensive Phototherapy immediately. Expose >80% body surface. Alert blood bank to crossmatch blood on standby in case bilirubin does not decline."
                recAr = "بدء علاج ضوئي ثلاثي فائق الكثافة فوراً. تعريض >80% من سطح الجسم للضوء. إبلاغ بنك الدم لتجهيز دم للمطابقة احتياطياً في حال عدم الاستجابة السريعة."

                therapyDetailsAr.add("مواصفات الضوئي الثلاثي: جهازان LED علويان على مسافة 25-30 سم + مرتبة Biliblanket تحت الرضيع.")
                therapyDetailsAr.add("أقصى مساحة مكشوفة: تجريد الطفل من الملابس مع الإبقاء على حفاضة صغيرة جداً أو نزعها واستخدام فراش ماص.")
                therapyDetailsAr.add("عدم قطع العلاج: استمرار العلاج الضوئي حتى أثناء الرضاعة (الرضاعة تحت الضوء أو عبر أنبوب معوي NG tube).")
                therapyDetailsAr.add("سوائل إضافية: تعويض السوائل الوريدية بزيادة 15-20 مل/كجم/يوم لمنع الجفاف ودعم التخلص من الصفراء بالبول والبراز.")

                therapyDetailsEn.add("Triple Setup: 2 overhead high-intensity LED banks at 25-30 cm + fiberoptic pad/mattress beneath infant.")
                therapyDetailsEn.add("Maximum Skin Exposure: Minimal diaper size, expose >80% of body surface area.")
                therapyDetailsEn.add("Do Not Interrupt: Maintain continuous phototherapy; feeds given via NG tube if needed.")
                therapyDetailsEn.add("Hydration Support: Add +15-20 mL/kg/day to IV fluid goal to optimize biliary excretion.")

                exchangeProtocolAr = "تنبيه الدم الاحتياطي: طلب توافق دم (Crossmatch) بحجم $doubleVolumeExchangeMl مل استعداداً لتبديل الدم إذا فشل الضوئي في خفض الصفراء بمعدل 1-2 مجم خلال 4 ساعات."
                exchangeProtocolEn = "Standby Crossmatch: Request $doubleVolumeExchangeMl mL blood for standby exchange transfusion if TSB fails to fall by 1-2 mg/dL within 4 hours."

                if (hasRiskFactors) {
                    ivigProtocolAr = "في حالات تكسير الدم المناعي (ABO/Rh): يُنصح بإعطاء IVIG بجرعة 0.75 - 1.0 جم/كجم وريدياً لتفادي الوصول لتبديل الدم."
                    ivigProtocolEn = "Isoimmune Hemolysis: Give IVIG 0.75 - 1.0 g/kg IV over 2h to halt hemolysis and avert exchange transfusion."
                }
            }

            // Case 3: Double Intensive Phototherapy (>=2.5 mg/dL above phototherapy threshold)
            mgDl >= (photoThreshold + 2.5) -> {
                modality = PhototherapyModality.DOUBLE
                alertLevel = AlertLevel.WARNING
                recheckIntervalHours = 4
                riskZoneEn = "HIGH: Significant elevation above Phototherapy Threshold"
                riskZoneAr = "مرتفع: الصفراء أعلى من عتبة العلاج بـ 2.5 مجم أو أكثر"
                recEn = "Start Double Intensive Phototherapy. Combine overhead high-intensity blue LED with fiberoptic blanket/pad underneath."
                recAr = "بدء علاج ضوئي ثنائي مكثف (Double Phototherapy). دمج مصباح LED علوي عالي الشدة مع فرشة ألياف ضوئية Biliblanket تحت الطفل."

                therapyDetailsAr.add("مواصفات الضوئي الثنائي: مصباح LED علوي عالي الشدة (طول موجي 460-490 نانومتر) + فرشة ضوئية Biliblanket تحت الظهر.")
                therapyDetailsAr.add("حماية العينين: تركيب غطاء العينين الخاص بالضوئي وفحصه كل 4 ساعات لمنع انزلاقه أو حجب مجرى الهواء.")
                therapyDetailsAr.add("التغذية والترطيب: تشجيع الرضاعة كل ساعتين ونصف إلى 3 ساعات؛ متابعة عدد الحفاضات المبللة ومراقبة وزن الطفل.")
                therapyDetailsAr.add("معدل الهبوط المتوقع: انخفاض الصفراء بمقدار 1 إلى 2 مجم/ديسيلتر خلال أول 4 إلى 6 ساعات.")

                therapyDetailsEn.add("Double Setup: Overhead blue LED phototherapy unit + fiberoptic blanket under back.")
                therapyDetailsEn.add("Eye & Gonad Protection: Ensure opaque eye shields are properly secured; inspect eyes every 4h.")
                therapyDetailsEn.add("Feeding Schedule: Breastfeed or bottle feed every 2.5 - 3 hours; monitor wet diapers and daily weight.")
                therapyDetailsEn.add("Target Decline: Expect 1 - 2 mg/dL drop in TSB within first 4 - 6 hours of intensive phototherapy.")
            }

            // Case 4: Single Standard Phototherapy (at or up to +2.5 mg/dL above photo threshold)
            mgDl >= photoThreshold -> {
                modality = PhototherapyModality.SINGLE
                alertLevel = AlertLevel.WARNING
                recheckIntervalHours = 6
                riskZoneEn = "ELEVATED: At / slightly above Phototherapy Threshold"
                riskZoneAr = "مرتفع طفيفاً: عند أو أعلى قليلاً من عتبة بدء العلاج الضوئي"
                recEn = "Start Single Standard Phototherapy. Position overhead blue LED light 30 - 35 cm above infant. Ensure adequate oral intake."
                recAr = "بدء علاج ضوئي أحادي قياسي (Single Phototherapy). وضع مصباح LED أزرق على ارتفاع 30-35 سم فوق الطفل مع تغطية العينين ودعم التغذية."

                therapyDetailsAr.add("مواصفات الضوئي الأحادي: جهاز ضوئي LED أزرق واحد فوق الحاضنة، المسافة 30 - 35 سم.")
                therapyDetailsAr.add("حماية العينين: تركيب غطاء واقي للعينين معتم لمنع تضرر شبكية العين من الضوء الأزرق.")
                therapyDetailsAr.add("دعم الرضاعة الطبيعية: يمكن إخراج الطفل لمدة 20-30 دقيقة للرضاعة وتغيير الحفاض ثم إعادته فوراً للضوء.")
                therapyDetailsAr.add("سوائل إضافية: إضافة 10-15 مل/كجم/يوم لسوائل الطفل لتعويض الفقدان غير المحسوس.")

                therapyDetailsEn.add("Single Setup: One overhead blue LED bank at 30-35 cm above bassinet/incubator.")
                therapyDetailsEn.add("Eye Shields: Apply opaque eye patches; check regularly for corneal safety.")
                therapyDetailsEn.add("Enteral Feeding: Infant may be briefly held (20-30 min) for breastfeeding every 3 hours.")
                therapyDetailsEn.add("Fluid Adjustment: Supplement +10-15 mL/kg/day to fluid quota to offset insensible water loss.")
            }

            // Case 5: No Phototherapy (Below threshold)
            else -> {
                modality = PhototherapyModality.NONE
                alertLevel = AlertLevel.SAFE
                recheckIntervalHours = if (diffFromPhoto >= -2.0) 8 else 12
                riskZoneEn = "NORMAL / LOW RISK: Below Phototherapy Threshold"
                riskZoneAr = "آمن / منخفض الخطورة: نسبة الصفراء أقل من عتبة العلاج الضوئي"
                recEn = "No phototherapy needed at this time. Optimize enteral feeding to stimulate bowel motility and bilirubin excretion."
                recAr = "لا حاجة لعلاج ضوئي في الوقت الراهن. تحسين الرضاعة الطبيعية لتحفيز حركة الأمعاء وإخراج الصفراء في البراز ومتابعة سريرية."

                therapyDetailsAr.add("خطة المتابعة: الرضاعة المتكررة كل 2-3 ساعات لزيادة إخراج البراز والعصارة الصفراوية.")
                therapyDetailsAr.add("المراقبة السريرية: متابعة امتداد اللون الأصفر بالجلد (قاعدة كرامر) ونشاط الرضيع.")
                therapyDetailsAr.add("تحذير الأهل: مراجعة المستشفى فوراً في حال قلة الرضاعة، الخمول الشديد، أو تحول البراز للون الأبيض/الطباشيري.")

                therapyDetailsEn.add("Care Plan: Frequent breastfeeding/formula every 2-3 hours to accelerate meconium/stool clearance.")
                therapyDetailsEn.add("Clinical Tracking: Monitor cephalocaudal jaundice progression and infant alertness.")
                therapyDetailsEn.add("Red Flags: Advise immediate evaluation if poor feeding, extreme lethargy, or acholic pale stools appear.")
            }
        }

        // Stopping criteria
        val stoppingTarget = max(3.0, (photoThreshold - 2.5))

        val roundedPhoto = (photoThreshold * 10.0).roundToInt() / 10.0
        val roundedExchange = (exchangeThreshold * 10.0).roundToInt() / 10.0
        val roundedMgDl = (mgDl * 10.0).roundToInt() / 10.0
        val roundedDiffPhoto = (diffFromPhoto * 10.0).roundToInt() / 10.0
        val roundedDiffExchange = (diffFromExchange * 10.0).roundToInt() / 10.0
        val roundedStopping = (stoppingTarget * 10.0).roundToInt() / 10.0

        return BilirubinResult(
            ageHours = hours,
            bilirubinValue = bilirubinInput,
            isUmol = isUmol,
            mgDlValue = roundedMgDl,
            photoThreshold = roundedPhoto,
            exchangeThreshold = roundedExchange,
            modality = modality,
            diffFromPhoto = roundedDiffPhoto,
            diffFromExchange = roundedDiffExchange,
            riskZoneEn = riskZoneEn,
            riskZoneAr = riskZoneAr,
            recommendationEn = recEn,
            recommendationAr = recAr,
            therapyDetailsEn = therapyDetailsEn,
            therapyDetailsAr = therapyDetailsAr,
            exchangeProtocolEn = exchangeProtocolEn,
            exchangeProtocolAr = exchangeProtocolAr,
            ivigProtocolEn = ivigProtocolEn,
            ivigProtocolAr = ivigProtocolAr,
            recheckIntervalHours = recheckIntervalHours,
            stoppingTargetMgDl = roundedStopping,
            alertLevel = alertLevel
        )
    }

    fun interpretABG(input: ABGInput): ABGAnalysisResult {
        val ph = input.ph
        val pco2 = input.pco2
        val hco3 = input.hco3
        val po2 = input.po2
        val fio2 = max(21.0, input.fio2Percent)
        val map = input.mapVentilator

        val isAcidemia = ph < 7.35
        val isAlkalemia = ph > 7.45

        val pco2Acid = pco2 > 45.0
        val pco2Alk = pco2 < 35.0
        val hco3Acid = hco3 < 22.0
        val hco3Alk = hco3 > 26.0

        val primaryEn: String
        val primaryAr: String
        val compEn: String
        val compAr: String
        val recsEn = mutableListOf<String>()
        val recsAr = mutableListOf<String>()

        when {
            isAcidemia -> {
                when {
                    pco2Acid && hco3Acid -> {
                        primaryEn = "Mixed Respiratory & Metabolic Acidosis"
                        primaryAr = "حموضة تنفسية وأيضية مختلطة"
                        compEn = "Severe combined disorder with no compensation"
                        compAr = "اضطراب حاد مركب دون تعويض"
                        recsEn.add("Urgent: Optimize ventilation to blow off CO2 (increase Rate/PIP).")
                        recsAr.add("عاجل: زيادة التهوية للتخلص من ثاني أكسيد الكربون (رفع معدل التنفس أو ضغط PIP).")
                        recsEn.add("Assess perfusion, blood pressure, and consider fluid resuscitation / inotropes for metabolic lactic component.")
                        recsAr.add("تقييم التروية الدموية وضغط الدم والإنعاش بالسوائل للمكون الأيضي.")
                    }
                    pco2Acid -> {
                        primaryEn = "Respiratory Acidosis (CO2 Retention)"
                        primaryAr = "حموضة تنفسية (احتباس ثاني أكسيد الكربون)"
                        if (hco3Alk) {
                            compEn = "Partially compensated by renal bicarbonate retention"
                            compAr = "معوض جزئياً باحتباس البيكربونات الكلوي"
                        } else {
                            compEn = "Uncompensated acute respiratory failure"
                            compAr = "غير معوض (فشل تنفسي حاد)"
                        }
                        recsEn.add("Hypoventilation: Increase Minute Ventilation by increasing Respiratory Rate or PIP/Vt.")
                        recsAr.add("نقص التهوية: زيادة التهوية الدقيقة بزيادة معدل التنفس أو ضغط PIP.")
                        recsEn.add("Check ETT position, patency, and rule out pneumothorax or mucus plugging.")
                        recsAr.add("فحص موضع وسالكية الأنبوبة الحنجرية واستبعاد الاسترواح الصدري.")
                    }
                    hco3Acid -> {
                        primaryEn = "Metabolic Acidosis (Low Bicarbonate / Base Deficit)"
                        primaryAr = "حموضة أيضية (انخفاض البيكربونات ونقص القواعد)"
                        if (pco2Alk) {
                            compEn = "Partially compensated by respiratory hyperventilation"
                            compAr = "معوض جزئياً بفرط التنفس التعويضي"
                        } else {
                            compEn = "Uncompensated metabolic acidosis"
                            compAr = "حموضة أيضية غير معوضة"
                        }
                        recsEn.add("Evaluate tissue perfusion, lactate, blood pressure, sepsis, and dehydration.")
                        recsAr.add("تقييم التروية النسيجية وحمض اللاكتيك والضغط وتسمم الدم والجفاف.")
                        recsEn.add("If perfusion compromised, consider Normal Saline 10 mL/kg bolus.")
                        recsAr.add("في حال هبوط التروية، إعطاء محلول ملحي 10 مل/كجم.")
                    }
                    else -> {
                        primaryEn = "Unspecified Acidemia"
                        primaryAr = "حموضة دم غير محددة"
                        compEn = "Borderline parameters"
                        compAr = "معاملات حدية"
                    }
                }
            }
            isAlkalemia -> {
                when {
                    pco2Alk && hco3Alk -> {
                        primaryEn = "Mixed Respiratory & Metabolic Alkalosis"
                        primaryAr = "قلوية تنفسية وأيضية مختلطة"
                        compEn = "Severe combined alkalosis"
                        compAr = "قلوية مركبة حادة"
                        recsEn.add("Decrease ventilator rate to avoid hypocapnia and cerebral vasoconstriction.")
                        recsAr.add("تقليل معدل التنفس لتجنب انخفاض CO2 الشديد وانقباض أوعية الدماغ.")
                    }
                    pco2Alk -> {
                        primaryEn = "Respiratory Alkalosis (Hyperventilation / Overventilation)"
                        primaryAr = "قلوية تنفسية (فرط تهوية صناعية)"
                        compEn = if (hco3Acid) "Partially compensated renal bicarbonate excretion" else "Uncompensated"
                        compAr = if (hco3Acid) "معوض جزئياً بإفراز الكلى للبيكربونات" else "غير معوض"
                        recsEn.add("Overventilation: Wean ventilator rate, PIP, or tidal volume.")
                        recsAr.add("فرط تهوية: تقليل معدل التنفس الصناعي أو ضغط PIP.")
                        recsEn.add("Avoid severe hypocapnia (pCO2 < 30) due to risk of periventricular leukomalacia (PVL).")
                        recsAr.add("تجنب الهبوط الشديد في pCO2 لتفادي تضرر أنسجة المخ لدى الخدج.")
                    }
                    hco3Alk -> {
                        primaryEn = "Metabolic Alkalosis"
                        primaryAr = "قلوية أيضية"
                        compEn = if (pco2Acid) "Compensatory hypoventilation" else "Uncompensated"
                        compAr = if (pco2Acid) "نقص تهوية تعويضي" else "غير معوض"
                        recsEn.add("Review diuretic therapy (Lasix), NG suction losses, or electrolyte imbalances.")
                        recsAr.add("مراجعة مدرات البول (اللازكس) وفقدان سوائل المعدة واضطراب الأملاح.")
                    }
                    else -> {
                        primaryEn = "Borderline Alkalemia"
                        primaryAr = "قلوية حدية"
                        compEn = "Normal compensation range"
                        compAr = "ضمن النطاق التعويضي"
                    }
                }
            }
            else -> {
                if (pco2Acid && hco3Alk) {
                    primaryEn = "Fully Compensated Respiratory Acidosis"
                    primaryAr = "حموضة تنفسية معوضة بالكامل"
                    compEn = "Chronic CO2 retention with adequate renal compensation (common in BPD/CLD)"
                    compAr = "احتباس CO2 مزمن مع تعويض كلوي كافٍ (شائع في الخلل الرئوي المزمن BPD)"
                    recsEn.add("Accept permissive hypercapnia if pH > 7.25-7.30 and infant is comfortable.")
                    recsAr.add("قبول فرط ثاني أكسيد الكربون المسموح به طالما الـ pH مستقر والطفل مرتاح.")
                } else if (pco2Alk && hco3Acid) {
                    primaryEn = "Fully Compensated Metabolic Acidosis"
                    primaryAr = "حموضة أيضية معوضة بالكامل"
                    compEn = "Respiratory hyperventilation normalized pH"
                    compAr = "فرط التنفس عادل حموضة الدم تماماً"
                } else {
                    primaryEn = "Normal Acid-Base Balance"
                    primaryAr = "توازن حمضي-قلوي طبيعي ومثالي"
                    compEn = "No significant compensation needed"
                    compAr = "لا يلزم تعويض"
                }
            }
        }

        val oxygenationEn: String
        val oxygenationAr: String
        when {
            po2 < 45.0 -> {
                oxygenationEn = "Severe Hypoxemia (pO2 < 45 mmHg)"
                oxygenationAr = "نقص أكسجين حاد في الدم (pO2 < 45 مم زئبق)"
                recsEn.add("Urgent: Increase FiO2, consider increasing PEEP/MAP to recruit alveoli.")
                recsAr.add("عاجل: زيادة تركيز الأكسجين FiO2، والنظر في رفع ضغط PEEP.")
            }
            po2 < 60.0 -> {
                oxygenationEn = "Mild to Moderate Hypoxemia (pO2 45-60 mmHg)"
                oxygenationAr = "نقص أكسجين طفيف إلى متوسط"
                recsEn.add("Titrate FiO2 to maintain SpO2 in target range (preterm: 90-95%).")
                recsAr.add("تعديل FiO2 للحفاظ على تشبع الأكسجين ضمن النطاق المستهدف (90-95% للخدج).")
            }
            po2 > 90.0 -> {
                oxygenationEn = "Hyperoxia (pO2 > 90 mmHg)"
                oxygenationAr = "فرط أكسجة في الدم (pO2 > 90 مم زئبق)"
                recsEn.add("Wean FiO2 promptly to prevent oxygen toxicity and Retinopathy of Prematurity (ROP).")
                recsAr.add("تخفيض نسبة FiO2 فوراً لتجنب سُمية الأكسجين واعتلال شبكية الخدج (ROP).")
            }
            else -> {
                oxygenationEn = "Adequate Oxygenation (pO2 60-80 mmHg)"
                oxygenationAr = "أكسجة ملائمة وطبيعية"
            }
        }

        val oi: Double? = if (po2 > 0 && map > 0) {
            val calcOi = (map * fio2) / po2
            (calcOi * 10.0).roundToInt() / 10.0
        } else null

        val pfRatio: Double? = if (fio2 > 0) {
            val calcPf = po2 / (fio2 / 100.0)
            (calcPf * 10.0).roundToInt() / 10.0
        } else null

        if (oi != null && oi >= 25.0) {
            recsEn.add("Critical Oxygenation Index (OI >= 25): Severe pulmonary hypertension (PPHN) or ARDS. Consider Inhaled Nitric Oxide (iNO) or HFOV.")
            recsAr.add("مؤشر أكسجة حرج (OI >= 25): ارتفاع ضغط شريان رئوي PPHN أو متلازمة ضائقة تنفسية حادة. النظر في أكسيد النيتريك iNO.")
        }

        val alertLevel = when {
            isAcidemia && ph < 7.20 -> AlertLevel.DANGER
            po2 < 45.0 || (oi != null && oi >= 25.0) -> AlertLevel.DANGER
            isAcidemia || isAlkalemia || po2 < 60.0 || po2 > 95.0 -> AlertLevel.WARNING
            else -> AlertLevel.SAFE
        }

        return ABGAnalysisResult(
            primaryDisorderEn = primaryEn,
            primaryDisorderAr = primaryAr,
            compensationEn = compEn,
            compensationAr = compAr,
            oxygenationEn = oxygenationEn,
            oxygenationAr = oxygenationAr,
            oxygenationIndex = oi,
            pfRatio = pfRatio,
            ventilatorRecommendationsEn = recsEn,
            ventilatorRecommendationsAr = recsAr,
            alertLevel = alertLevel
        )
    }

    /**
     * Holliday-Segar 100/50/20 formula and 4-2-1 hourly rule:
     * - First 10 kg: 100 mL/kg/day (4 mL/kg/hr)
     * - Second 10 kg (11-20 kg): 1000 mL + 50 mL/kg for each kg > 10 (40 mL/hr + 2 mL/kg/hr)
     * - Each kg > 20 kg: 1500 mL + 20 mL/kg for each kg > 20 (60 mL/hr + 1 mL/kg/hr)
     * Maximum adult/adolescent maintenance is usually capped at ~2500 mL/day.
     */
    fun calculateHollidaySegar(weightKg: Double): HollidaySegarResult {
        val w = max(0.4, weightKg)
        val dailyMl = when {
            w <= 10.0 -> w * 100.0
            w <= 20.0 -> 1000.0 + (w - 10.0) * 50.0
            else -> min(2500.0, 1500.0 + (w - 20.0) * 20.0)
        }
        val hourlyRate = (dailyMl / 24.0 * 10.0).roundToInt() / 10.0
        val shift8h = (dailyMl / 3.0 * 10.0).roundToInt() / 10.0

        val extra10 = (w - 10.0).coerceAtLeast(0.0)
        val extra20 = (w - 20.0).coerceAtLeast(0.0)

        val rule421DescEn = when {
            w <= 10.0 -> "4 mL/kg/hr (Weight: ${String.format("%.1f", w)} kg × 4)"
            w <= 20.0 -> "40 mL/hr + 2 mL/kg/hr for extra ${String.format("%.1f", extra10)} kg"
            else -> "60 mL/hr + 1 mL/kg/hr for extra ${String.format("%.1f", extra20)} kg"
        }
        val rule421DescAr = when {
            w <= 10.0 -> "قاعدة 4 مل/كجم/ساعة (الوزن ${String.format("%.1f", w)} كجم × 4)"
            w <= 20.0 -> "40 مل/ساعة + 2 مل/كجم/ساعة لكل كجم إضافي فوق 10 كجم"
            else -> "60 مل/ساعة + 1 مل/كجم/ساعة لكل كجم إضافي فوق 20 كجم"
        }

        return HollidaySegarResult(
            weightKg = (w * 10.0).roundToInt() / 10.0,
            dailyMaintenanceMl = (dailyMl * 10.0).roundToInt() / 10.0,
            hourlyMaintenanceRate = hourlyRate,
            shift8HourMl = shift8h,
            rule421DescEn = rule421DescEn,
            rule421DescAr = rule421DescAr
        )
    }

    /**
     * Comprehensive Pediatric Dehydration Deficit & Rehydration Schedule:
     * - Deficit Volume (mL) = Weight (kg) × Dehydration % × 10
     * - Resuscitation Bolus = 10-20 mL/kg Normal Saline 0.9% for shock/severe dehydration
     * - Net Remaining Deficit = Total Deficit - Resuscitation Bolus + Ongoing Losses
     * - 24-Hour Rehydration Plan:
     *   * First 8 hours: 50% of net deficit + 1/3 of daily maintenance
     *   * Next 16 hours: 50% of net deficit + 2/3 of daily maintenance
     */
    fun calculateDehydrationDeficit(
        weightKg: Double,
        dehydrationPercent: Double,
        bolusGivenMl: Double = 0.0,
        ongoingLossesMl: Double = 0.0
    ): DehydrationDeficitPlan {
        val w = max(0.4, weightKg)
        val pct = max(0.0, min(20.0, dehydrationPercent))

        val maintenance = calculateHollidaySegar(w).dailyMaintenanceMl
        val totalDeficit = w * pct * 10.0
        val resuscitationBolus = if (pct >= 10.0) w * 20.0 else if (pct >= 6.0) w * 10.0 else 0.0

        val effectiveBolus = max(bolusGivenMl, 0.0)
        val netDeficit = max(0.0, totalDeficit - effectiveBolus) + max(0.0, ongoingLossesMl)

        // First 8 hours: 50% of net deficit + 1/3 maintenance
        val first8hTotal = (0.5 * netDeficit) + (maintenance / 3.0)
        val first8hRate = (first8hTotal / 8.0 * 10.0).roundToInt() / 10.0

        // Next 16 hours: 50% of net deficit + 2/3 maintenance
        val next16hTotal = (0.5 * netDeficit) + (2.0 * maintenance / 3.0)
        val next16hRate = (next16hTotal / 16.0 * 10.0).roundToInt() / 10.0

        val total24h = effectiveBolus + netDeficit + maintenance

        val alertLevel = when {
            pct >= 10.0 -> AlertLevel.DANGER
            pct >= 6.0 -> AlertLevel.WARNING
            else -> AlertLevel.SAFE
        }

        val recsEn = mutableListOf<String>()
        val recsAr = mutableListOf<String>()

        if (pct >= 10.0) {
            recsEn.add("EMERGENCY SHOCK RESUSCITATION: Administer Normal Saline 0.9% 20 mL/kg (${(w * 20).toInt()} mL) IV rapid bolus over 20-30 min. Reassess perfusion.")
            recsAr.add("إنعاش فوري للصدمة: إعطاء محلول ملحي 0.9% بجرعة 20 مل/كجم (${(w * 20).toInt()} مل) وريدياً سريعاً على 20-30 دقيقة، ثم إعادة تقييم التروية.")
        }
        if (pct in 6.0..9.9) {
            recsEn.add("Moderate Dehydration: Give 50% of fluid deficit over first 8 hours (${first8hRate} mL/hr), then remaining 50% over next 16 hours (${next16hRate} mL/hr).")
            recsAr.add("جفاف متوسط: تعويض 50% من العجز في أول 8 ساعات بمعدل (${first8hRate} مل/ساعة)، ثم 50% المتبقية على الـ 16 ساعة التالية (${next16hRate} مل/ساعة).")
        }
        recsEn.add("Potassium Addition: Add KCl 20 mEq/L to IV fluids ONLY after confirming active urination (rule out anuria/acute renal failure).")
        recsAr.add("إضافة البوتاسيوم: تُضاف أمبولات KCl 20 mEq/L فقط بعد التأكد من نزول البول واستبعاد انحباس البول أو الفشل الكلوي الحاد.")

        return DehydrationDeficitPlan(
            weightKg = (w * 10.0).roundToInt() / 10.0,
            dehydrationPercent = pct,
            totalDeficitMl = (totalDeficit * 10.0).roundToInt() / 10.0,
            resuscitationBolusMl = (resuscitationBolus * 10.0).roundToInt() / 10.0,
            netRemainingDeficitMl = (netDeficit * 10.0).roundToInt() / 10.0,
            dailyMaintenanceMl = (maintenance * 10.0).roundToInt() / 10.0,
            total24HourFluidMl = (total24h * 10.0).roundToInt() / 10.0,
            first8HourTotalMl = (first8hTotal * 10.0).roundToInt() / 10.0,
            first8HourHourlyRate = first8hRate,
            next16HourTotalMl = (next16hTotal * 10.0).roundToInt() / 10.0,
            next16HourHourlyRate = next16hRate,
            ongoingLossesMl = ongoingLossesMl,
            alertLevel = alertLevel,
            recommendationsEn = recsEn,
            recommendationsAr = recsAr
        )
    }

    /**
     * WHO Child Growth Standards (0-5 years) & CDC Growth Charts (up to 18 years)
     * Calculates Weight-for-Age, Height/Length-for-Age, and BMI-for-Age percentiles and z-scores.
     */
    fun calculateGrowthPercentiles(
        ageMonths: Double,
        isMale: Boolean,
        weightKg: Double,
        heightCm: Double
    ): GrowthPercentileResult {
        val safeAge = max(0.0, min(ageMonths, 216.0)) // up to 18 years (216 months)
        val safeWeight = max(0.5, weightKg)
        val safeHeight = max(25.0, heightCm)

        val heightM = safeHeight / 100.0
        val bmi = (safeWeight / (heightM * heightM) * 10.0).roundToInt() / 10.0

        // Median Weight (kg) formula approximation aligned with WHO (0-60m) & CDC (60-216m)
        val medianWeight = if (isMale) {
            when {
                safeAge <= 12.0 -> 3.3 + (safeAge * 0.53)
                safeAge <= 60.0 -> 9.6 + ((safeAge - 12.0) * 0.18)
                else -> 18.3 + ((safeAge - 60.0) * 0.28)
            }
        } else {
            when {
                safeAge <= 12.0 -> 3.2 + (safeAge * 0.49)
                safeAge <= 60.0 -> 9.0 + ((safeAge - 12.0) * 0.17)
                else -> 17.5 + ((safeAge - 60.0) * 0.27)
            }
        }

        // Weight standard deviation (~12-16% coefficient of variation)
        val weightSd = medianWeight * 0.13
        val weightZ = ((safeWeight - medianWeight) / weightSd).coerceIn(-4.0, 4.0)
        val weightPct = (zScoreToPercentile(weightZ) * 10.0).roundToInt() / 10.0

        // Median Height (cm) formula approximation aligned with WHO & CDC
        val medianHeight = if (isMale) {
            when {
                safeAge <= 12.0 -> 50.0 + (safeAge * 2.1)
                safeAge <= 60.0 -> 75.0 + ((safeAge - 12.0) * 0.72)
                else -> 110.0 + ((safeAge - 60.0) * 0.42)
            }
        } else {
            when {
                safeAge <= 12.0 -> 49.0 + (safeAge * 2.05)
                safeAge <= 60.0 -> 74.0 + ((safeAge - 12.0) * 0.71)
                else -> 109.0 + ((safeAge - 60.0) * 0.41)
            }
        }

        val heightSd = medianHeight * 0.045
        val heightZ = ((safeHeight - medianHeight) / heightSd).coerceIn(-4.0, 4.0)
        val heightPct = (zScoreToPercentile(heightZ) * 10.0).roundToInt() / 10.0

        // Median BMI (kg/m^2)
        val medianBmi = if (safeAge <= 12.0) 16.8 - (safeAge * 0.05)
        else if (safeAge <= 60.0) 16.2 - ((safeAge - 12.0) * 0.02)
        else 15.3 + ((safeAge - 60.0) * 0.04)

        val bmiSd = medianBmi * 0.11
        val bmiZ = ((bmi - medianBmi) / bmiSd).coerceIn(-4.0, 4.0)
        val bmiPct = (zScoreToPercentile(bmiZ) * 10.0).roundToInt() / 10.0

        // Reference Points for visualization
        val wP3 = (medianWeight - (1.88 * weightSd) * 10.0).roundToInt() / 10.0
        val wP15 = (medianWeight - (1.04 * weightSd) * 10.0).roundToInt() / 10.0
        val wP50 = (medianWeight * 10.0).roundToInt() / 10.0
        val wP85 = (medianWeight + (1.04 * weightSd) * 10.0).roundToInt() / 10.0
        val wP97 = (medianWeight + (1.88 * weightSd) * 10.0).roundToInt() / 10.0

        val hP3 = (medianHeight - (1.88 * heightSd) * 10.0).roundToInt() / 10.0
        val hP15 = (medianHeight - (1.04 * heightSd) * 10.0).roundToInt() / 10.0
        val hP50 = (medianHeight * 10.0).roundToInt() / 10.0
        val hP85 = (medianHeight + (1.04 * heightSd) * 10.0).roundToInt() / 10.0
        val hP97 = (medianHeight + (1.88 * heightSd) * 10.0).roundToInt() / 10.0

        // Categorizations
        val wCatEn = when {
            weightPct < 3.0 -> "Severely Underweight (<3rd %ile)"
            weightPct < 15.0 -> "Mildly Underweight (3rd - 15th %ile)"
            weightPct <= 85.0 -> "Healthy Weight (15th - 85th %ile)"
            weightPct <= 97.0 -> "Overweight (85th - 97th %ile)"
            else -> "Obese (>97th %ile)"
        }
        val wCatAr = when {
            weightPct < 3.0 -> "نقص وزن حاد (أقل من المئين 3)"
            weightPct < 15.0 -> "نقص وزن طفيف (بين المئين 3 و 15)"
            weightPct <= 85.0 -> "وزن طبيعي وصحي (المئين 15 إلى 85)"
            weightPct <= 97.0 -> "زيادة في الوزن (المئين 85 إلى 97)"
            else -> "سمنة مفرطة (أعلى من المئين 97)"
        }

        val hCatEn = when {
            heightPct < 3.0 -> "Severe Stunting (<3rd %ile)"
            heightPct < 15.0 -> "Short Stature (3rd - 15th %ile)"
            heightPct <= 85.0 -> "Normal Height (15th - 85th %ile)"
            else -> "Tall Stature (>85th %ile)"
        }
        val hCatAr = when {
            heightPct < 3.0 -> "قصر قامة حاد / تقزم (< المئين 3)"
            heightPct < 15.0 -> "قامة قصيرة (بين المئين 3 و 15)"
            heightPct <= 85.0 -> "طول طبيعي ومثالي (المئين 15 إلى 85)"
            else -> "قامة طويلة (> المئين 85)"
        }

        val bmiCatEn = when {
            bmiPct < 5.0 -> "Underweight (<5th %ile)"
            bmiPct < 85.0 -> "Normal BMI (5th - 85th %ile)"
            bmiPct < 95.0 -> "Overweight (85th - 95th %ile)"
            else -> "Obese (>=95th %ile)"
        }
        val bmiCatAr = when {
            bmiPct < 5.0 -> "نقص في كتلة الجسم (< المئين 5)"
            bmiPct < 85.0 -> "كتلة جسم صحية (المئين 5 إلى 85)"
            bmiPct < 95.0 -> "زيادة كتلة الجسم (المئين 85 إلى 95)"
            else -> "سمنة سريرية (>= المئين 95)"
        }

        val alertLevel = when {
            weightPct < 3.0 || heightPct < 3.0 || bmiPct >= 97.0 -> AlertLevel.DANGER
            weightPct < 15.0 || heightPct < 15.0 || bmiPct >= 85.0 -> AlertLevel.WARNING
            else -> AlertLevel.SAFE
        }

        val overallAr = when (alertLevel) {
            AlertLevel.SAFE -> "نمو سليم ومتناسق يقع ضمن المعدلات الطبيعية لمنظمة الصحة العالمية (WHO)."
            AlertLevel.WARNING -> "المؤشرات تظهر انحرافاً طفيفاً يستدعي متابعة السعرات الحرارية والتغذية المتوازنة."
            AlertLevel.DANGER -> "تنبيه سريري: القيم تقع خارج النطاق الآمن (< 3% أو > 97%). يُنصح بفحص سوء التغذية أو الفشل الكلوي/القلبي."
        }
        val overallEn = when (alertLevel) {
            AlertLevel.SAFE -> "Growth is healthy, tracking well within WHO standard percentile corridors."
            AlertLevel.WARNING -> "Borderline values detected. Review caloric intake and track growth velocity closely."
            AlertLevel.DANGER -> "Clinical Alert: Extreme percentile deviation (<3rd or >97th %ile). Assess for failure to thrive or endocrine evaluation."
        }

        return GrowthPercentileResult(
            ageMonths = (safeAge * 10.0).roundToInt() / 10.0,
            genderIsMale = isMale,
            weightKg = (safeWeight * 100.0).roundToInt() / 100.0,
            heightCm = (safeHeight * 10.0).roundToInt() / 10.0,
            bmi = bmi,
            weightPercentile = weightPct,
            heightPercentile = heightPct,
            bmiPercentile = bmiPct,
            weightZScore = (weightZ * 100.0).roundToInt() / 100.0,
            heightZScore = (heightZ * 100.0).roundToInt() / 100.0,
            bmiZScore = (bmiZ * 100.0).roundToInt() / 100.0,
            weightCategoryEn = wCatEn,
            weightCategoryAr = wCatAr,
            heightCategoryEn = hCatEn,
            heightCategoryAr = hCatAr,
            bmiCategoryEn = bmiCatEn,
            bmiCategoryAr = bmiCatAr,
            overallAssessmentEn = overallEn,
            overallAssessmentAr = overallAr,
            alertLevel = alertLevel,
            weightP3 = wP3,
            weightP15 = wP15,
            weightP50 = wP50,
            weightP85 = wP85,
            weightP97 = wP97,
            heightP3 = hP3,
            heightP15 = hP15,
            heightP50 = hP50,
            heightP85 = hP85,
            heightP97 = hP97
        )
    }

    /**
     * Converts a z-score to standard cumulative normal percentile (0 - 100%)
     * using the Abramowitz and Stegun error function approximation.
     */
    private fun zScoreToPercentile(z: Double): Double {
        val cdf = 0.5 * (1.0 + erf(z / Math.sqrt(2.0)))
        return (cdf * 100.0).coerceIn(0.1, 99.9)
    }

    private fun erf(x: Double): Double {
        val sign = if (x < 0) -1.0 else 1.0
        val absX = Math.abs(x)
        val p = 0.3275911
        val t = 1.0 / (1.0 + p * absX)
        val y = 1.0 - (((((1.061405429 * t - 1.453152027) * t) + 1.421413741) * t - 0.284496736) * t + 0.254829592) * t * Math.exp(-absX * absX)
        return sign * y
    }
}
