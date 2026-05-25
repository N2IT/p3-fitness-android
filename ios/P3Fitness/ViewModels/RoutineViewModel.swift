import Foundation
import SwiftData
import Observation

// MARK: - Supporting Types

struct WorkoutRoutineInfo: Identifiable {
    let id: Int
    let name: String
    let exerciseNames: [String]
    let exerciseCount: Int
    let timesPerformed: Int
    let lastPerformedAt: Date?
}

struct RoutineExerciseEdit: Identifiable {
    var id: Int
    var exerciseId: Int
    var exerciseName: String
    var orderIndex: Int
    var defaultSets: Int
    var defaultReps: Int
}

// MARK: - RoutineViewModel

@Observable
final class RoutineViewModel {
    // Routine list state
    var routines: [WorkoutRoutineInfo] = []
    var isLoading: Bool = false
    var errorMessage: String? = nil

    // Exercise library state
    var exercises: [Exercise] = []
    var filteredExercises: [Exercise] = []
    var searchQuery: String = "" {
        didSet { applyFilters() }
    }
    var selectedMuscleGroup: String? = nil {
        didSet { applyFilters() }
    }

    // Edit state (used by RoutineEditView)
    var editingRoutineName: String = ""
    var editingExercises: [RoutineExerciseEdit] = []

    // MARK: - Load Routines

    @MainActor
    func loadRoutines(userId: Int, context: ModelContext) async {
        isLoading = true
        do {
            let descriptor = FetchDescriptor<WorkoutRoutine>(
                predicate: #Predicate<WorkoutRoutine> { $0.userId == userId },
                sortBy: [SortDescriptor(\.createdAt, order: .reverse)]
            )
            let fetched = try context.fetch(descriptor)
            routines = fetched.map { routine in
                let names = routine.routineExercises
                    .sorted { $0.orderIndex < $1.orderIndex }
                    .map { $0.exerciseName }
                return WorkoutRoutineInfo(
                    id: routine.id,
                    name: routine.name,
                    exerciseNames: names,
                    exerciseCount: names.count,
                    timesPerformed: routine.timesPerformed,
                    lastPerformedAt: routine.lastPerformedAt
                )
            }
        } catch {
            errorMessage = "Failed to load routines: \(error.localizedDescription)"
        }
        isLoading = false
    }

    // MARK: - Load Exercises (library)

    @MainActor
    func loadExercises(context: ModelContext) async {
        do {
            let descriptor = FetchDescriptor<Exercise>(sortBy: [SortDescriptor(\.name)])
            exercises = try context.fetch(descriptor)
            applyFilters()
        } catch {
            errorMessage = "Failed to load exercises: \(error.localizedDescription)"
        }
    }

    private func applyFilters() {
        var result = exercises
        if let group = selectedMuscleGroup, !group.isEmpty {
            result = result.filter { $0.muscleGroup == group }
        }
        let query = searchQuery.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        if !query.isEmpty {
            result = result.filter {
                $0.name.lowercased().contains(query) ||
                $0.muscleGroup.lowercased().contains(query) ||
                $0.equipment.lowercased().contains(query)
            }
        }
        filteredExercises = result
    }

    // MARK: - Create Routine

    @MainActor
    @discardableResult
    func createRoutine(name: String, userId: Int, context: ModelContext) async -> Int {
        let routine = WorkoutRoutine(userId: userId, name: name)
        context.insert(routine)
        do {
            try context.save()
            await loadRoutines(userId: userId, context: context)
            return routine.id
        } catch {
            errorMessage = "Failed to create routine: \(error.localizedDescription)"
            return 0
        }
    }

    // MARK: - Delete Routine

    @MainActor
    func deleteRoutine(routineId: Int, context: ModelContext) async {
        do {
            let descriptor = FetchDescriptor<WorkoutRoutine>(
                predicate: #Predicate<WorkoutRoutine> { $0.id == routineId }
            )
            if let routine = try context.fetch(descriptor).first {
                context.delete(routine)
                try context.save()
            }
            routines.removeAll { $0.id == routineId }
        } catch {
            errorMessage = "Failed to delete routine: \(error.localizedDescription)"
        }
    }

    // MARK: - Load Routine For Edit (used by RoutineEditView)

    @MainActor
    func loadRoutineForEdit(routineId: Int, context: ModelContext) async {
        do {
            let descriptor = FetchDescriptor<WorkoutRoutine>(
                predicate: #Predicate<WorkoutRoutine> { $0.id == routineId }
            )
            guard let routine = try context.fetch(descriptor).first else { return }
            editingRoutineName = routine.name
            editingExercises = routine.routineExercises
                .sorted { $0.orderIndex < $1.orderIndex }
                .map { re in
                    RoutineExerciseEdit(
                        id: re.id,
                        exerciseId: re.exerciseId,
                        exerciseName: re.exerciseName,
                        orderIndex: re.orderIndex,
                        defaultSets: re.defaultSets,
                        defaultReps: re.defaultReps
                    )
                }
        } catch {
            errorMessage = "Failed to load routine for editing: \(error.localizedDescription)"
        }
    }

    // MARK: - Save Routine Edits (used by RoutineEditView)

    @MainActor
    func saveRoutineEdits(routineId: Int, name: String, exercises: [RoutineExerciseEdit], context: ModelContext) async {
        do {
            // Update routine name
            let routineDescriptor = FetchDescriptor<WorkoutRoutine>(
                predicate: #Predicate<WorkoutRoutine> { $0.id == routineId }
            )
            if let routine = try context.fetch(routineDescriptor).first {
                routine.name = name
            }

            // Delete old RoutineExercise records
            let reDescriptor = FetchDescriptor<RoutineExercise>(
                predicate: #Predicate<RoutineExercise> { $0.routineId == routineId }
            )
            let existing = try context.fetch(reDescriptor)
            for re in existing { context.delete(re) }

            // Insert updated list
            for (index, edit) in exercises.enumerated() {
                let re = RoutineExercise(
                    routineId: routineId,
                    exerciseId: edit.exerciseId,
                    exerciseName: edit.exerciseName,
                    orderIndex: index,
                    defaultSets: edit.defaultSets,
                    defaultReps: edit.defaultReps
                )
                context.insert(re)
            }

            try context.save()
        } catch {
            errorMessage = "Failed to save routine: \(error.localizedDescription)"
        }
    }

    // MARK: - Generic save (used by ImportViewModel)

    @MainActor
    func saveRoutineExercises(routineId: Int, exercises: [RoutineExerciseEdit], context: ModelContext) async {
        await saveRoutineEdits(routineId: routineId, name: editingRoutineName, exercises: exercises, context: context)
    }
}
