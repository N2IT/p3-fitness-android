import SwiftUI
import SwiftData

struct WorkoutDetailView: View {
    let sessionId: String
    let userId: Int
    @State private var viewModel = HistoryViewModel()
    @Environment(\.modelContext) private var modelContext

    var body: some View {
        ZStack {
            Color(hex: "#0A0A0A").ignoresSafeArea()

            Group {
                if viewModel.isLoadingDetail {
                    VStack(spacing: 16) {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: Color(hex: "#0055FF")))
                            .scaleEffect(1.3)
                        Text("Loading workout...")
                            .font(.system(size: 14))
                            .foregroundColor(Color(white: 0.5))
                    }
                } else if let detail = viewModel.sessionDetail {
                    detailContent(detail)
                } else {
                    Text("Workout not found")
                        .foregroundColor(Color(white: 0.5))
                }
            }
        }
        .navigationTitle(viewModel.sessionDetail.map { sessionTitle($0.session) } ?? "Workout")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            Task {
                await viewModel.loadSessionDetail(sessionId: sessionId, context: modelContext)
            }
        }
    }

    private func sessionTitle(_ session: WorkoutSession) -> String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .none
        return "\(session.routineName) · \(formatter.string(from: session.completedAt))"
    }

    @ViewBuilder
    private func detailContent(_ detail: WorkoutSessionDetail) -> some View {
        ScrollView {
            VStack(spacing: 20) {
                // Summary stats bar
                summaryBar(detail.session)

                // Exercise sections
                ForEach(detail.exercises) { exercise in
                    ExerciseDetailCard(exercise: exercise)
                        .padding(.horizontal, 16)
                        .transition(.opacity.combined(with: .move(edge: .bottom)))
                }

                Color.clear.frame(height: 20)
            }
            .padding(.top, 16)
        }
    }

    // MARK: - Summary Bar

    private func summaryBar(_ session: WorkoutSession) -> some View {
        HStack(spacing: 0) {
            SummaryStatColumn(
                icon: "scalemass.fill",
                value: formatVolume(session.totalVolume),
                label: "Volume",
                color: Color(hex: "#0055FF")
            )
            Divider().frame(width: 1, height: 40).background(Color(white: 0.1))
            SummaryStatColumn(
                icon: "dumbbell.fill",
                value: "\(session.exerciseCount)",
                label: "Exercises",
                color: Color(hex: "#0055FF")
            )
            if let dur = session.durationMinutes {
                Divider().frame(width: 1, height: 40).background(Color(white: 0.1))
                SummaryStatColumn(
                    icon: "clock.fill",
                    value: formatDuration(dur),
                    label: "Duration",
                    color: Color(hex: "#0055FF")
                )
            }
            if session.prCount > 0 {
                Divider().frame(width: 1, height: 40).background(Color(white: 0.1))
                SummaryStatColumn(
                    icon: "trophy.fill",
                    value: "\(session.prCount)",
                    label: "PRs Set",
                    color: Color(hex: "#FF6600")
                )
            }
        }
        .padding(.vertical, 16)
        .background(Color(hex: "#141414"))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(white: 0.1), lineWidth: 1))
        .padding(.horizontal, 16)
    }

    private func formatVolume(_ v: Double) -> String {
        if v >= 1000 { return String(format: "%.1fk", v / 1000) }
        return "\(Int(v))"
    }

    private func formatDuration(_ mins: Int) -> String {
        if mins < 60 { return "\(mins)m" }
        let h = mins / 60
        let m = mins % 60
        return m == 0 ? "\(h)h" : "\(h)h\(m)m"
    }
}

// MARK: - Summary Stat Column

struct SummaryStatColumn: View {
    let icon: String
    let value: String
    let label: String
    let color: Color

    var body: some View {
        VStack(spacing: 5) {
            Image(systemName: icon)
                .font(.system(size: 14))
                .foregroundColor(color)
            Text(value)
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.white)
            Text(label)
                .font(.system(size: 11))
                .foregroundColor(Color(white: 0.4))
        }
        .frame(maxWidth: .infinity)
    }
}

// MARK: - Exercise Detail Card

struct ExerciseDetailCard: View {
    let exercise: ExerciseDetail

