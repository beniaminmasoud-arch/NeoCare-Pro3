package com.example.data.repository

import com.example.data.model.DrugAgeBand
import com.example.data.model.DrugCategory
import com.example.data.model.DrugInfo
import com.example.data.model.PediatricFormulation
import com.example.data.model.ReconstitutionGuide

object DrugCatalog {
    val drugs: List<DrugInfo> = listOf(
        // === 1. ANTIPYRETICS & ANALGESICS (0 to 18 Years) ===
        DrugInfo(
            id = "paracetamol",
            nameEn = "Paracetamol (Acetaminophen)",
            nameAr = "باراسيتامول (أدول / بانادول / تمبرا)",
            category = DrugCategory.ANTIPYRETIC_ANALGESIC,
            ageRangeEn = "Birth to 18 Years",
            ageRangeAr = "من حديثي الولادة حتى 18 سنة",
            doseDescriptionEn = "10 - 15 mg/kg/dose PO / IV / PR every 4 - 6 hours",
            doseDescriptionAr = "10 - 15 مجم/كجم للجرعة شراب أو وريدي أو لبوس كل 4 إلى 6 ساعات",
            dosePerKgMin = 10.0,
            dosePerKgMax = 15.0,
            doseUnit = "mg/kg",
            stockConcentration = 24.0, // e.g. syrup 120mg/5mL = 24 mg/mL
            stockUnit = "mg/mL",
            defaultRoute = "PO / IV / PR",
            frequencyEn = "q4-6h PRN (Max 4 doses/day, max 1000 mg/dose, max 4000 mg/day)",
            frequencyAr = "كل 4-6 ساعات عند اللزوم (أقصى حد 4 جرعات/يوم، أقصى جرعة واحدة 1000 مجم)",
            maxSingleDose = 1000.0,
            maxDailyDose = 4000.0,
            administrationGuideEn = "PO: Give with or without food. Use calibrated syringe. IV: Infuse over 15 min. PR: Insert suppository gently. Space doses by at least 4 hours.",
            administrationGuideAr = "طريقة الأخذ: يؤخذ بالفم مع أو بدون طعام باستخدام سرنجة القياس؛ أو وريدياً بالتنقيط البطيء على 15 دقيقة؛ أو لبوس شرجي. لا تتجاوز 4 جرعات في 24 ساعة.",
            cautionsEn = "Hepatotoxic in overdose. In extreme preterms (<32w), extend interval to q8-12h. Also used for PDA closure (15 mg/kg q6h x3-6d).",
            cautionsAr = "تجنب تجاوز الجرعة القصوى لحماية الكبد. في الخدج <32 أسبوع تُباعد الجرعات كل 8-12 ساعة. يُستخدم أيضاً لغلق القناة الشريانية PDA.",
            ageBands = listOf(
                DrugAgeBand(
                    ageGroupEn = "Neonates (0-28d)",
                    ageGroupAr = "حديثي الولادة (0-28 يوم)",
                    doseDescriptionEn = "10 - 15 mg/kg/dose q8-12h",
                    doseDescriptionAr = "10 - 15 مجم/كجم كل 8-12 ساعة",
                    dosePerKgMin = 10.0,
                    dosePerKgMax = 15.0,
                    frequencyEn = "q8-12h",
                    frequencyAr = "كل 8-12 ساعة"
                ),
                DrugAgeBand(
                    ageGroupEn = "Infants & Children (1m - 12y)",
                    ageGroupAr = "الرضع والأطفال (شهر - 12 سنة)",
                    doseDescriptionEn = "15 mg/kg/dose q4-6h (Max 1000 mg)",
                    doseDescriptionAr = "15 مجم/كجم كل 4-6 ساعات (أقصى جرعة 1000 مجم)",
                    dosePerKgMin = 15.0,
                    dosePerKgMax = 15.0,
                    frequencyEn = "q4-6h",
                    frequencyAr = "كل 4-6 ساعات",
                    maxDoseMg = 1000.0
                ),
                DrugAgeBand(
                    ageGroupEn = "Adolescents (12 - 18y)",
                    ageGroupAr = "المراهقون (12 - 18 سنة)",
                    doseDescriptionEn = "500 - 1000 mg q4-6h (Max 4000 mg/day)",
                    doseDescriptionAr = "500 - 1000 مجم كل 4-6 ساعات (أقصى حد 4000 مجم/يوم)",
                    dosePerKgMin = 15.0,
                    dosePerKgMax = 15.0,
                    frequencyEn = "q4-6h",
                    frequencyAr = "كل 4-6 ساعات",
                    maxDoseMg = 1000.0
                )
            ),
            formulations = listOf(
                PediatricFormulation("Syrup 120 mg / 5 mL", "شراب 120 مجم / 5 مل", 24.0, "PO"),
                PediatricFormulation("Syrup 250 mg / 5 mL (Forte)", "شراب فورت 250 مجم / 5 مل", 50.0, "PO"),
                PediatricFormulation("IV Infusion 10 mg / mL (Perfalgan)", "محلول وريدي 10 مجم / مل", 10.0, "IV"),
                PediatricFormulation("Infant Drops 100 mg / mL", "نقط للرضع 100 مجم / مل", 100.0, "PO")
            )
        ),

        DrugInfo(
            id = "ibuprofen",
            nameEn = "Ibuprofen",
            nameAr = "إيبوبروفين (بروفين / كيتوفان)",
            category = DrugCategory.ANTIPYRETIC_ANALGESIC,
            ageRangeEn = "3 Months to 18 Years (Neonate only for PDA)",
            ageRangeAr = "من 3 أشهر حتى 18 سنة (ولحديثي الولادة لغلق PDA فقط)",
            doseDescriptionEn = "5 - 10 mg/kg/dose PO every 6 - 8 hours (Max 400 mg/dose)",
            doseDescriptionAr = "5 - 10 مجم/كجم للجرعة شراب كل 6 إلى 8 ساعات (أقصى جرعة 400 مجم)",
            dosePerKgMin = 5.0,
            dosePerKgMax = 10.0,
            doseUnit = "mg/kg",
            stockConcentration = 20.0, // syrup 100mg/5mL = 20 mg/mL
            stockUnit = "mg/mL",
            defaultRoute = "PO",
            frequencyEn = "q6-8h with meals (Max 40 mg/kg/day, max single 400 mg)",
            frequencyAr = "كل 6-8 ساعات بعد الأكل (أقصى جرعة يومية 40 مجم/كجم، أقصى جرعة واحدة 400 مجم)",
            maxSingleDose = 400.0,
            maxDailyDose = 2400.0,
            administrationGuideEn = "PO: Must be taken with food, milk, or a full glass of water to reduce gastric irritation. Do not use in infants < 3 months or < 5 kg for fever.",
            administrationGuideAr = "طريقة الأخذ: يؤخذ عن طريق الفم دائماً بعد الطعام أو مع الحليب لتجنب تهيج المعدة. يُرج الشراب جيداً. ممنوع لمن هم أقل من 3 أشهر أو أقل من 5 كجم لخفض الحرارة.",
            cautionsEn = "Contraindicated in active GI bleeding, severe renal impairment, and dehydration. Safe for PDA in preterms (10 mg/kg then 5 mg/kg q24h x2).",
            cautionsAr = "ممنوع في حالات الجفاف الشديد وقرحة المعدة والقصور الكلوي والجدري المائي.",
            formulations = listOf(
                PediatricFormulation("Syrup 100 mg / 5 mL", "شراب أطفال 100 مجم / 5 مل", 20.0, "PO"),
                PediatricFormulation("Syrup 200 mg / 5 mL (Forte)", "شراب فورت 200 مجم / 5 مل", 40.0, "PO"),
                PediatricFormulation("Tablets 200 mg / 400 mg", "أقراص 200 أو 400 مجم", 200.0, "PO")
            )
        ),

        // === 2. ANTIBIOTICS & ANTI-INFECTIVES (0 to 18 Years) ===
        DrugInfo(
            id = "ampicillin",
            nameEn = "Ampicillin",
            nameAr = "أمبيسيلين (خط أول لتسمم الدم في الحضانة)",
            category = DrugCategory.ANTIBIOTIC,
            ageRangeEn = "Birth to 18 Years",
            ageRangeAr = "من حديثي الولادة حتى 18 سنة",
            doseDescriptionEn = "Neonates: 50 - 100 mg/kg/dose IV (Meningitis: 100-200 mg/kg/day). Children: 100-200 mg/kg/day",
            doseDescriptionAr = "حديثي الولادة: 50 - 100 مجم/كجم للجرعة وريدياً (السحايا: 100-200 مجم/كجم/يوم). الأطفال: 100-200 مجم/كجم/يوم",
            dosePerKgMin = 50.0,
            dosePerKgMax = 100.0,
            doseUnit = "mg/kg",
            stockConcentration = 100.0, // 500mg in 5 mL = 100 mg/mL
            stockUnit = "mg/mL",
            defaultRoute = "IV / IM",
            frequencyEn = "q12h (<=7d old), q8h (>7d old), q6h for severe sepsis/meningitis (Max 2000 mg/dose, 12000 mg/day)",
            frequencyAr = "كل 12 ساعة (عمر <=7 أيام)، كل 8 ساعات (>7 أيام)، أو كل 6 ساعات في السحايا",
            maxSingleDose = 2000.0,
            maxDailyDose = 12000.0,
            administrationGuideEn = "IV: Push slowly over 3 - 5 minutes, or infuse over 15-30 min. Reconstitute with Sterile Water or Normal Saline immediately before use (unstable in solution).",
            administrationGuideAr = "طريقة الأخذ: حقن وريدي بطيء على 3-5 دقائق أو تنقيط وريدي على 15-30 دقيقة. يُحل بماء معقم أو محلول ملح ويُعطى فوراً لعدم استقراره بالمحلول بعد الحل.",
            cautionsEn = "First-line empirical coverage for Listeria and Group B Strep (GBS) when combined with Gentamicin. Adjust in renal impairment.",
            cautionsAr = "العلاج الأساسي الأول لتسمم الدم والميكروب السبحي والليستيريا مع الجنتامايسين في الحضانة.",
            reconstitution = ReconstitutionGuide(
                isPowder = true,
                diluentEn = "Sterile Water for Injection, or 0.9% Normal Saline",
                diluentAr = "ماء معقم للحقن، أو محلول ملحي 0.9% (Normal Saline)",
                powderVolumeStepsEn = "• Vial 500 mg: Add 4.8 mL sterile water to yield 5 mL at 100 mg/mL.\n• Vial 1 g (1000 mg): Add 9.6 mL sterile water to yield 10 mL at 100 mg/mL.\n• For IM injection: Add 1.8 mL sterile water or 1% lidocaine to 500 mg vial (yields 250 mg/mL).",
                powderVolumeStepsAr = "• فيال 500 مجم بودرة: يُضاف 4.8 مل ماء معقم لينتج 5 مل بتركيز 100 مجم/مل.\n• فيال 1 جم (1000 مجم): يُضاف 9.6 مل ماء معقم لينتج 10 مل بتركيز 100 مجم/مل.\n• للحقن العضلي (IM): يُحل فيال 500 مجم بـ 1.8 مل ماء معقم أو ليدوكايين 1% بدون أدرينالين (تركيز 250 مجم/مل لتسكين ألم العضلة).",
                administrationMethodEn = "IV Push: Push slowly over 3 to 5 minutes (rate <= 100 mg/min). Intermittent IV Infusion: Dilute dose in 25 - 50 mL NS and infuse over 15 - 30 minutes. IM: Deep into anterolateral thigh.",
                administrationMethodAr = "وريدي مباشر (IV Push): حقن بطيء جداً على 3 إلى 5 دقائق. تسريب وريدي متقطع: تُخفف الجرعة في 25 - 50 مل محلول ملح وتُسرب على 15 - 30 دقيقة. حقن عضلي: عميق في الجزء الأمامي الخارجي للفخذ.",
                stabilityEn = "Administer within 1 hour after reconstitution at room temperature (or 4 hours refrigerated at 2-8°C). Rapidly loses potency in high dextrose solutions.",
                stabilityAr = "يجب إعطاؤه خلال ساعة واحدة فقط من الحل في حرارة الغرفة (أو 4 ساعات بالثلاجة). يفقد فاعليته بسرعة في محاليل الجلوكوز."
            ),
            ageBands = listOf(
                DrugAgeBand(
                    ageGroupEn = "Neonates <= 7 days",
                    ageGroupAr = "الولدان <= 7 أيام",
                    doseDescriptionEn = "50 - 100 mg/kg/dose IV q12h",
                    doseDescriptionAr = "50 - 100 مجم/كجم وريدي كل 12 ساعة",
                    dosePerKgMin = 50.0,
                    dosePerKgMax = 100.0,
                    frequencyEn = "q12h",
                    frequencyAr = "كل 12 ساعة"
                ),
                DrugAgeBand(
                    ageGroupEn = "Neonates > 7 days & Infants",
                    ageGroupAr = "الولدان > 7 أيام والرضع",
                    doseDescriptionEn = "50 - 100 mg/kg/dose IV q8h (Meningitis q6h)",
                    doseDescriptionAr = "50 - 100 مجم/كجم وريدي كل 8 ساعات (السحايا كل 6 ساعات)",
                    dosePerKgMin = 50.0,
                    dosePerKgMax = 100.0,
                    frequencyEn = "q8h",
                    frequencyAr = "كل 8 ساعات"
                ),
                DrugAgeBand(
                    ageGroupEn = "Children & Adolescents (1 - 18y)",
                    ageGroupAr = "الأطفال والمراهقون (1 - 18 سنة)",
                    doseDescriptionEn = "100 - 200 mg/kg/day divided q6h IV (Max 12 g/day)",
                    doseDescriptionAr = "100 - 200 مجم/كجم/يوم مقسمة كل 6 ساعات (أقصى حد 12 جم/يوم)",
                    dosePerKgMin = 25.0,
                    dosePerKgMax = 50.0,
                    frequencyEn = "q6h",
                    frequencyAr = "كل 6 ساعات",
                    maxDoseMg = 2000.0
                )
            ),
            formulations = listOf(
                PediatricFormulation("Vial 500 mg IV / IM (reconstituted to 100 mg/mL)", "فيال 500 مجم وريدي (تركيز 100 مجم/مل)", 100.0, "IV"),
                PediatricFormulation("Vial 1000 mg IV / IM (reconstituted to 100 mg/mL)", "فيال 1000 مجم وريدي (تركيز 100 مجم/مل)", 100.0, "IV")
            )
        ),

        DrugInfo(
            id = "amoxicillin",
            nameEn = "Amoxicillin",
            nameAr = "أموكسيسيلين (إيموكس / أموكسيل)",
            category = DrugCategory.ANTIBIOTIC,
            ageRangeEn = "Birth to 18 Years",
            ageRangeAr = "من حديثي الولادة حتى 18 سنة",
            doseDescriptionEn = "Standard: 40 - 50 mg/kg/day. High-dose (Otitis/Pneumonia): 80 - 90 mg/kg/day",
            doseDescriptionAr = "عادي: 40 - 50 مجم/كجم/يوم. جرعة عالية (التهاب الأذن والالتهاب الرئوي): 80 - 90 مجم/كجم/يوم",
            dosePerKgMin = 40.0,
            dosePerKgMax = 90.0,
            doseUnit = "mg/kg/day",
            stockConcentration = 50.0, // 250mg/5mL = 50 mg/mL
            stockUnit = "mg/mL",
            defaultRoute = "PO",
            frequencyEn = "Divided q8h or q12h (Max 1000 mg/dose, 2000-3000 mg/day)",
            frequencyAr = "مقسمة على جرعتين (كل 12 ساعة) أو 3 جرعات (كل 8 ساعات)، أقصى جرعة واحدة 1000 مجم",
            maxSingleDose = 1000.0,
            maxDailyDose = 3000.0,
            administrationGuideEn = "PO: Shake suspension well before each dose. Can be given with or without meals. Reconstituted suspension must be refrigerated and discarded after 14 days.",
            administrationGuideAr = "طريقة الأخذ: يؤخذ بالفم مع أو بدون طعام. يُرج الشراب جيداً قبل كل جرعة. يُحفظ المعلق في الثلاجة بعد التحضير ويُتلف بعد 14 يوماً.",
            cautionsEn = "Check for penicillin allergy. Complete full 7-10 day course.",
            cautionsAr = "التأكد من عدم وجود حساسية البنسلين. إكمال الكورس العلاجي بالكامل (7-10 أيام).",
            reconstitution = ReconstitutionGuide(
                isPowder = true,
                diluentEn = "Boiled and cooled water (or purified water) for dry powder oral suspension",
                diluentAr = "ماء مغلي ومبرد (أو ماء مقطر نقي) لتحضير المعلق الفموي البودرة الجافة",
                powderVolumeStepsEn = "• Tap the bottle first to loosen the dry powder.\n• Add water up to about half the bottle line mark, recap and shake vigorously.\n• Add water carefully exactly up to the indicated ring/arrow mark on the bottle label.\n• Invert and shake well; allow foam to subside for 2 minutes to confirm the liquid is level with the mark.",
                powderVolumeStepsAr = "• رَجّ الزجاجة أولاً وهي جافة لفك حبيبات المسحوق العالقة بالقاع.\n• إضافة ماء مغلي ومبرد حتى نصف علامة الزجاجة، ثم إغلاق الغطاء والرجّ بقوة.\n• إكمال إضافة الماء بحذر وتأنٍّ حتى خط العلامة أو السهم المطبوع على الزجاجة بالضبط.\n• قَلْب الزجاجة ورجّها جيداً، ثم تركها دقيقتين لتهدأ الرغوة والتأكد من ثبات مستوى المحلول عند الخط.",
                administrationMethodEn = "PO: Administer with calibrated measuring syringe (not household spoons). Give with or between feeds.",
                administrationMethodAr = "بالفم (PO): يُعطى حصراً بسرنجة قياس فموية مدرجة. يؤخذ مع الرضعة أو بين الرضعات.",
                stabilityEn = "Reconstituted oral suspension MUST be stored in refrigerator (2-8°C). Discard any unused portion after 14 days.",
                stabilityAr = "المعلق المحلول يجب حفظه في الثلاجة (2-8 درجات مئوية). يُتلف المتبقي تماماً بعد 14 يوماً من يوم الحل."
            ),
            ageBands = listOf(
                DrugAgeBand(
                    ageGroupEn = "Neonates (0 - 28d)",
                    ageGroupAr = "حديثي الولادة (0 - 28 يوم)",
                    doseDescriptionEn = "20 - 30 mg/kg/day divided q12h",
                    doseDescriptionAr = "20 - 30 مجم/كجم/يوم مقسمة كل 12 ساعة",
                    dosePerKgMin = 20.0,
                    dosePerKgMax = 30.0,
                    frequencyEn = "q12h",
                    frequencyAr = "كل 12 ساعة"
                ),
                DrugAgeBand(
                    ageGroupEn = "Infants & Children (1m - 12y)",
                    ageGroupAr = "الرضع والأطفال (شهر - 12 سنة)",
                    doseDescriptionEn = "40 - 90 mg/kg/day divided q8-12h (Max 1000 mg/dose)",
                    doseDescriptionAr = "40 - 90 مجم/كجم/يوم مقسمة كل 8-12 ساعة (أقصى جرعة 1000 مجم)",
                    dosePerKgMin = 40.0,
                    dosePerKgMax = 90.0,
                    frequencyEn = "q8-12h",
                    frequencyAr = "كل 8-12 ساعة",
                    maxDoseMg = 1000.0
                ),
                DrugAgeBand(
                    ageGroupEn = "Adolescents (12 - 18y)",
                    ageGroupAr = "المراهقون (12 - 18 سنة)",
                    doseDescriptionEn = "500 - 1000 mg PO q8h or 875 mg q12h",
                    doseDescriptionAr = "500 - 1000 مجم بالفم كل 8 ساعات أو 875 مجم كل 12 ساعة",
                    dosePerKgMin = 20.0,
                    dosePerKgMax = 30.0,
                    frequencyEn = "q8-12h",
                    frequencyAr = "كل 8-12 ساعة",
                    maxDoseMg = 1000.0
                )
            ),
            formulations = listOf(
                PediatricFormulation("Suspension 125 mg / 5 mL", "معلق 125 مجم / 5 مل", 25.0, "PO"),
                PediatricFormulation("Suspension 250 mg / 5 mL", "معلق 250 مجم / 5 مل", 50.0, "PO"),
                PediatricFormulation("Suspension 500 mg / 5 mL", "معلق 500 مجم / 5 مل", 100.0, "PO")
            )
        ),

        DrugInfo(
            id = "augmentin",
            nameEn = "Amoxicillin / Clavulanate (Augmentin)",
            nameAr = "أوجمنتين (أموكسيسيلين + كلافولانات)",
            category = DrugCategory.ANTIBIOTIC,
            ageRangeEn = "Birth to 18 Years",
            ageRangeAr = "من حديثي الولادة حتى 18 سنة",
            doseDescriptionEn = "40 - 90 mg/kg/day (amoxicillin component) divided q12h",
            doseDescriptionAr = "40 - 90 مجم/كجم/يوم (محسوبة على الأموكسيسيلين) مقسمة كل 12 ساعة",
            dosePerKgMin = 45.0,
            dosePerKgMax = 90.0,
            doseUnit = "mg/kg/day",
            stockConcentration = 62.4, // 312mg/5mL = 62.4 mg/mL
            stockUnit = "mg/mL",
            defaultRoute = "PO / IV",
            frequencyEn = "Divided q12h (BID formulation 7:1) or q8h (4:1) with meals (Max 1000 mg/dose)",
            frequencyAr = "كل 12 ساعة (تركيبة BID) أو كل 8 ساعات، مع الطعام (أقصى جرعة واحدة 1000 مجم)",
            maxSingleDose = 1000.0,
            maxDailyDose = 3000.0,
            administrationGuideEn = "PO: Take at the start of a meal to enhance absorption and minimize gastrointestinal upset/diarrhea. Keep refrigerated.",
            administrationGuideAr = "طريقة الأخذ: يؤخذ بالفم في بداية الوجبة لزيادة الامتصاص وتفادي الإسهال واضطراب المعدة. يُحفظ المعلق في الثلاجة بعد الحل.",
            cautionsEn = "May cause antibiotic-associated diarrhea. Contraindicated if history of amoxicillin-clavulanate jaundice.",
            cautionsAr = "قد يسبب إسهالاً مصاحباً للمضادات الحيوية. ممنوع في حال وجود حساسية بنسلين أو يرقان كبدي سابق.",
            reconstitution = ReconstitutionGuide(
                isPowder = true,
                diluentEn = "Water for Injections (for IV vials); Boiled and cooled water for oral suspension",
                diluentAr = "ماء معقم للحقن (للفيالات الوريدية)؛ ماء مغلي ومبرد للشراب المعلق الفموي",
                powderVolumeStepsEn = "• IV Vial 600 mg: Dissolve in 10 mL Water for Injections (yields 60 mg/mL).\n• IV Vial 1.2 g: Dissolve in 20 mL Water for Injections (yields 60 mg/mL).\n• Oral Dry Powder: Shake bottle to loosen powder. Add water in 2 equal portions up to the label arrow mark; shake vigorously between and after. Allow foam to settle.",
                powderVolumeStepsAr = "• فيال وريدي 600 مجم بودرة: يُحل في 10 مل ماء معقم للحقن (تركيز 60 مجم/مل).\n• فيال وريدي 1.2 جم بودرة: يُحل في 20 مل ماء معقم للحقن (تركيز 60 مجم/مل).\n• المعلق الفموي البودرة: تُرج الزجاجة وهي جافة، ثم يُضاف الماء على دفعتين حتى علامة السهم مع الرج العنيف بعد كل إضافة. يُترك دقيقتين لتهدأ الرغوة والتأكد من مستوى السائل.",
                administrationMethodEn = "IV Push: Inject slowly over 3 - 4 minutes. IV Infusion: Further dilute in 50 mL Normal Saline 0.9% and infuse over 30 minutes. PO: Administer at start of a meal to reduce GI adverse effects.",
                administrationMethodAr = "وريدي مباشر (IV Push): حقن بطيء جداً على 3 - 4 دقائق. تسريب وريدي: يُخفف في 50 مل محلول ملح 0.9% ويُعطى على 30 دقيقة. فموياً (PO): يُعطى مع اللقمة الأولى من الوجبة لتفادي المغص والإسهال.",
                stabilityEn = "IV Solution is UNSTABLE: Administer within 15 - 20 minutes of reconstitution. Do not freeze. Oral suspension is stable for 7 days in refrigerator (2-8°C).",
                stabilityAr = "تحذير: المحلول الوريدي غير مستقر سريع التحلل: يجب إعطاؤه خلال 15 - 20 دقيقة كحد أقصى من الحل. الشراب المعلق صالح 7 أيام فقط في الثلاجة."
            ),
            formulations = listOf(
                PediatricFormulation("Suspension 228 mg / 5 mL (BID)", "معلق 228 مجم / 5 مل (كل 12 ساعة)", 45.6, "PO"),
                PediatricFormulation("Suspension 312 mg / 5 mL", "معلق 312 مجم / 5 مل (كل 8 ساعات)", 62.4, "PO"),
                PediatricFormulation("Suspension 457 mg / 5 mL (BID)", "معلق 457 مجم / 5 مل (كل 12 ساعة)", 91.4, "PO"),
                PediatricFormulation("IV Vial 600 mg / 1.2 g", "حقن وريدية 600 مجم أو 1.2 جم", 100.0, "IV")
            )
        ),

        DrugInfo(
            id = "ceftriaxone",
            nameEn = "Ceftriaxone (Rocephin)",
            nameAr = "سيفترياكسون (روسيفين)",
            category = DrugCategory.ANTIBIOTIC,
            ageRangeEn = "1 Month to 18 Years (CONTRAINDICATED in Neonates)",
            ageRangeAr = "من شهر حتى 18 سنة (ممنوع في حديثي الولادة)",
            doseDescriptionEn = "50 - 75 mg/kg/day IV/IM once daily (Meningitis: 100 mg/kg/day, max 4 g/day)",
            doseDescriptionAr = "50 - 75 مجم/كجم/يوم وريدي/عضلي مرة يومياً (السحايا: 100 مجم/كجم، أقصى حد 4 جم/يوم)",
            dosePerKgMin = 50.0,
            dosePerKgMax = 100.0,
            doseUnit = "mg/kg/day",
            stockConcentration = 100.0, // 1g reconstituted in 10 mL = 100 mg/mL
            stockUnit = "mg/mL",
            defaultRoute = "IV / IM",
            frequencyEn = "Once daily (q24h) or divided q12h for meningitis (Max 2000-4000 mg/day)",
            frequencyAr = "مرة واحدة يومياً (كل 24 ساعة)، أو مقسم كل 12 ساعة في السحايا (أقصى حد 2-4 جم/يوم)",
            maxSingleDose = 2000.0,
            maxDailyDose = 4000.0,
            administrationGuideEn = "IV: Infuse over 30 minutes. NEVER administer simultaneously with calcium-containing IV solutions (Ringer's/TPN) - risk of fatal precipitation. IM: Reconstitute with 1% lidocaine for pain.",
            administrationGuideAr = "طريقة الأخذ: حقن وريدي بالتنقيط البطيء على 30 دقيقة. تحذير خطير: يُمنع إعطاؤه مطلقاً مع محاليل تحتوي على كالسيوم (مثل رينجر أو محاليل التغذية) لتفادي الترسب القاتل في الرئة والكلى. للعضل: يُخلط بليدوكايين 1% لتخفيف الألم.",
            cautionsEn = "CONTRAINDICATED in neonates <= 28 days (causes biliary sludging and displaces bilirubin causing kernicterus). Use Cefotaxime instead in neonates.",
            cautionsAr = "ممنوع تماماً في حديثي الولادة <= 28 يوماً لأنه يزيح الصفراء عن الألبومين مسبباً اليرقان النووي، ويستبدل بالسيفوتاكسيم للولدان.",
            reconstitution = ReconstitutionGuide(
                isPowder = true,
                diluentEn = "Sterile Water for Injection (IV); 1% Lidocaine WITHOUT epinephrine (IM only)",
                diluentAr = "ماء معقم للحقن (للوريد فقط)؛ ليدوكايين 1% بدون أدرينالين (للحقن العضلي فقط)",
                powderVolumeStepsEn = "• IV 500 mg vial: Add 4.8 mL sterile water -> yields 5 mL at 100 mg/mL.\n• IV 1 g vial: Add 9.6 mL sterile water -> yields 10 mL at 100 mg/mL.\n• IM 500 mg vial: Add 1.8 mL 1% lidocaine -> yields 250 mg/mL.\n• IM 1 g vial: Add 3.5 mL 1% lidocaine -> yields 250 mg/mL.",
                powderVolumeStepsAr = "• فيال 500 مجم للوريد: يُضاف 4.8 مل ماء معقم -> الناتج 5 مل بتركيز 100 مجم/مل.\n• فيال 1 جم للوريد: يُضاف 9.6 مل ماء معقم -> الناتج 10 مل بتركيز 100 مجم/مل.\n• فيال 500 مجم للعضل: يُضاف 1.8 مل ليدوكايين 1% -> تركيز 250 مجم/مل لتخفيف الألم.\n• فيال 1 جم للعضل: يُضاف 3.5 مل ليدوكايين 1% -> تركيز 250 مجم/مل.",
                administrationMethodEn = "IV Infusion: Dilute reconstituted dose in 50 - 100 mL NS or D5W and infuse over 30 minutes. IM: Deep into anterior-lateral thigh. NEVER give IV push. NEVER mix with Calcium/Ringer's.",
                administrationMethodAr = "تسريب وريدي: يُخفف في 50 - 100 مل محلول ملح أو دكستروز 5% ويُسرب على 30 دقيقة. عضلي: حقن عميق في الفخذ. تحذير: ممنوع حقنه وريدياً سريعاً. ممنوع خلطه بأي محاليل كالسيوم.",
                stabilityEn = "FATAL WARNING: Incompatible with ANY calcium-containing fluids (Ringer's Lactate, Hartmann's, TPN) - risk of lethal precipitate. CONTRAINDICATED in neonates <= 28 days.",
                stabilityAr = "تحذير قاتل: يُمنع نهائياً خلطه أو حله أو إعطاؤه في نفس الوريد مع أي محاليل كالسيوم (كالرينجر أو التغذية الوريدية). ممنوع منعاً باتاً في حديثي الولادة <= 28 يوماً."
            ),
            formulations = listOf(
                PediatricFormulation("Vial 500 mg IV / IM", "فيال 500 مجم وريدي / عضلي", 100.0, "IV"),
                PediatricFormulation("Vial 1 g IV / IM", "فيال 1 جم وريدي / عضلي", 100.0, "IV")
            )
        ),

        DrugInfo(
            id = "cefotaxime",
            nameEn = "Cefotaxime (Claforan)",
            nameAr = "سيفوتاكسيم (كلافوران - آمن للولدان)",
            category = DrugCategory.ANTIBIOTIC,
            ageRangeEn = "Birth to 18 Years",
            ageRangeAr = "من حديثي الولادة حتى 18 سنة",
            doseDescriptionEn = "Neonates: 50 mg/kg/dose. Children: 100 - 200 mg/kg/day divided q6-8h",
            doseDescriptionAr = "حديثي الولادة: 50 مجم/كجم للجرعة. الأطفال: 100 - 200 مجم/كجم/يوم مقسمة كل 6-8 ساعات",
            dosePerKgMin = 50.0,
            dosePerKgMax = 200.0,
            doseUnit = "mg/kg",
            stockConcentration = 100.0,
            stockUnit = "mg/mL",
            defaultRoute = "IV / IM",
            frequencyEn = "Neonates: q12h (<=7d), q8h (>7d). Children: q6-8h (Max 12 g/day)",
            frequencyAr = "الولدان: كل 12 ساعة (عمر <=7 أيام)، كل 8 ساعات (>7 أيام). الأطفال: كل 6-8 ساعات",
            maxSingleDose = 2000.0,
            maxDailyDose = 12000.0,
            administrationGuideEn = "IV: Push slowly over 3 - 5 minutes, or infuse over 20-30 min. Safe in neonatal jaundice and does not precipitate with calcium.",
            administrationGuideAr = "طريقة الأخذ: حقن وريدي بطيء على 3-5 دقائق أو تنقيط وريدي على 20-30 دقيقة. آمن تماماً للولدان والمصابين بالصفراء.",
            cautionsEn = "Preferred 3rd-generation cephalosporin in neonates with suspected sepsis or meningitis.",
            cautionsAr = "المضاد الحيوي المفضل من الجيل الثالث في الحضانة للوقاية وعلاج تسمم الدم والسحايا.",
            reconstitution = ReconstitutionGuide(
                isPowder = true,
                diluentEn = "Sterile Water for Injection, 0.9% Normal Saline, or D5W",
                diluentAr = "ماء معقم للحقن، محلول ملحي 0.9%، أو دكستروز 5%",
                powderVolumeStepsEn = "• Vial 500 mg: Add 5 mL sterile water -> yields 5 mL at 100 mg/mL.\n• Vial 1 g (1000 mg): Add 9.6 mL sterile water -> yields 10 mL at 100 mg/mL.\n• IM: Add 2 mL sterile water/1% lidocaine to 500 mg, or 4 mL to 1 g vial.",
                powderVolumeStepsAr = "• فيال 500 مجم بودرة: يُضاف 5 مل ماء معقم -> الناتج 5 مل بتركيز 100 مجم/مل.\n• فيال 1 جم (1000 مجم) بودرة: يُضاف 9.6 مل ماء معقم -> الناتج 10 مل بتركيز 100 مجم/مل.\n• للحقن العضلي: يُحل فيال 500 مجم بـ 2 مل، وفيال 1 جم بـ 4 مل (ماء معقم أو ليدوكايين 1%).",
                administrationMethodEn = "IV Push: Inject slowly over 3 to 5 minutes. IV Infusion: Dilute in 50 mL NS/D5W and infuse over 20 to 30 minutes. IM: Deep into thigh muscle.",
                administrationMethodAr = "وريدي مباشر: حقن بطيء على 3 إلى 5 دقائق. تسريب وريدي: يُخفف في 50 مل محلول ملح أو دكستروز 5% ويُعطى على 20 - 30 دقيقة. حقن عضلي: عميق في الفخذ.",
                stabilityEn = "Stable for 24 hours at room temperature, 7 days refrigerated (2-8°C). Completely safe with calcium and in neonatal jaundice.",
                stabilityAr = "صالح لمدة 24 ساعة في حرارة الغرفة ولـ 7 أيام في الثلاجة (2-8 درجات مئوية). آمن تماماً مع الكالسيوم ومناسب لليرقان."
            ),
            formulations = listOf(
                PediatricFormulation("Vial 500 mg IV / IM", "فيال 500 مجم", 100.0, "IV"),
                PediatricFormulation("Vial 1 g IV / IM", "فيال 1 جم", 100.0, "IV")
            )
        ),

        DrugInfo(
            id = "vancomycin",
            nameEn = "Vancomycin",
            nameAr = "فانكومايسين (علاج بكتيريا MRSA المقاومة)",
            category = DrugCategory.ANTIBIOTIC,
            ageRangeEn = "Birth to 18 Years",
            ageRangeAr = "من حديثي الولادة حتى 18 سنة",
            doseDescriptionEn = "Neonates: 10 - 15 mg/kg/dose. Children: 15 mg/kg/dose IV q6-8h (Max 2 g/day)",
            doseDescriptionAr = "حديثي الولادة: 10 - 15 مجم/كجم للجرعة. الأطفال: 15 مجم/كجم كل 6-8 ساعات وريدياً",
            dosePerKgMin = 15.0,
            dosePerKgMax = 15.0,
            doseUnit = "mg/kg",
            stockConcentration = 50.0, // 500mg in 10 mL = 50 mg/mL initial reconstitution
            stockUnit = "mg/mL",
            defaultRoute = "IV Infusion ONLY",
            frequencyEn = "Neonates: q8-18h based on PMA and renal function. Children: q6-8h",
            frequencyAr = "الولدان: كل 8 إلى 18 ساعة حسب العمر الرحمي ووظائف الكلى. الأطفال: كل 6-8 ساعات",
            maxSingleDose = 1000.0,
            maxDailyDose = 2000.0,
            administrationGuideEn = "STRICTLY NO IV PUSH. Must be infused slowly over AT LEAST 60 minutes (rate <= 10 mg/min) to prevent Red Man Syndrome and cardiac arrest.",
            administrationGuideAr = "ممنوع منعاً باتاً الحقن الوريدي المباشر أو السريع. يُعطى حصراً بالتسريب الوريدي البطيء على مدار 60 دقيقة على الأقل عبر مضخة المحاليل.",
            cautionsEn = "Nephrotoxic and ototoxic. Therapeutic drug monitoring required (trough 10-15 mcg/mL, severe 15-20 mcg/mL).",
            cautionsAr = "مراقبة وظائف الكلى ومستوى قاع الدواء بالدم (Trough Level) تجنباً لسمية الكلى والسمع.",
            reconstitution = ReconstitutionGuide(
                isPowder = true,
                diluentEn = "Sterile Water for Injection, then MANDATORY secondary dilution in 0.9% Normal Saline or D5W",
                diluentAr = "ماء معقم للحقن، ثم تخفيف ثانوي إلزامي في محلول ملحي 0.9% أو دكستروز 5%",
                powderVolumeStepsEn = "• Vial 500 mg: Add 10 mL Sterile Water for Injection (yields 50 mg/mL).\n• Vial 1 g: Add 20 mL Sterile Water for Injection (yields 50 mg/mL).\n• MANDATORY SECONDARY DILUTION: Dilute drawn dose to concentration <= 5 mg/mL (e.g. 50 mg dose diluted in at least 10-20 mL NS).",
                powderVolumeStepsAr = "• فيال 500 مجم بودرة: يُضاف 10 مل ماء معقم للحقن (تركيز أولي 50 مجم/مل).\n• فيال 1 جم بودرة: يُضاف 20 مل ماء معقم للحقن (تركيز أولي 50 مجم/مل).\n• خطوة التخفيف الإلزامية: يجب تخفيف الجرعة المسحوبة في محلول ملحي 0.9% أو دكستروز ليصل التركيز إلى 5 مجم/مل أو أقل (مثال: جرعة 50 مجم تُخفف في 10-20 مل على الأقل).",
                administrationMethodEn = "Slow IV Infusion via pump over at least 60 minutes (90-120 min for doses > 1 g). Never give IM or IV push.",
                administrationMethodAr = "تسريب وريدي بطيء عبر مضخة المحاليل على مدار 60 دقيقة على الأقل (أو 90-120 دقيقة للجرعات الكبيرة). يُمنع العضل ويُمنع الوريدي المباشر.",
                stabilityEn = "Reconstituted vial stable 24h at room temperature, 14 days refrigerated. Infusion bag stable 48h.",
                stabilityAr = "الفيال المحلول صالح 24 ساعة بحرارة الغرفة و14 يوماً بالثلاجة. كيس التسريب صالح 48 ساعة."
            ),
            formulations = listOf(
                PediatricFormulation("Vial 500 mg Powder (reconstituted to 50 mg/mL)", "فيال 500 مجم بودرة", 50.0, "IV"),
                PediatricFormulation("Vial 1 g Powder (reconstituted to 50 mg/mL)", "فيال 1 جم بودرة", 50.0, "IV")
            )
        ),

        DrugInfo(
            id = "azithromycin",
            nameEn = "Azithromycin (Zithromax)",
            nameAr = "أزيثرومايسين (زيثروماكس)",
            category = DrugCategory.ANTIBIOTIC,
            ageRangeEn = "6 Months to 18 Years",
            ageRangeAr = "من 6 أشهر حتى 18 سنة",
            doseDescriptionEn = "10 mg/kg once daily on Day 1 (max 500 mg), then 5 mg/kg once daily on Days 2 - 5 (max 250 mg)",
            doseDescriptionAr = "10 مجم/كجم في اليوم الأول (أقصى حد 500 مجم)، ثم 5 مجم/كجم يومياً للأيام 2 إلى 5 (أقصى حد 250 مجم)",
            dosePerKgMin = 10.0,
            dosePerKgMax = 10.0,
            doseUnit = "mg/kg",
            stockConcentration = 40.0, // 200mg/5mL = 40 mg/mL
            stockUnit = "mg/mL",
            defaultRoute = "PO / IV",
            frequencyEn = "Once daily (q24h) for 3 to 5 days",
            frequencyAr = "مرة واحدة يومياً لمدة 3 إلى 5 أيام فقط",
            maxSingleDose = 500.0,
            administrationGuideEn = "PO: Give once daily at the same time. Can be given with food to reduce nausea, but absorption is highest 1h before or 2h after meals. Shake suspension well.",
            administrationGuideAr = "طريقة الأخذ: يؤخذ بالفم مرة واحدة يومياً في نفس الموعد. يُفضل على معدة خاوية قبل الأكل بساعة أو بعده بساعتين، أو مع وجبة خفيفة إذا سبب غثياناً.",
            cautionsEn = "Prolongs QT interval. Used for atypical pneumonia, pertussis, and streptococcal pharyngitis in penicillin-allergic patients.",
            cautionsAr = "مراقبة رسم القلب في مرضى اضطراب النبض. فعال في الالتهاب الرئوي اللانمطي وحساسية البنسلين.",
            formulations = listOf(
                PediatricFormulation("Suspension 200 mg / 5 mL", "معلق أطفال 200 مجم / 5 مل", 40.0, "PO"),
                PediatricFormulation("Capsules / Tablets 250 mg / 500 mg", "كبسولات أو أقراص 250 و 500 مجم", 250.0, "PO")
            )
        ),

        // === 3. RESPIRATORY & ASTHMA (0 to 18 Years) ===
        DrugInfo(
            id = "salbutamol",
            nameEn = "Salbutamol / Albuterol (Ventolin)",
            nameAr = "سالبوتامول (فنتولين موسع للشعب)",
            category = DrugCategory.RESPIRATORY_ASTHMA,
            ageRangeEn = "Birth to 18 Years",
            ageRangeAr = "من حديثي الولادة حتى 18 سنة",
            doseDescriptionEn = "Nebulizer: 0.15 mg/kg (min 1.25 mg, max 2.5 - 5 mg). MDI Spacer: 2 - 6 puffs q20min",
            doseDescriptionAr = "جلسة رذاذ: 0.15 مجم/كجم (أدنى حد 1.25 مجم، أقصى حد 2.5-5 مجم). بخاخ مع قمع: 2-6 بخات",
            dosePerKgMin = 0.15,
            dosePerKgMax = 0.3,
            doseUnit = "mg/kg",
            stockConcentration = 5.0, // respirator solution 5 mg/mL (0.5%)
            stockUnit = "mg/mL",
            defaultRoute = "Inhalation (Nebulizer / MDI)",
            frequencyEn = "q20min x3 doses for acute severe asthma, then q1-4h PRN",
            frequencyAr = "كل 20 دقيقة (حتى 3 جلسات في النوبة الحادة)، ثم كل 1-4 ساعات عند اللزوم",
            maxSingleDose = 5.0,
            administrationGuideEn = "Inhalation: Dilute in 2 - 3 mL of Normal Saline in nebulizer chamber and run with oxygen 6-8 L/min over 10-15 min. MDI: Shake canister, attach spacer (Aerochamber), seal mask over child's face, deliver 1 puff at a time and allow child to breathe 5-6 tidal breaths.",
            administrationGuideAr = "طريقة الأخذ: جلسة استنشاق: يُخفف الدواء في 2-3 مل محلول ملحي في حجرة النيبولايزر مع تدفق أكسجين 6-8 لتر/دقيقة. البخاخ: يُرج جيداً، يُركب في قمع الاستنشاق (Spacer) مع تثبيت القناع على وجه الطفل وإعطاء بخة واحدة كل مرة مع تنفس 5-6 مرات.",
            cautionsEn = "Causes tachycardia, tremors, and hypokalemia. Monitor heart rate and oxygen saturation.",
            cautionsAr = "يسبب تسارع ضربات القلب ورعشة باليدين وهبوط البوتاسيوم مع الجرعات المتكررة.",
            formulations = listOf(
                PediatricFormulation("Respirator Solution 5 mg / mL (0.5%)", "محلول جلسات رذاذ 5 مجم / مل", 5.0, "Inhalation"),
                PediatricFormulation("MDI Inhaler 100 mcg / puff", "بخاخ استنشاق 100 ميكروجرام / بFocusValueخة", 0.1, "Inhalation"),
                PediatricFormulation("Syrup 2 mg / 5 mL", "شراب فنتولين 2 مجم / 5 مل", 0.4, "PO")
            )
        ),

        DrugInfo(
            id = "prednisolone",
            nameEn = "Prednisolone / Prednisone",
            nameAr = "بريدنيزولون (زيلون / بريدسول)",
            category = DrugCategory.RESPIRATORY_ASTHMA,
            ageRangeEn = "1 Month to 18 Years",
            ageRangeAr = "من شهر حتى 18 سنة",
            doseDescriptionEn = "1 - 2 mg/kg/day PO (Max 60 mg/day) for 3 - 5 days in acute asthma exacerbation",
            doseDescriptionAr = "1 - 2 مجم/كجم/يوم شراب بالفم (أقصى حد 60 مجم/يوم) لمدة 3-5 أيام لنوبات الربو الحادة",
            dosePerKgMin = 1.0,
            dosePerKgMax = 2.0,
            doseUnit = "mg/kg/day",
            stockConcentration = 3.0, // syrup 15mg/5mL = 3 mg/mL
            stockUnit = "mg/mL",
            defaultRoute = "PO",
            frequencyEn = "Once daily in the morning with food (or divided q12h) for 3-5 days (no taper needed)",
            frequencyAr = "مرة واحدة صباحاً مع الإفطار (أو مقسم كل 12 ساعة) لمدة 3-5 أيام دون حاجة للتدريج",
            maxSingleDose = 60.0,
            maxDailyDose = 60.0,
            administrationGuideEn = "PO: Administer in the morning with breakfast or milk to reduce gastric irritation and mimic diurnal cortisol rhythm. Short courses (<14 days) do not require tapering.",
            administrationGuideAr = "طريقة الأخذ: يؤخذ بالفم صباحاً مع وجبة الإفطار أو الحليب لتقليل اضطراب المعدة. الكورسات القصيرة (أقل من أسبوعين) لا تتطلب إيقافاً تدريجياً.",
            cautionsEn = "Avoid in live vaccine administration. May cause transient behavioral changes or hyperglycemia.",
            cautionsAr = "تجنب إعطاء التطعيمات الحية أثناء العلاج. قد يسبب تقلبات في المزاج وارتفاع مؤقت في السكر.",
            formulations = listOf(
                PediatricFormulation("Syrup 5 mg / 5 mL", "شراب 5 مجم / 5 مل", 1.0, "PO"),
                PediatricFormulation("Syrup 15 mg / 5 mL (Forte)", "شراب فورت 15 مجم / 5 مل", 3.0, "PO"),
                PediatricFormulation("Tablets 5 mg / 20 mg", "أقراص 5 مجم و 20 مجم", 5.0, "PO")
            )
        ),

        DrugInfo(
            id = "dexamethasone_ped",
            nameEn = "Dexamethasone",
            nameAr = "ديكساميثازون (لعلاج الخناق Croup وحساسية الصدر)",
            category = DrugCategory.STEROID,
            ageRangeEn = "Birth to 18 Years",
            ageRangeAr = "من حديثي الولادة حتى 18 سنة",
            doseDescriptionEn = "Croup (Laryngotracheobronchitis): 0.15 - 0.6 mg/kg single dose PO/IM/IV (Max 16 mg)",
            doseDescriptionAr = "الخناق الوليدي وسعال النباح (Croup): 0.15 - 0.6 مجم/كجم جرعة واحدة (أقصى حد 16 مجم)",
            dosePerKgMin = 0.15,
            dosePerKgMax = 0.6,
            doseUnit = "mg/kg",
            stockConcentration = 4.0, // 4 mg/mL or syrup 2mg/5mL
            stockUnit = "mg/mL",
            defaultRoute = "PO / IM / IV",
            frequencyEn = "Single dose for croup (may repeat once after 24h if severe)",
            frequencyAr = "جرعة واحدة كافية لمعظم حالات الخناق؛ يمكن تكرارها بعد 24 ساعة في الحالات الشديدة",
            maxSingleDose = 16.0,
            administrationGuideEn = "PO/IM/IV: Highly effective orally; oral solution or injectable form can be given orally mixed with a spoonful of sweet syrup/juice to mask bitter taste.",
            administrationGuideAr = "طريقة الأخذ: فعال جداً عن طريق الفم؛ يمكن إعطاء الأمبول الوريدي للشرب بالفم بعد خلطه بملعقة عصير لتحسين الطعم المر، أو حقن وريدي/عضلي.",
            cautionsEn = "Long biological half-life (36-54 hours). DART protocol used in NICU for BPD weaning.",
            cautionsAr = "مفعول طويل يمتد إلى 36-54 ساعة. يُستخدم أيضاً في بروتوكول DART لفصل الخدج عن التنفس الصناعي.",
            formulations = listOf(
                PediatricFormulation("Ampoule 4 mg / mL (IV/IM)", "أمبول 4 مجم / 1 مل", 4.0, "IV"),
                PediatricFormulation("Ampoule 8 mg / 2 mL", "أمبول 8 مجم / 2 مل", 4.0, "IV"),
                PediatricFormulation("Oral Solution 2 mg / 5 mL", "محلول فموي 2 مجم / 5 مل", 0.4, "PO")
            )
        ),

        // === 4. GASTROINTESTINAL & ANTIEMETICS ===
        DrugInfo(
            id = "ondansetron",
            nameEn = "Ondansetron (Zofran)",
            nameAr = "أوندانسيترون (زوفران - لعلاج الترجيع الحاد والنزلة المعوية)",
            category = DrugCategory.GI_NUTRITION,
            ageRangeEn = "6 Months to 18 Years",
            ageRangeAr = "من 6 أشهر حتى 18 سنة",
            doseDescriptionEn = "0.15 mg/kg/dose PO / IV (Max 4 mg for <30kg, max 8 mg for >30kg)",
            doseDescriptionAr = "0.15 مجم/كجم للجرعة بالفم أو وريدياً (أقصى حد 4 مجم للوزن <30 كجم، 8 مجم لما فوق 30 كجم)",
            dosePerKgMin = 0.15,
            dosePerKgMax = 0.15,
            doseUnit = "mg/kg",
            stockConcentration = 2.0, // 4mg in 2 mL IV = 2 mg/mL, syrup 4mg/5mL = 0.8 mg/mL
            stockUnit = "mg/mL",
            defaultRoute = "PO (ODT) / IV",
            frequencyEn = "q8h PRN for vomiting (facilitates oral rehydration solution tolerance)",
            frequencyAr = "كل 8 ساعات عند اللزوم لمنع القيء وتمكين الطفل من شرب محلول الجفاف",
            maxSingleDose = 8.0,
            administrationGuideEn = "PO: Place orally disintegrating tablet (ODT) on child's tongue; dissolves in seconds without water. IV: Push slowly over 2 - 5 minutes. Wait 15-30 minutes after dose before attempting oral rehydration fluids.",
            administrationGuideAr = "طريقة الأخذ: قرص سريع الذوبان بالفم (ODT) يوضع على لسان الطفل ليذوب خلال ثوانٍ بدون ماء؛ أو حقن وريدي بطيء على 2-5 دقائق. انتظر 15 إلى 30 دقيقة بعد إعطائه قبل البدء بإعطاء محلول الجفاف بالملعقة.",
            cautionsEn = "Do not use for undiagnosed abdominal pain or bowel obstruction. May cause mild diarrhea or headache.",
            cautionsAr = "ممنوع في حالات الانسداد المعوي والاشتباه بالتهاب الزائدة الدودية. قد يسبب إسهالاً خفيفاً.",
            formulations = listOf(
                PediatricFormulation("Oral Disintegrating Tablet (ODT) 4 mg", "أقراص سريعة الذوبان بالفم 4 مجم", 4.0, "PO"),
                PediatricFormulation("Syrup 4 mg / 5 mL", "شراب زوفران 4 مجم / 5 مل", 0.8, "PO"),
                PediatricFormulation("Ampoule 4 mg / 2 mL IV", "أمبول وريدي 4 مجم / 2 مل", 2.0, "IV")
            )
        ),

        // === 5. ALLERGY & ANTIHISTAMINES ===
        DrugInfo(
            id = "cetirizine",
            nameEn = "Cetirizine (Zyrtec)",
            nameAr = "سيتريزين (زيرتك مضاد للحساسية)",
            category = DrugCategory.ALLERGY_ANTIHISTAMINE,
            ageRangeEn = "6 Months to 18 Years",
            ageRangeAr = "من 6 أشهر حتى 18 سنة",
            doseDescriptionEn = "6m-2y: 2.5 mg once daily. 2-5y: 2.5 - 5 mg daily. >6y: 5 - 10 mg daily",
            doseDescriptionAr = "6ش-سنتين: 2.5 مجم مرة يومياً. 2-5 سنوات: 2.5-5 مجم. >6 سنوات: 5-10 مجم يومياً",
            dosePerKgMin = 0.25,
            dosePerKgMax = 0.25,
            doseUnit = "mg/kg",
            stockConcentration = 1.0, // syrup 5mg/5mL = 1 mg/mL, drops 10mg/mL
            stockUnit = "mg/mL",
            defaultRoute = "PO",
            frequencyEn = "Once daily in the evening (q24h)",
            frequencyAr = "مرة واحدة يومياً مساءً قبل النوم",
            maxSingleDose = 10.0,
            administrationGuideEn = "PO: Administer once daily in the evening with or without food. Drops can be added to water or formula.",
            administrationGuideAr = "طريقة الأخذ: يؤخذ بالفم مرة واحدة يومياً مساءً قبل النوم مع أو بدون طعام. يمكن خلط النقط بقليل من الماء أو العصير.",
            cautionsEn = "Second-generation antihistamine with minimal sedation. Adjust dose in renal impairment.",
            cautionsAr = "مضاد هيستامين من الجيل الثاني آمن وقليل النعاس. يخفف أعراض الرشح والحكة الجلدية والأرتيكاريا.",
            formulations = listOf(
                PediatricFormulation("Oral Drops 10 mg / mL", "نقط فموية 10 مجم / 1 مل", 10.0, "PO"),
                PediatricFormulation("Syrup 5 mg / 5 mL", "شراب 5 مجم / 5 مل", 1.0, "PO"),
                PediatricFormulation("Tablets 10 mg", "أقراص 10 مجم", 10.0, "PO")
            )
        ),

        // === 6. EMERGENCY & RESUSCITATION (NRP / PALS) ===
        DrugInfo(
            id = "epinephrine_anaphylaxis",
            nameEn = "Epinephrine 1:1,000 (Anaphylaxis)",
            nameAr = "أدرينالين 1:1,000 (للصدمة التحسسية الحادة)",
            category = DrugCategory.EMERGENCY,
            ageRangeEn = "Birth to 18 Years",
            ageRangeAr = "من حديثي الولادة حتى 18 سنة",
            doseDescriptionEn = "0.01 mg/kg (0.01 mL/kg of 1:1,000) IM in mid-anterolateral thigh",
            doseDescriptionAr = "0.01 مجم/كجم (0.01 مل/كجم من محلول 1:1,000) حقن عضلي في الفخذ",
            dosePerKgMin = 0.01,
            dosePerKgMax = 0.01,
            doseUnit = "mL/kg",
            stockConcentration = 1.0,
            stockUnit = "mL",
            defaultRoute = "IM (Anterolateral Thigh)",
            frequencyEn = "Repeat every 5 - 15 minutes PRN if symptoms persist (Max single 0.3 mg child, 0.5 mg adolescent)",
            frequencyAr = "تكرار كل 5 إلى 15 دقيقة عند اللزوم (أقصى حد 0.3 مل للأطفال، 0.5 مل للمراهقين)",
            maxSingleDose = 0.5,
            administrationGuideEn = "IM: Inject into the mid-outer anterolateral thigh (vastus lateralis) for rapid systemic absorption. NEVER give 1:1,000 IV! Use 1:10,000 for IV cardiac arrest.",
            administrationGuideAr = "طريقة الأخذ: حقن عضلي حصراً في منتصف الجزء الخارجي من الفخذ لسرعة الامتصاص. تحذير قاتل: ممنوع إعطاء تركيز 1:1000 في الوريد! الحقن الوريدي مخصص لتركيز 1:10,000 المخفف فقط في توقف القلب.",
            cautionsEn = "First-line lifesaving medication for anaphylactic shock. Prompt administration saves lives.",
            cautionsAr = "العلاج المنقذ للحياة الأول والأساسي لصدمة الحساسية الحادة وتورم الحنجرة.",
            isDirectVolumePerKg = true
        ),

        DrugInfo(
            id = "epinephrine_cpr",
            nameEn = "Epinephrine 1:10,000 (CPR Cardiac Arrest)",
            nameAr = "أدرينالين 1:10,000 (إنعاش توقف القلب CPR)",
            category = DrugCategory.EMERGENCY,
            doseDescriptionEn = "0.01 mg/kg (0.1 mL/kg of 1:10,000 solution) IV / IO",
            doseDescriptionAr = "0.01 مجم/كجم (0.1 مل/كجم من محلول 1:10,000 المخفف) وريدي أو عبر العظم",
            dosePerKgMin = 0.1,
            dosePerKgMax = 0.1,
            doseUnit = "mL/kg",
            stockConcentration = 1.0,
            stockUnit = "mL",
            defaultRoute = "IV / IO",
            frequencyEn = "Every 3 - 5 minutes during CPR (PALS / NRP guidelines)",
            frequencyAr = "كل 3 إلى 5 دقائق أثناء الإنعاش القلبي الرئوي",
            maxSingleDose = 1.0,
            administrationGuideEn = "IV/IO: Rapid push followed immediately by 2 - 5 mL Normal Saline flush and elevation of extremity to ensure rapid central delivery.",
            administrationGuideAr = "طريقة الأخذ: حقن وريدي سريع يتبعه فوراً دفعة محلول ملحي 2-5 مل مع رفع الطرف للأعلى لتسريع وصول الدواء للقلب.",
            cautionsEn = "Follow with CPR compressions. ETT route (0.5-1 mL/kg) only if IV/IO access is impossible.",
            cautionsAr = "الاستمرار في الضغط الصدري CPR مباشرة. يُعطى عبر الأنبوبة الحنجرية فقط في حال تعذر الخط الوريدي.",
            isDirectVolumePerKg = true
        ),

        DrugInfo(
            id = "normal_saline",
            nameEn = "Normal Saline 0.9% (Shock Bolus)",
            nameAr = "محلول ملحي نظامي 0.9% (دفعة إنعاش الصدمة)",
            category = DrugCategory.EMERGENCY,
            doseDescriptionEn = "10 - 20 mL/kg IV/IO rapid push over 5 to 20 minutes",
            doseDescriptionAr = "10 - 20 مل/كجم وريدياً أو عبر العظم بالتنقيط السريع على 5-20 دقيقة",
            dosePerKgMin = 10.0,
            dosePerKgMax = 20.0,
            doseUnit = "mL/kg",
            stockConcentration = 1.0,
            stockUnit = "mL",
            defaultRoute = "IV / IO",
            frequencyEn = "Repeat once or twice if shock persists (assess lung sounds for crackles/hepatomegaly)",
            frequencyAr = "تكرار الجرعة إذا استمرت الصدمة (مع سماع الصدر والتأكد من عدم وجود تجمع سوائل)",
            maxSingleDose = 1000.0,
            administrationGuideEn = "IV/IO: Infuse using pressure bag or push-pull syringe method in hypovolemic or septic shock.",
            administrationGuideAr = "طريقة الأخذ: ضخ وريدي سريع باستخدام سرنجة سحب ودفع أو كيس ضغط لعلاج هبوط الضغط والصدمة الإنتانية ونقص السوائل.",
            cautionsEn = "In extreme preterms (<30w), give slowly over 20-30 min to prevent intraventricular hemorrhage.",
            cautionsAr = "للخدج الشديدين (<30 أسبوع) يُعطى على 20-30 دقيقة لتفادي نزيف المخ IVH.",
            isDirectVolumePerKg = true
        ),

        // === 7. ANTICONVULSANTS & NEUROLOGY ===
        DrugInfo(
            id = "phenobarbital",
            nameEn = "Phenobarbital",
            nameAr = "فينوباربيتال (مضاد التشنجات للولدان والرضع)",
            category = DrugCategory.NEUROLOGY,
            doseDescriptionEn = "Loading: 20 mg/kg IV over 20 min. Maintenance: 3 - 5 mg/kg/day",
            doseDescriptionAr = "جرعة تحميل: 20 مجم/كجم وريدياً على 20 دقيقة. استمرار: 3 - 5 مجم/كجم/يوم",
            dosePerKgMin = 20.0,
            dosePerKgMax = 20.0,
            doseUnit = "mg/kg",
            stockConcentration = 65.0,
            stockUnit = "mg/mL",
            defaultRoute = "IV slow / PO",
            frequencyEn = "Loading STAT; maintenance started 24h later divided q12h (Max loading 1000 mg)",
            frequencyAr = "جرعة التحميل فورياً؛ جرعة الاستمرار تبدأ بعد 24 ساعة مقسمة كل 12 ساعة",
            maxSingleDose = 1000.0,
            administrationGuideEn = "IV: Infuse slowly over at least 15 - 20 minutes (rate not exceeding 1 mg/kg/min) to avoid respiratory arrest or severe hypotension.",
            administrationGuideAr = "طريقة الأخذ: حقن وريدي بطيء جداً على مدى 15 إلى 20 دقيقة (بمعدل لا يتجاوز 1 مجم/كجم/دقيقة) لتفادي توقف التنفس وهبوط الضغط الحاد.",
            cautionsEn = "First-line anticonvulsant for neonatal seizures. Have bag-mask ventilation ready.",
            cautionsAr = "العلاج الأول لتشنجات حديثي الولادة ونقص الأكسجين HIE. يجب تجهيز أدوات دعم التنفس بجوار المريض.",
            formulations = listOf(
                PediatricFormulation("Ampoule 65 mg / mL", "أمبول 65 مجم / مل", 65.0, "IV"),
                PediatricFormulation("Ampoule 130 mg / 2 mL", "أمبول 130 مجم / 2 مل", 65.0, "IV"),
                PediatricFormulation("Syrup 15 mg / 5 mL", "شراب 15 مجم / 5 مل", 3.0, "PO")
            )
        ),

        DrugInfo(
            id = "levetiracetam",
            nameEn = "Levetiracetam (Keppra)",
            nameAr = "ليفيتيراسيتام (كيبرا مضاد للتشنجات)",
            category = DrugCategory.NEUROLOGY,
            ageRangeEn = "1 Month to 18 Years",
            ageRangeAr = "من شهر حتى 18 سنة",
            doseDescriptionEn = "Loading: 40 - 60 mg/kg IV over 15 min. Maintenance: 20 - 30 mg/kg q12h",
            doseDescriptionAr = "تحميل: 40 - 60 مجم/كجم وريدياً على 15 دقيقة. استمرار: 20 - 30 مجم/كجم كل 12 ساعة",
            dosePerKgMin = 40.0,
            dosePerKgMax = 60.0,
            doseUnit = "mg/kg",
            stockConcentration = 100.0,
            stockUnit = "mg/mL",
            defaultRoute = "IV / PO",
            frequencyEn = "Loading dose STAT, then q12h maintenance (Max 3000 mg/day)",
            frequencyAr = "جرعة التحميل فورياً ثم كل 12 ساعة (أقصى حد 3000 مجم/يوم)",
            maxSingleDose = 2000.0,
            maxDailyDose = 3000.0,
            administrationGuideEn = "IV: Dilute in 50 - 100 mL Normal Saline or D5W and infuse over 15 minutes. PO: Take twice daily with or without food.",
            administrationGuideAr = "طريقة الأخذ: وريدياً يُخفف في 50-100 مل محلول ملحي ويُعطى على 15 دقيقة. بالفم: يؤخذ بانتظام كل 12 ساعة مع أو بدون طعام.",
            cautionsEn = "Excellent hemodynamic safety with no cardiac depression. May cause mild irritability.",
            cautionsAr = "آمن جداً على الدورة الدموية والضغط ولا يثبط عضلة القلب.",
            formulations = listOf(
                PediatricFormulation("Oral Solution 100 mg / mL", "محلول فموي 100 مجم / مل", 100.0, "PO"),
                PediatricFormulation("IV Vial 500 mg / 5 mL", "فيال وريدي 500 مجم / 5 مل", 100.0, "IV"),
                PediatricFormulation("Tablets 250 mg / 500 mg", "أقراص 250 و 500 مجم", 250.0, "PO")
            )
        ),

        // === 8. CARDIOVASCULAR & INOTROPES ===
        DrugInfo(
            id = "dopamine",
            nameEn = "Dopamine Infusion",
            nameAr = "دوبامين (رافع ضغط ومقوي لانقباض القلب)",
            category = DrugCategory.CARDIO_DIURETIC,
            doseDescriptionEn = "5 - 20 mcg/kg/min continuous IV infusion",
            doseDescriptionAr = "5 - 20 ميكروجرام/كجم/دقيقة بالتنقيط الوريدي المستمر",
            dosePerKgMin = 5.0,
            dosePerKgMax = 20.0,
            doseUnit = "mcg/kg/min",
            stockConcentration = 40.0,
            stockUnit = "mg/mL",
            defaultRoute = "IV Continuous Infusion",
            frequencyEn = "Continuous titration via infusion pump",
            frequencyAr = "تنقيط مستمر عبر مضخة المحاليل مع المعايرة حسب الضغط",
            administrationGuideEn = "IV: Must be infused continuously via a calibrated syringe/infusion pump through a central venous line (UVC or PICC) to avoid peripheral extravasation necrosis.",
            administrationGuideAr = "طريقة الأخذ: يُعطى بالتنقيط المستمر فقط عبر مضخة محاليل دقيقة ويفضل خط وريدي مركزي UVC أو PICC لتفادي تنخر الأنسجة.",
            cautionsEn = "Extravasation causes severe tissue necrosis. Target MAP >= Gestational age in weeks in neonates.",
            cautionsAr = "تسربه خارج الوريد يسبب تلفاً شديداً للأنسجة. الضغط الشرياني المستهدف MAP >= أسابيع الحمل للولدان.",
            formulations = listOf(
                PediatricFormulation("Ampoule 200 mg / 5 mL (40 mg/mL)", "أمبول 200 مجم / 5 مل", 40.0, "IV")
            )
        ),

        // === 9. NICU & SPECIFIC METABOLIC DRUGS ===
        DrugInfo(
            id = "caffeine_citrate",
            nameEn = "Caffeine Citrate",
            nameAr = "سترات الكافيين (لانقطاع نفس الخدج AOP)",
            category = DrugCategory.RESPIRATORY_ASTHMA,
            ageRangeEn = "Preterm Neonates",
            ageRangeAr = "الخدج وحديثي الولادة",
            doseDescriptionEn = "Loading: 20 mg/kg IV/PO once. Maintenance: 5 - 10 mg/kg/day daily",
            doseDescriptionAr = "تحميل: 20 مجم/كجم مرة واحدة. استمرار: 5 - 10 مجم/كجم/يوم يومياً",
            dosePerKgMin = 20.0,
            dosePerKgMax = 20.0,
            doseUnit = "mg/kg",
            stockConcentration = 20.0,
            stockUnit = "mg/mL",
            defaultRoute = "IV slow / PO",
            frequencyEn = "Once daily (q24h)",
            frequencyAr = "مرة واحدة يومياً (كل 24 ساعة)",
            administrationGuideEn = "IV/PO: Infuse loading dose over 30 minutes. Maintenance can be given as a slow IV push or orally via NG tube with feeds.",
            administrationGuideAr = "طريقة الأخذ: جرعة التحميل تُعطى وريدياً على 30 دقيقة. جرعة الاستمرار تُعطى وريدياً ببطء أو بالفم عبر أنبوب المعدة مع الرضاعة.",
            cautionsEn = "Note: 20 mg caffeine citrate = 10 mg caffeine base. Hold if resting heart rate > 180 bpm.",
            cautionsAr = "20 مجم سترات كافيين = 10 مجم كافيين نقي. إيقاف مؤقت إذا زاد نبض الرضيع عن 180 دقيقة.",
            formulations = listOf(
                PediatricFormulation("Oral / IV Solution 20 mg / mL", "محلول وريدي وفموي 20 مجم / مل", 20.0, "IV")
            )
        ),

        DrugInfo(
            id = "curosurf",
            nameEn = "Poractant alfa (Curosurf)",
            nameAr = "كيوروسيرف (سيرفاكتانت رئة الخدج)",
            category = DrugCategory.RESPIRATORY_ASTHMA,
            ageRangeEn = "Preterm Neonates with RDS",
            ageRangeAr = "الخدج المصابين بالضائقة التنفسية RDS",
            doseDescriptionEn = "200 mg/kg (2.5 mL/kg) initial dose via endotracheal tube",
            doseDescriptionAr = "200 مجم/كجم (2.5 مل/كجم) جرعة أولى عبر الأنبوبة الحنجرية",
            dosePerKgMin = 2.5,
            dosePerKgMax = 2.5,
            doseUnit = "mL/kg",
            stockConcentration = 1.0,
            stockUnit = "mL",
            defaultRoute = "Endotracheal (LISA / INSURE / ETT)",
            frequencyEn = "Subsequent dose: 100 mg/kg (1.25 mL/kg) q12h if FiO2 > 30%",
            frequencyAr = "الجرعة الثانية: 100 مجم/كجم (1.25 مل/كجم) كل 12 ساعة عند استمرار الحاجة لأكسجين > 30%",
            administrationGuideEn = "ETT: Warm vial naturally to room temperature. Do not shake. Instill into trachea via feeding catheter or catheter-free (LISA/MIST technique) with bag ventilation or CPAP.",
            administrationGuideAr = "طريقة الأخذ: تدفئة الأمبول طبيعياً بدرجة حرارة الغرفة دون رج. يُحقن داخل القصبة الهوائية عبر قسطرة رفيعة (تقنية LISA) أو عبر الأنبوبة الحنجرية مع دعم CPAP.",
            cautionsEn = "Transient airway obstruction and bradycardia during instillation. Monitor SpO2 and adjust PIP as lung compliance rapidly improves.",
            cautionsAr = "مراقبة الأكسجين ونبض القلب وتخفيض ضغط التنفس PIP فوراً مع تحسن مرونة الرئة لمنع انفجار الحويصلات.",
            isDirectVolumePerKg = true
        ),

        DrugInfo(
            id = "ivig",
            nameEn = "Intravenous Immunoglobulin (IVIG)",
            nameAr = "جلوبيولين مناعي وريدي (IVIG)",
            category = DrugCategory.METABOLIC,
            ageRangeEn = "Birth to 18 Years",
            ageRangeAr = "من حديثي الولادة حتى 18 سنة",
            doseDescriptionEn = "0.5 - 1.0 g/kg (500 - 1000 mg/kg) IV infusion over 2 hours",
            doseDescriptionAr = "0.5 - 1.0 جم/كجم وريدياً بالتنقيط البطيء على مدى ساعتين",
            dosePerKgMin = 0.5,
            dosePerKgMax = 1.0,
            doseUnit = "g/kg",
            stockConcentration = 0.05,
            stockUnit = "g/mL",
            defaultRoute = "IV infusion over 2h",
            frequencyEn = "Single dose for isoimmune hemolysis/jaundice; repeat in 12h if TSB continues rising",
            frequencyAr = "جرعة واحدة لتكسير الدم والصفراء المناعية، يمكن تكرارها بعد 12 ساعة إذا لزم",
            administrationGuideEn = "IV: Start infusion slowly at 0.5 mL/kg/hr for first 15-30 min, then double rate every 15 min if tolerated up to 4-8 mL/kg/hr. Pre-medicate with paracetamol if needed.",
            administrationGuideAr = "طريقة الأخذ: بالتنقيط الوريدي البطيء بمعدل 0.5 مل/كجم/ساعة في أول نصف ساعة ثم مضاعفة المعدل تدريجياً. مراقبة الحرارة والضغط والطفح الجلدي.",
            cautionsEn = "First-line therapy to prevent exchange transfusion in Rh and ABO hemolytic disease with positive Coombs.",
            cautionsAr = "العلاج الأساسي لمنع الحاجة لتبديل الدم في حالات تكسير الدم المناعي الناتج عن فصائل الدم ABO و Rh.",
            formulations = listOf(
                PediatricFormulation("Vial 5% (50 mg / mL)", "محلول 5% (50 مجم / مل)", 0.05, "IV"),
                PediatricFormulation("Vial 10% (100 mg / mL)", "محلول 10% (100 مجم / مل)", 0.1, "IV")
            )
        ),

        DrugInfo(
            id = "vitamin_k",
            nameEn = "Phytomenadione (Vitamin K1)",
            nameAr = "فيتامين ك1 (للوقاية من نزيف حديثي الولادة)",
            category = DrugCategory.METABOLIC,
            ageRangeEn = "Birth to 18 Years",
            ageRangeAr = "من حديثي الولادة حتى 18 سنة",
            doseDescriptionEn = "Term: 1 mg IM once at birth. Preterm (<1.5kg): 0.5 mg IM once. Bleeding/OD: 1-5 mg IV",
            doseDescriptionAr = "مكتمل النمو: 1 مجم عضلياً فور الولادة. الخدج (<1.5 كجم): 0.5 مجم عضلياً. النزيف: 1-5 مجم وريدياً",
            dosePerKgMin = 0.5,
            dosePerKgMax = 1.0,
            doseUnit = "mg",
            stockConcentration = 2.0,
            stockUnit = "mg/mL",
            defaultRoute = "IM / IV",
            frequencyEn = "Single prophylactic dose at birth",
            frequencyAr = "جرعة وقائية واحدة فور الولادة",
            administrationGuideEn = "IM: Inject into anterolateral thigh within 1-6 hours of birth. Protect ampoule from light.",
            administrationGuideAr = "طريقة الأخذ: حقن عضلي في الفخذ خلال أول ساعات بعد الولادة للوقاية الحتمية من نزيف المخ والجهاز الهضمي. حماية الأمبول من الضوء.",
            cautionsEn = "Crucial prophylaxis against Vitamin K Deficiency Bleeding (VKDB / HDN).",
            cautionsAr = "وقاية أساسية ضد مرض نزيف الوليد النزفي.",
            formulations = listOf(
                PediatricFormulation("Ampoule 2 mg / 0.2 mL (Pediatric)", "أمبول أطفال 2 مجم / 0.2 مل", 10.0, "IM"),
                PediatricFormulation("Ampoule 10 mg / 1 mL", "أمبول 10 مجم / 1 مل", 10.0, "IM")
            )
        ),

        DrugInfo(
            id = "furosemide",
            nameEn = "Furosemide (Lasix)",
            nameAr = "فوروسيميد (لازكس مدر للبول)",
            category = DrugCategory.CARDIO_DIURETIC,
            ageRangeEn = "Birth to 18 Years",
            ageRangeAr = "من حديثي الولادة حتى 18 سنة",
            doseDescriptionEn = "1 mg/kg/dose IV (PO: 2 mg/kg/dose). Max single 40 - 80 mg",
            doseDescriptionAr = "1 مجم/كجم للجرعة وريدياً (بالفم: 2 مجم/كجم). أقصى جرعة واحدة 40 - 80 مجم",
            dosePerKgMin = 1.0,
            dosePerKgMax = 1.0,
            doseUnit = "mg/kg",
            stockConcentration = 10.0,
            stockUnit = "mg/mL",
            defaultRoute = "IV slow / PO",
            frequencyEn = "q12-24h (Max 6 mg/kg/day)",
            frequencyAr = "كل 12 إلى 24 ساعة (أقصى جرعة 6 مجم/كجم/يوم)",
            maxSingleDose = 80.0,
            maxDailyDose = 160.0,
            administrationGuideEn = "IV: Push slowly over 2 - 5 minutes. Rapid IV push may cause ototoxicity/tinnitus. PO: Give with food or milk.",
            administrationGuideAr = "طريقة الأخذ: حقن وريدي بطيء على 2-5 دقائق. الحقن السريع قد يؤثر على عصب السمع. بالفم: يؤخذ مع الحليب أو الطعام صباحاً.",
            cautionsEn = "Monitor electrolytes (hypokalemia, hyponatremia) and urine output. Prolonged use in preterms increases nephrocalcinosis risk.",
            cautionsAr = "مراقبة شوارد الدم (البوتاسيوم والصوديوم) وكمية البول. الاستخدام الطويل للخدج قد يسبب تكلس الكلى.",
            formulations = listOf(
                PediatricFormulation("Ampoule 20 mg / 2 mL (10 mg/mL)", "أمبول وريدي 20 مجم / 2 مل", 10.0, "IV"),
                PediatricFormulation("Oral Solution 10 mg / mL", "محلول فموي 10 مجم / مل", 10.0, "PO"),
                PediatricFormulation("Tablets 40 mg", "أقراص 40 مجم", 40.0, "PO")
            )
        ),

        DrugInfo(
            id = "calcium_gluconate",
            nameEn = "Calcium Gluconate 10%",
            nameAr = "جلوكونات الكالسيوم 10%",
            category = DrugCategory.METABOLIC,
            ageRangeEn = "Birth to 18 Years",
            ageRangeAr = "من حديثي الولادة حتى 18 سنة",
            doseDescriptionEn = "100 - 200 mg/kg (1 - 2 mL/kg of 10% solution) slow IV infusion over 20-30 min",
            doseDescriptionAr = "100 - 200 مجم/كجم (1 - 2 مل/كجم من محلول 10%) وريدياً ببطء شديد على 20-30 دقيقة",
            dosePerKgMin = 1.0,
            dosePerKgMax = 2.0,
            doseUnit = "mL/kg",
            stockConcentration = 1.0,
            stockUnit = "mL",
            defaultRoute = "IV slow infusion",
            frequencyEn = "STAT for symptomatic hypocalcemia; repeat in 6h PRN (or 1 mL per 100 mL blood during exchange transfusion)",
            frequencyAr = "فورياً لنقص الكالسيوم العرضي، أو 1 مل لكل 100 مل دم أثناء تبديل الدم الكامل",
            maxSingleDose = 20.0,
            administrationGuideEn = "IV: Dilute at least 1:1 with D5W or Normal Saline and infuse slowly over 20-30 minutes with continuous ECG monitoring. Rapid injection causes cardiac arrest!",
            administrationGuideAr = "طريقة الأخذ: يُخفف بنسبة 1:1 على الأقل بمحلول سكر أو ملح ويُضخ ببطء شديد على 20-30 دقيقة مع مراقبة رسم القلب. الحقن السريع يسبب هبوطاً وتوقفاً في عضلة القلب!",
            cautionsEn = "Stop immediately if bradycardia occurs. Incompatible with sodium bicarbonate (causes insoluble precipitate). Extravasation causes tissue necrosis.",
            cautionsAr = "إيقاف الحقن فوراً إذا تباطأ النبض. غير متوافق كيميائياً مع بيكربونات الصوديوم. تسربه خارج الوريد يسبب تنخر الأنسجة.",
            isDirectVolumePerKg = true,
            formulations = listOf(
                PediatricFormulation("Ampoule 10% (100 mg / mL)", "أمبول 10% (100 مجم / 1 مل)", 100.0, "IV")
            )
        )
    )
}
