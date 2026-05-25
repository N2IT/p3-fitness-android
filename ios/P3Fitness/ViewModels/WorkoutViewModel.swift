import Foundation
import SwiftData
import Observation

// MARK: - Supporting Types

struct WorkoutSet: Identifiable {
    let id = UUID()
    var weight: Double
    var reps: Int
    var isCompleted: Bool
    var isPR: Bool
}

struct WorkoutExerciseSection: Identifiable {
    let id: Int // exerciseId
    let exerciseName: String
    var sets: [WorkoutSet]
}

struct PRCelebrationData {
    let exerciseName: String
    let recordType: String
    let value: Double

    var displayLabel: String {
        switch recordType {
        case "max_weight": return "New Max Weight"
        case "max_reps":   return "New Max Reps"
        case "max_volume": return "New Volume PR"
        case "est_1rm":    return "New Est. 1RM"
        default:           return "Personal Record"
        }
    }

    var formattedValue: String {
        if recordType == "max_reps" {
            return "\(Int(value)) reps"
        }
        return String(format: "%.1f", value)
    }
}

// MARK: - WorkoutViewModel

@Observable
final class WorkoutViewModel {

    // MARK: - State
    var routineName: String = ""
    var exerciseSections: [WorkoutExerciseSection] = []
    var currentWorkoutSessionId: String = UUID().uuidString
    var isFinished: Bool = false
    var isLoading: Bool = false
    var errorMessage: String? = nil

    private var routineIdForSave: Int = 0
    private var userIdForSave: Int = 0

    // MARK: - Load Workout

    @MainActor
    func loadWorkout(routineId: Int, userId: Int, context: ModelContext) async {
        isLoading = true
        routineIdForSave = routineId
        userIdForSave = userId
        currentWorkoutSessionId = UUID().uuidString
        exerciseSections = []

        do {
            let descriptor = FetchDescriptor<WorkoutRoutine>(
                predicate: #Predicate<WorkoutRoutine> { $0.id == routineId }
            )
            guard let routine = try context.fetch(descriptor).first else {
                errorMessage = "Routine not found."
                isLoading = false
                return
            }

            routineName = routine.name
            let sorted = routine.routineExercises.sorted { $0.orderIndex < $1.orderIndex }

            var sections: [WorkoutExerciseSection] = []
            for re in sorted {
                let lastWeight = await lastWeightUsed(exerciseId: re.exerciseId, userId: userId, context: context)
                let sets = (0..<re.defaultSets).map { _ in
                    WorkoutSet(weight: lastWeight, reps: re.defaultReps, isCompleted: false, isPR: false)
                }
                sections.append(WorkoutExerciseSection(
                    id: re.exerciseId,
                    exerciseName: re.exerciseName,
                    sets: sets
                ))
            }
            exerciseSections = sections
        } catch {
            errorMessage = "Failed to load routine: \(error.localizedDescription)"
        }

        isLoading = false
    }

    // MARK: - Set Weight / Reps by UUID

    func updateSetWeight(exerciseIndex: Int, setId: UUID, weight: Double) {
        guard exerciseIndex < exerciseSections.count,
              let setIdx = exerciseSections[exerciseIndex].sets.firstIndex(where: { $0.id == setId }) else { return }
        exerciseSections[exerciseIndex].sets[setIdx].weight = weight
    }

    func updateSetReps(exerciseIndex: Int, setId: UUID, reps: Int) {
        guard exerciseIndex < exerciseSections.count,
              let setIdx = exerciseSections[exerciseIndex].sets.firstIndex(where: { $0.id == setId }) else { return }
        exerciseSections[exerciseIndex].sets[setIdx].reps = reps
    }

    func addSet(exerciseIndex: Int) {
        guard exerciseIndex < exerciseSections.count else { return }
        let lastSet = exerciseSections[exerciseIndex].sets.last
        let newSet = WorkoutSet(
            weight: lastSet?.weight ?? 0,
            reps: lastSet?.reps ?? 10,
            isCompleted: false,
            isPR: false
        )
        exerciseSections[exerciseIndex].sets.append(newSet)
    }

    func removeSet(exerciseIndex: Int) {
        guard exerciseIndex < exerciseSections.count,
              exerciseSections[exerciseIndex].sets.count > 1 else { return }
        exerciseSections[exerciseIndex].sets.removeLast()
    }

    // MARK: - Toggle Set Complete (returns PRCelebrationData if PR hit)

