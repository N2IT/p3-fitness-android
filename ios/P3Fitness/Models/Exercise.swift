import SwiftData
import Foundation

@Model
final class Exercise {
    var id: Int
    var name: String
    var muscleGroup: String
    var equipment: String
    var isCustom: Bool

    @Relationship(deleteRule: .cascade) var logs: [ExerciseLog] = []
    @Relationship(deleteRule: .cascade) var personalRecords: [PersonalRecord] = []

    static let muscleGroups = ["Chest", "Back", "Shoulders", "Arms", "Legs", "Core", "Other"]
    static let equipmentTypes = ["Barbell", "Dumbbell", "Cable", "Machine", "Bodyweight"]

    init(id: Int, name: String, muscleGroup: String, equipment: String, isCustom: Bool = false) {
        self.id = id
        self.name = name
        self.muscleGroup = muscleGroup
        self.equipment = equipment
        self.isCustom = isCustom
    }
}
