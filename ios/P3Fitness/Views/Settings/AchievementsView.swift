import SwiftUI
import SwiftData

// MARK: - Achievement Definition

struct AchievementDefinition: Identifiable {
    let id: String
    let icon: String
    let iconColor: Color
    let title: String
    let description: String
    let threshold: Int
    let category: AchievementCategory

    enum AchievementCategory { case workouts, prs, consistency }
}

// MARK: - Achievements View

struct AchievementsView: View {
    let userId: Int
    @State private var viewModel = SettingsViewModel()
    @Environment(\.modelContext) private var modelContext

    private let achievements: [AchievementDefinition] = [
        AchievementDefinition(
            id: "first_workout",
            icon: "flame.fill",
            iconColor: Color(hex: "#FF6600"),
            title: "First Workout",
            description: "Complete your very first workout",
            threshold: 1,
            category: .workouts
        ),
        AchievementDefinition(
            id: "week_warrior",
            icon: "calendar.badge.checkmark",
            iconColor: Color(hex: "#0055FF"),
            title: "Week Warrior",
            description: "Complete 7 total workouts",
            threshold: 7,
            category: .workouts
        ),
        AchievementDefinition(
            id: "month_warrior",
            icon: "medal.fill",
            iconColor: Color(hex: "#FFD700"),
            title: "Month Warrior",
            description: "Complete 30 total workouts",
            threshold: 30,
            category: .workouts
        ),
        AchievementDefinition(
            id: "century_club",
            icon: "crown.fill",
            iconColor: Color(hex: "#FFD700"),
            title: "Century Club",
            description: "Complete 100 total workouts",
            threshold: 100,
            category: .workouts
        ),
        AchievementDefinition(
            id: "pr_machine",
            icon: "trophy.fill",
            iconColor: Color(hex: "#FF6600"),
            title: "PR Machine",
            description: "Set 10 personal records",
            threshold: 10,
            category: .prs
        ),
        AchievementDefinition(
            id: "consistency_king",
            icon: "bolt.fill",
            iconColor: Color(hex: "#00CC66"),
            title: "Consistency King",
            description: "Work out 3 days in a row",
            threshold: 3,
            category: .consistency
        )
    ]

    private let columns = [
        GridItem(.flexible(), spacing: 12),
        GridItem(.flexible(), spacing: 12)
    ]

    var body: some View {
        ZStack {
            Color(hex: "#0A0A0A").ignoresSafeArea()

            ScrollView {
                VStack(spacing: 24) {
                    // Stats summary
                    statsSummary

                    // Grid title
                    HStack {
                        Text("ACHIEVEMENTS")
                            .font(.system(size: 11, weight: .semibold))
                            .foregroundColor(Color(white: 0.4))
                            .tracking(2)
                        Spacer()
                        let achieved = achievements.filter { isAchieved($0) }.count
                        Text("\(achieved)/\(achievements.count)")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(Color(hex: "#FF6600"))
                    }
                    .padding(.horizontal, 16)

                    // Achievement grid
                    LazyVGrid(columns: columns, spacing: 12) {
                        ForEach(achievements) { achievement in
                            AchievementCard(
                                achievement: achievement,
                                isAchieved: isAchieved(achievement),
                                progress: progressFor(achievement),
                                total: achievement.threshold
                            )
                        }
                    }
                    .padding(.horizontal, 16)

                    Color.clear.frame(height: 20)
                }
                .padding(.top, 16)
            }
        }
        .navigationTitle("Achievements")
        .navigationBarTitleDisplayMode(.large)
        .onAppear {
            Task {
                await viewModel.loadAchievementData(userId: userId, context: modelContext)
            }
        }
    }

    // MARK: - Stats Summary

