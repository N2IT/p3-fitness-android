import SwiftData
import Foundation

@Model
final class RoutineExercise {
    var id: Int
    var routineId: Int
    var exerciseId: Int
    var exerciseName: String
    var orderIndex: Int
    var defaultSets: Int
    var defaultReps: Int

    init(routineId: Int, exerciseId: Int, exerciseName: String, orderIndex: Int, defaultSets: Int = 3, defaultReps: Int = 10) {
        self.id = Int(Date().timeIntervalSince1970 * 1000) % 1_000_000_000
        self.routineId = routineId
        self.exerciseId = exerciseId
        self.exerciseName = exerciseName
        self.orderIndex = orderIndex
        self.defaultSets = defaultSets
        self.defaultReps = defaultReps
    }
}
