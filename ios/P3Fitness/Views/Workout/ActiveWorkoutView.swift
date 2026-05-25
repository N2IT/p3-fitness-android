import SwiftUI
import SwiftData

// MARK: - Active Workout View

struct ActiveWorkoutView: View {
    let routineId: Int
    let userId: Int
    @State private var viewModel = WorkoutViewModel()
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss

    // Custom keypad state
    @State private var activeFieldId: UUID? = nil
    @State private var activeFieldType: FieldType? = nil  // .weight or .reps
    @State private var activeSetId: UUID? = nil
    @State private var activeExerciseIndex: Int? = nil
    @State private var keypadInput: String = ""

    // PR celebration
    @State private var prCelebration: PRCelebrationData? = nil
    @State private var prCelebrationOpacity: Double = 0
    @State private var prScale: CGFloat = 0.8

    // Finish workout confirmation
    @State private var showFinishConfirm: Bool = false
    @State private var isFinishing: Bool = false

    // Timer
    @State private var elapsedSeconds: Int = 0
    @State private var timerTask: Task<Void, Never>? = nil

    enum FieldType { case weight, reps }

    var body: some View {
        ZStack {
            Color(hex: "#0A0A0A").ignoresSafeArea()

            VStack(spacing: 0) {
                // Header
                workoutHeader

                Divider().background(Color(white: 0.1))

                // Exercise sections
                ScrollView {
                    LazyVStack(spacing: 20) {
                        ForEach(Array(viewModel.exerciseSections.enumerated()), id: \.element.id) { exIndex, section in
                            ExerciseSectionView(
                                section: section,
                                exerciseIndex: exIndex,
                                activeSetId: $activeSetId,
                                onTapWeight: { setId in
                                    activateField(exerciseIndex: exIndex, setId: setId, type: .weight, section: section)
                                },
                                onTapReps: { setId in
                                    activateField(exerciseIndex: exIndex, setId: setId, type: .reps, section: section)
                                },
                                onToggleComplete: { setId in
                                    Task {
                                        if let pr = await viewModel.toggleSetComplete(
                                            exerciseIndex: exIndex,
                                            setId: setId,
                                            context: modelContext
                                        ) {
                                            showPRCelebration(pr)
                                        }
                                    }
                                },
                                onAddSet: {
                                    viewModel.addSet(exerciseIndex: exIndex)
                                },
                                onRemoveSet: {
                                    viewModel.removeSet(exerciseIndex: exIndex)
                                }
                            )
                        }

                        // Bottom spacer for keyboard
                        Color.clear.frame(height: activeSetId != nil ? 280 : 100)
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 16)
                }

                // Finish button
                if activeSetId == nil {
                    finishButton
                }
            }

            // Custom numeric keypad overlay
            if activeSetId != nil {
                VStack {
                    Spacer()
                    NumericKeypadView(
                        input: $keypadInput,
                        fieldType: activeFieldType ?? .weight,
                        onDone: commitKeypadInput,
                        onDismiss: {
                            activeSetId = nil
                            activeFieldType = nil
                            keypadInput = ""
                        }
                    )
                }
                .ignoresSafeArea(edges: .bottom)
                .transition(.move(edge: .bottom))
            }

            // PR Celebration Overlay
            if let pr = prCelebration {
                PRCelebrationOverlay(data: pr)
                    .opacity(prCelebrationOpacity)
                    .scaleEffect(prScale)
                    .ignoresSafeArea()
            }
        }
        .navigationBarHidden(true)
        .preferredColorScheme(.dark)
        .onAppear {
            Task {
                await viewModel.loadWorkout(routineId: routineId, userId: userId, context: modelContext)
                startTimer()
            }
        }
        .onDisappear {
            timerTask?.cancel()
        }
        .confirmationDialog("Finish Workout?", isPresented: $showFinishConfirm, titleVisibility: .visible) {
            Button("Finish & Save") {
                Task { await finishWorkout() }
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("Your workout will be saved to history.")
        }
    }

    // MARK: - Workout Header

    private var workoutHeader: some View {
        HStack(alignment: .center) {
            Button(action: { dismiss() }) {
                Image(systemName: "xmark")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(Color(white: 0.6))
                    .frame(width: 36, height: 36)
                    .background(Color(hex: "#1E1E1E"))
                    .clipShape(Circle())
            }

            Spacer()

            VStack(spacing: 2) {
                Text(viewModel.routineName)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.white)
                    .lineLimit(1)
                Text(formatTime(elapsedSeconds))
                    .font(.system(size: 22, weight: .black, design: .monospaced))
                    .foregroundColor(Color(hex: "#0055FF"))
            }

            Spacer()

            // Placeholder to balance layout
            Color.clear.frame(width: 36, height: 36)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
    }

    // MARK: - Finish Button

    private var finishButton: some View {
        Button {
            showFinishConfirm = true
        } label: {
            HStack(spacing: 10) {
                if isFinishing {
                    ProgressView()
                        .tint(.white)
                        .scaleEffect(0.9)
                } else {
                    Image(systemName: "checkmark.circle.fill")
                        .font(.system(size: 18))
                    Text("Finish Workout")
                        .font(.system(size: 17, weight: .bold))
                }
            }
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .frame(height: 56)
            .background(
                LinearGradient(
                    colors: [Color(hex: "#0055FF"), Color(hex: "#0033CC")],
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .shadow(color: Color(hex: "#0055FF").opacity(0.4), radius: 12, y: 4)
        }
        .disabled(isFinishing)
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
    }

    // MARK: - Helpers

    private func activateField(exerciseIndex: Int, setId: UUID, type: FieldType, section: WorkoutExerciseSection) {
        guard let set = section.sets.first(where: { $0.id == setId }) else { return }
        activeExerciseIndex = exerciseIndex
        activeSetId = setId
        activeFieldType = type
        keypadInput = type == .weight
            ? (set.weight == 0 ? "" : formatWeightInput(set.weight))
            : (set.reps == 0 ? "" : "\(set.reps)")
        withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {}
    }

    private func commitKeypadInput() {
        guard let exIndex = activeExerciseIndex, let setId = activeSetId, let type = activeFieldType else {
            activeSetId = nil
            return
        }
        let value = Double(keypadInput) ?? 0
        if type == .weight {
            viewModel.updateSetWeight(exerciseIndex: exIndex, setId: setId, weight: value)
        } else {
            viewModel.updateSetReps(exerciseIndex: exIndex, setId: setId, reps: Int(value))
        }
        activeSetId = nil
        activeFieldType = nil
        keypadInput = ""
    }

    private func formatWeightInput(_ v: Double) -> String {
        v.truncatingRemainder(dividingBy: 1) == 0 ? "\(Int(v))" : "\(v)"
    }

    private func formatTime(_ seconds: Int) -> String {
        let h = seconds / 3600
        let m = (seconds % 3600) / 60
        let s = seconds % 60
        return String(format: "%02d:%02d:%02d", h, m, s)
    }

    private func startTimer() {
        timerTask = Task {
            while !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 1_000_000_000)
                if !Task.isCancelled {
                    elapsedSeconds += 1
                }
            }
        }
    }

    private func finishWorkout() async {
        isFinishing = true
        timerTask?.cancel()
        await viewModel.finishWorkout(durationMinutes: elapsedSeconds / 60, context: modelContext)
        isFinishing = false
        dismiss()
    }

    private func showPRCelebration(_ pr: PRCelebrationData) {
        prCelebration = pr
        withAnimation(.spring(response: 0.4, dampingFraction: 0.6)) {
            prCelebrationOpacity = 1
            prScale = 1.0
        }
        Task {
            try? await Task.sleep(nanoseconds: 2_200_000_000)
            withAnimation(.easeOut(duration: 0.4)) {
                prCelebrationOpacity = 0
                prScale = 1.1
            }
            try? await Task.sleep(nanoseconds: 400_000_000)
            prCelebration = nil
            prScale = 0.8
        }
    }
}

// MARK: - Exercise Section View

struct ExerciseSectionView: View {
    let section: WorkoutExerciseSection
    let exerciseIndex: Int
    @Binding var activeSetId: UUID?
    let onTapWeight: (UUID) -> Void
    let onTapReps: (UUID) -> Void
    let onToggleComplete: (UUID) -> Void
    let onAddSet: () -> Void
    let onRemoveSet: () -> Void

