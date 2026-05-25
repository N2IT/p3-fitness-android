import SwiftData
import Foundation

@Model
final class PersonalRecord {
    var id: Int
    var exerciseId: Int
    var userId: Int
    var recordType: String // "max_weight", "max_reps", "max_volume", "est_1rm"
    var value: Double
    var weight: Double
    var reps: Int
    var workoutSessionId: String
    var achievedAt: Date

    init(exerciseId: Int, userId: Int, recordType: String, value: Double, weight: Double, reps: Int, workoutSessionId: String) {
        self.id = Int(Date().timeIntervalSince1970 * 1000000) % 1_000_000_000
        self.exerciseId = exerciseId
        self.userId = userId
        self.recordType = recordType
        self.value = value
        self.weight = weight
        self.reps = reps
        self.workoutSessionId = workoutSessionId
        self.achievedAt = Date()
    }
}
