import SwiftUI
import SwiftData

@main
struct P3FitnessApp: App {
    let container: ModelContainer

    init() {
        do {
            container = try ModelContainer(
                for: User.self, WorkoutRoutine.self, Exercise.self,
                     RoutineExercise.self, ExerciseLog.self,
                     PersonalRecord.self, BodyWeightEntry.self,
                configurations: ModelConfiguration(isStoredInMemoryOnly: false)
            )
            // Seed exercises if needed
            Task { @MainActor in
                await SeedData.seedIfNeeded(context: container.mainContext)
            }
        } catch {
            fatalError("Failed to initialize database: \(error)")
        }
    }

    var body: some Scene {
        WindowGroup {
            RootView()
                .modelContainer(container)
                .preferredColorScheme(.dark)
        }
    }
}

struct RootView: View {
    @State private var currentUserId: Int?

    var body: some View {
        if let userId = currentUserId {
            MainTabView(userId: userId, onLogout: { currentUserId = nil })
        } else {
            LoginView(onLogin: { userId in currentUserId = userId })
        }
    }
}
