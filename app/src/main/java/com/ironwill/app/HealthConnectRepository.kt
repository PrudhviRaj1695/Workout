package com.ironwill.app

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class DailyHealthReport(
    val date: LocalDate,
    val steps: Long?,
    val distanceKm: Double?,
    val activeCalories: Double?,
    val totalCaloriesBurned: Double?,
    val sleepHours: Double?,
    val latestWeightKg: Double?,
    val workouts: Int?,
    val missingSources: List<String>
)

object HealthConnectRepository {
    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class)
    )

    suspend fun loadToday(context: Context): DailyHealthReport {
        val client = HealthConnectClient.getOrCreate(context)
        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val start = today.atStartOfDay(zone).toInstant()
        val end = now
        val missing = mutableListOf<String>()

        val steps = try {
            client.readRecords(ReadRecordsRequest(StepsRecord::class, TimeRangeFilter.between(start, end)))
                .records.sumOf { it.count }
        } catch (_: Exception) { missing.add("Steps"); null }

        val distance = try {
            client.readRecords(ReadRecordsRequest(DistanceRecord::class, TimeRangeFilter.between(start, end)))
                .records.sumOf { it.distance.inMeters } / 1000.0
        } catch (_: Exception) { missing.add("Distance"); null }

        val activeCalories = try {
            client.readRecords(ReadRecordsRequest(ActiveCaloriesBurnedRecord::class, TimeRangeFilter.between(start, end)))
                .records.sumOf { it.energy.inKilocalories }
        } catch (_: Exception) { missing.add("Active calories"); null }

        val totalCalories = try {
            client.readRecords(ReadRecordsRequest(TotalCaloriesBurnedRecord::class, TimeRangeFilter.between(start, end)))
                .records.sumOf { it.energy.inKilocalories }
        } catch (_: Exception) { missing.add("Total calories"); null }

        val sleep = try {
            val yesterday = start.minus(Duration.ofDays(1))
            val records = client.readRecords(ReadRecordsRequest(SleepSessionRecord::class, TimeRangeFilter.between(yesterday, end))).records
            records.filter { it.endTime <= end && it.endTime >= start.minus(Duration.ofHours(16)) }
                .sumOf { Duration.between(it.startTime, it.endTime).toMinutes() } / 60.0
        } catch (_: Exception) { missing.add("Sleep"); null }

        val weight = try {
            val from = now.minus(Duration.ofDays(30))
            client.readRecords(ReadRecordsRequest(WeightRecord::class, TimeRangeFilter.between(from, end)))
                .records.maxByOrNull { it.time }?.weight?.inKilograms
        } catch (_: Exception) { missing.add("Weight"); null }

        val workouts = try {
            client.readRecords(ReadRecordsRequest(ExerciseSessionRecord::class, TimeRangeFilter.between(start, end)))
                .records.size
        } catch (_: Exception) { missing.add("Workouts"); null }

        return DailyHealthReport(
            date = today, steps = steps, distanceKm = distance, activeCalories = activeCalories, totalCaloriesBurned = totalCalories,
            sleepHours = sleep, latestWeightKg = weight, workouts = workouts, missingSources = missing
        )
    }
}
