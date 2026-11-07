package com.example.data.repository

import com.example.data.local.PatientDao
import com.example.data.model.NeonatePatient
import kotlinx.coroutines.flow.Flow

class PatientRepository(private val patientDao: PatientDao) {
    val allPatients: Flow<List<NeonatePatient>> = patientDao.getAllPatients()

    suspend fun getPatientById(id: Long): NeonatePatient? = patientDao.getPatientById(id)

    suspend fun insertPatient(patient: NeonatePatient): Long = patientDao.insertPatient(patient)

    suspend fun updatePatient(patient: NeonatePatient) = patientDao.updatePatient(patient)

    suspend fun deletePatient(patient: NeonatePatient) = patientDao.deletePatient(patient)
}