    var totalVolume: Double {
        exercise.sets.reduce(0) { $0 + $1.weight * Double($1.reps) }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            // Header
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(exercise.name)
                        .font(.system(size: 17, weight: .bold))
                        .foregroundColor(.white)

                    // PR badges
                    if !exercise.prs.isEmpty {
                        HStack(spacing: 6) {
                            ForEach(exercise.prs.prefix(3)) { pr in
                                PRBadge(recordType: pr.recordType)
                            }
                        }
                    }
                }
                Spacer()
                VStack(alignment: .trailing, spacing: 3) {
                    Text("Vol.")
                        .font(.system(size: 11))
                        .foregroundColor(Color(white: 0.4))
                    Text(formatVolume(totalVolume))
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(Color(hex: "#0055FF"))
                }
            }

            Divider().background(Color(white: 0.1))

            // Column headers
            HStack {
                Text("SET")
                    .frame(width: 40, alignment: .center)
                Text("WEIGHT")
                    .frame(maxWidth: .infinity, alignment: .center)
                Text("REPS")
                    .frame(maxWidth: .infinity, alignment: .center)
                Text("VOL.")
                    .frame(maxWidth: .infinity, alignment: .center)
            }
            .font(.system(size: 10, weight: .semibold))
            .foregroundColor(Color(white: 0.35))
            .tracking(1)

            // Set rows
            VStack(spacing: 6) {
                ForEach(Array(exercise.sets.enumerated()), id: \.element.id) { index, log in
                    let isPR = exercise.prs.contains(where: {
                        $0.workoutSessionId == log.workoutSessionId
                    })
                    LogSetRow(setNumber: index + 1, log: log, isPR: isPR)
                }
            }
        }
        .padding(16)
        .background(Color(hex: "#141414"))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(white: 0.1), lineWidth: 1))
    }

    private func formatVolume(_ v: Double) -> String {
        if v >= 1000 { return String(format: "%.1fk", v / 1000) }
        return "\(Int(v))"
    }
}

struct LogSetRow: View {
    let setNumber: Int
    let log: ExerciseLog
    let isPR: Bool

    var volume: Double { log.weight * Double(log.reps) }

    var body: some View {
        HStack {
            Group {
                if isPR {
                    Text("⭐")
                        .font(.system(size: 14))
                } else {
                    Text("\(setNumber)")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(Color(white: 0.5))
                }
            }
            .frame(width: 40, alignment: .center)

            Text(formatWeight(log.weight))
                .font(.system(size: 14, weight: .semibold, design: .monospaced))
                .foregroundColor(.white)
                .frame(maxWidth: .infinity, alignment: .center)

            Text("\(log.reps)")
                .font(.system(size: 14, weight: .semibold, design: .monospaced))
                .foregroundColor(.white)
                .frame(maxWidth: .infinity, alignment: .center)

            Text(formatVolume(volume))
                .font(.system(size: 13, weight: .medium, design: .monospaced))
                .foregroundColor(Color(white: 0.5))
                .frame(maxWidth: .infinity, alignment: .center)
        }
        .padding(.vertical, 6)
        .background(
            isPR ? Color(hex: "#FF6600").opacity(0.05) : Color.clear
        )
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }

    private func formatWeight(_ w: Double) -> String {
        w.truncatingRemainder(dividingBy: 1) == 0 ? "\(Int(w))" : String(format: "%.1f", w)
    }

    private func formatVolume(_ v: Double) -> String {
        v.truncatingRemainder(dividingBy: 1) == 0 ? "\(Int(v))" : String(format: "%.0f", v)
    }
}

struct PRBadge: View {
    let recordType: String

    var body: some View {
        HStack(spacing: 4) {
            Text("🏆")
                .font(.system(size: 10))
            Text(recordType)
                .font(.system(size: 10, weight: .bold))
                .foregroundColor(Color(hex: "#FF6600"))
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 3)
        .background(Color(hex: "#FF6600").opacity(0.12))
        .clipShape(Capsule())
        .overlay(Capsule().stroke(Color(hex: "#FF6600").opacity(0.25), lineWidth: 1))
    }
}

#Preview {
    NavigationStack {
        WorkoutDetailView(sessionId: "session-1", userId: 1)
    }
    .modelContainer(for: [], inMemory: true)
}
