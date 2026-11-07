package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertLevel
import com.example.data.model.NeonatePatient
import com.example.ui.components.ClinicalCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.UnitInputField
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoCareViewModel
import com.example.ui.viewmodel.WorkstationState

@Composable
fun PatientManagementScreen(
    state: WorkstationState,
    patients: List<NeonatePatient>,
    viewModel: NeoCareViewModel,
    modifier: Modifier = Modifier
) {
    val isAr = state.isArabic
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var editingPatient by remember { mutableStateOf<NeonatePatient?>(null) }
    var sbarReport by remember(state.activePatient, state.isArabic, state.customTfiInput, state.dextrosePercentInput, state.abgInput) {
        mutableStateOf(viewModel.generateSBARReport())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Section: Active Patient and Actions
        item {
            SectionHeader(
                title = if (isAr) "سجل المرضى في الحضانة" else "NICU Patient Roster",
                subtitle = if (isAr) "إدارة الأطفال المنومين، وتحديد الطفل النشط للحسابات السريرية" else "Manage admitted neonates and set active patient for calculations",
                icon = Icons.Default.Person,
                action = {
                    Button(
                        onClick = {
                            editingPatient = null
                            showDialog = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        modifier = Modifier.testTag("add_patient_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isAr) "إضافة مولود" else "New Neonate", fontSize = 12.sp)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // List of Patients
        if (patients.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isAr) "لا يوجد مرضى مسجلين حالياً. اضغط 'إضافة مولود'." else "No patient records. Click 'New Neonate' to admit.",
                        color = SlateMuted
                    )
                }
            }
        } else {
            items(patients, key = { it.id }) { patient ->
                val isActive = patient.id == state.activePatient.id
                PatientRosterCard(
                    patient = patient,
                    isActive = isActive,
                    isArabic = isAr,
                    onSetActive = { viewModel.setActivePatient(patient) },
                    onEdit = {
                        editingPatient = patient
                        showDialog = true
                    },
                    onDelete = { viewModel.deletePatient(patient) }
                )
            }
        }

        // Section: SBAR Clinical Handover Generator
        item {
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(
                title = if (isAr) "تقرير التسليم السريري (SBAR Handover)" else "SBAR Clinical Handover Report",
                subtitle = if (isAr) "تقرير طبي منظم للتسليم بين أطباء وتمريض الحضانة" else "Structured shift handover report ready to copy or share",
                icon = Icons.Default.Summarize
            )

            ClinicalCard(
                title = if (isAr) "تقرير التسليم لـ ${state.activePatient.name}" else "Handover Note: ${state.activePatient.name}",
                badgeText = if (isAr) "جاهز للإرسال" else "Ready",
                alertLevel = AlertLevel.SAFE
            ) {
                // SBAR Text Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateBg)
                        .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = sbarReport,
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SlateDark,
                        lineHeight = 17.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions: Copy to Clipboard & Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("NeoCare SBAR Report", sbarReport)
                            clipboard.setPrimaryClip(clip)
                            viewModel.showSnackbar(if (isAr) "تم نسخ التقرير إلى الحافظة" else "Report copied to clipboard")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealLightContainer, contentColor = TealPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_sbar_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isAr) "نسخ التقرير" else "Copy Note", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, sbarReport)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, if (isAr) "مشاركة تقرير التسليم" else "Share Handover Report")
                            context.startActivity(shareIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_sbar_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isAr) "مشاركة التقرير" else "Share Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Add / Edit Patient Modal Dialog
    if (showDialog) {
        PatientEditDialog(
            patient = editingPatient,
            isArabic = isAr,
            onDismiss = { showDialog = false },
            onSave = { updatedPatient ->
                viewModel.savePatient(updatedPatient)
                showDialog = false
            }
        )
    }
}

@Composable
fun PatientRosterCard(
    patient: NeonatePatient,
    isActive: Boolean,
    isArabic: Boolean,
    onSetActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) TealLightContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isActive) 1.5.dp else 1.dp,
            if (isActive) TealPrimary else SlateBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isActive) TealPrimary else SlateBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isActive) Color.White else SlateMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = patient.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = SlateDark
                            )
                            if (isActive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(TealPrimary)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isArabic) "النشط حالياً" else "ACTIVE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${patient.incubatorBed} • ${patient.gestationalWeeks}w+${patient.gestationalDays}d • ${String.format(java.util.Locale.US, "%.2f", patient.weightKg)} kg • DOL ${patient.dayOfLife}",
                            fontSize = 11.sp,
                            color = SlateMuted
                        )
                    }
                }

                // Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isActive) {
                        IconButton(onClick = onSetActive, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Check, contentDescription = "Set Active", tint = TealPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SlateMuted, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (patient.diagnosis.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${if (isArabic) "التشخيص: " else "Diagnosis: "}${patient.diagnosis}",
                    fontSize = 11.sp,
                    color = SlateDark,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PatientEditDialog(
    patient: NeonatePatient?,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onSave: (NeonatePatient) -> Unit
) {
    var name by remember { mutableStateOf(patient?.name ?: "") }
    var bed by remember { mutableStateOf(patient?.incubatorBed ?: "") }
    var gaWeeks by remember { mutableStateOf(patient?.gestationalWeeks?.toString() ?: "32") }
    var gaDays by remember { mutableStateOf(patient?.gestationalDays?.toString() ?: "0") }
    var birthWtKg by remember { mutableStateOf(patient?.let { String.format(java.util.Locale.US, "%.2f", it.birthWeightKg) } ?: "1.50") }
    var currentWtKg by remember { mutableStateOf(patient?.let { String.format(java.util.Locale.US, "%.2f", it.weightKg) } ?: "1.50") }
    var dol by remember { mutableStateOf(patient?.dayOfLife?.toString() ?: "1") }
    var diagnosis by remember { mutableStateOf(patient?.diagnosis ?: "") }
    var notes by remember { mutableStateOf(patient?.clinicalNotes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (patient == null)
                    (if (isArabic) "تسجيل مريض وليد جديد" else "Admit New Neonate")
                else
                    (if (isArabic) "تعديل بيانات المريض" else "Edit Patient Profile"),
                fontWeight = FontWeight.Bold,
                color = TealPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UnitInputField(
                    value = name,
                    onValueChange = { name = it },
                    label = if (isArabic) "اسم الطفل / كود الحضانة" else "Patient Name / ID",
                    isDecimal = false
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UnitInputField(
                        value = bed,
                        onValueChange = { bed = it },
                        label = if (isArabic) "الحاضنة / السرير" else "Bed / Incubator",
                        isDecimal = false,
                        modifier = Modifier.weight(1f)
                    )

                    UnitInputField(
                        value = dol,
                        onValueChange = { dol = it },
                        label = if (isArabic) "عمر الطفل (DOL)" else "Day of Life",
                        unit = if (isArabic) "يوم" else "d",
                        isDecimal = false,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UnitInputField(
                        value = gaWeeks,
                        onValueChange = { gaWeeks = it },
                        label = if (isArabic) "العمر الحملي" else "GA Weeks",
                        unit = "w",
                        isDecimal = false,
                        modifier = Modifier.weight(1f)
                    )

                    UnitInputField(
                        value = gaDays,
                        onValueChange = { gaDays = it },
                        label = if (isArabic) "أيام إضافية" else "GA Days",
                        unit = "d",
                        isDecimal = false,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UnitInputField(
                        value = birthWtKg,
                        onValueChange = { birthWtKg = it },
                        label = if (isArabic) "وزن الولادة (كجم)" else "Birth Weight (kg)",
                        unit = "kg",
                        modifier = Modifier.weight(1f)
                    )

                    UnitInputField(
                        value = currentWtKg,
                        onValueChange = { currentWtKg = it },
                        label = if (isArabic) "الوزن الحالي (كجم)" else "Current Weight (kg)",
                        unit = "kg",
                        modifier = Modifier.weight(1f)
                    )
                }

                UnitInputField(
                    value = diagnosis,
                    onValueChange = { diagnosis = it },
                    label = if (isArabic) "التشخيص الطبي" else "Diagnosis",
                    isDecimal = false
                )

                UnitInputField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = if (isArabic) "ملاحظات سريرية" else "Clinical Notes",
                    isDecimal = false
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bKg = birthWtKg.toDoubleOrNull() ?: 1.5
                    val cKg = currentWtKg.toDoubleOrNull() ?: 1.5
                    val p = NeonatePatient(
                        id = patient?.id ?: 0L,
                        name = name.ifBlank { if (isArabic) "رضيع غير مسمى" else "Unnamed Infant" },
                        incubatorBed = bed,
                        gestationalWeeks = gaWeeks.toIntOrNull() ?: 32,
                        gestationalDays = gaDays.toIntOrNull() ?: 0,
                        birthWeightGrams = bKg * 1000.0,
                        currentWeightGrams = cKg * 1000.0,
                        dayOfLife = dol.toIntOrNull() ?: 1,
                        diagnosis = diagnosis,
                        clinicalNotes = notes
                    )
                    onSave(p)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text(text = if (isArabic) "حفظ السجل" else "Save Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )
}
