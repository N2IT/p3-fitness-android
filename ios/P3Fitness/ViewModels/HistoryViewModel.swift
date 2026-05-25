import Foundation
import SwiftData
import Observation

// MARK: - Supporting Types

struct WorkoutSession: Identifiable {
    let id: String          // workoutSessionId
    let routineName: String
    let completedAt: Date
    let exerciseCount: Int
    let totalVolume: Double
    let durationMinutes: Int?
    let prCount: Int
}

struct WorkoutSessionDetail {
    let session: WorkoutSession
    let exercises: [ExerciseDetail]
}

struct ExerciseDetail: Identifiable {
    let id: Int             // exerciseId
    let name: String
    let sets: [ExerciseLog]
    let prs: [PersonalRecord]

    var totalVolume: Double {
        sets.reduce(0) { $0 + $1.weight * Double($1.reps) }
    }

    var bestSet: ExerciseLog? {
        sets.max(by: { ($0.weight * Double($0.reps)) < ($1.weight * Double($1.reps)) })
    }
}

// MARK: - HistoryViewModel

@Observable
final class HistoryViewModel {
    // Sessions list state
    var sessions: [WorkoutSession] = []
    var isLoading: Bool = false

    // Session detail state (used by WorkoutDetailView)
    var sessionDetail: WorkoutSessionDetail? = nil
    var isLoadingDetail: Bool = false

    var errorMessage: String? = nil

    // MARK: - Load Sessions

    @MainActor
    func loadSessions(userId: Int, context: ModelContext) async {
        isLoading = true

        do {
            let logDescriptor = FetchDescriptor<ExerciseLog>(
                predicate: #Predicate<ExerciseLog> { $0.userId == userId },
                sortBy: [SortDescriptor(\.completedAt, order: .reverse)]
            )
            let allLogs = try context.fetch(logDescriptor)

            let prDescriptor = FetchDescriptor<PersonalRecord>(
                predicate: #Predicate<PersonalRecord> { $0.userId == userId }
            )
            let allPRs = try context.fetch(prDescriptor)

            // Group logs by session ID
            var sessionMap: [String: [ExerciseLog]] = [:]
            for log in allLogs {
                sessionMap[log.workoutSessionId, default: []].append(log)
            }

            // Fetch routine names
            let routineDescriptor = FetchDescriptor<WorkoutRoutine>()
            let allRoutines = try context.fetch(routineDescriptor)
            let routineNames: [Int: String] = Dictionary(
                uniqueKeysWithValues: allRoutines.map { ($0.id, $0.name) }
            )

            var builtSessions: [WorkoutSession] = []
            for (sessionId, logs) in sessionMap {
                let sortedLogs = logs.sorted { $0.completedAt < $1.completedAt }
                let completedAt = sortedLogs.last?.completedAt ?? Date()
                let routineId = sortedLogs.first?.routineId ?? 0
                let routineName = routineNames[routineId] ?? "Workout"
                let exerciseIds = Set(logs.map { $0.exerciseId })
                let totalVolume = logs.reduce(0.0) { $0 + $1.weight * Double($1.reps) }
                let prCount = allPRs.filter { $0.workoutSessionId == sessionId }.count

                let duration: Int?
                if let first = sortedLogs.first?.completedAt,
                   let last = sortedLogs.last?.completedAt {
                    let diff = Int(last.timeIntervalSince(first) / 60)
                    duration = diff > 0 ? diff : nil
                } else {
                    duration = nil
                }

                builtSessions.append(WorkoutSession(
                    id: sessionId,
                    routineName: routineName,
                    completedAt: completedAt,
                    exerciseCount: exerciseIds.count,
                    totalVolume: totalVolume,
                    durationMinutes: duration,
                    prCount: prCount
                ))
            }

            sessions = builtSessions.sorted { $0.completedAt > $1.completedAt }
        } catch {
            errorMessage = "Failed to load history: \(error.localizedDescription)"
        }

        isLoading = false
    }

