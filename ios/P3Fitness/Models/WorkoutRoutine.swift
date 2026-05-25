import SwiftData
import Foundation

@Model
final class WorkoutRoutine {
    var id: Int
    var userId: Int
    var name: String
    var lastPerformedAt: Date?
    var timesPerformed: Int
    var createdAt: Date

    @Relationship(deleteRule: .cascade) var routineExercises: [RoutineExercise] = []

    init(userId: Int, name: String) {
        self.id = Int(Date().timeIntervalSince1970 * 1000) % 1_000_000_000
        self.userId = userId
        self.name = name
        self.timesPerformed = 0
        self.createdAt = Date()
    }
}
