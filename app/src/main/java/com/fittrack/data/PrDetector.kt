package com.fittrack.data

import com.fittrack.data.dao.PersonalRecordDao
import com.fittrack.data.entity.PersonalRecord

data class NewPr(
    val exerciseId: Int,
    val recordType: String,
    val oldValue: Double?,
    val newValue: Double
)

class PrDetector(private val personalRecordDao: PersonalRecordDao) {

    suspend fun checkForPrs(
        exerciseId: Int,
        userId: Int,
        weight: Double,
        reps: Int,
        workoutSessionId: String
    ): List<NewPr> {
        if (weight <= 0 || reps <= 0) return emptyList()

        val prs = mutableListOf<NewPr>()

        // Max weight
        val currentMaxWeight = personalRecordDao.getBestRecord(exerciseId, userId, "max_weight")
        if (currentMaxWeight == null || weight > currentMaxWeight.value) {
            prs.add(NewPr(exerciseId, "max_weight", currentMaxWeight?.value, weight))
            personalRecordDao.insert(
                PersonalRecord(
                    exerciseId = exerciseId, userId = userId,
                    recordType = "max_weight", value = weight,
                    weight = weight, reps = reps,
                    workoutSessionId = workoutSessionId
                )
            )
        }

        // Max reps at this weight or higher
        val currentMaxReps = personalRecordDao.getBestRecord(exerciseId, userId, "max_reps")
        if (currentMaxReps == null || reps > currentMaxReps.value.toInt()) {
            prs.add(NewPr(exerciseId, "max_reps", currentMaxReps?.value, reps.toDouble()))
            personalRecordDao.insert(
                PersonalRecord(
                    exerciseId = exerciseId, userId = userId,
                    recordType = "max_reps", value = reps.toDouble(),
                    weight = weight, reps = reps,
                    workoutSessionId = workoutSessionId
                )
            )
        }

        // Volume (weight × reps)
        val volume = weight * reps
        val currentMaxVolume = personalRecordDao.getBestRecord(exerciseId, userId, "max_volume")
        if (currentMaxVolume == null || volume > currentMaxVolume.value) {
            prs.add(NewPr(exerciseId, "max_volume", currentMaxVolume?.value, volume))
            personalRecordDao.insert(
                PersonalRecord(
                    exerciseId = exerciseId, userId = userId,
                    recordType = "max_volume", value = volume,
                    weight = weight, reps = reps,
                    workoutSessionId = workoutSessionId
                )
            )
        }

        // Estimated 1RM (Epley formula)
        val est1rm = if (reps == 1) weight else weight * (1 + reps / 30.0)
        val currentMax1rm = personalRecordDao.getBestRecord(exerciseId, userId, "est_1rm")
        if (currentMax1rm == null || est1rm > currentMax1rm.value) {
            prs.add(NewPr(exerciseId, "est_1rm", currentMax1rm?.value, est1rm))
            personalRecordDao.insert(
                PersonalRecord(
                    exerciseId = exerciseId, userId = userId,
                    recordType = "est_1rm", value = est1rm,
                    weight = weight, reps = reps,
                    workoutSessionId = workoutSessionId
                )
            )
        }

        return prs
    }
}
