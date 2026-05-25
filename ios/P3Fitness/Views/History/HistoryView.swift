import SwiftUI
import SwiftData

struct HistoryView: View {
    let userId: Int
    @State private var viewModel = HistoryViewModel()
    @Environment(\.modelContext) private var modelContext

    var body: some View {
        ZStack {
            Color(hex: "#0A0A0A").ignoresSafeArea()

            Group {
                if viewModel.isLoading {
                    VStack(spacing: 16) {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: Color(hex: "#0055FF")))
                            .scaleEffect(1.3)
                        Text("Loading history...")
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(Color(white: 0.5))
                    }
                } else if viewModel.sessions.isEmpty {
                    emptyStateView
                } else {
                    sessionListView
                }
            }
        }
        .navigationTitle("History")
        .navigationBarTitleDisplayMode(.large)
        .onAppear {
            Task {
                await viewModel.loadSessions(userId: userId, context: modelContext)
            }
        }
    }

    // MARK: - Empty State

    private var emptyStateView: some View {
        VStack(spacing: 24) {
            ZStack {
                Circle()
                    .fill(Color(hex: "#141414"))
                    .frame(width: 100, height: 100)
                Image(systemName: "clock.badge.xmark")
                    .font(.system(size: 38))
                    .foregroundColor(Color(white: 0.25))
            }

            VStack(spacing: 8) {
                Text("No Workouts Yet")
                    .font(.system(size: 22, weight: .bold))
                    .foregroundColor(.white)
                Text("Complete your first workout to\nsee your history here")
                    .font(.system(size: 15))
                    .foregroundColor(Color(white: 0.5))
                    .multilineTextAlignment(.center)
                    .lineSpacing(4)
            }
        }
        .padding(40)
    }

    // MARK: - Session List

    private var sessionListView: some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                ForEach(groupedSessions, id: \.weekLabel) { group in
                    // Week header
                    HStack {
                        Text(group.weekLabel)
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundColor(Color(white: 0.4))
                            .tracking(1)
                        Spacer()
                        Text("\(group.sessions.count) workout\(group.sessions.count == 1 ? "" : "s")")
                            .font(.system(size: 11))
                            .foregroundColor(Color(white: 0.3))
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 20)
                    .padding(.bottom, 8)

                    ForEach(group.sessions) { session in
                        NavigationLink(destination: WorkoutDetailView(sessionId: session.id, userId: userId)) {
                            SessionCard(session: session)
                        }
                        .buttonStyle(PlainButtonStyle())
                        .padding(.horizontal, 16)
                        .padding(.bottom, 10)
                        .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                            Button(role: .destructive) {
                                Task {
                                    await viewModel.deleteSession(sessionId: session.id, context: modelContext)
                                }
                            } label: {
                                Label("Delete", systemImage: "trash")
                            }
                        }
                    }
                }

                Color.clear.frame(height: 20)
            }
        }
    }

    // MARK: - Grouping

    struct WeekGroup: Identifiable {
        let id: String
        let weekLabel: String
        let sessions: [WorkoutSession]
    }

    private var groupedSessions: [WeekGroup] {
        let calendar = Calendar.current
        var groups: [String: [WorkoutSession]] = [:]
        var orderKeys: [String] = []

        for session in viewModel.sessions {
            let weekStart = calendar.dateInterval(of: .weekOfYear, for: session.completedAt)?.start ?? session.completedAt
            let key = ISO8601DateFormatter().string(from: weekStart)
            if groups[key] == nil {
                groups[key] = []
                orderKeys.append(key)
            }
            groups[key]?.append(session)
        }

        return orderKeys.compactMap { key -> WeekGroup? in
            guard let sessions = groups[key], let firstDate = sessions.first?.completedAt else { return nil }
            let weekStart = calendar.dateInterval(of: .weekOfYear, for: firstDate)?.start ?? firstDate
            let weekEnd = calendar.date(byAdding: .day, value: 6, to: weekStart) ?? firstDate
            let formatter = DateFormatter()
            formatter.dateFormat = "MMM d"
            let label = "\(formatter.string(from: weekStart)) – \(formatter.string(from: weekEnd))"
            return WeekGroup(id: key, weekLabel: label, sessions: sessions)
        }
    }
}

// MARK: - Session Card

struct SessionCard: View {
    let session: WorkoutSession

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Header row
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(session.routineName)
                        .font(.system(size: 17, weight: .bold))
                        .foregroundColor(.white)
                    Text(relativeDate(session.completedAt))
                        .font(.system(size: 13))
                        .foregroundColor(Color(white: 0.5))
                }
                Spacer()
                if session.prCount > 0 {
                    HStack(spacing: 4) {
                        Text("🏆")
                            .font(.system(size: 13))
                        Text("\(session.prCount) PR\(session.prCount == 1 ? "" : "s")")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(Color(hex: "#FF6600"))
                    }
                    .padding(.horizontal, 10)
                    .padding(.vertical, 5)
                    .background(Color(hex: "#FF6600").opacity(0.12))
                    .clipShape(Capsule())
                    .overlay(Capsule().stroke(Color(hex: "#FF6600").opacity(0.25), lineWidth: 1))
                }
            }

            Divider().background(Color(white: 0.1))

            // Stats row
            HStack(spacing: 0) {
                HistoryStatItem(
                    icon: "scalemass",
                    value: formatVolume(session.totalVolume),
                    label: "volume"
                )
                Divider().frame(width: 1, height: 24).background(Color(white: 0.1))
                HistoryStatItem(
                    icon: "figure.strengthtraining.traditional",
                    value: "\(session.exerciseCount)",
                    label: "exercises"
                )
                Divider().frame(width: 1, height: 24).background(Color(white: 0.1))
                HistoryStatItem(
                    icon: "clock",
                    value: session.durationMinutes.map { formatDuration($0) } ?? "—",
                    label: "duration"
                )
            }
        }
        .padding(16)
        .background(Color(hex: "#141414"))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(white: 0.1), lineWidth: 1))
        .shadow(color: .black.opacity(0.25), radius: 8, y: 3)
    }

    private func relativeDate(_ date: Date) -> String {
        let formatter = RelativeDateTimeFormatter()
        formatter.unitsStyle = .full
        return formatter.localizedString(for: date, relativeTo: Date())
    }

    private func formatVolume(_ v: Double) -> String {
        if v >= 1000 { return String(format: "%.1fk", v / 1000) }
        return v.truncatingRemainder(dividingBy: 1) == 0 ? "\(Int(v))" : String(format: "%.1f", v)
    }

    private func formatDuration(_ mins: Int) -> String {
        if mins < 60 { return "\(mins)m" }
        let h = mins / 60
        let m = mins % 60
        return m == 0 ? "\(h)h" : "\(h)h \(m)m"
    }
}

struct HistoryStatItem: View {
    let icon: String
    let value: String
    let label: String

    var body: some View {
        VStack(spacing: 3) {
            HStack(spacing: 4) {
                Image(systemName: icon)
                    .font(.system(size: 11))
                    .foregroundColor(Color(hex: "#0055FF").opacity(0.8))
                Text(value)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(.white)
            }
            Text(label)
                .font(.system(size: 10))
                .foregroundColor(Color(white: 0.4))
        }
        .frame(maxWidth: .infinity)
    }
}

#Preview {
    NavigationStack {
        HistoryView(userId: 1)
    }
    .modelContainer(for: [], inMemory: true)
}
