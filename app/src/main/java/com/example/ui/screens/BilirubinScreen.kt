package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertLevel
import com.example.data.model.PhototherapyModality
import com.example.data.repository.ClinicalFormulas
import com.example.ui.components.ClinicalCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.UnitInputField
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoCareViewModel
import com.example.ui.viewmodel.WorkstationState
import java.util.Locale

@Composable
fun BilirubinScreen(
    state: WorkstationState,
    viewModel: NeoCareViewModel,
    modifier: Modifier = Modifier
) {
    val isAr = state.isArabic
    val weightKg = state.activePatient.weightKg

    val biliVal = state.bilirubinInput.toDoubleOrNull() ?: 10.5
    val ageHrs = state.bilirubinAgeHours.toDoubleOrNull() ?: 48.0

    val biliResult = remember(
        ageHrs,
        biliVal,
        state.bilirubinIsUmol,
        state.activePatient.gestationalWeeks,
        state.bilirubinRiskFactor,
        weightKg
    ) {
        ClinicalFormulas.evaluateBilirubin(
            ageHours = ageHrs,
            bilirubinInput = biliVal,
            isUmol = state.bilirubinIsUmol,
            gestationalWeeks = state.activePatient.gestationalWeeks,
            hasRiskFactors = state.bilirubinRiskFactor,
            weightKg = weightKg
        )
    }

    var showPreventionDetails by remember { mutableStateOf(false) }
    var showExchangeGuide by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        // === 1. TITLE & QUICK AGE SELECTOR ===
        item {
            SectionHeader(
                title = if (isAr) "محطة قياس الصفراء والعلاج الضوئي" else "Bilirubin & Phototherapy Workstation",
                subtitle = if (isAr) "تحديد درجات العلاج الضوئي (أحادي، ثنائي، ثلاثي) وتبديل الدم وطرق الوقاية" else "Phototherapy degrees (Single, Double, Triple), Exchange & Prevention",
                icon = Icons.Default.LightMode
            )

            // Age Quick Chips (12h, 24h, 36h, 48h, 72h, 96h, 120h)
            Text(
                text = if (isAr) "عمر الرضيع بالساعات (اختر سريعاً أو أدخل القيمة):" else "Postnatal Age (hrs):",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlateMuted
            )

            Spacer(modifier = Modifier.height(4.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val commonHours = listOf(12, 24, 36, 48, 72, 96, 120)
                items(commonHours) { h ->
                    val isSelected = state.bilirubinAgeHours == h.toString()
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.updateBilirubinAgeHours(h.toString()) },
                        label = {
                            Text(text = if (isAr) "$h س" else "${h}h", fontSize = 11.sp)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }

        // === 2. MEASUREMENT INPUT CARD ===
        item {
            ClinicalCard(
                title = if (isAr) "مدخلات قياس البليروبين (TSB)" else "Bilirubin Inputs",
                badgeText = "${biliResult.mgDlValue} mg/dL",
                alertLevel = biliResult.alertLevel
            ) {
                // 1. Gestational Age (Weeks) Selector Chips (<35w, 35-37w, >=38w)
                Text(
                    text = if (isAr) "العمر الحملي بالأسابيع (Gestational Age):" else "Gestational Age (weeks):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val gaOptions = listOf(
                        Triple(32, if (isAr) "< 35 أسبوع (خديج)" else "< 35w (Preterm)", 32),
                        Triple(36, if (isAr) "35 - 37 أسبوع" else "35-37w", 36),
                        Triple(39, if (isAr) ">= 38 أسبوع (مكتمل)" else ">= 38w (Term)", 39)
                    )
                    gaOptions.forEach { (gaVal, label, targetVal) ->
                        val isSelected = when (gaVal) {
                            32 -> state.activePatient.gestationalWeeks < 35
                            36 -> state.activePatient.gestationalWeeks in 35..37
                            else -> state.activePatient.gestationalWeeks >= 38
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.savePatient(state.activePatient.copy(gestationalWeeks = targetVal))
                            },
                            label = { Text(text = label, fontSize = 10.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Weight and Age in Hours Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    UnitInputField(
                        value = String.format(Locale.US, "%.2f", weightKg),
                        onValueChange = {
                            val parsed = it.toDoubleOrNull()
                            if (parsed != null && parsed > 0) {
                                viewModel.updatePatientWeight(parsed)
                            }
                        },
                        label = if (isAr) "وزن الطفل (كيلوجرام)" else "Neonate Weight",
                        unit = "kg",
                        modifier = Modifier.weight(1f),
                        testTag = "bili_weight_input"
                    )

                    UnitInputField(
                        value = state.bilirubinAgeHours,
                        onValueChange = { viewModel.updateBilirubinAgeHours(it) },
                        label = if (isAr) "العمر بالساعات" else "Age in Hours",
                        unit = if (isAr) "ساعة" else "hrs",
                        modifier = Modifier.weight(1f),
                        testTag = "bili_age_input"
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 3. Bilirubin Value and Unit Toggle Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    UnitInputField(
                        value = state.bilirubinInput,
                        onValueChange = { viewModel.updateBilirubinValue(it) },
                        label = if (isAr) "نسبة الصفراء (TSB)" else "Total Bilirubin",
                        unit = if (state.bilirubinIsUmol) "µmol/L" else "mg/dL",
                        modifier = Modifier.weight(1.2f),
                        testTag = "bili_val_input"
                    )

                    FilterChip(
                        selected = state.bilirubinIsUmol,
                        onClick = { viewModel.toggleBilirubinUnit() },
                        label = {
                            Text(text = if (state.bilirubinIsUmol) "µmol/L" else "mg/dL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        },
                        modifier = Modifier
                            .weight(0.8f)
                            .align(Alignment.CenterVertically)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 4. Neurotoxicity Risk Factors Toggle
                FilterChip(
                    selected = state.bilirubinRiskFactor,
                    onClick = { viewModel.toggleBilirubinRisk() },
                    label = {
                        Text(
                            text = if (isAr) "عوامل خطورة التسمم العصبي (تكسير دم، سيبسيس، ألبومين < 3)" else "Neurotoxicity Risk (Hemolysis / Sepsis / Albumin < 3)",
                            fontSize = 11.sp,
                            fontWeight = if (state.bilirubinRiskFactor) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Thresholds Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateBg)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = if (isAr) "عتبة الضوئي" else "Photo Threshold", fontSize = 10.5.sp, color = SlateMuted)
                        Text(text = "${biliResult.photoThreshold} mg/dL", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WarnAmber)
                    }

                    Divider(modifier = Modifier.height(28.dp).width(1.dp), color = SlateBorder)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = if (isAr) "عتبة تبديل الدم" else "Exchange Level", fontSize = 10.5.sp, color = SlateMuted)
                        Text(text = "${biliResult.exchangeThreshold} mg/dL", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DangerRed)
                    }

                    Divider(modifier = Modifier.height(28.dp).width(1.dp), color = SlateBorder)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = if (isAr) "الوزن الحالي" else "Weight", fontSize = 10.5.sp, color = SlateMuted)
                        Text(text = "${String.format("%.2f", weightKg)} kg", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TealPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // === 3. RESULT & TREATMENT MODALITY DECISION CARD ===
        item {
            val (modalityBg, modalityBorder, modalityColor) = when (biliResult.modality) {
                PhototherapyModality.NONE -> Triple(SafeGreenSoft, SafeGreen, SafeGreen)
                PhototherapyModality.SINGLE -> Triple(WarnAmberSoft, WarnAmber, WarnAmber)
                PhototherapyModality.DOUBLE -> Triple(Color(0xFFFFEDD5), Color(0xFFEA580C), Color(0xFFC2410C))
                PhototherapyModality.TRIPLE -> Triple(DangerRedSoft, DangerRed, DangerRed)
                PhototherapyModality.EXCHANGE -> Triple(Color(0xFFFFE4E6), Color(0xFFBE123C), Color(0xFF9F1239))
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = modalityBg),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, modalityBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Modality Badge Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isAr) "القرار العلاجي المعتمد:" else "Treatment Decision:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateDark
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(modalityBorder)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isAr) biliResult.modality.titleAr else biliResult.modality.titleEn,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isAr) biliResult.riskZoneAr else biliResult.riskZoneEn,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = modalityColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isAr) biliResult.recommendationAr else biliResult.recommendationEn,
                        fontSize = 12.sp,
                        color = SlateDark,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Metrics Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.85f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = if (isAr) "الفارق عن العتبة" else "Diff from Photo", fontSize = 10.sp, color = SlateMuted)
                            Text(
                                text = "${if (biliResult.diffFromPhoto > 0) "+" else ""}${biliResult.diffFromPhoto} mg/dL",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = if (biliResult.diffFromPhoto > 0) WarnAmber else SafeGreen
                            )
                        }

                        Divider(modifier = Modifier.height(24.dp).width(1.dp), color = SlateBorder)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = if (isAr) "المسافة حتى التبديل" else "To Exchange", fontSize = 10.sp, color = SlateMuted)
                            Text(
                                text = "${biliResult.diffFromExchange} mg/dL",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = if (biliResult.diffFromExchange <= 2.0) DangerRed else SlateDark
                            )
                        }

                        Divider(modifier = Modifier.height(24.dp).width(1.dp), color = SlateBorder)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = if (isAr) "إعادة الفحص بعد" else "Recheck In", fontSize = 10.sp, color = SlateMuted)
                            Text(
                                text = if (isAr) "${biliResult.recheckIntervalHours} ساعات" else "${biliResult.recheckIntervalHours} hrs",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = TealPrimary
                            )
                        }
                    }

                    // Guidance details for the selected modality
                    val details = if (isAr) biliResult.therapyDetailsAr else biliResult.therapyDetailsEn
                    if (details.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isAr) "التعليمات التمريضية والطبية الخاصة بهذه الدرجة:" else "Specific Care Protocol:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = SlateDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        details.forEach { tip ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(text = "• ", color = modalityBorder, fontWeight = FontWeight.Bold)
                                Text(text = tip, fontSize = 11.sp, color = SlateDark, lineHeight = 16.sp)
                            }
                        }
                    }

                    // Exchange Transfusion Box
                    val exchText = if (isAr) biliResult.exchangeProtocolAr else biliResult.exchangeProtocolEn
                    if (exchText != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEE2E2))
                                .border(1.dp, DangerRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Bloodtype, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isAr) "بروتوكول تبديل الدم (Double Volume Exchange):" else "Exchange Transfusion Protocol:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = DangerRed
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = exchText, fontSize = 10.5.sp, color = Color(0xFF7F1D1D), lineHeight = 15.sp)
                            }
                        }
                    }

                    // IVIG Protocol Box
                    val ivigText = if (isAr) biliResult.ivigProtocolAr else biliResult.ivigProtocolEn
                    if (ivigText != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyanLightContainer)
                                .border(1.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Medication, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isAr) "بروتوكول الجلوبيولين المناعي (IVIG):" else "IVIG Administration Guide:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = CyanAccent
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = ivigText, fontSize = 10.5.sp, color = Color(0xFF0C4A6E), lineHeight = 15.sp)
                            }
                        }
                    }

                    // Discontinuation Target
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isAr)
                            "✓ عتبة إيقاف العلاج الضوئي: إيقاف العلاج عند هبوط الصفراء إلى ${biliResult.stoppingTargetMgDl} مجم/ديسيلتر أو أقل، وفحص ارتداد الصفراء (Rebound) بعد 12-24 ساعة."
                        else
                            "✓ Discontinuation: Stop phototherapy when TSB falls to ${biliResult.stoppingTargetMgDl} mg/dL or below; check rebound in 12-24 hours.",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // === 4. DEDICATED PREVENTION & SCREENING SECTION ===
        item {
            SectionHeader(
                title = if (isAr) "طرق الوقاية من ارتفاع الصفراء ومضاعفاتها" else "Hyperbilirubinemia Prevention & Screening",
                subtitle = if (isAr) "إجراءات وقائية لحماية الرضيع من اليرقان النووي وتلف الدماغ" else "Clinical practices to prevent severe hyperbilirubinemia & Kernicterus",
                icon = Icons.Default.Shield
            )

            ClinicalCard(
                title = if (isAr) "الدليل الوقائي الشامل للوليد" else "Neonatal Prevention Protocol",
                badgeText = if (isAr) "إرشادات AAP" else "AAP Guidelines",
                alertLevel = AlertLevel.SAFE
            ) {
                // Key Prevention Items
                PreventionGuidelineItem(
                    title = if (isAr) "1. الرضاعة المبكرة والمتكررة (Enteral Feeding)" else "1. Early & Frequent Enteral Feeding",
                    desc = if (isAr)
                        "بدء الرضاعة الطبيعية في أول ساعة بعد الولادة، وتكرارها 8 إلى 12 مرة يومياً. الحليب يسرع خروج العقي (Meconium) الغني بالبليروبين ويمنع إعادة امتصاصه عبر الدورة المعوية الكبدية."
                    else
                        "Initiate breastfeeding within the first hour; feed 8-12 times daily. Early milk stimulates gut motility and meconium clearance, preventing enterohepatic bilirubin reabsorption.",
                    icon = Icons.Default.ChildCare
                )

                PreventionGuidelineItem(
                    title = if (isAr) "2. فحص دم الحبل السري وزمرة الدم (Cord Blood Screening)" else "2. Cord Blood & Hemolysis Screening",
                    desc = if (isAr)
                        "فحص فصيلة دم الرضيع، عامل ريزوس (Rh)، واختبار كومبس المباشر (DAT/Coombs) لجميع مواليد الأمهات ذوات فصيلة O أو Rh سلبي، لكشف تكسير الدم المناعي مبكراً."
                    else
                        "Screen infant blood type, Rh, and Direct Antiglobulin Test (DAT/Coombs) for all mothers with blood group O or Rh-negative to anticipate rapid hemolysis.",
                    icon = Icons.Default.Bloodtype
                )

                PreventionGuidelineItem(
                    title = if (isAr) "3. قياس الصفراء عبر الجلد (Transcutaneous Bilirubin - TcB)" else "3. Transcutaneous Bilirubin (TcB) Screening",
                    desc = if (isAr)
                        "إجراء مسح غير باضع بواسطة جهاز TcB لجميع المواليد قبل الخروج من المستشفى أو عند ظهور أي اصفرار بالجلد؛ وإذا كانت النتيجة قريبة من العتبة يتم تأكيدها بتحليل الدم TSB."
                    else
                        "Universal pre-discharge screening using non-invasive TcB devices for all neonates before hospital discharge; confirm by serum TSB if approaching phototherapy range.",
                    icon = Icons.Default.DeviceThermostat
                )

                PreventionGuidelineItem(
                    title = if (isAr) "4. فحص نقص إنزيم الفول (G6PD Deficiency)" else "4. G6PD Deficiency & Sepsis Screening",
                    desc = if (isAr)
                        "فحص نقص G6PD ومؤشرات العدوى وتسمم الدم (Sepsis) عند ظهور يرقان غير متوقع أو مقاوم للعلاج الضوئي، مع تجنب الأدوية والمواد المؤكسدة."
                    else
                        "Screen for G6PD deficiency and sepsis in cases of unexplained early or exaggerated hyperbilirubinemia, and strictly avoid oxidative medications.",
                    icon = Icons.Default.Science
                )

                PreventionGuidelineItem(
                    title = if (isAr) "5. مراقبة الوزن والجفاف (Hydration & Weight Tracking)" else "5. Hydration & Weight Loss Monitoring",
                    desc = if (isAr)
                        "وزن الطفل يومياً بدقة بالكيلوجرام؛ فقدان أكثر من 10% من وزن الولادة أو قلة الحفاضات المبللة (<4 باليوم) يستدعي دعماً إضافياً بالسوائل لحماية الكلى ومنع تركز البليروبين."
                    else
                        "Monitor daily weight in kg; weight loss >10% from birth or poor diaper output (<4 wet/day) warrants hydration supplementation to avoid hemoconcentration.",
                    icon = Icons.Default.Scale
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // === 5. COMPARISON OF ALL PHOTOTHERAPY DEGREES ===
        item {
            SectionHeader(
                title = if (isAr) "مقارنة ومواصفات درجات العلاج الضوئي" else "Comparison of Phototherapy Degrees",
                subtitle = if (isAr) "الأحادي، الثنائي، الثلاثي، وتبديل الدم مع المواصفات الفنية" else "Single, Double, Triple Phototherapy and Exchange specs",
                icon = Icons.Default.Tune
            )

            ClinicalCard(
                title = if (isAr) "دليل مواصفات أجهزة العلاج الضوئي" else "Phototherapy Device Standards",
                badgeText = "AAP Guidelines",
                alertLevel = AlertLevel.SAFE
            ) {
                DegreeDetailCard(
                    title = if (isAr) "العلاج الضوئي الأحادي (Single)" else "Single Phototherapy",
                    tag = if (isAr) "قياسي" else "Standard",
                    tagColor = WarnAmber,
                    specs = if (isAr) listOf(
                        "جهاز LED علوي واحد (أزرق 460-490 نانومتر)",
                        "المسافة: 30 إلى 35 سم فوق الحاضنة",
                        "الشدة الإشعاعية: >= 30 µW/cm²/nm",
                        "يمكن قطع العلاج لفترات قصيرة (20-30 دقيقة) للرضاعة",
                        "إعادة فحص البليروبين خلال 6 إلى 8 ساعات"
                    ) else listOf(
                        "1 overhead blue LED unit (460-490 nm)",
                        "Distance: 30-35 cm above infant",
                        "Irradiance: >= 30 µW/cm²/nm",
                        "Brief holding for breastfeeding permitted",
                        "Recheck TSB in 6-8 hours"
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                DegreeDetailCard(
                    title = if (isAr) "العلاج الضوئي الثنائي (Double)" else "Double Intensive Phototherapy",
                    tag = if (isAr) "مكثف" else "Intensive",
                    tagColor = Color(0xFFEA580C),
                    specs = if (isAr) listOf(
                        "جهازان LED علويان أو جهاز علوي + فرشة Biliblanket تحت الظهر",
                        "الشدة الإشعاعية: >= 35 µW/cm²/nm مع تعريض >60-80% من سطح الجلد",
                        "تقليل حجم الحفاضة لأقصى حد لزيادة المساحة المعرضة",
                        "إضافة سوائل وريدية +15 مل/كجم/يوم لتعويض الفقدان غير المحسوس",
                        "إعادة فحص البليروبين خلال 4 إلى 6 ساعات"
                    ) else listOf(
                        "2 overhead units OR 1 overhead + fiberoptic blanket under back",
                        "Irradiance: >= 35 µW/cm²/nm covering >60-80% body surface area",
                        "Minimal diaper size for maximum skin illumination",
                        "Supplement +15 mL/kg/day to daily fluid target",
                        "Recheck TSB in 4-6 hours"
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                DegreeDetailCard(
                    title = if (isAr) "العلاج الضوئي الثلاثي (Triple)" else "Triple Super-Intensive",
                    tag = if (isAr) "فائق الشدة" else "Super-Intensive",
                    tagColor = DangerRed,
                    specs = if (isAr) listOf(
                        "جهازان علويان + فرشة ألياف ضوئية سفلية بكامل ظهر وبطن الرضيع",
                        "يُمنع إيقاف العلاج تماماً؛ الرضاعة تتم داخل الحاضنة أو عبر أنبوب معوي",
                        "تجهيز دم متوافق (Crossmatch) بحجم تبادلي تحسباً لتبديل الدم",
                        "إعطاء IVIG إذا كان السبب تكسير دم مناعي",
                        "إعادة قياس الصفراء كل ساعتين إلى 3 ساعات"
                    ) else listOf(
                        "2 overhead LED banks + fiberoptic mattress beneath infant",
                        "Continuous un-interrupted illumination; feeds given via NG tube",
                        "Blood bank alerted for standby crossmatched blood",
                        "IVIG considered for isoimmune hemolysis",
                        "Recheck TSB every 2-3 hours"
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                DegreeDetailCard(
                    title = if (isAr) "تبديل الدم الكامل (Double-Volume Exchange Transfusion)" else "Double Volume Exchange Transfusion",
                    tag = if (isAr) "تدخل حرج" else "Emergency",
                    tagColor = Color(0xFF9F1239),
                    specs = if (isAr) listOf(
                        "حجم الدم التبادلي: 160 مل/كجم (مكتمل) أو 180 مل/كجم (خديج) = ${(weightKg * 160).toInt()} مل لوزن الطفل",
                        "دم طازج (<5 أيام) مفلتر من الكريات البيضاء ومعقم وخالي من CMV ومطابق للأم",
                        "حقن 1 مل جلوكونات كالسيوم 10% بعد كل 100 مل دم متبادل للوقاية من نقص الكالسيوم",
                        "مراقبة النبض والضغط والحرارة وسكر الدم والكالسيوم بعد الإجراء",
                        "استمرار الضوئي الثلاثي بعد التبديل وإعادة قياس TSB بعد ساعتين"
                    ) else listOf(
                        "Double Volume: 160 mL/kg (Term) or 180 mL/kg (Preterm) = ${(weightKg * 160).toInt()} mL",
                        "Fresh (<5d), irradiated, leukodepleted, CMV-negative crossmatched blood",
                        "1 mL of 10% Calcium Gluconate IV slow push per 100 mL blood exchanged",
                        "Continuous vitals, glucose, and ionized calcium monitoring",
                        "Resume intensive phototherapy post-exchange and recheck TSB in 2 hours"
                    )
                )
            }
        }
    }
}

@Composable
fun PreventionGuidelineItem(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(TealLightContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = SlateDark)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = desc, fontSize = 11.sp, color = SlateMuted, lineHeight = 16.sp)
        }
    }
}

@Composable
fun DegreeDetailCard(
    title: String,
    tag: String,
    tagColor: Color,
    specs: List<String>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SlateBg)
            .border(0.8.dp, SlateBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = SlateDark)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(tagColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = tag, fontWeight = FontWeight.Bold, fontSize = 9.5.sp, color = tagColor)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            specs.forEach { spec ->
                Row(modifier = Modifier.padding(vertical = 1.5.dp)) {
                    Text(text = "✓ ", fontSize = 10.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                    Text(text = spec, fontSize = 10.5.sp, color = SlateDark, lineHeight = 15.sp)
                }
            }
        }
    }
}
