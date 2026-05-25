import SwiftData
import Foundation

@Model
final class ExerciseLog {
    var id: Int
    var exerciseId: Int
    var routineId: Int
    var userId: Int
    var workoutSessionId: String
    var setNumber: Int
    var weight: Double
    var reps: Int
    var completedAt: Date

    init(exerciseId: Int, routineId: Int, userId: Int, workoutSessionId: String, setNumber: Int, weight: Double, reps: Int) {
        self.id = Int(Date().timeIntervalSince1970 * 1000000) % 1_000_000_000
        self.exerciseId = exerciseId
        self.routineId = routineId
        self.userId = userId
        self.workoutSessionId = workoutSessionId
        self.setNumber = setNumber
        self.weight = weight
        self.reps = reps
        self.completedAt = Date()
    }
}
