import SwiftData
import Foundation

@Model
final class User {
    var id: Int
    var username: String
    var unitPreference: String // "lbs" or "kg"
    var createdAt: Date

    @Relationship(deleteRule: .cascade) var routines: [WorkoutRoutine] = []
    @Relationship(deleteRule: .cascade) var bodyWeightEntries: [BodyWeightEntry] = []

    init(username: String, unitPreference: String = "lbs") {
        self.id = Int(Date().timeIntervalSince1970 * 1000) % 1_000_000
        self.username = username
        self.unitPreference = unitPreference
        self.createdAt = Date()
    }
}
