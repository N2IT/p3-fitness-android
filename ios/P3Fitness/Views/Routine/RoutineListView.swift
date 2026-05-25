import SwiftUI
import SwiftData

struct RoutineListView: View {
    let userId: Int
    @State private var viewModel = RoutineViewModel()
    @Environment(\.modelContext) private var modelContext

    @State private var showCreateSheet: Bool = false
    @State private var newRoutineName: String = ""
    @State private var routineToStart: WorkoutRoutineInfo? = nil
    @State private var navigateToEdit: Int? = nil
    @State private var listOpacity: Double = 0

    var body: some View {
        ZStack {
            Color(hex: "#0A0A0A").ignoresSafeArea()

            Group {
                if viewModel.isLoading {
                    VStack(spacing: 16) {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: Color(hex: "#0055FF")))
                            .scaleEffect(1.3)
                        Text("Loading routines...")
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(Color(white: 0.5))
                    }
                } else if viewModel.routines.isEmpty {
                    emptyStateView
                } else {
                    routineListContent
                }
            }
        }
        .navigationTitle("My Routines")
        .navigationBarTitleDisplayMode(.large)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: { showCreateSheet = true }) {
                    ZStack {
                        Circle()
                            .fill(Color(hex: "#0055FF"))
                            .frame(width: 32, height: 32)
                        Image(systemName: "plus")
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(.white)
                    }
                }
            }
        }
        .sheet(isPresented: $showCreateSheet) {
            createRoutineSheet
        }
        .navigationDestination(item: $routineToStart) { routine in
            ActiveWorkoutView(routineId: routine.id, userId: userId)
        }
        .navigationDestination(unwrapping: $navigateToEdit) { $routineId in
            RoutineEditView(routineId: routineId, userId: userId)
        }
        .onAppear {
            Task {
                await viewModel.loadRoutines(userId: userId, context: modelContext)
                withAnimation(.easeOut(duration: 0.4)) {
                    listOpacity = 1.0
                }
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
                Image(systemName: "dumbbell.fill")
                    .font(.system(size: 40))
                    .foregroundColor(Color(hex: "#0055FF").opacity(0.6))
            }

            VStack(spacing: 8) {
                Text("No Routines Yet")
                    .font(.system(size: 22, weight: .bold))
                    .foregroundColor(.white)
                Text("Create your first routine to start\ntracking your workouts")
                    .font(.system(size: 15, weight: .regular))
                    .foregroundColor(Color(white: 0.5))
                    .multilineTextAlignment(.center)
                    .lineSpacing(4)
            }

            Button(action: { showCreateSheet = true }) {
                HStack(spacing: 8) {
                    Image(systemName: "plus")
                        .font(.system(size: 15, weight: .bold))
                    Text("Create Routine")
                        .font(.system(size: 16, weight: .semibold))
                }
                .foregroundColor(.white)
                .frame(width: 180, height: 50)
                .background(
                    LinearGradient(
                        colors: [Color(hex: "#0055FF"), Color(hex: "#0033CC")],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                )
                .cornerRadius(14)
                .shadow(color: Color(hex: "#0055FF").opacity(0.4), radius: 10, x: 0, y: 5)
            }
        }
        .padding(40)
    }

    // MARK: - Routine List
    private var routineListContent: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                ForEach(viewModel.routines) { routine in
                    RoutineCard(
                        routine: routine,
                        onStartWorkout: {
                            routineToStart = routine
                        },
                        onEdit: {
                            navigateToEdit = routine.id
                        },
                        onDelete: {
                            Task {
                                await viewModel.deleteRoutine(routineId: routine.id, context: modelContext)
                            }
                        }
                    )
                    .transition(.asymmetric(
                        insertion: .opacity.combined(with: .move(edge: .bottom)),
                        removal: .opacity.combined(with: .scale(scale: 0.95))
                    ))
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
        }
        .opacity(listOpacity)
    }

    // MARK: - Create Routine Sheet
    private var createRoutineSheet: some View {
        NavigationStack {
            ZStack {
                Color(hex: "#0A0A0A").ignoresSafeArea()

                VStack(spacing: 24) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Routine Name")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(Color(white: 0.6))
                            .kerning(0.5)

                        TextField("", text: $newRoutineName, prompt: Text("e.g. Push Day, Leg Day...")
                            .foregroundColor(Color(white: 0.3)))
                            .foregroundColor(.white)
                            .font(.system(size: 16))
                            .padding(.horizontal, 16)
                            .padding(.vertical, 14)
                            .background(Color(hex: "#141414"))
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color(white: 0.15), lineWidth: 1)
                            )
                    }

                    Button(action: createRoutine) {
                        Text("Create Routine")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity, minHeight: 52)
                            .background(
                                LinearGradient(
                                    colors: newRoutineName.isEmpty
                                        ? [Color(white: 0.2), Color(white: 0.15)]
                                        : [Color(hex: "#0055FF"), Color(hex: "#0033CC")],
                                    startPoint: .leading,
                                    endPoint: .trailing
                                )
                            )
                            .cornerRadius(14)
                    }
                    .disabled(newRoutineName.isEmpty)

                    Spacer()
                }
                .padding(24)
            }
            .navigationTitle("New Routine")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        newRoutineName = ""
                        showCreateSheet = false
                    }
                    .foregroundColor(Color(hex: "#0055FF"))
                }
            }
        }
        .presentationDetents([.height(280)])
        .presentationBackground(Color(hex: "#0A0A0A"))
        .presentationDragIndicator(.visible)
    }

    private func createRoutine() {
        guard !newRoutineName.isEmpty else { return }
        Task {
            await viewModel.createRoutine(name: newRoutineName, userId: userId, context: modelContext)
            newRoutineName = ""
            showCreateSheet = false
        }
    }
}