    @MainActor
    func toggleSetComplete(exerciseIndex: Int, setId: UUID, context: ModelContext) async -> PRCelebrationData? {
        guard exerciseIndex < exerciseSections.count,
              let setIdx = exerciseSections[exerciseIndex].sets.firstIndex(where: { $0.id == setId }) else { return nil }

        let wasCompleted = exerciseSections[exerciseIndex].sets[setIdx].isCompleted
        exerciseSections[exerciseIndex].sets[setIdx].isCompleted = !wasCompleted

        guard exerciseSections[exerciseIndex].sets[setIdx].isCompleted else { return nil }

        let section = exerciseSections[exerciseIndex]
        let set = section.sets[setIdx]
        let exerciseId = section.id
        let userId = userIdForSave

        // Log the completed set
        let log = ExerciseLog(
            exerciseId: exerciseId,
            routineId: routineIdForSave,
            userId: userId,
            workoutSessionId: currentWorkoutSessionId,
            setNumber: setIdx + 1,
            weight: set.weight,
            reps: set.reps
        )
        context.insert(log)
        try? context.save()

        // Check for PRs
        let prResult = await checkAndSavePR(
            exerciseId: exerciseId,
            exerciseName: section.exerciseName,
            exerciseIndex: exerciseIndex,
            setIdx: setIdx,
            weight: set.weight,
            reps: set.reps,
            userId: userId,
            context: context
        )
        return prResult
    }

    // MARK: - PR Detection

    @MainActor
    private func checkAndSavePR(
        exerciseId: Int,
        exerciseName: String,
        exerciseIndex: Int,
        setIdx: Int,
        weight: Double,
        reps: Int,
        userId: Int,
        context: ModelContext
    ) async -> PRCelebrationData? {
        do {
            let descriptor = FetchDescriptor<PersonalRecord>(
                predicate: #Predicate<PersonalRecord> {
                    $0.exerciseId == exerciseId && $0.userId == userId
                }
            )
            let existingRecords = try context.fetch(descriptor)
            let prResults = PRDetector.checkPRs(
                exerciseId: exerciseId,
                userId: userId,
                weight: weight,
                reps: reps,
                existingRecords: existingRecords
            )

            guard !prResults.isEmpty else { return nil }

            // Mark set as PR
            exerciseSections[exerciseIndex].sets[setIdx].isPR = true

            // Save new PR records (replace old ones of same type)
            for pr in prResults {
                let oldDesc = FetchDescriptor<PersonalRecord>(
                    predicate: #Predicate<PersonalRecord> {
                        $0.exerciseId == exerciseId &&
                        $0.userId == userId &&
                        $0.recordType == pr.recordType
                    }
                )
                let old = try context.fetch(oldDesc)
                for record in old { context.delete(record) }

                let newPR = PersonalRecord(
                    exerciseId: exerciseId,
                    userId: userId,
                    recordType: pr.recordType,
                    value: pr.newValue,
                    weight: weight,
                    reps: reps,
                    workoutSessionId: currentWorkoutSessionId
                )
                context.insert(newPR)
            }
            try context.save()

            // Return the primary PR for celebration
            if let primary = PRDetector.primaryPR(from: prResults) {
                return PRCelebrationData(
                    exerciseName: exerciseName,
                    recordType: primary.recordType,
                    value: primary.newValue
                )
            }
            return nil
        } catch {
            print("PR check error: \(error)")
            return nil
        }
    }

    // MARK: - Finish Workout

    @MainActor
    func finishWorkout(durationMinutes: Int, context: ModelContext) async {
        do {
            let routineId = routineIdForSave
            let descriptor = FetchDescriptor<WorkoutRoutine>(
                predicate: #Predicate<WorkoutRoutine> { $0.id == routineId }
            )
            if let routine = try context.fetch(descriptor).first {
                routine.timesPerformed += 1
                routine.lastPerformedAt = Date()
            }
            try context.save()
        } catch {
            errorMessage = "Failed to save workout: \(error.localizedDescription)"
        }
        isFinished = true
    }

    // MARK: - Helpers

    private func lastWeightUsed(exerciseId: Int, userId: Int, context: ModelContext) async -> Double {
        do {
            var descriptor = FetchDescriptor<ExerciseLog>(
                predicate: #Predicate<ExerciseLog> { $0.exerciseId == exerciseId && $0.userId == userId },
                sortBy: [SortDescriptor(\.completedAt, order: .reverse)]
            )
            descriptor.fetchLimit = 1
            return try context.fetch(descriptor).first?.weight ?? 0
        } catch {
            return 0
        }
    }

    var completedSetsCount: Int {
        exerciseSections.flatMap { $0.sets }.filter { $0.isCompleted }.count
    }

    var totalSetsCount: Int {
        exerciseSections.flatMap { $0.sets }.count
    }

    var completionPercent: Double {
        guard totalSetsCount > 0 else { return 0 }
        return Double(completedSetsCount) / Double(totalSetsCount)
    }
}
