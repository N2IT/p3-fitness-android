import Foundation

struct PRResult {
    let recordType: String
    let newValue: Double
    let previousValue: Double

    var improvementPercent: Double {
        guard previousValue > 0 else { return 0 }
        return ((newValue - previousValue) / previousValue) * 100
    }

    var displayLabel: String {
        switch recordType {
        case "max_weight":  return "New Max Weight PR"
        case "max_reps":    return "New Max Reps PR"
        case "max_volume":  return "New Volume PR"
        case "est_1rm":     return "New Estimated 1RM PR"
        default:            return "New Personal Record"
        }
    }

    var formattedValue: String {
        switch recordType {
        case "max_reps": return "\(Int(newValue)) reps"
        default:         return String(format: "%.1f", newValue)
        }
    }
}

enum PRDetector {
    // MARK: - Main check

    /// Checks all four PR types for a single completed set.
    /// - Parameters:
    ///   - exerciseId: ID of the exercise being logged.
    ///   - userId: ID of the current user.
    ///   - weight: Weight used for the set.
    ///   - reps: Reps completed for the set.
    ///   - existingRecords: All previously stored PersonalRecord objects for this (exerciseId, userId) pair.
    /// - Returns: Array of PRResult objects for each broken record (may be empty).
    static func checkPRs(
        exerciseId: Int,
        userId: Int,
        weight: Double,
        reps: Int,
        existingRecords: [PersonalRecord]
    ) -> [PRResult] {
        var results: [PRResult] = []

        // Helper: find existing record value for a given type
        func existing(_ type: String) -> Double? {
            existingRecords
                .filter { $0.recordType == type && $0.exerciseId == exerciseId && $0.userId == userId }
                .max(by: { $0.value < $1.value })
                .map { $0.value }
        }

        // 1. Max Weight
        let maxWeightPrev = existing("max_weight") ?? 0
        if weight > maxWeightPrev {
            results.append(PRResult(recordType: "max_weight", newValue: weight, previousValue: maxWeightPrev))
        }

        // 2. Max Reps
        let maxRepsPrev = existing("max_reps") ?? 0
        let repsDouble = Double(reps)
        if repsDouble > maxRepsPrev {
            results.append(PRResult(recordType: "max_reps", newValue: repsDouble, previousValue: maxRepsPrev))
        }

        // 3. Max Volume (weight × reps in a single set)
        let volume = weight * Double(reps)
        let maxVolumePrev = existing("max_volume") ?? 0
        if volume > maxVolumePrev {
            results.append(PRResult(recordType: "max_volume", newValue: volume, previousValue: maxVolumePrev))
        }

        // 4. Estimated 1RM — Epley formula: weight * (1 + reps / 30)
        let est1RM = weight * (1.0 + Double(reps) / 30.0)
        let est1RMPrev = existing("est_1rm") ?? 0
        if est1RM > est1RMPrev {
            results.append(PRResult(recordType: "est_1rm", newValue: est1RM, previousValue: est1RMPrev))
        }

        return results
    }

    // MARK: - Convenience: most impressive PR from a set of results

    static func primaryPR(from results: [PRResult]) -> PRResult? {
        // Priority: est_1rm > max_weight > max_volume > max_reps
        let priority = ["est_1rm", "max_weight", "max_volume", "max_reps"]
        for type in priority {
            if let pr = results.first(where: { $0.recordType == type }) {
                return pr
            }
        }
        return results.first
    }
}
