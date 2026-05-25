import Foundation
import SwiftData
import Observation

// MARK: - Supporting Types

struct ProgressDataPoint: Identifiable {
    let id = UUID()
    let date: Date
    let value: Double
}

enum ProgressMetric: String, CaseIterable {
    case maxWeight      = "Max Weight"
    case totalVolume    = "Total Volume"
    case estimatedOneRM = "Est. 1RM"
}

enum TimeRange: String, CaseIterable {
    case oneMonth    = "1M"
    case threeMonths = "3M"
    case sixMonths   = "6M"
    case oneYear     = "1Y"
    case all         = "All"

    var days: Int? {
        switch self {
        case .oneMonth:    return 30
        case .threeMonths: return 90
        case .sixMonths:   return 180
        case .oneYear:     return 365
        case .all:         return nil
        }
    }
}

// MARK: - ProgressViewModel

@Observable
final class ProgressViewModel {
    // Exercise selection
    var allExercises: [Exercise] = []
    var selectedExercise: Exercise? = nil

    // Chart config
    var selectedMetric: ProgressMetric = .maxWeight
    var selectedTimeRange: TimeRange = .threeMonths

    // Chart data
    var chartData: [ProgressDataPoint] = []
    var personalRecords: [PersonalRecord] = []
    var isLoadingChart: Bool = false

    // Body weight
    var bodyWeightEntries: [BodyWeightEntry] = []

    // Settings pass-through
    var unitPreference: String = "lbs"

    // Cached userId for internal use
    private var cachedUserId: Int = 0

    // Trend calculation
    var trendChange: Double? {
        guard chartData.count >= 2,
              let first = chartData.first?.value,
              let last = chartData.last?.value,
              first > 0 else { return nil }
        return ((last - first) / first) * 100
    }

    var errorMessage: String? = nil

    // MARK: - Load All Data

    @MainActor
    func loadData(userId: Int, context: ModelContext) async {
        cachedUserId = userId
        await loadExercises(context: context)
        await loadBodyWeightEntries(userId: userId, context: context)
        await loadUnitPreference(userId: userId, context: context)
    }

    // MARK: - Load Exercises

    @MainActor
    func loadExercises(context: ModelContext) async {
        do {
            let descriptor = FetchDescriptor<Exercise>(sortBy: [SortDescriptor(\.name)])
            allExercises = try context.fetch(descriptor)
        } catch {
            errorMessage = "Failed to load exercises: \(error.localizedDescription)"
        }
    }

    // MARK: - Select Exercise

    @MainActor
    func selectExercise(_ exercise: Exercise, context: ModelContext) async {
        let userId = cachedUserId
        guard userId > 0 else { return }
        selectedExercise = exercise
        await reloadChart(userId: userId, context: context)
    }

    // Used when userId is known
    @MainActor
    func selectExercise(_ exercise: Exercise, userId: Int, context: ModelContext) async {
        selectedExercise = exercise
        await reloadChart(userId: userId, context: context)
    }

    // MARK: - Reload Chart

    @MainActor
    func reloadChart(context: ModelContext) async {
        let userId = cachedUserId
        guard userId > 0 else { return }
        await reloadChart(userId: userId, context: context)
    }

    @MainActor
    func reloadChart(userId: Int, context: ModelContext) async {
        guard let exercise = selectedExercise else { return }
        isLoadingChart = true

        do {
            let exerciseId = exercise.id
            let logDescriptor = FetchDescriptor<ExerciseLog>(
                predicate: #Predicate<ExerciseLog> { $0.exerciseId == exerciseId && $0.userId == userId },
                sortBy: [SortDescriptor(\.completedAt)]
            )
            let allLogs = try context.fetch(logDescriptor)

            let prDescriptor = FetchDescriptor<PersonalRecord>(
                predicate: #Predicate<PersonalRecord> { $0.exerciseId == exerciseId && $0.userId == userId }
            )
            personalRecords = try context.fetch(prDescriptor)

            // Apply time range filter
            let cutoff: Date
            if let days = selectedTimeRange.days {
                cutoff = Calendar.current.date(byAdding: .day, value: -days, to: Date()) ?? Date.distantPast
            } else {
                cutoff = Date.distantPast
            }
            let filtered = allLogs.filter { $0.completedAt >= cutoff }

            // Group by session
            var sessionGroups: [String: [ExerciseLog]] = [:]
            for log in filtered {
                sessionGroups[log.workoutSessionId, default: []].append(log)
            }

            var points: [ProgressDataPoint] = []
            for (_, logs) in sessionGroups {
                guard let representativeDate = logs.first?.completedAt else { continue }
                let value = computeMetricValue(for: logs, metric: selectedMetric)
                if value > 0 {
                    points.append(ProgressDataPoint(date: representativeDate, value: value))
                }
            }
            chartData = points.sorted { $0.date < $1.date }
        } catch {
            errorMessage = "Failed to load chart data: \(error.localizedDescription)"
        }

        isLoadingChart = false
    }

    // MARK: - Body Weight

    @MainActor
    func loadBodyWeightEntries(userId: Int, context: ModelContext) async {
        do {
            let descriptor = FetchDescriptor<BodyWeightEntry>(
                predicate: #Predicate<BodyWeightEntry> { $0.userId == userId },
                sortBy: [SortDescriptor(\.loggedAt, order: .reverse)]
            )
            bodyWeightEntries = try context.fetch(descriptor)
        } catch {
            errorMessage = "Failed to load body weight: \(error.localizedDescription)"
        }
    }

    @MainActor
    func logBodyWeight(userId: Int, weight: Double, context: ModelContext) async {
        let entry = BodyWeightEntry(userId: userId, weight: weight)
        context.insert(entry)
        do {
            try context.save()
            await loadBodyWeightEntries(userId: userId, context: context)
        } catch {
            errorMessage = "Failed to save body weight: \(error.localizedDescription)"
        }
    }

    @MainActor
    func deleteBodyWeightEntry(id: Int, context: ModelContext) async {
        let userId = cachedUserId
        do {
            let descriptor = FetchDescriptor<BodyWeightEntry>(
                predicate: #Predicate<BodyWeightEntry> { $0.id == id }
            )
            if let entry = try context.fetch(descriptor).first {
                context.delete(entry)
                try context.save()
                await loadBodyWeightEntries(userId: userId, context: context)
            }
        } catch {
            errorMessage = "Failed to delete entry: \(error.localizedDescription)"
        }
    }

    // MARK: - Unit Preference

    @MainActor
    private func loadUnitPreference(userId: Int, context: ModelContext) async {
        do {
            let descriptor = FetchDescriptor<User>(
                predicate: #Predicate<User> { $0.id == userId }
            )
            if let user = try context.fetch(descriptor).first {
                unitPreference = user.unitPreference
            }
        } catch { /* non-fatal */ }
    }

    // MARK: - Metric Computation

    private func computeMetricValue(for logs: [ExerciseLog], metric: ProgressMetric) -> Double {
        switch metric {
        case .maxWeight:
            return logs.map { $0.weight }.max() ?? 0
        case .totalVolume:
            return logs.reduce(0) { $0 + $1.weight * Double($1.reps) }
        case .estimatedOneRM:
            return logs.map { log -> Double in
                guard log.reps > 0, log.weight > 0 else { return 0 }
                return log.weight * (1.0 + Double(log.reps) / 30.0)
            }.max() ?? 0
        }
    }

}