    var completedCount: Int { section.sets.filter(\.isCompleted).count }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Exercise header
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(section.exerciseName)
                        .font(.system(size: 17, weight: .bold))
                        .foregroundColor(.white)
                    Text("\(completedCount)/\(section.sets.count) sets complete")
                        .font(.system(size: 12))
                        .foregroundColor(completedCount == section.sets.count ? Color(hex: "#00CC66") : Color(white: 0.5))
                }
                Spacer()

                // Progress ring
                ZStack {
                    Circle()
                        .stroke(Color(white: 0.1), lineWidth: 3)
                    Circle()
                        .trim(from: 0, to: section.sets.isEmpty ? 0 : CGFloat(completedCount) / CGFloat(section.sets.count))
                        .stroke(Color(hex: "#00CC66"), style: StrokeStyle(lineWidth: 3, lineCap: .round))
                        .rotationEffect(.degrees(-90))
                        .animation(.easeInOut(duration: 0.4), value: completedCount)
                }
                .frame(width: 32, height: 32)
            }

            // Column headers
            HStack(spacing: 0) {
                Text("SET")
                    .frame(width: 40, alignment: .center)
                Text("WEIGHT")
                    .frame(maxWidth: .infinity, alignment: .center)
                Text("REPS")
                    .frame(maxWidth: .infinity, alignment: .center)
                Text("✓")
                    .frame(width: 48, alignment: .center)
            }
            .font(.system(size: 10, weight: .semibold))
            .foregroundColor(Color(white: 0.4))
            .tracking(1)

            // Set rows
            VStack(spacing: 6) {
                ForEach(Array(section.sets.enumerated()), id: \.element.id) { setIndex, set in
                    WorkoutSetRow(
                        setNumber: setIndex + 1,
                        set: set,
                        isActive: activeSetId == set.id,
                        onTapWeight: { onTapWeight(set.id) },
                        onTapReps: { onTapReps(set.id) },
                        onToggleComplete: { onToggleComplete(set.id) }
                    )
                }
            }

            // Add/Remove set controls
            HStack(spacing: 12) {
                Button(action: onAddSet) {
                    HStack(spacing: 6) {
                        Image(systemName: "plus")
                            .font(.system(size: 12, weight: .bold))
                        Text("Add Set")
                            .font(.system(size: 13, weight: .semibold))
                    }
                    .foregroundColor(Color(hex: "#0055FF"))
                    .frame(maxWidth: .infinity)
                    .frame(height: 36)
                    .background(Color(hex: "#0055FF").opacity(0.1))
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                    .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color(hex: "#0055FF").opacity(0.2), lineWidth: 1))
                }

                if section.sets.count > 1 {
                    Button(action: onRemoveSet) {
                        HStack(spacing: 6) {
                            Image(systemName: "minus")
                                .font(.system(size: 12, weight: .bold))
                            Text("Remove")
                                .font(.system(size: 13, weight: .semibold))
                        }
                        .foregroundColor(Color(hex: "#FF3333").opacity(0.8))
                        .frame(width: 110)
                        .frame(height: 36)
                        .background(Color(hex: "#FF3333").opacity(0.08))
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                        .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color(hex: "#FF3333").opacity(0.15), lineWidth: 1))
                    }
                }
            }
        }
        .padding(16)
        .background(Color(hex: "#141414"))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(white: 0.1), lineWidth: 1))
    }
}

