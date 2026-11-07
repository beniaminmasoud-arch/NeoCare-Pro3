package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertLevel
import com.example.data.model.ApgarInput
import com.example.data.repository.ClinicalFormulas
import com.example.ui.components.ClinicalCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoCareViewModel
import com.example.ui.viewmodel.WorkstationState
import java.util.Locale

@Composable
fun ApgarScreen(
    state: WorkstationState,
    viewModel: NeoCareViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAr = state.isArabic
    val apgar = state.apgarInput
    val minute = state.apgarTimingMinute
    val patient = state.activePatient
    val weightKg = patient.weightKg

    // Resuscitation dosages customized for patient weight in kg
    val epiDoseMl = String.format(Locale.US, "%.2f", weightKg * 0.2) // 0.2 mL/kg of 1:10,000 solution (0.02 mg/kg)
    val bolusFluidMl = String.format(Locale.US, "%.1f", weightKg * 10.0) // 10 mL/kg of 0.9% NS
    val airway = remember(patient.currentWeightGrams, patient.gestationalWeeks) {
        ClinicalFormulas.calculateAirwayEquipment(
            weightGrams = patient.currentWeightGrams,
            gestationalWeeks = patient.gestationalWeeks
        )
    }

    val (cardBg, cardBorder, statusColor) = when (apgar.alertLevel) {
        AlertLevel.SAFE -> Triple(SafeGreenSoft, SafeGreen, SafeGreen)
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
                title = if (isAr) "مقياس أبغار التفاعلي للوليد (APGAR Score)" else "Interactive APGAR Scoring Workstation",
                subtitle = if (isAr) "تقييم العلامات الحيوية الخمسة فور الولادة وتحديد خطة الإنعاش السريري NRP" else "Assessment of 5 transition parameters and real-time NRP resuscitation guide",
                icon = Icons.Default.Assessment
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

                    // Pre-ductal SpO2 target pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyanLightContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${if (isAr) "الهدف SpO2:" else "Target SpO2:"} ${apgar.getSpO2Target(minute)}",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // === 2. TIMING INTERVAL CHIPS (1 min, 5 min, 10 min, 20 min) ===
        item {
            Text(
                text = if (isAr) "توقيت التقييم بعد الولادة (اختر الدقيقة):" else "Postnatal Assessment Timing:",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlateMuted
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1, 5, 10, 20).forEach { m ->
                    val isSel = minute == m
                    val savedRecord = state.apgarRecords[m]
                    val savedScoreText = if (savedRecord != null) " (${savedRecord.totalScore})" else ""

                    FilterChip(
                        selected = isSel,
                        onClick = { viewModel.selectApgarMinute(m) },
                        label = {
                            Text(
                                text = "${if (isAr) "الدقيقة $m" else "Min $m"}$savedScoreText",
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (savedRecord != null) {
                            {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isSel) Color.White else SafeGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else null,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("apgar_timing_min_$m")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // === 3. HERO SCORE DISPLAY & NRP CLINICAL INTERPRETATION ===
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apgar_hero_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Top row: timing badge & total score badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAr) "تقييم الدقيقة $minute بعد الولادة" else "Minute $minute Evaluation",
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
                                text = "${apgar.totalScore} / 10",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status headline
                    Text(
                        text = if (isAr) apgar.statusAr else apgar.statusEn,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = statusColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { apgar.totalScore / 10f },
                        color = statusColor,
                        trackColor = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Parameter Score Breakdown Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.85f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ApgarSummaryPill("A (لون)", "A (Color)", apgar.appearance)
                        ApgarSummaryPill("P (نبض)", "P (Pulse)", apgar.pulse)
                        ApgarSummaryPill("G (منعكس)", "G (Grimace)", apgar.grimace)
                        ApgarSummaryPill("A (عضل)", "A (Tone)", apgar.activity)
                        ApgarSummaryPill("R (تنفس)", "R (Resp)", apgar.respiration)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // NRP Clinical Action Guidance
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
                                    imageVector = if (apgar.alertLevel == AlertLevel.DANGER) Icons.Default.Warning else Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAr) "توصيات وبروتوكول الإنعاش الفوري (NRP Guide):" else "Clinical NRP Protocol & Actions:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = statusColor
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = apgar.getNrpActionGuidance(minute, isAr),
                                fontSize = 11.sp,
                                color = SlateDark,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Emergency dosages (if critical / depressed)
                    if (apgar.totalScore < 7) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (apgar.totalScore <= 3) Color(0xFFFEE2E2) else WarnAmberSoft)
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (isAr)
                                        "⚡ جرعات الإنعاش لوزن ${String.format(Locale.US, "%.2f", weightKg)} كجم:\n• إبينفرين 1:10,000 عبر UVC/وريد = $epiDoseMl مل (0.02 مجم/كجم)\n• محلول ملحي 0.9% دافع = $bolusFluidMl مل (10 مل/كجم)\n• مقاس الأنبوبة ETT = ${airway.ettInternalDiameterMm} مم (عمق الشفة: ${airway.ettDepthAtLipCm} سم)"
                                    else
                                        "⚡ Emergency Resuscitation for ${String.format(Locale.US, "%.2f", weightKg)} kg:\n• Epinephrine 1:10,000 IV/UVC = $epiDoseMl mL (0.02 mg/kg)\n• Normal Saline bolus = $bolusFluidMl mL (10 mL/kg)\n• ETT Tube size = ${airway.ettInternalDiameterMm} mm (depth: ${airway.ettDepthAtLipCm} cm)",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (apgar.totalScore <= 3) Color(0xFF7F1D1D) else Color(0xFF78350F),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    // Action buttons: Save Score & Copy Note
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.saveApgarRecord(minute, apgar) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = cardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_apgar_button")
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isAr) "حفظ نتيجة الدقيقة $minute" else "Save Min $minute",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val note = if (isAr) {
                                    "تقرير مقياس أبغار للرضيع (${patient.name})\nالوزن: ${String.format(Locale.US, "%.2f", weightKg)} كجم | العمر: ${patient.gestationalWeeks} أسبوع\nتوقيت التقييم: الدقيقة $minute\nالنتيجة الإجمالية: ${apgar.totalScore}/10 (${apgar.statusAr})\n• المظهر ولون الجلد: ${apgar.appearanceLabelAr}\n• النبض ومعدل القلب: ${apgar.pulseLabelAr}\n• استجابة المنعكسات: ${apgar.grimaceLabelAr}\n• النشاط والتوتر العضلي: ${apgar.activityLabelAr}\n• الجهد التنفسي: ${apgar.respirationLabelAr}\nتشبع الأكسجين المستهدف: ${apgar.getSpO2Target(minute)}"
                                } else {
                                    "APGAR Score Report for ${patient.name}\nWeight: ${String.format(Locale.US, "%.2f", weightKg)} kg | GA: ${patient.gestationalWeeks}w\nTiming: Minute $minute\nTotal Score: ${apgar.totalScore}/10 (${apgar.statusEn})\n• Appearance: ${apgar.appearanceLabelEn}\n• Pulse: ${apgar.pulseLabelEn}\n• Grimace: ${apgar.grimaceLabelEn}\n• Activity: ${apgar.activityLabelEn}\n• Respiration: ${apgar.respirationLabelEn}\nPre-ductal SpO2 Target: ${apgar.getSpO2Target(minute)}"
                                }
                                clipboard.setPrimaryClip(ClipData.newPlainText("APGAR Report", note))
                                viewModel.saveApgarRecord(minute, apgar)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(0.9f)
                                .testTag("copy_apgar_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAr) "نسخ التقرير" else "Copy Note",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // === 4. QUICK PRESET CLINICAL SCENARIOS ===
        item {
            Text(
                text = if (isAr) "سيناريوهات وحالات سريرية سريعة (ضغطة واحدة):" else "Clinical Scenario Presets (1-Tap):",
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
                        label = if (isAr) "وليد طبيعي وردي (10/10)" else "Vigorous Term (10/10)",
                        color = SafeGreen,
                        onClick = { viewModel.updateApgar(ApgarInput(2, 2, 2, 2, 2)) }
                    )
                }
                item {
                    PresetChip(
                        label = if (isAr) "زرقة أطراف شائعة (9/10)" else "Acrocyanosis (9/10)",
                        color = CyanAccent,
                        onClick = { viewModel.updateApgar(ApgarInput(1, 2, 2, 2, 2)) }
                    )
                }
                item {
                    PresetChip(
                        label = if (isAr) "هبوط متوسط / CPAP (5/10)" else "Moderate / CPAP (5/10)",
                        color = WarnAmber,
                        onClick = { viewModel.updateApgar(ApgarInput(1, 1, 1, 1, 1)) }
                    )
                }
                item {
                    PresetChip(
                        label = if (isAr) "إنعاش حرج فوري (2/10)" else "Severe Asphyxia (2/10)",
                        color = DangerRed,
                        onClick = { viewModel.updateApgar(ApgarInput(0, 1, 0, 0, 1)) }
                    )
                }
                item {
                    PresetChip(
                        label = if (isAr) "توقف قلب وليدي (0/10)" else "Cardiac Arrest (0/10)",
                        color = Color(0xFF7F1D1D),
                        onClick = { viewModel.updateApgar(ApgarInput(0, 0, 0, 0, 0)) }
                    )
                }
                item {
                    PresetChip(
                        label = if (isAr) "إعادة تعيين (0/10)" else "Reset (0/10)",
                        color = SlateMuted,
                        onClick = { viewModel.resetApgar() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // === 5. FIVE INTERACTIVE APGAR PARAMETER SELECTORS ===
        // Parameter 1: Appearance (A)
        item {
            ApgarParameterCard(
                letter = "A",
                titleAr = "المظهر ولون الجلد (Appearance)",
                titleEn = "Appearance (Skin Color)",
                subtitleAr = "تقييم التروية الدموية المركزية والطرفية",
                subtitleEn = "Central vs peripheral cyanosis assessment",
                icon = Icons.Default.Palette,
                selectedValue = apgar.appearance,
                testTagPrefix = "apgar_appearance",
                options = listOf(
                    ApgarOption(
                        points = 0,
                        titleAr = "أزرق / شاحب بالكامل",
                        titleEn = "Blue / Pale all over",
                        descAr = "زرقة مركزية وشحوب حاد بالجلد والغشاء المخاطي",
                        descEn = "Central cyanosis, severe pallor throughout body"
                    ),
                    ApgarOption(
                        points = 1,
                        titleAr = "زرقة في الأطراف (Acrocyanosis)",
                        titleEn = "Acrocyanosis",
                        descAr = "جذع وردي طبيعي مع زرقة في كفي اليدين والقدمين",
                        descEn = "Body pink, blue hands and feet (normal initial transition)"
                    ),
                    ApgarOption(
                        points = 2,
                        titleAr = "وردي بالكامل بدون زرقة",
                        titleEn = "Completely Pink",
                        descAr = "تروية ممتازة ولون وردي صحي في كامل الجسد والأطراف",
                        descEn = "Good central & peripheral perfusion, no cyanosis"
                    )
                ),
                isAr = isAr,
                onSelect = { viewModel.updateApgar(apgar.copy(appearance = it)) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Parameter 2: Pulse (P)
        item {
            ApgarParameterCard(
                letter = "P",
                titleAr = "النبض ومعدل ضربات القلب (Pulse)",
                titleEn = "Pulse (Heart Rate)",
                subtitleAr = "أهم مؤشر سريري للاستجابة والإنعاش الوليدي",
                subtitleEn = "Single most critical vital sign for resuscitation response",
                icon = Icons.Default.Favorite,
                selectedValue = apgar.pulse,
                testTagPrefix = "apgar_pulse",
                options = listOf(
                    ApgarOption(
                        points = 0,
                        titleAr = "لا يوجد نبض (غائب)",
                        titleEn = "Absent",
                        descAr = "توقف القلب، لا يوجد أي نبض محسوس أو مسموع",
                        descEn = "No heart rate heard or felt via stethoscope / umbilical palpation"
                    ),
                    ApgarOption(
                        points = 1,
                        titleAr = "أقل من 100 دقة/دقيقة",
                        titleEn = "< 100 bpm",
                        descAr = "بطء في معدل القلب (<100 bpm) يستدعي تهوية فورية PPV",
                        descEn = "Bradycardia, indicates inadequate ventilation / hypoxia"
                    ),
                    ApgarOption(
                        points = 2,
                        titleAr = "100 دقة/دقيقة أو أكثر",
                        titleEn = ">= 100 bpm",
                        descAr = "معدل ضربات قلب طبيعي ومطمئن لدى حديث الولادة",
                        descEn = "Normal vigorous neonatal heart rate"
                    )
                ),
                isAr = isAr,
                onSelect = { viewModel.updateApgar(apgar.copy(pulse = it)) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Parameter 3: Grimace (G)
        item {
            ApgarParameterCard(
                letter = "G",
                titleAr = "استجابة المنعكسات للمنبهات (Grimace)",
                titleEn = "Grimace (Reflex Irritability)",
                subtitleAr = "رد الفعل للمنبهات اللمسية وشفط الإفرازات بالأنف",
                subtitleEn = "Response to tactile stimulation and bulb suctioning",
                icon = Icons.Default.Mood,
                selectedValue = apgar.grimace,
                testTagPrefix = "apgar_grimace",
                options = listOf(
                    ApgarOption(
                        points = 0,
                        titleAr = "لا استجابة / ارتخاء تام",
                        titleEn = "Flaccid / No Response",
                        descAr = "انعدام أي رد فعل عند شفط الفم أو النقر على باطن القدم",
                        descEn = "No motor response to suctioning or sole flicking"
                    ),
                    ApgarOption(
                        points = 1,
                        titleAr = "تكشيرة خفيفة / حركة طفيفة",
                        titleEn = "Grimace / Weak response",
                        descAr = "امتعاض طفيف في الوجه أو حركة طفيفة عند التحفيز",
                        descEn = "Facial grimace, weak whimpering response to suctioning"
                    ),
                    ApgarOption(
                        points = 2,
                        titleAr = "بكاء قوي / سعال أو عطاس",
                        titleEn = "Cry, Cough, or Sneeze",
                        descAr = "استجابة عصبية ممتازة مع بكاء نشط وسعال ودفاع للمنبه",
                        descEn = "Vigorous cry, coughs, sneezes, or pulls away from stimulus"
                    )
                ),
                isAr = isAr,
                onSelect = { viewModel.updateApgar(apgar.copy(grimace = it)) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Parameter 4: Activity (A)
        item {
            ApgarParameterCard(
                letter = "A",
                titleAr = "النشاط والتوتر العضلي (Activity)",
                titleEn = "Activity (Muscle Tone)",
                subtitleAr = "درجة ثني الأطراف ومقاومة الجاذبية والحركة الذاتية",
                subtitleEn = "Degree of limb flexion, resistance, and spontaneous movement",
                icon = Icons.Default.FitnessCenter,
                selectedValue = apgar.activity,
                testTagPrefix = "apgar_activity",
                options = listOf(
                    ApgarOption(
                        points = 0,
                        titleAr = "ارتخاء تام / رخو بلا حركة",
                        titleEn = "Limp / Flaccid",
                        descAr = "أطراف مفرودة ومسترخية تماماً دون أي توتر عضلي",
                        descEn = "Completely flaccid, limp extremities with no tone"
                    ),
                    ApgarOption(
                        points = 1,
                        titleAr = "انثناء طفيف في الأطراف",
                        titleEn = "Some flexion",
                        descAr = "ثني خفيف بالركبتين أو المرفقين مع ضعف الحركة الذاتية",
                        descEn = "Some flexion of extremities, weak motor resistance"
                    ),
                    ApgarOption(
                        points = 2,
                        titleAr = "حركة نشطة وأطراف منثنية جيداً",
                        titleEn = "Active motion, well flexed",
                        descAr = "توتر عضلي ممتاز وحركة نشطة قوية للأطراف الأربعة",
                        descEn = "Spontaneous active movement, well-flexed arms and legs"
                    )
                ),
                isAr = isAr,
                onSelect = { viewModel.updateApgar(apgar.copy(activity = it)) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Parameter 5: Respiration (R)
        item {
            ApgarParameterCard(
                letter = "R",
                titleAr = "الجهد التنفسي والبكاء (Respiration)",
                titleEn = "Respiration (Respiratory Effort)",
                subtitleAr = "انتظام التهوية وقوة البكاء بعد نزول الحبل السري",
                subtitleEn = "Regularity of spontaneous breathing and vocal cry",
                icon = Icons.Default.Air,
                selectedValue = apgar.respiration,
                testTagPrefix = "apgar_respiration",
                options = listOf(
                    ApgarOption(
                        points = 0,
                        titleAr = "انقطاع التنفس (غائب / Apnea)",
                        titleEn = "Absent (Apnea)",
                        descAr = "لا يوجد أي جهد تنفسي، انقطاع تام في التنفس يستدعي PPV",
                        descEn = "No spontaneous respiratory effort, requires immediate PPV"
                    ),
                    ApgarOption(
                        points = 1,
                        titleAr = "تنفس بطيء / غير منتظم / بكاء ضعيف",
                        titleEn = "Slow, irregular, weak cry",
                        descAr = "تنفس سطحي متقطع أو بكاء خافت يدل على تعب تنفسي",
                        descEn = "Shallow, irregular hypoventilation or weak whimpering cry"
                    ),
                    ApgarOption(
                        points = 2,
                        titleAr = "بكاء قوي مستمر وتنفس منتظم",
                        titleEn = "Good, vigorous cry",
                        descAr = "جهد تنفسي طبيعي ممتاز وتمدد صدري متناسق مع بكاء قوي",
                        descEn = "Strong regular respiratory effort, loud healthy continuous cry"
                    )
                ),
                isAr = isAr,
                onSelect = { viewModel.updateApgar(apgar.copy(respiration = it)) }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // === 6. INTERVAL TIMELINE & RECORDED SCORES COMPARISON ===
        item {
            ClinicalCard(
                title = if (isAr) "سجل متابعة وتطور درجات أبغار (Timeline Trend)" else "APGAR Score Interval Trend",
                icon = Icons.Default.History,
                badgeText = "${state.apgarRecords.size} Recorded",
                alertLevel = AlertLevel.SAFE
            ) {
                Text(
                    text = if (isAr)
                        "مقارنة تكيف الرضيع عبر الدقائق 1 و 5 و 10 بعد الولادة لتقييم الاستجابة للإنعاش:"
                    else
                        "Comparison of transition and resuscitation response across standardized intervals:",
                    fontSize = 11.sp,
                    color = SlateMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 5, 10).forEach { m ->
                        val record = state.apgarRecords[m]
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (record != null) {
                                        when (record.alertLevel) {
                                            AlertLevel.SAFE -> SafeGreenSoft
                                            AlertLevel.WARNING -> WarnAmberSoft
                                            AlertLevel.DANGER -> DangerRedSoft
                                        }
                                    } else SlateBg
                                )
                                .border(
                                    0.8.dp,
                                    if (record != null) {
                                        when (record.alertLevel) {
                                            AlertLevel.SAFE -> SafeGreen
                                            AlertLevel.WARNING -> WarnAmber
                                            AlertLevel.DANGER -> DangerRed
                                        }
                                    } else SlateBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.selectApgarMinute(m) }
                                .padding(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = if (isAr) "الدقيقة $m" else "Min $m",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateMuted
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                if (record != null) {
                                    Text(
                                        text = "${record.totalScore} / 10",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = when (record.alertLevel) {
                                            AlertLevel.SAFE -> SafeGreen
                                            AlertLevel.WARNING -> WarnAmber
                                            AlertLevel.DANGER -> DangerRed
                                        }
                                    )
                                    Text(
                                        text = if (record.totalScore >= 7) (if (isAr) "مستقر ✓" else "Stable ✓") else (if (isAr) "إنعاش ⚠" else "NRP ⚠"),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SlateDark
                                    )
                                } else {
                                    Text(
                                        text = "-- / 10",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SlateMuted
                                    )
                                    Text(
                                        text = if (isAr) "غير مسجل" else "Not saved",
                                        fontSize = 9.sp,
                                        color = SlateMuted
                                    )
                                }
                            }
                        }
                    }
                }

                // Prognostic note for 5-min and 10-min scores
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(TealLightContainer)
                        .padding(8.dp)
                ) {
                    Text(
                        text = if (isAr)
                            "💡 أهمية الدقيقة 5 و 10: درجة أبغار في الدقيقة 5 هي الأهم للتنبؤ بنجاح الإنعاش. استمرار النتيجة < 5 في الدقيقة 10 يستدعي تقييم بروتوكول التبريد العلاجي لحماية الدماغ (Hypothermia Protocol) للمواليد مكتملي النمو المصابين بنقص الأكسجين الحاد."
                        else
                            "💡 Clinical Significance: The 5-minute APGAR is key for predicting resuscitation response. A score < 5 at 10 minutes warrants consideration for therapeutic hypothermia in term infants with hypoxic-ischemic encephalopathy (HIE).",
                        fontSize = 10.5.sp,
                        color = TealOnContainer,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

// === HELPER COMPOSABLES ===

data class ApgarOption(
    val points: Int,
    val titleAr: String,
    val titleEn: String,
    val descAr: String,
    val descEn: String
)

@Composable
fun ApgarParameterCard(
    letter: String,
    titleAr: String,
    titleEn: String,
    subtitleAr: String,
    subtitleEn: String,
    icon: ImageVector,
    selectedValue: Int,
    testTagPrefix: String,
    options: List<ApgarOption>,
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
            // Header Row
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
                            text = letter,
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
                                2 -> SafeGreenSoft
                                1 -> WarnAmberSoft
                                else -> DangerRedSoft
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "+$selectedValue ${if (isAr) "نقطة" else "pts"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (selectedValue) {
                            2 -> SafeGreen
                            1 -> WarnAmber
                            else -> DangerRed
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3 Interactive Selectable Option Tiles
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                options.forEach { opt ->
                    val isSelected = selectedValue == opt.points
                    val borderColor = if (isSelected) TealPrimary else SlateBorder
                    val bgColor = if (isSelected) TealLightContainer.copy(alpha = 0.55f) else SlateBg

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .border(if (isSelected) 1.5.dp else 0.8.dp, borderColor, RoundedCornerShape(8.dp))
                            .clickable { onSelect(opt.points) }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("${testTagPrefix}_${opt.points}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Radio Indicator
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelect(opt.points) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = TealPrimary,
                                    unselectedColor = SlateMuted
                                ),
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isAr) opt.titleAr else opt.titleEn,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 11.5.sp,
                                        color = if (isSelected) TealPrimary else SlateDark
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (isSelected) TealPrimary else SlateMuted.copy(alpha = 0.15f)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "${opt.points} pts",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else SlateMuted
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = if (isAr) opt.descAr else opt.descEn,
                                    fontSize = 10.sp,
                                    color = if (isSelected) SlateDark else SlateMuted,
                                    lineHeight = 14.sp
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
fun ApgarSummaryPill(titleAr: String, titleEn: String, points: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = titleAr, fontSize = 9.sp, color = SlateMuted, fontWeight = FontWeight.SemiBold)
        Text(
            text = "+$points",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = when (points) {
                2 -> SafeGreen
                1 -> WarnAmber
                else -> DangerRed
            }
        )
    }
}

@Composable
fun PresetChip(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
