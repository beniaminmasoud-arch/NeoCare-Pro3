package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NeonatePatient
import com.example.ui.theme.*

@Composable
fun PatientSelectorBar(
    patient: NeonatePatient,
    isArabic: Boolean,
    onSwitchPatientClick: () -> Unit,
    onLanguageToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Patient Avatar, Name, Bed, and Language Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(TealLightContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChildCare,
                            contentDescription = if (isArabic) "أيقونة الرضيع" else "Neonate Icon",
                            tint = TealPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = patient.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = if (patient.incubatorBed.isNotBlank()) patient.incubatorBed else if (isArabic) "حاضنة عامة" else "General Incubator",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )
                    }
                }

                // Action controls: Switch/Edit Patient & Language Switcher
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Language Toggle
                    AssistChip(
                        onClick = onLanguageToggle,
                        label = {
                            Text(
                                text = if (isArabic) "EN" else "عربي",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Language,
                                contentDescription = "Language",
                                modifier = Modifier.size(14.dp),
                                tint = TealPrimary
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = TealLightContainer,
                            labelColor = TealPrimary
                        ),
                        border = null,
                        modifier = Modifier.testTag("language_toggle_button")
                    )

                    // Switch Patient
                    IconButton(
                        onClick = onSwitchPatientClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("switch_patient_button")
                    ) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = if (isArabic) "تبديل أو تعديل الرضيع" else "Switch Patient",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Badges: Weight, GA, CGA, DOL
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Weight Badge
                PatientMetricChip(
                    label = if (isArabic) "الوزن الحالي" else "Weight",
                    value = "${String.format("%.2f", patient.weightKg)} kg (${patient.currentWeightGrams.toInt()} g)",
                    changePercent = patient.weightChangePercentage,
                    modifier = Modifier.weight(1f)
                )

                // Gestational Age & CGA Badge
                PatientMetricChip(
                    label = if (isArabic) "العمر الحملي" else "GA / CGA",
                    value = "${patient.gestationalWeeks}w+${patient.gestationalDays}d | CGA: ${patient.correctedAgeWeeks}w",
                    modifier = Modifier.weight(1f)
                )

                // DOL Badge
                PatientMetricChip(
                    label = if (isArabic) "عمر الطفل" else "DOL",
                    value = if (isArabic) "يوم ${patient.dayOfLife}" else "Day ${patient.dayOfLife}",
                    modifier = Modifier.weight(0.7f)
                )
            }
        }
    }
}

@Composable
fun PatientMetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    changePercent: Double? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SlateBg)
            .border(0.8.dp, SlateBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                color = SlateMuted,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                fontSize = 11.5.sp,
                color = SlateDark,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (changePercent != null && kotlin.math.abs(changePercent) > 0.05) {
                val isLoss = changePercent < 0
                Text(
                    text = "${if (changePercent > 0) "+" else ""}${String.format("%.1f", changePercent)}% ${if (isLoss) "loss" else "gain"}",
                    fontSize = 9.sp,
                    color = if (isLoss && kotlin.math.abs(changePercent) > 10.0) DangerRed else if (isLoss) WarnAmber else SafeGreen,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
