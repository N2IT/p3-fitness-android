import SwiftUI
import SwiftData

struct MainTabView: View {
    let userId: Int
    let onLogout: () -> Void

    @State private var selectedTab: Int = 0
    @State private var unitPreference: String = "lbs"
    @Environment(\.modelContext) private var modelContext

    var body: some View {
        ZStack(alignment: .bottom) {
            // Tab content
            ZStack {
                switch selectedTab {
                case 0:
                    NavigationStack {
                        RoutineListView(userId: userId)
                    }
                    .transition(.asymmetric(
                        insertion: .opacity.combined(with: .scale(scale: 0.98)),
                        removal: .opacity
                    ))
                case 1:
                    NavigationStack {
                        HistoryView(userId: userId)
                    }
                    .transition(.asymmetric(
                        insertion: .opacity.combined(with: .scale(scale: 0.98)),
                        removal: .opacity
                    ))
                case 2:
                    NavigationStack {
                        ProgressView(userId: userId)
                    }
                    .transition(.asymmetric(
                        insertion: .opacity.combined(with: .scale(scale: 0.98)),
                        removal: .opacity
                    ))
                case 3:
                    NavigationStack {
                        ImportRoutineView(userId: userId)
                    }
                    .transition(.asymmetric(
                        insertion: .opacity.combined(with: .scale(scale: 0.98)),
                        removal: .opacity
                    ))
                case 4:
                    NavigationStack {
                        SettingsView(userId: userId, onLogout: onLogout)
                    }
                    .transition(.asymmetric(
                        insertion: .opacity.combined(with: .scale(scale: 0.98)),
                        removal: .opacity
                    ))
                default:
                    EmptyView()
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .animation(.easeInOut(duration: 0.2), value: selectedTab)

            // Custom bottom tab bar
            CustomTabBar(selectedTab: $selectedTab)
        }
        .ignoresSafeArea(edges: .bottom)
        .preferredColorScheme(.dark)
        .background(Color(hex: "#0A0A0A"))
        .onAppear {
            loadUnitPreference()
        }
    }

    private func loadUnitPreference() {
        // Unit preference can be fetched from the model context if needed
    }
}

struct CustomTabBar: View {
    @Binding var selectedTab: Int

    private let tabs: [(icon: String, label: String)] = [
        ("dumbbell.fill", "Workout"),
        ("clock.fill", "History"),
        ("chart.line.uptrend.xyaxis", "Progress"),
        ("wand.and.stars", "Import"),
        ("person.circle.fill", "Profile")
    ]

    var body: some View {
        HStack(spacing: 0) {
            ForEach(Array(tabs.enumerated()), id: \.offset) { index, tab in
                TabBarButton(
                    icon: tab.icon,
                    label: tab.label,
                    isSelected: selectedTab == index,
                    action: {
                        withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                            selectedTab = index
                        }
                    }
                )
            }
        }
        .padding(.horizontal, 8)
        .padding(.top, 12)
        .padding(.bottom, 28)
        .background(
            ZStack {
                Color(hex: "#0F0F0F")
                    .opacity(0.97)
                Rectangle()
                    .fill(Color(white: 0.12))
                    .frame(height: 1)
                    .frame(maxHeight: .infinity, alignment: .top)
            }
        )
        .overlay(
            Rectangle()
                .fill(Color(white: 0.12))
                .frame(height: 1),
            alignment: .top
        )
    }
}

struct TabBarButton: View {
    let icon: String
    let label: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: 4) {
                ZStack {
                    if isSelected {
                        RoundedRectangle(cornerRadius: 10)
                            .fill(Color(hex: "#0055FF").opacity(0.15))
                            .frame(width: 44, height: 32)
                            .transition(.scale.combined(with: .opacity))
                    }

                    Image(systemName: icon)
                        .font(.system(size: isSelected ? 20 : 19, weight: isSelected ? .semibold : .regular))
                        .foregroundColor(isSelected ? Color(hex: "#0055FF") : Color(white: 0.5))
                        .scaleEffect(isSelected ? 1.1 : 1.0)
                        .animation(.spring(response: 0.3, dampingFraction: 0.7), value: isSelected)
                }
                .frame(height: 32)

                Text(label)
                    .font(.system(size: 10, weight: isSelected ? .semibold : .regular))
                    .foregroundColor(isSelected ? Color(hex: "#0055FF") : Color(white: 0.5))
            }
            .frame(maxWidth: .infinity)
            .contentShape(Rectangle())
        }
        .buttonStyle(PlainButtonStyle())
    }
}

#Preview {
    MainTabView(userId: 1, onLogout: {})
        .modelContainer(for: [], inMemory: true)
}
