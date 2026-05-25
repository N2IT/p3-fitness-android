import SwiftData
import Foundation

@Model
final class BodyWeightEntry {
    var id: Int
    var userId: Int
    var weight: Double
    var loggedAt: Date

    init(userId: Int, weight: Double) {
        self.id = Int(Date().timeIntervalSince1970 * 1000) % 1_000_000_000
        self.userId = userId
        self.weight = weight
        self.loggedAt = Date()
    }
}
