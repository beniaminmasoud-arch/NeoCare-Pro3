package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertLevel
import com.example.data.model.SilvermanInput
import com.example.ui.components.ClinicalCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoCareViewModel
import com.example.ui.viewmodel.WorkstationState
import java.util.Locale

@Composable
fun SilvermanScreen(
    state: WorkstationState,
    viewModel: NeoCareViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAr = state.isArabic
    val silverman = state.silvermanInput
    val patient = state.activePatient
    val weightKg = patient.weightKg

    val (cardBg, cardBorder, statusColor) = when (silverman.alertLevel) {
        AlertLevel.SAFE -> if (silverman.totalScore == 0) Triple(TealLightContainer, TealPrimary, TealPrimary) else Triple(SafeGreenSoft, SafeGreen, SafeGreen)
        AlertLevel.WARNING -> Triple(WarnAmberSoft, WarnAmber, WarnAmber)
        AlertLevel.DANGER -> Triple(DangerRedSoft, DangerRed, DangerRed)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // === 1. HEADER SECTION ===
        item {
            Spacer(modifier = Modifier.height(10.dp))
            SectionHeader(
                title = if (isAr) "مقياس سيلفرman أندرسون التفاعلي (Silverman-Anderson)" else "Interactive Silverman-Anderson Workstation",
                subtitle = if (isAr) "تقييم العلامات الخمسة للضائقة التنفسية وتحديد مستوى الدعم التنفسي (CPAP / ETT)" else "Assessment of 5 respiratory distress signs & clinical support guidance",
                icon = Icons.Default.Air
            )

            // Patient Context Strip
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorder, RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(TealLightContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ChildCare, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = patient.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SlateDark)
                            Text(
                                text = "${patient.gestationalWeeks}w • ⚖ ${String.format(Locale.US, "%.2f", weightKg)} kg",
                                fontSize = 10.5.sp,
                                color = TealPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Score indicator pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(cardBorder.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${silverman.totalScore} / 10",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // === 2. REAL-TIME HERO SCORE CARD & SEVERITY INTERPRETATION ===
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("silverman_hero_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Air,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAr) "مستوى الضائقة التنفسية:" else "Respiratory Distress Level:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = SlateDark
                            )
                        }

                        // Big Score Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(cardBorder)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${silverman.totalScore} / 10",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isAr) silverman.statusAr else silverman.statusEn,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = statusColor
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { silverman.totalScore / 10f },
                        color = statusColor,
                        trackColor = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Clinical Signs Breakdown Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.85f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SilvermanSummaryPill(if (isAr) "أنف" else "Flare", silverman.nasalFlaring)
                        SilvermanSummaryPill(if (isAr) "أنين" else "Grunt", silverman.grunting)
                        SilvermanSummaryPill(if (isAr) "سحب" else "Retract", silverman.retractions)
                        SilvermanSummaryPill(if (isAr) "نمط" else "Pattern", silverman.breathingPattern)
                        SilvermanSummaryPill(if (isAr) "زرقة" else "Cyanosis", silverman.cyanosis)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Respiratory Protocol Guidance
                    val respActionAr = when {
                        silverman.totalScore == 0 ->
                            "✓ استقرار تنفسي تام: الطفل يتنفس بهدوء في هواء الغرفة دون سحب عضلي أو تأوه زفيري. استمرار المراقبة الروتينية ومتابعة تشبع الأكسجين SpO2."
                        silverman.totalScore in 1..3 ->
                            "💡 ضائقة تنفسية طفيفة (Mild Distress): تنظيف وسالكية مجرى الهواء والأنف، تقديم أكسجين رطب خفيف عبر قنية أنفية (Low-Flow Nasal Cannula) مع الحفاظ على SpO2 بين 90% و 95% ومراقبة الجهد التنفسي."
                        silverman.totalScore in 4..6 ->
                            "⚠ ضائقة تنفسية متوسطة (Moderate Distress): بدء دعم تنفسي غير باضع عبر جهاز CPAP أنفي بضغط 5 إلى 7 سمH2O، سحب عينة غازات دم شرياني ABG، وتقييم إعطاء جرعة سيرفاكتانت (Surfactant) مبكرة عبر تقنية LISA للخدج."
                        else ->
                            "🚨 فشل تنفسي حاد وشيك (Severe Distress / Failure): تحضير فوري للأنبوبة الحنجرية (ETT) والتنفس الصناعي الميكانيكي. إعطاء سيرفاكتانت (Survanta/Curosurf)، عمل أشعة سينية فورية للصدر CXR لاستبعاد الاسترواح الصدري."
                    }
                    val respActionEn = when {
                        silverman.totalScore == 0 ->
                            "✓ Normal Breathing: No retractions or grunting. Infant comfortable on room air. Continue routine NICU/nursery observation."
                        silverman.totalScore in 1..3 ->
                            "💡 Mild Distress: Clear airway and suction gently. Consider humidified low-flow oxygen via nasal cannula to maintain target SpO2 (90-95%). Close observation."
                        silverman.totalScore in 4..6 ->
                            "⚠ Moderate Distress: Initiate Nasal CPAP (PEEP 5-7 cmH2O), obtain arterial blood gas (ABG), and evaluate early surfactant therapy (via LISA) in preterm neonates."
                        else ->
                            "🚨 Impending Respiratory Failure: Urgent endotracheal intubation (ETT) and invasive mechanical ventilation indicated. Administer surfactant and obtain emergency chest radiograph."
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.9f))
                            .border(0.8.dp, cardBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (silverman.alertLevel == AlertLevel.DANGER) Icons.Default.Warning else Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAr) "بروتوكول التدخل السريري المعتمد:" else "Clinical Respiratory Action Plan:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = statusColor
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isAr) respActionAr else respActionEn,
                                fontSize = 11.sp,
                                color = SlateDark,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Copy Note Button
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val note = if (isAr) {
                                "تقرير مقياس سيلفرمان لضيق التنفس (${patient.name})\nالوزن: ${String.format(Locale.US, "%.2f", weightKg)} كجم | العمر الحملي: ${patient.gestationalWeeks} أسبوع\nدرجة سيلفرمان الإجمالية: ${silverman.totalScore}/10 (${silverman.statusAr})\n• اتساع فتحات الأنف: ${silverman.nasalFlaringLabelAr}\n• الأنين والتأوه الزفيري: ${silverman.gruntingLabelAr}\n• سحب الصدر والقص: ${silverman.retractionsLabelAr}\n• توافق حركة الصدر والبطن: ${silverman.breathingPatternLabelAr}\n• الزرقة ولون الجلد: ${silverman.cyanosisLabelAr}\nالتوصية: $respActionAr"
                            } else {
                                "Silverman-Anderson Score Report for ${patient.name}\nWeight: ${String.format(Locale.US, "%.2f", weightKg)} kg | GA: ${patient.gestationalWeeks}w\nTotal Silverman Score: ${silverman.totalScore}/10 (${silverman.statusEn})\n• Nasal Flaring: ${silverman.nasalFlaringLabelEn}\n• Grunting: ${silverman.gruntingLabelEn}\n• Retractions: ${silverman.retractionsLabelEn}\n• Breathing Pattern: ${silverman.breathingPatternLabelEn}\n• Cyanosis: ${silverman.cyanosisLabelEn}\nRecommendation: $respActionEn"
                            }
                            clipboard.setPrimaryClip(ClipData.newPlainText("Silverman Report", note))
                            viewModel.showSnackbar(if (isAr) "تم نسخ تقرير مقياس سيلفرمان بنجاح" else "Silverman report copied to clipboard")
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("copy_silverman_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAr) "نسخ تقرير سيلفرمان إلى الحافظة" else "Copy Silverman Report",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // === 3. QUICK PRESET CLINICAL SCENARIOS ===
        item {
            Text(
                text = if (isAr) "سيناريوهات سريرية جاهزة (ضغطة واحدة):" else "Clinical Presets (1-Tap):",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlateMuted
            )

            Spacer(modifier = Modifier.height(4.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    PresetChip(
                        label = if (isAr) "طبيعي مستقر (0/10)" else "Normal / Stable (0/10)",
                        color = SafeGreen,
                        onClick = { viewModel.updateSilverman(SilvermanInput(0, 0, 0, 0, 0)) }
                    )
                }
                item {
                    PresetChip(
                        label = if (isAr) "ضائقة طفيفة (2/10)" else "Mild Distress (2/10)",
                        color = CyanAccent,
                        onClick = { viewModel.updateSilverman(SilvermanInput(1, 0, 1, 0, 0)) }
                    )
                }
                item {
                    PresetChip(
                        label = if (isAr) "ضائقة متوسطة / CPAP (5/10)" else "Moderate / CPAP (5/10)",
                        color = WarnAmber,
                        onClick = { viewModel.updateSilverman(SilvermanInput(1, 1, 1, 1, 1)) }
                    )
                }
                item {
                    PresetChip(
                        label = if (isAr) "فشل تنفسي حاد / تنبيب (8/10)" else "Severe / Intubation (8/10)",
                        color = DangerRed,
                        onClick = { viewModel.updateSilverman(SilvermanInput(2, 2, 2, 1, 1)) }
                    )
                }
                item {
                    PresetChip(
                        label = if (isAr) "أقصى درجة حرجة (10/10)" else "Critical Maximum (10/10)",
                        color = Color(0xFF7F1D1D),
                        onClick = { viewModel.updateSilverman(SilvermanInput(2, 2, 2, 2, 2)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // === 4. FIVE INTERACTIVE CLINICAL SIGNS SELECTORS ===
        // Sign 1: Nasal Flaring
        item {
            SilvermanSignCard(
                signNumber = 1,
                titleAr = "اتساع فتحات الأنف (Nasal Flaring)",
                titleEn = "Nasal Flaring",
                subtitleAr = "دليل على زيادة مقاومة مجرى الهواء العلوي والجهد التنفسي",
                subtitleEn = "Compensatory mechanism to decrease upper airway resistance",
                icon = Icons.Default.Air,
                selectedValue = silverman.nasalFlaring,
                testTagPrefix = "silverman_nasal_flaring",
                options = listOf(
                    SilvermanOption(
                        points = 0,
                        titleAr = "لا يوجد (0)",
                        titleEn = "None (0)",
                        descAr = "فتحات الأنف هادئة ومستقرة بدون أي حركة ملحوظة",
                        descEn = "Nares quiet and still during breathing"
                    ),
                    SilvermanOption(
                        points = 1,
                        titleAr = "اتساع طفيف / خفيف (1)",
                        titleEn = "Minimal Flaring (1)",
                        descAr = "حركة اتساع طفيفة تظهر مع الشهيق وتختفي في الزفير",
                        descEn = "Slight dilation of alae nasi on inspiration"
                    ),
                    SilvermanOption(
                        points = 2,
                        titleAr = "اتساع واضح ومستمر (2)",
                        titleEn = "Marked Flaring (2)",
                        descAr = "اتساع شديد وبارز ومستمر في فتحات الأنف مع كل تنفس",
                        descEn = "Prominent, continuous dilation with each breath"
                    )
                ),
                isAr = isAr,
                onSelect = { viewModel.updateSilverman(silverman.copy(nasalFlaring = it)) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Sign 2: Grunting
        item {
            SilvermanSignCard(
                signNumber = 2,
                titleAr = "الأنين / التأوه الزفيري (Expiratory Grunting)",
                titleEn = "Expiratory Grunting",
                subtitleAr = "محاولة وليدية ذاتية للحفاظ على السعة الوظيفية المتبقية (Auto-PEEP)",
                subtitleEn = "Partial glottic closure to maintain alveolar functional residual capacity",
                icon = Icons.Default.RecordVoiceOver,
                selectedValue = silverman.grunting,
                testTagPrefix = "silverman_grunting",
                options = listOf(
                    SilvermanOption(
                        points = 0,
                        titleAr = "لا يوجد أنين (0)",
                        titleEn = "None (0)",
                        descAr = "زفير هادئ تماماً دون أي صوت تأوه",
                        descEn = "Quiet expiration without any grunting"
                    ),
                    SilvermanOption(
                        points = 1,
                        titleAr = "يُسمع بالسماعة الطبية فقط (1)",
                        titleEn = "Stethoscope Only (1)",
                        descAr = "صوت أنين زفيري خافت يُسمع فقط بوضع السماعة على الصدر/الحنجرة",
                        descEn = "Audible only with stethoscope placed over chest/neck"
                    ),
                    SilvermanOption(
                        points = 2,
                        titleAr = "يُسمع بالأذن المجردة بدون سماعة (2)",
                        titleEn = "Audible with Naked Ear (2)",
                        descAr = "أنين واضح وصريح يُسمع بسهولة من مسافة بجانب الحاضنة",
                        descEn = "Loud expiratory sound clearly audible without stethoscope"
                    )
                ),
                isAr = isAr,
                onSelect = { viewModel.updateSilverman(silverman.copy(grunting = it)) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Sign 3: Retractions
        item {
            SilvermanSignCard(
                signNumber = 3,
                titleAr = "سحب الصدر وعظم القص (Chest & Xiphoid Retractions)",
                titleEn = "Retractions (Intercostal & Sternal)",
                subtitleAr = "انغمار الأنسجة الرخوة ناتج عن ضغط سالب شديد داخل التجويف الصدري",
                subtitleEn = "Inward pulling of intercostal spaces and xiphoid cartilage",
                icon = Icons.Default.VerticalAlignBottom,
                selectedValue = silverman.retractions,
                testTagPrefix = "silverman_retractions",
                options = listOf(
                    SilvermanOption(
                        points = 0,
                        titleAr = "لا يوجد سحب (0)",
                        titleEn = "None (0)",
                        descAr = "جدار الصدر يتمدد بسلاسة دون أي انخفاض بين الأضلاع أو أسفل القص",
                        descEn = "No retraction or inward suctioning of chest wall"
                    ),
                    SilvermanOption(
                        points = 1,
                        titleAr = "سحب طفيف / مرئي بالكاد (1)",
                        titleEn = "Just Visible / Mild (1)",
                        descAr = "سحب خفيف بين الأضلاع السفلية أو تحت عظم القص مع الشهيق",
                        descEn = "Mild indentations visible between lower ribs or below sternum"
                    ),
                    SilvermanOption(
                        points = 2,
                        titleAr = "سحب صدري وقصي عميق وشديد (2)",
                        titleEn = "Marked & Deep (2)",
                        descAr = "انخساف صدري وقصي عميق وواضح جداً يشير لمقاومة رئوية عالية",
                        descEn = "Severe, deep inward cave-in of xiphoid and intercostal spaces"
                    )
                ),
                isAr = isAr,
                onSelect = { viewModel.updateSilverman(silverman.copy(retractions = it)) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Sign 4: Breathing Pattern
        item {
            SilvermanSignCard(
                signNumber = 4,
                titleAr = "توافق حركة الصدر والبطن (Breathing Pattern)",
                titleEn = "Breathing Pattern (Thoraco-Abdominal)",
                subtitleAr = "تناسق حركة الحجاب الحاجز وعضلات القفص الصدري أثناء الشهيق",
                subtitleEn = "Coordination between diaphragmatic and chest wall expansion",
                icon = Icons.Default.CompareArrows,
                selectedValue = silverman.breathingPattern,
                testTagPrefix = "silverman_breathing_pattern",
                options = listOf(
                    SilvermanOption(
                        points = 0,
                        titleAr = "متزامن ومنتظم (0)",
                        titleEn = "Synchronized (0)",
                        descAr = "ارتفاع متزامن ومتناغم بين الصدر والبطن في وقت واحد مع الشهيق",
                        descEn = "Chest and abdomen rise together synchronously on inspiration"
                    ),
                    SilvermanOption(
                        points = 1,
                        titleAr = "تأخر أو تباطؤ في الشهيق (1)",
                        titleEn = "Lag on Inspiration (1)",
                        descAr = "البطن يرتفع أولاً ويليه الصدر بتأخر ملحوظ أثناء الشهيق",
                        descEn = "Abdomen expands first followed by delayed chest expansion"
                    ),
                    SilvermanOption(
                        points = 2,
                        titleAr = "تأرجح متناقض سيسو (See-Saw) (2)",
                        titleEn = "See-Saw / Paradoxical (2)",
                        descAr = "حركة متناقضة: ارتفاع البطن مع انخفاض الصدر مثل الأرجوحة",
                        descEn = "Paradoxical motion: chest sinks as abdomen rises (exhaustion)"
                    )
                ),
                isAr = isAr,
                onSelect = { viewModel.updateSilverman(silverman.copy(breathingPattern = it)) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Sign 5: Cyanosis
        item {
            SilvermanSignCard(
                signNumber = 5,
                titleAr = "الزرقة ولون الجلد (Cyanosis & Oxygenation)",
                titleEn = "Cyanosis (Oxygenation Status)",
                subtitleAr = "تقييم نقص الأكسجين بالدم واستجابة الرضيع للأكسجين الإضافي",
                subtitleEn = "Clinical oxygen saturation indicator and response to oxygen therapy",
                icon = Icons.Default.Palette,
                selectedValue = silverman.cyanosis,
                testTagPrefix = "silverman_cyanosis",
                options = listOf(
                    SilvermanOption(
                        points = 0,
                        titleAr = "لا توجد زرقة / وردي بهواء الغرفة (0)",
                        titleEn = "None / Completely Pink (0)",
                        descAr = "لون وردي طبيعي في اللسان والشفتين والأطراف بدون أكسجين",
                        descEn = "Completely pink in room air (SpO2 >= 90%)"
                    ),
                    SilvermanOption(
                        points = 1,
                        titleAr = "زرقة أطراف أو تستجيب للأكسجين (1)",
                        titleEn = "Relieved by Low O2 (1)",
                        descAr = "زرقة طفيفة أو زرقة بالأطراف تتحسن مع إعطاء أكسجين خفيف",
                        descEn = "Peripheral cyanosis or central cyanosis responsive to low-flow O2"
                    ),
                    SilvermanOption(
                        points = 2,
                        titleAr = "زرقة مركزية واضحة أو مستمرة مع الأكسجين (2)",
                        titleEn = "Central Cyanosis / Refractory (2)",
                        descAr = "زرقة زرقاء داكنة في اللسان والشفتين مستمرة رغم إعطاء الأكسجين",
                        descEn = "Severe central cyanosis refractory to oxygen (shunting/failure)"
                    )
                ),
                isAr = isAr,
                onSelect = { viewModel.updateSilverman(silverman.copy(cyanosis = it)) }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // === 5. SEVERITY TIERS REFERENCE CARD ===
        item {
            ClinicalCard(
                title = if (isAr) "دليل تصنيف درجات سيلفرمان السريري" else "Silverman Severity Score Reference",
                icon = Icons.Default.MenuBook,
                badgeText = "0 - 10 Score",
                alertLevel = AlertLevel.SAFE
            ) {
                SilvermanTierRow("0", if (isAr) "تنفس طبيعي ومستقر (Normal)" else "Normal Breathing", if (isAr) "لا توجد ضائقة تنفسية، مراقبة روتينية" else "No distress, room air observation", SafeGreen)
                Divider(modifier = Modifier.padding(vertical = 4.dp), color = SlateBorder)
                SilvermanTierRow("1 - 3", if (isAr) "ضائقة تنفسية طفيفة (Mild Distress)" else "Mild Respiratory Distress", if (isAr) "أكسجين رطب خفيف ومراقبة مستمرة" else "Low-flow O2 cannula, close monitoring", CyanAccent)
                Divider(modifier = Modifier.padding(vertical = 4.dp), color = SlateBorder)
                SilvermanTierRow("4 - 6", if (isAr) "ضائقة تنفسية متوسطة (Moderate Distress)" else "Moderate Respiratory Distress", if (isAr) "بدء CPAP أنفي 5-7 سمH2O وتقييم السيرفاكتانت" else "Nasal CPAP 5-7 cmH2O, assess surfactant", WarnAmber)
                Divider(modifier = Modifier.padding(vertical = 4.dp), color = SlateBorder)
                SilvermanTierRow("7 - 10", if (isAr) "فشل تنفسي حاد وشيك (Impending Failure)" else "Severe / Impending Failure", if (isAr) "تنبيب حنجري ETT فوري وتنفس صناعي ميكانيكي" else "Urgent intubation & mechanical ventilation", DangerRed)
            }
        }
    }
}

// === HELPER COMPOSABLES FOR SILVERMAN SCREEN ===

data class SilvermanOption(
    val points: Int,
    val titleAr: String,
    val titleEn: String,
    val descAr: String,
    val descEn: String
)

@Composable
fun SilvermanSignCard(
    signNumber: Int,
    titleAr: String,
    titleEn: String,
    subtitleAr: String,
    subtitleEn: String,
    icon: ImageVector,
    selectedValue: Int,
    testTagPrefix: String,
    options: List<SilvermanOption>,
    isAr: Boolean,
    onSelect: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(TealLightContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$signNumber",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = TealPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isAr) titleAr else titleEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = SlateDark
                        )
                        Text(
                            text = if (isAr) subtitleAr else subtitleEn,
                            fontSize = 10.sp,
                            color = SlateMuted
                        )
                    }
                }

                // Points Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (selectedValue) {
                                0 -> SafeGreenSoft
                                1 -> WarnAmberSoft
                                else -> DangerRedSoft
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "+$selectedValue ${if (isAr) "درجة" else "pts"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (selectedValue) {
                            0 -> SafeGreen
                            1 -> WarnAmber
                            else -> DangerRed
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3 Interactive Options (0, 1, 2)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                options.forEach { opt ->
                    val isSelected = selectedValue == opt.points
                    val borderColor = if (isSelected) {
                        when (opt.points) {
                            0 -> SafeGreen
                            1 -> WarnAmber
                            else -> DangerRed
                        }
                    } else SlateBorder

                    val optBg = if (isSelected) {
                        when (opt.points) {
                            0 -> SafeGreenSoft
                            1 -> WarnAmberSoft
                            else -> DangerRedSoft
                        }
                    } else SlateBg

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(optBg)
                            .border(if (isSelected) 1.5.dp else 0.8.dp, borderColor, RoundedCornerShape(8.dp))
                            .clickable { onSelect(opt.points) }
                            .padding(10.dp)
                            .testTag("${testTagPrefix}_${opt.points}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Radio indicator
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) borderColor else Color.White)
                                    .border(1.5.dp, borderColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isAr) opt.titleAr else opt.titleEn,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) SlateDark else SlateDark.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        text = "${opt.points} ${if (isAr) "ن" else "pt"}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSelected) borderColor else SlateMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isAr) opt.descAr else opt.descEn,
                                    fontSize = 10.5.sp,
                                    color = SlateMuted,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SilvermanSummaryPill(label: String, score: Int) {
    val color = when (score) {
        0 -> SafeGreen
        1 -> WarnAmber
        else -> DangerRed
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = SlateMuted, fontWeight = FontWeight.SemiBold)
        Text(text = "$score", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = color)
    }
}

@Composable
fun SilvermanTierRow(range: String, title: String, desc: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = range, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = SlateDark)
            Text(text = desc, fontSize = 10.sp, color = SlateMuted)
        }
    }
}