    private var statsSummary: some View {
        HStack(spacing: 0) {
            AchievementStat(
                icon: "dumbbell.fill",
                value: "\(viewModel.totalWorkouts)",
                label: "Workouts",
                color: Color(hex: "#0055FF")
            )
            Divider().frame(width: 1, height: 40).background(Color(white: 0.1))
            AchievementStat(
                icon: "scalemass.fill",
                value: formatVolume(viewModel.totalVolumeLifted),
                label: "Vol. Lifted",
                color: Color(hex: "#FF6600")
            )
            Divider().frame(width: 1, height: 40).background(Color(white: 0.1))
            AchievementStat(
                icon: "trophy.fill",
                value: "\(viewModel.totalPRs)",
                label: "PRs Set",
                color: Color(hex: "#FFD700")
            )
        }
        .padding(.vertical, 18)
        .background(Color(hex: "#141414"))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(white: 0.1), lineWidth: 1))
        .padding(.horizontal, 16)
    }

    // MARK: - Helpers

    private func isAchieved(_ achievement: AchievementDefinition) -> Bool {
        switch achievement.category {
        case .workouts:
            return viewModel.totalWorkouts >= achievement.threshold
        case .prs:
            return viewModel.totalPRs >= achievement.threshold
        case .consistency:
            return viewModel.maxConsecutiveDays >= achievement.threshold
        }
    }

    private func progressFor(_ achievement: AchievementDefinition) -> Int {
        switch achievement.category {
        case .workouts:
            return min(viewModel.totalWorkouts, achievement.threshold)
        case .prs:
            return min(viewModel.totalPRs, achievement.threshold)
        case .consistency:
            return min(viewModel.maxConsecutiveDays, achievement.threshold)
        }
    }

    private func formatVolume(_ v: Double) -> String {
        if v >= 1_000_000 { return String(format: "%.1fM", v / 1_000_000) }
        if v >= 1_000 { return String(format: "%.1fk", v / 1_000) }
        return "\(Int(v))"
    }
}

// MARK: - Achievement Card

struct AchievementCard: View {
    let achievement: AchievementDefinition
    let isAchieved: Bool
    let progress: Int
    let total: Int

    private var progressFraction: CGFloat {
        total == 0 ? 0 : min(1.0, CGFloat(progress) / CGFloat(total))
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(alignment: .top) {
                ZStack {
                    Circle()
                        .fill(isAchieved
                              ? achievement.iconColor.opacity(0.2)
                              : Color(white: 0.1))
                        .frame(width: 44, height: 44)
                    Image(systemName: achievement.icon)
                        .font(.system(size: 20))
                        .foregroundColor(isAchieved ? achievement.iconColor : Color(white: 0.25))
                }

                Spacer()

                if isAchieved {
                    Image(systemName: "checkmark.seal.fill")
                        .font(.system(size: 18))
                        .foregroundColor(Color(hex: "#00CC66"))
                        .transition(.scale.combined(with: .opacity))
                }
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(achievement.title)
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(isAchieved ? .white : Color(white: 0.4))
                    .lineLimit(1)

                Text(achievement.description)
                    .font(.system(size: 11))
                    .foregroundColor(Color(white: 0.35))
                    .lineLimit(2)
                    .fixedSize(horizontal: false, vertical: true)
            }

            // Progress bar
            VStack(alignment: .leading, spacing: 5) {
                GeometryReader { geo in
                    ZStack(alignment: .leading) {
                        RoundedRectangle(cornerRadius: 3)
                            .fill(Color(white: 0.1))
                            .frame(height: 6)
                        RoundedRectangle(cornerRadius: 3)
                            .fill(
                                isAchieved
                                    ? LinearGradient(colors: [Color(hex: "#00CC66"), Color(hex: "#00AA55")], startPoint: .leading, endPoint: .trailing)
                                    : LinearGradient(colors: [achievement.iconColor, achievement.iconColor.opacity(0.6)], startPoint: .leading, endPoint: .trailing)
                            )
                            .frame(width: geo.size.width * progressFraction, height: 6)
                            .animation(.easeOut(duration: 0.6), value: progressFraction)
                    }
                }
                .frame(height: 6)

                Text(isAchieved ? "Achieved!" : "\(progress)/\(total)")
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundColor(isAchieved ? Color(hex: "#00CC66") : Color(white: 0.35))
            }
        }
        .padding(14)
        .background(
            isAchieved
                ? Color(hex: "#141414").opacity(1)
                : Color(hex: "#111111")
        )
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(
                    isAchieved ? achievement.iconColor.opacity(0.25) : Color(white: 0.08),
                    lineWidth: isAchieved ? 1.5 : 1
                )
        )
        .shadow(
            color: isAchieved ? achievement.iconColor.opacity(0.12) : .clear,
            radius: 8, y: 3
        )
        .opacity(isAchieved ? 1.0 : 0.7)
    }
}

// MARK: - Achievement Stat

struct AchievementStat: View {
    let icon: String
    let value: String
    let label: String
    let color: Color

    var body: some View {
        VStack(spacing: 6) {
            Image(systemName: icon)
                .font(.system(size: 16))
                .foregroundColor(color)
            Text(value)
                .font(.system(size: 20, weight: .black, design: .monospaced))
                .foregroundColor(.white)
            Text(label)
                .font(.system(size: 11))
                .foregroundColor(Color(white: 0.4))
        }
        .frame(maxWidth: .infinity)
    }
}

#Preview {
    NavigationStack {
        AchievementsView(userId: 1)
    }
    .modelContainer(for: [], inMemory: true)
}