// MARK: - Workout Set Row

struct WorkoutSetRow: View {
    let setNumber: Int
    let set: WorkoutSet
    let isActive: Bool
    let onTapWeight: () -> Void
    let onTapReps: () -> Void
    let onToggleComplete: () -> Void

    var body: some View {
        HStack(spacing: 0) {
            // Set number
            ZStack {
                if set.isPR {
                    Text("⭐")
                        .font(.system(size: 14))
                } else {
                    Text("\(setNumber)")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(set.isCompleted ? Color(hex: "#00CC66") : Color(white: 0.5))
                }
            }
            .frame(width: 40, alignment: .center)

            // Weight input
            Button(action: onTapWeight) {
                Text(set.weight == 0 ? "—" : formatWeight(set.weight))
                    .font(.system(size: 16, weight: .semibold, design: .monospaced))
                    .foregroundColor(set.isCompleted ? Color(hex: "#00CC66") : .white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 40)
                    .background(
                        RoundedRectangle(cornerRadius: 8)
                            .fill(isActive ? Color(hex: "#0055FF").opacity(0.15) : Color(hex: "#1E1E1E"))
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(isActive ? Color(hex: "#0055FF").opacity(0.5) : Color.clear, lineWidth: 1)
                    )
            }
            .buttonStyle(PlainButtonStyle())
            .padding(.horizontal, 4)

            // Reps input
            Button(action: onTapReps) {
                Text(set.reps == 0 ? "—" : "\(set.reps)")
                    .font(.system(size: 16, weight: .semibold, design: .monospaced))
                    .foregroundColor(set.isCompleted ? Color(hex: "#00CC66") : .white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 40)
                    .background(
                        RoundedRectangle(cornerRadius: 8)
                            .fill(isActive ? Color(hex: "#0055FF").opacity(0.15) : Color(hex: "#1E1E1E"))
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(isActive ? Color(hex: "#0055FF").opacity(0.5) : Color.clear, lineWidth: 1)
                    )
            }
            .buttonStyle(PlainButtonStyle())
            .padding(.horizontal, 4)

            // Complete toggle
            Button(action: onToggleComplete) {
                ZStack {
                    RoundedRectangle(cornerRadius: 8)
                        .fill(set.isCompleted ? Color(hex: "#00CC66") : Color(hex: "#1E1E1E"))
                        .frame(width: 36, height: 36)
                    Image(systemName: set.isCompleted ? "checkmark" : "circle")
                        .font(.system(size: set.isCompleted ? 14 : 18, weight: .bold))
                        .foregroundColor(set.isCompleted ? .white : Color(white: 0.3))
                }
                .animation(.spring(response: 0.3, dampingFraction: 0.7), value: set.isCompleted)
            }
            .buttonStyle(PlainButtonStyle())
            .frame(width: 48, alignment: .center)
        }
        .background(
            set.isCompleted
                ? Color(hex: "#00CC66").opacity(0.05)
                : Color.clear
        )
        .clipShape(RoundedRectangle(cornerRadius: 10))
    }