    // MARK: - Load Session Detail

    @MainActor
    func loadSessionDetail(sessionId: String, context: ModelContext) async {
        isLoadingDetail = true

        do {
            let logDescriptor = FetchDescriptor<ExerciseLog>(
                predicate: #Predicate<ExerciseLog> { $0.workoutSessionId == sessionId },
                sortBy: [SortDescriptor(\.setNumber)]
            )
            let logs = try context.fetch(logDescriptor)

            let prDescriptor = FetchDescriptor<PersonalRecord>(
                predicate: #Predicate<PersonalRecord> { $0.workoutSessionId == sessionId }
            )
            let prs = try context.fetch(prDescriptor)

            // Build exercise detail map
            var exerciseMap: [Int: (name: String, logs: [ExerciseLog])] = [:]
            for log in logs {
                if exerciseMap[log.exerciseId] == nil {
                    let exDescriptor = FetchDescriptor<Exercise>(
                        predicate: #Predicate<Exercise> { $0.id == log.exerciseId }
                    )
                    let exName = (try? context.fetch(exDescriptor).first?.name) ?? "Unknown Exercise"
                    exerciseMap[log.exerciseId] = (name: exName, logs: [])
                }
                exerciseMap[log.exerciseId]?.logs.append(log)
            }

            let exerciseDetails: [ExerciseDetail] = exerciseMap.map { (exerciseId, data) in
                let exercisePRs = prs.filter { $0.exerciseId == exerciseId }
                return ExerciseDetail(
                    id: exerciseId,
                    name: data.name,
                    sets: data.logs.sorted { $0.setNumber < $1.setNumber },
                    prs: exercisePRs
                )
            }.sorted { $0.name < $1.name }

            // Find matching session from cache or build minimal one
            if let session = sessions.first(where: { $0.id == sessionId }) {
                sessionDetail = WorkoutSessionDetail(session: session, exercises: exerciseDetails)
            } else {
                let completedAt = logs.max(by: { $0.completedAt < $1.completedAt })?.completedAt ?? Date()
                let routineId = logs.first?.routineId ?? 0
                let routineDescriptor = FetchDescriptor<WorkoutRoutine>(
                    predicate: #Predicate<WorkoutRoutine> { $0.id == routineId }
                )
                let routineName = (try? context.fetch(routineDescriptor).first?.name) ?? "Workout"
                let totalVolume = logs.reduce(0.0) { $0 + $1.weight * Double($1.reps) }

                let minimalSession = WorkoutSession(
                    id: sessionId,
                    routineName: routineName,
                    completedAt: completedAt,
                    exerciseCount: exerciseDetails.count,
                    totalVolume: totalVolume,
                    durationMinutes: nil,
                    prCount: prs.count
                )
                sessionDetail = WorkoutSessionDetail(session: minimalSession, exercises: exerciseDetails)
            }
        } catch {
            errorMessage = "Failed to load session detail: \(error.localizedDescription)"
        }

        isLoadingDetail = false
    }

    // MARK: - Delete Session

    @MainActor
    func deleteSession(sessionId: String, context: ModelContext) async {
        do {
            let logDescriptor = FetchDescriptor<ExerciseLog>(
                predicate: #Predicate<ExerciseLog> { $0.workoutSessionId == sessionId }
            )
            for log in try context.fetch(logDescriptor) { context.delete(log) }

            let prDescriptor = FetchDescriptor<PersonalRecord>(
                predicate: #Predicate<PersonalRecord> { $0.workoutSessionId == sessionId }
            )
            for pr in try context.fetch(prDescriptor) { context.delete(pr) }

            try context.save()
            sessions.removeAll { $0.id == sessionId }
            if sessionDetail?.session.id == sessionId {
                sessionDetail = nil
            }
        } catch {
            errorMessage = "Failed to delete session: \(error.localizedDescription)"
        }
    }
}
