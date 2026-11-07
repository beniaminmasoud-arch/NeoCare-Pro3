package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ABGInput
import com.example.data.model.AlertLevel
import com.example.data.model.ApgarInput
import com.example.data.model.SilvermanInput
import com.example.data.repository.ClinicalFormulas
import com.example.ui.components.ClinicalCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.UnitInputField
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoCareViewModel
import com.example.ui.viewmodel.WorkstationState

@Composable
fun ScoresAndAirwayScreen(
    state: WorkstationState,
    viewModel: NeoCareViewModel,
    modifier: Modifier = Modifier
) {
    val isAr = state.isArabic
    val apgar = state.apgarInput
    val silverman = state.silvermanInput
    val abg = state.abgInput

    val abgResult = remember(abg) {
        ClinicalFormulas.interpretABG(abg)
    }

    val airway = remember(state.activePatient.currentWeightGrams, state.activePatient.gestationalWeeks) {
        ClinicalFormulas.calculateAirwayEquipment(
            weightGrams = state.activePatient.currentWeightGrams,
            gestationalWeeks = state.activePatient.gestationalWeeks
        )
    }

    var phStr by remember(abg.ph) { mutableStateOf(abg.ph.toString()) }
    var pco2Str by remember(abg.pco2) { mutableStateOf(abg.pco2.toString()) }
    var po2Str by remember(abg.po2) { mutableStateOf(abg.po2.toString()) }
    var hco3Str by remember(abg.hco3) { mutableStateOf(abg.hco3.toString()) }
    var fio2Str by remember(abg.fio2Percent) { mutableStateOf(abg.fio2Percent.toString()) }
    var mapStr by remember(abg.mapVentilator) { mutableStateOf(abg.mapVentilator.toString()) }

    fun commitABG() {
        viewModel.updateABG(
            ABGInput(
                ph = phStr.toDoubleOrNull() ?: 7.35,
                pco2 = pco2Str.toDoubleOrNull() ?: 40.0,
                po2 = po2Str.toDoubleOrNull() ?: 65.0,
                hco3 = hco3Str.toDoubleOrNull() ?: 22.0,
                fio2Percent = fio2Str.toDoubleOrNull() ?: 21.0,
                mapVentilator = mapStr.toDoubleOrNull() ?: 8.0
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // === 1. APGAR SCORE ===
        item {
            SectionHeader(
                title = if (isAr) "مقياس أبغار للوليد (APGAR Score)" else "APGAR Score Calculator",
                subtitle = if (isAr) "تقييم تكيف الوليد في الدقيقة 1 و 5 و 10 مع بروتوكول الإنعاش الفوري NRP" else "Newborn transition assessment & NRP Resuscitation Guide",
                icon = Icons.Default.Assessment
            )

            // Timing selector chips with SpO2 target reminder
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple(1, "الدقيقة 1 (SpO2 60-65%)", "Min 1 (SpO2 60-65%)"),
                    Triple(5, "الدقيقة 5 (SpO2 80-85%)", "Min 5 (SpO2 80-85%)"),
                    Triple(10, "الدقيقة 10 (SpO2 85-95%)", "Min 10 (SpO2 85-95%)")
                ).forEach { (minute, labelAr, labelEn) ->
                    val isSel = state.apgarTimingMinute == minute
                    FilterChip(
                        selected = isSel,
                        onClick = { viewModel.updateApgar(apgar, minute) },
                        label = {
                            Text(
                                text = if (isAr) labelAr else labelEn,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Preset Row for APGAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SuggestionChip(
                    onClick = { viewModel.updateApgar(ApgarInput(appearance = 2, pulse = 2, grimace = 2, activity = 2, respiration = 2)) },
                    label = { Text(if (isAr) "وليد طبيعي (10/10)" else "Vigorous (10/10)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f)
                )
                SuggestionChip(
                    onClick = { viewModel.updateApgar(ApgarInput(appearance = 1, pulse = 1, grimace = 1, activity = 1, respiration = 1)) },
                    label = { Text(if (isAr) "هبوط متوسط (5/10)" else "Moderate (5/10)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f)
                )
                SuggestionChip(
                    onClick = { viewModel.updateApgar(ApgarInput(appearance = 0, pulse = 1, grimace = 0, activity = 0, respiration = 1)) },
                    label = { Text(if (isAr) "إنعاش حرج (2/10)" else "Critical (2/10)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f)
                )
            }

            ClinicalCard(
                title = if (isAr) "نتيجة مقياس أبغار (${apgar.totalScore}/10)" else "APGAR Result (${apgar.totalScore}/10)",
                badgeText = "${apgar.totalScore} / 10",
                alertLevel = apgar.alertLevel
            ) {
                val apgarBg = when (apgar.alertLevel) {
                    AlertLevel.SAFE -> SafeGreenSoft
                    AlertLevel.WARNING -> WarnAmberSoft
                    AlertLevel.DANGER -> DangerRedSoft
                }
                val apgarColor = when (apgar.alertLevel) {
                    AlertLevel.SAFE -> SafeGreen
                    AlertLevel.WARNING -> WarnAmber
                    AlertLevel.DANGER -> DangerRed
                }

                // Status Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(apgarBg)
                        .border(1.dp, apgarColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isAr) apgar.statusAr else apgar.statusEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = apgarColor
                            )
                            Text(
                                text = "Score ${apgar.totalScore}/10",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = apgarColor
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Real-time Actionable NRP Clinical Steps
                        val nrpGuideAr = when {
                            apgar.totalScore in 7..10 ->
                                "✓ الإجراءات السريرية: رعاية وليدية روتينية، تجفيف وتدفئة، وضع جلد-لجلد مع الأم، مراقبة منتظمة للعلامات الحيوية."
                            apgar.totalScore in 4..6 ->
                                "⚠ بروتوكول هبوط متوسط (NRP): تجفيف وتدفئة فورية، تعديل وضعية الرأس مع شفط الفم ثم الأنف، تحفيز لمسي، تقديم أكسجين حر أو ضغط CPAP إذا استمرت الزرقة أو صعوبة التنفس. مراقبة SpO2."
                            else ->
                                "🚨 طوارئ إنعاش وليدي فوري (STAT NRP): بدء تهوية بالضغط الإيجابي (PPV) بقناع 100% O2 فوراً. فحص النبض: إذا كان النبض < 60 د/د بعد 30 ثانية، ابدأ تدليك الصدر (3:1)، جهز أنبوبة ETT، قسطرة وريد السرة UVC، وحقن إبينفرين 0.02 مجم/كجم."
                        }
                        val nrpGuideEn = when {
                            apgar.totalScore in 7..10 ->
                                "✓ Clinical Action: Routine newborn care, warmth, skin-to-skin contact, ongoing observation."
                            apgar.totalScore in 4..6 ->
                                "⚠ Moderate Depression Protocol (NRP): Warm and dry, position airway and suction mouth/nose, tactile stimulation, provide blow-by O2 or CPAP for persistent central cyanosis/labored breathing. Target SpO2."
                            else ->
                                "🚨 EMERGENCY STAT NRP RESUSCITATION: Initiate Positive Pressure Ventilation (PPV) with 100% O2 immediately. If HR < 60 bpm after 30s PPV, begin chest compressions (3:1 ratio), prepare ETT intubation, emergency UVC line, and Epinephrine (0.02 mg/kg)."
                        }

                        Text(
                            text = if (isAr) nrpGuideAr else nrpGuideEn,
                            fontSize = 11.sp,
                            color = SlateDark,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ScoreSelectorRow(
                    label = if (isAr) "1. المظهر ولون الجلد (Appearance)" else "1. Appearance (Skin Color)",
                    options = if (isAr) listOf("أزرق / شاحب (0)", "زرقة بالأطراف (1)", "وردي بالكامل (2)")
                    else listOf("Blue/Pale (0)", "Acrocyanosis (1)", "Pink All Over (2)"),
                    selectedIndex = apgar.appearance,
                    onSelect = { viewModel.updateApgar(apgar.copy(appearance = it)) }
                )

                ScoreSelectorRow(
                    label = if (isAr) "2. النبض ومعدل ضربات القلب (Pulse)" else "2. Pulse (Heart Rate)",
                    options = if (isAr) listOf("لا يوجد نبض (0)", "< 100 د/د (1)", ">= 100 د/د (2)")
                    else listOf("Absent (0)", "< 100 bpm (1)", ">= 100 bpm (2)"),
                    selectedIndex = apgar.pulse,
                    onSelect = { viewModel.updateApgar(apgar.copy(pulse = it)) }
                )

                ScoreSelectorRow(
                    label = if (isAr) "3. استجابة المنعكسات (Grimace)" else "3. Grimace (Reflex Irritability)",
                    options = if (isAr) listOf("لا استجابة (0)", "تكشيرة خفيفة (1)", "بكاء قوي/سعال (2)")
                    else listOf("No response (0)", "Grimace (1)", "Cry/Cough/Sneeze (2)"),
                    selectedIndex = apgar.grimace,
                    onSelect = { viewModel.updateApgar(apgar.copy(grimace = it)) }
                )

                ScoreSelectorRow(
                    label = if (isAr) "4. النشاط والتوتر العضلي (Activity)" else "4. Activity (Muscle Tone)",
                    options = if (isAr) listOf("ارتخاء رخو (0)", "انثناء طفيف (1)", "حركة نشطة (2)")
                    else listOf("Limp / Flaccid (0)", "Some flexion (1)", "Active motion (2)"),
                    selectedIndex = apgar.activity,
                    onSelect = { viewModel.updateApgar(apgar.copy(activity = it)) }
                )

                ScoreSelectorRow(
                    label = if (isAr) "5. التنفس والجهد التنفسي (Respiration)" else "5. Respiration (Effort)",
                    options = if (isAr) listOf("لا يوجد تنفس (0)", "بطيء/غير منتظم (1)", "بكاء قوي مستمر (2)")
                    else listOf("Absent (0)", "Slow / Irregular (1)", "Good strong cry (2)"),
                    selectedIndex = apgar.respiration,
                    onSelect = { viewModel.updateApgar(apgar.copy(respiration = it)) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // === 2. SILVERMAN-ANDERSON RESPIRATORY SCORE ===
        item {
            SectionHeader(
                title = if (isAr) "مقياس سيلفرمان لضيق التنفس (Silverman-Anderson)" else "Silverman-Anderson Retraction Score",
                subtitle = if (isAr) "تقييم درجة الضائقة التنفسية وسحب العضلات وخطة الدعم التنفسي" else "Respiratory distress severity & clinical respiratory support guide",
                icon = Icons.Default.Air
            )

            // Quick Presets for Silverman
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SuggestionChip(
                    onClick = { viewModel.updateSilverman(SilvermanInput(0, 0, 0, 0, 0)) },
                    label = { Text(if (isAr) "طبيعي (0/10)" else "Normal (0/10)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f)
                )
                SuggestionChip(
                    onClick = { viewModel.updateSilverman(SilvermanInput(0, 1, 1, 0, 0)) },
                    label = { Text(if (isAr) "ضائقة خفيفة (2/10)" else "Mild (2/10)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f)
                )
                SuggestionChip(
                    onClick = { viewModel.updateSilverman(SilvermanInput(1, 1, 1, 1, 1)) },
                    label = { Text(if (isAr) "متوسطة (5/10)" else "Moderate (5/10)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f)
                )
                SuggestionChip(
                    onClick = { viewModel.updateSilverman(SilvermanInput(2, 2, 2, 1, 1)) },
                    label = { Text(if (isAr) "فشل وشيك (8/10)" else "Severe (8/10)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f)
                )
            }

            ClinicalCard(
                title = if (isAr) "درجة سيلفرمان (${silverman.totalScore}/10)" else "Silverman Score (${silverman.totalScore}/10)",
                badgeText = "${silverman.totalScore} / 10",
                alertLevel = silverman.alertLevel
            ) {
                val silverBg = when (silverman.alertLevel) {
                    AlertLevel.SAFE -> if (silverman.totalScore == 0) TealLightContainer else SafeGreenSoft
                    AlertLevel.WARNING -> WarnAmberSoft
                    AlertLevel.DANGER -> DangerRedSoft
                }
                val silverColor = when (silverman.alertLevel) {
                    AlertLevel.SAFE -> if (silverman.totalScore == 0) TealPrimary else SafeGreen
                    AlertLevel.WARNING -> WarnAmber
                    AlertLevel.DANGER -> DangerRed
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(silverBg)
                        .border(1.dp, silverColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isAr) silverman.statusAr else silverman.statusEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = silverColor
                            )
                            Text(
                                text = "${silverman.totalScore}/10",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = silverColor
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Real-time Actionable Respiratory Protocol
                        val respActionAr = when {
                            silverman.totalScore == 0 ->
                                "✓ الرضيع مستقر تنفسياً: لا توجد مؤشرات سحب أو تأوه. استمرار هواء الغرفة والمراقبة الروتينية."
                            silverman.totalScore in 1..3 ->
                                "💡 ضائقة تنفسية طفيفة: تنظيف وسالكية مجرى الهواء، تدفق أكسجين رطب خفيف (Low-Flow Nasal Cannula) عند الحاجة للحفاظ على تشبع SpO2 90-95%."
                            silverman.totalScore in 4..6 ->
                                "⚠ ضائقة تنفسية متوسطة: بدء دعم تنفسي غير باضع عبر جهاز CPAP أنفي بضغط 5 - 7 سمH2O، سحب عينة غازات دم شرياني ABG، وتقييم الحاجة لسيرفاكتانت (Surfactant) إذا كان خديجاً."
                            else ->
                                "🚨 فشل تنفسي حاد وشيك (Impending Respiratory Failure): تحضير فوري للأنبوبة الحنجرية (ETT) والتنفس الصناعي الميكانيكي. إعطاء سيرفاكتانت، وتصوير الصدر بالأشعة (CXR) لاستبعاد الاسترواح الصدري."
                        }
                        val respActionEn = when {
                            silverman.totalScore == 0 ->
                                "✓ Respiratory Stable: No retractions or grunting. Routine room air observation."
                            silverman.totalScore in 1..3 ->
                                "💡 Mild Distress: Clear airway, consider humidified low-flow oxygen via nasal cannula to maintain target SpO2 (90-95%)."
                            silverman.totalScore in 4..6 ->
                                "⚠ Moderate Distress: Initiate Nasal CPAP (PEEP 5-7 cmH2O), perform arterial blood gas (ABG), and evaluate for early surfactant administration in preterm infants."
                            else ->
                                "🚨 Severe Respiratory Failure: Urgent endotracheal intubation (ETT) and mechanical ventilation indicated. Administer surfactant and obtain urgent chest X-ray."
                        }

                        Text(
                            text = if (isAr) respActionAr else respActionEn,
                            fontSize = 11.sp,
                            color = SlateDark,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ScoreSelectorRow(
                    label = if (isAr) "1. حركة أعلى الصدر (Upper Chest)" else "1. Upper Chest Movement",
                    options = if (isAr) listOf("متزامن مع البطن (0)", "تأخر بالشهيق (1)", "تأرجح سيسو (2)")
                    else listOf("Synchronized (0)", "Lag on inspir. (1)", "See-saw (2)"),
                    selectedIndex = silverman.upperChest,
                    onSelect = { viewModel.updateSilverman(silverman.copy(upperChest = it)) }
                )

                ScoreSelectorRow(
                    label = if (isAr) "2. سحب أسفل الصدر (Lower Chest)" else "2. Lower Chest Retraction",
                    options = if (isAr) listOf("لا يوجد (0)", "مرئي قليلاً (1)", "شديد وواضح (2)")
                    else listOf("None (0)", "Just visible (1)", "Marked (2)"),
                    selectedIndex = silverman.lowerChest,
                    onSelect = { viewModel.updateSilverman(silverman.copy(lowerChest = it)) }
                )

                ScoreSelectorRow(
                    label = if (isAr) "3. سحب عظم القص (Xiphoid Retraction)" else "3. Xiphoid Retraction",
                    options = if (isAr) listOf("لا يوجد (0)", "مرئي قليلاً (1)", "شديد وواضح (2)")
                    else listOf("None (0)", "Just visible (1)", "Marked (2)"),
                    selectedIndex = silverman.xiphoidRetract,
                    onSelect = { viewModel.updateSilverman(silverman.copy(xiphoidRetract = it)) }
                )

                ScoreSelectorRow(
                    label = if (isAr) "4. اتساع فتحات الأنف (Nares Dilation)" else "4. Nares Flaring",
                    options = if (isAr) listOf("لا يوجد (0)", "خفيف (1)", "واضح وملحوظ (2)")
                    else listOf("None (0)", "Minimal (1)", "Marked (2)"),
                    selectedIndex = silverman.naresFlaring,
                    onSelect = { viewModel.updateSilverman(silverman.copy(naresFlaring = it)) }
                )

                ScoreSelectorRow(
                    label = if (isAr) "5. التأوه الزفيري (Expiratory Grunt)" else "5. Expiratory Grunt",
                    options = if (isAr) listOf("لا يوجد (0)", "يُسمع بالسماعة (1)", "يُسمع بالأذن المجردة (2)")
                    else listOf("None (0)", "Stethoscope only (1)", "Audible naked ear (2)"),
                    selectedIndex = silverman.expiratoryGrunt,
                    onSelect = { viewModel.updateSilverman(silverman.copy(expiratoryGrunt = it)) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // === 3. AIRWAY & RESUSCITATION EQUIPMENT ===
        item {
            SectionHeader(
                title = if (isAr) "قياسات مجرى الهواء وقساطر السرة" else "Airway & Umbilical Line Sizing",
                subtitle = if (isAr) "مخصصة لوزن ${String.format("%.2f", state.activePatient.weightKg)} كجم (${state.activePatient.gestationalWeeks} أسبوع)"
                else "Customized for ${String.format("%.2f", state.activePatient.weightKg)} kg (${state.activePatient.gestationalWeeks}w)",
                icon = Icons.Default.MedicalServices
            )

            ClinicalCard(
                title = if (isAr) "الأنبوبة الحنجرية (ETT) والمنظار" else "Endotracheal Tube (ETT) Specs",
                badgeText = "ETT ${airway.ettInternalDiameterMm} mm",
                alertLevel = AlertLevel.SAFE
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AirwayStatBox(
                        title = if (isAr) "قطر الأنبوبة (ID)" else "ETT Size (ID)",
                        value = "${airway.ettInternalDiameterMm} mm",
                        sub = if (isAr) "غير مقيدة" else "Uncuffed",
                        modifier = Modifier.weight(1f)
                    )

                    AirwayStatBox(
                        title = if (isAr) "عمق الشفة" else "Lip Depth",
                        value = "${airway.ettDepthAtLipCm} cm",
                        sub = if (isAr) "الوزن + 6" else "NRP",
                        highlight = true,
                        modifier = Modifier.weight(1f)
                    )

                    AirwayStatBox(
                        title = if (isAr) "شفرة المنظار" else "Blade",
                        value = airway.laryngoscopeBlade,
                        sub = "Miller",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AirwayStatBox(
                        title = if (isAr) "قسطرة وريد السرة" else "UVC Depth",
                        value = "${airway.uvcDepthCm} cm",
                        sub = "IVC/RA",
                        modifier = Modifier.weight(1f)
                    )

                    AirwayStatBox(
                        title = if (isAr) "شريان السرة العالي" else "UAC High",
                        value = "${airway.uacDepthHighCm} cm",
                        sub = "T6 - T8",
                        modifier = Modifier.weight(1f)
                    )

                    AirwayStatBox(
                        title = if (isAr) "قسطرة الشفط" else "Suction",
                        value = airway.suctionCatheterFr,
                        sub = "French",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // === 4. ARTERIAL BLOOD GAS (ABG) INTERPRETER ===
        item {
            SectionHeader(
                title = if (isAr) "مفسر غازات الدم الشرياني (ABG Interpreter)" else "Arterial Blood Gas (ABG)",
                subtitle = if (isAr) "التوازن الحمضي القلوي ومؤشر الأكسجة (OI)" else "Acid-base balance & Oxygenation Index (OI)",
                icon = Icons.Default.Bloodtype
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UnitInputField(
                    value = phStr,
                    onValueChange = { phStr = it; commitABG() },
                    label = "pH",
                    helperText = "7.35-7.45",
                    modifier = Modifier.weight(1f)
                )

                UnitInputField(
                    value = pco2Str,
                    onValueChange = { pco2Str = it; commitABG() },
                    label = "pCO2",
                    unit = "mmHg",
                    modifier = Modifier.weight(1f)
                )

                UnitInputField(
                    value = po2Str,
                    onValueChange = { po2Str = it; commitABG() },
                    label = "pO2",
                    unit = "mmHg",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UnitInputField(
                    value = hco3Str,
                    onValueChange = { hco3Str = it; commitABG() },
                    label = "HCO3",
                    unit = "mEq/L",
                    modifier = Modifier.weight(1f)
                )

                UnitInputField(
                    value = fio2Str,
                    onValueChange = { fio2Str = it; commitABG() },
                    label = "FiO2",
                    unit = "%",
                    modifier = Modifier.weight(1f)
                )

                UnitInputField(
                    value = mapStr,
                    onValueChange = { mapStr = it; commitABG() },
                    label = "MAP",
                    unit = "cmH2O",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            ClinicalCard(
                title = if (isAr) "التشخيص لغازات الدم" else "ABG Analysis",
                badgeText = if (isAr) abgResult.primaryDisorderAr else abgResult.primaryDisorderEn,
                alertLevel = abgResult.alertLevel
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (abgResult.alertLevel) {
                                AlertLevel.SAFE -> SafeGreenSoft
                                AlertLevel.WARNING -> WarnAmberSoft
                                AlertLevel.DANGER -> DangerRedSoft
                            }
                        )
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = if (isAr) abgResult.primaryDisorderAr else abgResult.primaryDisorderEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = when (abgResult.alertLevel) {
                                AlertLevel.SAFE -> SafeGreen
                                AlertLevel.WARNING -> WarnAmber
                                AlertLevel.DANGER -> DangerRed
                            }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isAr) "التعويض: ${abgResult.compensationAr}" else "Compensation: ${abgResult.compensationEn}",
                            fontSize = 11.sp,
                            color = SlateDark
                        )
                    }
                }

                if (abgResult.oxygenationIndex != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Oxygenation Index (OI): ${abgResult.oxygenationIndex} • PaO2/FiO2: ${abgResult.pfRatio?.toInt() ?: "--"}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TealPrimary
                    )
                }
            }
        }
    }
}