    private func formatWeight(_ w: Double) -> String {
        w.truncatingRemainder(dividingBy: 1) == 0 ? "\(Int(w))" : String(format: "%.1f", w)
    }
}

// MARK: - Numeric Keypad

struct NumericKeypadView: View {
    @Binding var input: String
    let fieldType: ActiveWorkoutView.FieldType
    let onDone: () -> Void
    let onDismiss: () -> Void

    private let keys: [[String]] = [
        ["7", "8", "9"],
        ["4", "5", "6"],
        ["1", "2", "3"],
        [".", "0", "⌫"]
    ]

    var body: some View {
        VStack(spacing: 0) {
            // Input display
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(fieldType == .weight ? "WEIGHT" : "REPS")
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundColor(Color(white: 0.4))
                        .tracking(2)
                    Text(input.isEmpty ? "0" : input)
                        .font(.system(size: 32, weight: .bold, design: .monospaced))
                        .foregroundColor(.white)
                }
                Spacer()
                Button("Done") {
                    onDone()
                }
                .font(.system(size: 17, weight: .bold))
                .foregroundColor(.white)
                .padding(.horizontal, 24)
                .padding(.vertical, 10)
                .background(LinearGradient(
                    colors: [Color(hex: "#0055FF"), Color(hex: "#0033CC")],
                    startPoint: .leading, endPoint: .trailing
                ))
                .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 14)
            .background(Color(hex: "#1E1E1E"))

