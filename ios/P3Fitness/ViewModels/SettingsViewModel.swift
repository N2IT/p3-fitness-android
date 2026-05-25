import Foundation
import SwiftData
import Observation

@Observable
final class SettingsViewModel {
    var username: String = ""
    var unitPreference: String = "lbs"
    var totalWorkouts: Int = 0
    var totalPRs: Int = 0
    var totalVolumeLifted: Double = 0
    var maxConsecutiveDays: Int = 0
    var memberSince: Date? = nil
    var isLoading: Bool = false
    var errorMessage: String? = nil

    // MARK: - Load User Data

    @MainActor
    func loadUserData(userId: Int, context: ModelContext) async {
        isLoading = true
        do {
            let descriptor = FetchDescriptor<User>(
                predicate: #Predicate<User> { $0.id == userId }
            )
            if let user = try context.fetch(descriptor).first {
                username = user.username
                unitPreference = user.unitPreference
                memberSince = user.createdAt
            }

            // Count unique workout sessions
            let logDescriptor = FetchDescriptor<ExerciseLog>(
                predicate: #Predicate<ExerciseLog> { $0.userId == userId }
            )
            let allLogs = try context.fetch(logDescriptor)
            totalWorkouts = Set(allLogs.map { $0.workoutSessionId }).count
        } catch {
            errorMessage = "Failed to load settings: \(error.localizedDescription)"
        }
        isLoading = false
    }

    // MARK: - Save Unit Preference

    @MainActor
    func saveUnitPreference(userId: Int, context: ModelContext) async {
        do {
            let descriptor = FetchDescriptor<User>(
                predicate: #Predicate<User> { $0.id == userId }
            )
            if let user = try context.fetch(descriptor).first {
                user.unitPreference = unitPreference
                try context.save()
            }
        } catch {
            errorMessage = "Failed to update unit preference: \(error.localizedDescription)"
        }
    }

    // MARK: - Update Username

    @MainActor
    func updateUsername(userId: Int, newUsername: String, context: ModelContext) async {
        let trimmed = newUsername.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else {
            errorMessage = "Username cannot be empty."
            return
        }
        do {
            let descriptor = FetchDescriptor<User>(
                predicate: #Predicate<User> { $0.id == userId }
            )
            if let user = try context.fetch(descriptor).first {
                user.username = trimmed
                try context.save()
                username = trimmed
            }
        } catch {
            errorMessage = "Failed to update username: \(error.localizedDescription)"
        }
    }

    // MARK: - Export Workout CSV

    @MainActor
    func exportWorkoutCSV(userId: Int, context: ModelContext) async -> URL? {
        do {
            let logDescriptor = FetchDescriptor<ExerciseLog>(
                predicate: #Predicate<ExerciseLog> { $0.userId == userId },
                sortBy: [SortDescriptor(\.completedAt)]
            )
            let logs = try context.fetch(logDescriptor)

            let exerciseDescriptor = FetchDescriptor<Exercise>()
            let exercises = try context.fetch(exerciseDescriptor)
            let exerciseNames: [Int: String] = Dictionary(
                uniqueKeysWithValues: exercises.map { ($0.id, $0.name) }
            )

            let routineDescriptor = FetchDescriptor<WorkoutRoutine>()
            let routines = try context.fetch(routineDescriptor)
            let routineNames: [Int: String] = Dictionary(
                uniqueKeysWithValues: routines.map { ($0.id, $0.name) }
            )

            var csv = "Date,Session ID,Routine,Exercise,Set,Weight (\(unitPreference)),Reps,Volume\n"
            for log in logs {
                let date = ISO8601DateFormatter().string(from: log.completedAt)
                let session = log.workoutSessionId
                let routine = routineNames[log.routineId] ?? "Unknown"
                let exercise = exerciseNames[log.exerciseId] ?? "Unknown"
                let volume = log.weight * Double(log.reps)
                csv += "\"\(date)\",\"\(session)\",\"\(routine)\",\"\(exercise)\",\(log.setNumber),\(log.weight),\(log.reps),\(String(format: "%.1f", volume))\n"
            }

            let formatter = DateFormatter()
            formatter.dateFormat = "yyyy-MM-dd"
            let dateStr = formatter.string(from: Date())
            let fileName = "P3Fitness_Export_\(dateStr).csv"
            let url = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
                .appendingPathComponent(fileName)

            try csv.write(to: url, atomically: true, encoding: .utf8)
            return url
        } catch {
            errorMessage = "Export failed: \(error.localizedDescription)"
            return nil
        }
    }

    // MARK: - Load Achievement Data

    @MainActor
    func loadAchievementData(userId: Int, context: ModelContext) async {
        await loadUserData(userId: userId, context: context)

        do {
            // Total PRs
            let prDescriptor = FetchDescriptor<PersonalRecord>(
                predicate: #Predicate<PersonalRecord> { $0.userId == userId }
            )
            totalPRs = try context.fetchCount(prDescriptor)

            // Total Volume
            let logDescriptor = FetchDescriptor<ExerciseLog>(
                predicate: #Predicate<ExerciseLog> { $0.userId == userId }
            )
            let allLogs = try context.fetch(logDescriptor)
            totalVolumeLifted = allLogs.reduce(0.0) { $0 + $1.weight * Double($1.reps) }

            // Max consecutive workout days
            let sessionDates: [Date] = Array(Set(allLogs.map { Calendar.current.startOfDay(for: $0.completedAt) })).sorted()
            var maxStreak = 0
            var currentStreak = 0
            var lastDate: Date? = nil
            for date in sessionDates {
                if let last = lastDate,
                   Calendar.current.dateComponents([.day], from: last, to: date).day == 1 {
                    currentStreak += 1
                    maxStreak = max(maxStreak, currentStreak)
                } else {
                    currentStreak = 1
                }
                lastDate = date
            }
            maxConsecutiveDays = maxStreak
        } catch {
            errorMessage = "Failed to load achievement data: \(error.localizedDescription)"
        }
    }

    // MARK: - Clear All Data

    @MainActor
    func clearAllData(userId: Int, context: ModelContext) async {
        do {
            let logDesc = FetchDescriptor<ExerciseLog>(
                predicate: #Predicate<ExerciseLog> { $0.userId == userId }
            )
            for log in try context.fetch(logDesc) { context.delete(log) }

            let prDesc = FetchDescriptor<PersonalRecord>(
                predicate: #Predicate<PersonalRecord> { $0.userId == userId }
            )
            for pr in try context.fetch(prDesc) { context.delete(pr) }

            let bwDesc = FetchDescriptor<BodyWeightEntry>(
                predicate: #Predicate<BodyWeightEntry> { $0.userId == userId }
            )
            for bw in try context.fetch(bwDesc) { context.delete(bw) }

            try context.save()
            await loadUserData(userId: userId, context: context)
        } catch {
            errorMessage = "Failed to clear data: \(error.localizedDescription)"
        }
    }
}
