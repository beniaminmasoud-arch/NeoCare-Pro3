package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.NeonatePatient
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Query("SELECT * FROM neonate_patients ORDER BY createdAt DESC")
    fun getAllPatients(): Flow<List<NeonatePatient>>

    @Query("SELECT * FROM neonate_patients WHERE id = :id LIMIT 1")
    suspend fun getPatientById(id: Long): NeonatePatient?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: NeonatePatient): Long

    @Update
    suspend fun updatePatient(patient: NeonatePatient)

    @Delete
    suspend fun deletePatient(patient: NeonatePatient)

    @Query("DELETE FROM neonate_patients")
    suspend fun clearAll()
}
