import Foundation
import SwiftData
import Observation

// MARK: - Supporting Types

enum ImportMode: String, CaseIterable {
    case url  = "URL"
    case text = "Text"
}

enum ImportState {
    case idle
    case loading
    case preview(ImportedRoutine)
    case error(String)
    case success
}

// MARK: - ImportViewModel

@Observable
final class ImportViewModel {
    var inputText: String = ""
    var importMode: ImportMode = .url
    var importState: ImportState = .idle
    var routineName: String = ""

    // MARK: - Import from URL

    @MainActor
    func importFromURL(_ urlString: String, userId: Int, context: ModelContext) async {
        importState = .loading
        do {
            let apiKeyManager = APIKeyManager.shared
            let routine = try await RoutineImporter.importFromURL(urlString, apiKeyManager: apiKeyManager)
            importState = .preview(routine)
        } catch {
            importState = .error(error.localizedDescription)
        }
    }

    // MARK: - Import from Text

    @MainActor
    func importFromText(_ text: String, userId: Int, context: ModelContext) async {
        importState = .loading
        do {
            let apiKeyManager = APIKeyManager.shared
            let routine = try await RoutineImporter.importFromText(text, apiKeyManager: apiKeyManager)
            importState = .preview(routine)
        } catch {
            importState = .error(error.localizedDescription)
        }
    }

    // MARK: - Save Imported Routine

    @MainActor
    func saveImportedRoutine(routine: ImportedRoutine, customName: String, userId: Int, context: ModelContext) async {
        var finalRoutine = routine
        let trimmedName = customName.trimmingCharacters(in: .whitespacesAndNewlines)
        if !trimmedName.isEmpty {
            finalRoutine.name = trimmedName
        }

        let workoutRoutine = WorkoutRoutine(userId: userId, name: finalRoutine.name)
        context.insert(workoutRoutine)

        // Fetch all existing exercises for name-matching
        let allExDescriptor = FetchDescriptor<Exercise>()
        let allExercises = (try? context.fetch(allExDescriptor)) ?? []

        for (index, importedEx) in finalRoutine.exercises.enumerated() {
            let matchedExercise = allExercises.first {
                $0.name.lowercased() == importedEx.name.lowercased()
            }

            let exerciseId: Int
            let exerciseName: String

            if let existing = matchedExercise {
                exerciseId = existing.id
                exerciseName = existing.name
            } else {
                // Create a new custom exercise
                let newId = abs(Int(Date().timeIntervalSince1970 * 1000) % 1_000_000_000) + index + 1000
                let newEx = Exercise(
                    id: newId,
                    name: importedEx.name,
                    muscleGroup: validatedMuscleGroup(importedEx.muscleGroup),
                    equipment: validatedEquipment(importedEx.equipment),
                    isCustom: true
                )
                context.insert(newEx)
                exerciseId = newId
                exerciseName = importedEx.name
            }

            let re = RoutineExercise(
                routineId: workoutRoutine.id,
                exerciseId: exerciseId,
                exerciseName: exerciseName,
                orderIndex: index,
                defaultSets: max(1, importedEx.sets),
                defaultReps: max(1, importedEx.reps)
            )
            context.insert(re)
        }

        do {
            try context.save()
            importState = .success
        } catch {
            importState = .error("Failed to save routine: \(error.localizedDescription)")
        }
    }

    // MARK: - Reset

    func reset() {
        inputText = ""
        importState = .idle
        routineName = ""
    }

    // MARK: - Helpers

    private func validatedMuscleGroup(_ group: String) -> String {
        Exercise.muscleGroups.first { $0.lowercased() == group.lowercased() } ?? "Other"
    }

    private func validatedEquipment(_ equipment: String) -> String {
        Exercise.equipmentTypes.first { $0.lowercased() == equipment.lowercased() } ?? "Bodyweight"
    }
}