            Divider().background(Color(white: 0.1))

            // Keypad grid
            VStack(spacing: 1) {
                ForEach(keys, id: \.self) { row in
                    HStack(spacing: 1) {
                        ForEach(row, id: \.self) { key in
                            KeypadButton(
                                label: key,
                                isDisabled: key == "." && (fieldType == .reps || input.contains("."))
                            ) {
                                handleKey(key)
                            }
                        }
                    }
                }
            }
            .background(Color(white: 0.08))

            Color(hex: "#141414").frame(height: 34) // Safe area padding
        }
        .background(Color(hex: "#141414"))
    }

    private func handleKey(_ key: String) {
        switch key {
        case "⌫":
            if !input.isEmpty { input.removeLast() }
        case ".":
            if !input.contains(".") && fieldType == .weight {
                if input.isEmpty { input = "0" }
                input.append(".")
            }
        default:
            if input.count < 6 {
                if input == "0" && key != "." { input = key }
                else { input.append(key) }
            }
        }
    }
}

struct KeypadButton: View {
    let label: String
    let isDisabled: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            ZStack {
                if label == "⌫" {
                    Image(systemName: "delete.left")
                        .font(.system(size: 20, weight: .medium))
                        .foregroundColor(isDisabled ? Color(white: 0.2) : Color(white: 0.7))
                } else {
                    Text(label)
                        .font(.system(size: 24, weight: .medium))
                        .foregroundColor(isDisabled ? Color(white: 0.2) : .white)
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 58)
            .background(
                label == "⌫" ? Color(hex: "#1A1A1A") :
                label == "." ? Color(hex: "#181818") :
                Color(hex: "#141414")
            )
            .contentShape(Rectangle())
        }
        .buttonStyle(PlainButtonStyle())
        .disabled(isDisabled)
    }
}

// MARK: - PR Celebration Overlay

struct PRCelebrationOverlay: View {
    let data: PRCelebrationData

    var body: some View {
        ZStack {
            Color.black.opacity(0.85)

            VStack(spacing: 20) {
                Text("🏆")
                    .font(.system(size: 72))

                VStack(spacing: 8) {
                    Text("NEW PERSONAL RECORD!")
                        .font(.system(size: 20, weight: .black))
                        .foregroundColor(Color(hex: "#FF6600"))
                        .tracking(2)

                    Text(data.exerciseName)
                        .font(.system(size: 24, weight: .bold))
                        .foregroundColor(.white)

                    Text(data.recordType)
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(Color(white: 0.7))

                    HStack(spacing: 4) {
                        Text(formatValue(data.value))
                            .font(.system(size: 36, weight: .black, design: .monospaced))
                            .foregroundColor(Color(hex: "#FF6600"))
                    }
                }
                .padding(.horizontal, 32)
                .padding(.vertical, 28)
                .background(Color(hex: "#1E1E1E"))
                .clipShape(RoundedRectangle(cornerRadius: 24))
                .overlay(RoundedRectangle(cornerRadius: 24).stroke(Color(hex: "#FF6600").opacity(0.4), lineWidth: 2))
            }
        }
        .ignoresSafeArea()
    }

    private func formatValue(_ v: Double) -> String {
        v.truncatingRemainder(dividingBy: 1) == 0 ? "\(Int(v))" : String(format: "%.1f", v)
    }
}

#Preview {
    NavigationStack {
        ActiveWorkoutView(routineId: 1, userId: 1)
    }
    .modelContainer(for: [], inMemory: true)
}