// MARK: - Routine Card

struct RoutineCard: View {
    let routine: WorkoutRoutineInfo
    let onStartWorkout: () -> Void
    let onEdit: () -> Void
    let onDelete: () -> Void

    @State private var showDeleteConfirm = false

    var body: some View {
        VStack(spacing: 0) {
            // Header
            Button(action: onEdit) {
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: 6) {
                        Text(routine.name)
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.white)

                        if !routine.exerciseNames.isEmpty {
                            Text(routine.exerciseNames.prefix(3).joined(separator: " · "))
                                .font(.system(size: 13, weight: .regular))
                                .foregroundColor(Color(white: 0.5))
                                .lineLimit(1)
                        }
                    }

                    Spacer()

                    Image(systemName: "chevron.right")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(Color(white: 0.3))
                        .padding(.top, 4)
                }
                .padding(.horizontal, 16)
                .padding(.top, 16)
                .padding(.bottom, 12)
            }
            .buttonStyle(PlainButtonStyle())

            // Stats row
            HStack(spacing: 0) {
                StatPill(icon: "figure.strengthtraining.traditional", value: "\(routine.exerciseCount)", label: "exercises")
                Divider()
                    .frame(width: 1, height: 28)
                    .background(Color(white: 0.12))
                StatPill(icon: "arrow.counterclockwise", value: "\(routine.timesPerformed)", label: "times")
                Divider()
                    .frame(width: 1, height: 28)
                    .background(Color(white: 0.12))
                StatPill(
                    icon: "clock",
                    value: routine.lastPerformedAt.map { relativeDate($0) } ?? "Never",
                    label: "last"
                )
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 12)

            Divider()
                .background(Color(white: 0.1))

            // Action row
            HStack(spacing: 0) {
                Button(action: onEdit) {
                    HStack(spacing: 6) {
                        Image(systemName: "pencil")
                            .font(.system(size: 13))
                        Text("Edit")
                            .font(.system(size: 14, weight: .medium))
                    }
                    .foregroundColor(Color(white: 0.6))
                    .frame(maxWidth: .infinity, minHeight: 44)
                }
                .buttonStyle(PlainButtonStyle())

                Divider()
                    .frame(width: 1, height: 28)
                    .background(Color(white: 0.1))

                Button(action: { showDeleteConfirm = true }) {
                    HStack(spacing: 6) {
                        Image(systemName: "trash")
                            .font(.system(size: 13))
                        Text("Delete")
                            .font(.system(size: 14, weight: .medium))
                    }
                    .foregroundColor(Color(hex: "#FF3333").opacity(0.8))
                    .frame(maxWidth: .infinity, minHeight: 44)
                }
                .buttonStyle(PlainButtonStyle())

                Divider()
                    .frame(width: 1, height: 28)
                    .background(Color(white: 0.1))

                Button(action: onStartWorkout) {
                    HStack(spacing: 6) {
                        Image(systemName: "play.fill")
                            .font(.system(size: 12))
                        Text("Start")
                            .font(.system(size: 14, weight: .semibold))
                    }
                    .foregroundColor(Color(hex: "#0055FF"))
                    .frame(maxWidth: .infinity, minHeight: 44)
                }
                .buttonStyle(PlainButtonStyle())
            }
        }
        .background(Color(hex: "#141414"))
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color(white: 0.1), lineWidth: 1)
        )
        .shadow(color: .black.opacity(0.3), radius: 8, x: 0, y: 4)
        .confirmationDialog("Delete \"\(routine.name)\"?", isPresented: $showDeleteConfirm, titleVisibility: .visible) {
            Button("Delete", role: .destructive, action: onDelete)
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("This will permanently delete this routine and all associated data.")
        }
    }

    private func relativeDate(_ date: Date) -> String {
        let formatter = RelativeDateTimeFormatter()
        formatter.unitsStyle = .short
        return formatter.localizedString(for: date, relativeTo: Date())
    }
}

struct StatPill: View {
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

// MARK: - Navigation Helpers

extension View {
    func navigationDestination<Value: Hashable, Destination: View>(
        unwrapping value: Binding<Value?>,
        @ViewBuilder destination: @escaping (Binding<Value>) -> Destination
    ) -> some View {
        self.navigationDestination(isPresented: Binding(
            get: { value.wrappedValue != nil },
            set: { if !$0 { value.wrappedValue = nil } }
        )) {
            if let binding = Binding(value) {
                destination(binding)
            }
        }
    }
}

#Preview {
    NavigationStack {
        RoutineListView(userId: 1)
    }
    .modelContainer(for: [], inMemory: true)
}
