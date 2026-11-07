package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "neonate_patients")
data class NeonatePatient(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val incubatorBed: String = "",
    val gestationalWeeks: Int = 32,
    val gestationalDays: Int = 0,
    val birthWeightGrams: Double = 1500.0,
    val currentWeightGrams: Double = 1500.0,
    val dayOfLife: Int = 1,
    val ageMonths: Int = 0,
    val ageYears: Int = 0,
    val diagnosis: String = "",
    val clinicalNotes: String = "",
    val isMale: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val weightKg: Double get() = currentWeightGrams / 1000.0
    val birthWeightKg: Double get() = birthWeightGrams / 1000.0

    val isNeonate: Boolean get() = (ageYears == 0 && ageMonths == 0 && dayOfLife <= 28)

    val ageDisplayEn: String
        get() = when {
            ageYears > 0 -> "$ageYears y" + if (ageMonths > 0) " $ageMonths m" else ""
            ageMonths > 0 -> "$ageMonths months" + if (dayOfLife > 0) " $dayOfLife d" else ""
            else -> "$dayOfLife days"
        }

    val ageDisplayAr: String
        get() = when {
            ageYears > 0 -> "$ageYears سنة" + if (ageMonths > 0) " و $ageMonths شهر" else ""
            ageMonths > 0 -> "$ageMonths شهر" + if (dayOfLife > 0) " و $dayOfLife يوم" else ""
            else -> "$dayOfLife يوم"
        }

    // Weight loss/gain percentage from birth weight
    val weightChangePercentage: Double
        get() = if (birthWeightGrams > 0) {
            ((currentWeightGrams - birthWeightGrams) / birthWeightGrams) * 100.0
        } else 0.0

    // Corrected Gestational Age (CGA) in weeks + days
    val correctedAgeWeeks: Int
        get() {
            val totalDays = (gestationalWeeks * 7) + gestationalDays + dayOfLife
            return totalDays / 7
        }

    val correctedAgeDays: Int
        get() {
            val totalDays = (gestationalWeeks * 7) + gestationalDays + dayOfLife
            return totalDays % 7
        }
}
