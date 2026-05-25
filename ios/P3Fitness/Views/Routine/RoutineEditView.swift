import SwiftUI
import SwiftData

struct RoutineEditView: View {
    let routineId: Int
    let userId: Int
    @State private var viewModel = RoutineViewModel()
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss

    @State private var routineName: String = ""
    @State private var exercises: [RoutineExerciseEdit] = []
    @State private var showExercisePicker: Bool = false
    @State private var isSaving: Bool = false
    @State private var showSavedBanner: Bool = false

    var body: some View {
        ZStack {
            Color(hex: "#0A0A0A").ignoresSafeArea()

            VStack(spacing: 0) {
                // Routine name field
                VStack(alignment: .leading, spacing: 6) {
                    Text("ROUTINE NAME")
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundColor(Color(white: 0.4))
                        .tracking(2)
                    TextField("", text: $routineName, prompt: Text("e.g. Push Day, Leg Day...")
                        .foregroundColor(Color(white: 0.3)))
                        .foregroundColor(.white)
                        .font(.system(size: 18, weight: .semibold))
                        .padding(.horizontal, 16)
                        .padding(.vertical, 14)
                        .background(Color(hex: "#141414"))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color(white: 0.15), lineWidth: 1)
                        )
                }
                .padding(.horizontal, 16)
                .padding(.top, 16)
                .padding(.bottom, 12)

                Divider().background(Color(white: 0.1))

                // Exercise list header
                HStack {
                    Text("EXERCISES")
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundColor(Color(white: 0.4))
                        .tracking(2)
                    Spacer()
                    Text("\(exercises.count)")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(Color(hex: "#0055FF"))
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)

                if exercises.isEmpty {
                    emptyExercisesState
                } else {
                    exerciseList
                }

                Spacer()

                // Add exercise button
                Button {
                    showExercisePicker = true
                } label: {
                    HStack(spacing: 10) {
                        Image(systemName: "plus.circle.fill")
                            .font(.system(size: 18))
                        Text("Add Exercise")
                            .font(.system(size: 16, weight: .semibold))
                    }
                    .foregroundColor(Color(hex: "#0055FF"))
                    .frame(maxWidth: .infinity)
                    .frame(height: 52)
                    .background(Color(hex: "#0055FF").opacity(0.1))
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                    .overlay(
                        RoundedRectangle(cornerRadius: 14)
                            .stroke(Color(hex: "#0055FF").opacity(0.3), lineWidth: 1)
                    )
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 16)
            }

            // Saved banner
            if showSavedBanner {
                VStack {
                    Spacer()
                    HStack(spacing: 10) {
                        Image(systemName: "checkmark.circle.fill")
                            .foregroundColor(Color(hex: "#00CC66"))
                        Text("Routine saved!")
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundColor(.white)
                    }
                    .padding(.horizontal, 24)
                    .padding(.vertical, 14)
                    .background(Color(hex: "#1E1E1E"))
                    .clipShape(Capsule())
                    .shadow(color: .black.opacity(0.4), radius: 12, y: 4)
                    .padding(.bottom, 100)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                }
            }
        }
        .navigationTitle("Edit Routine")
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(false)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button {
                    saveRoutine()
                } label: {
                    if isSaving {
                        ProgressView()
                            .tint(Color(hex: "#0055FF"))
                            .scaleEffect(0.8)
                    } else {
                        Text("Save")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundColor(Color(hex: "#0055FF"))
                    }
                }
                .disabled(routineName.isEmpty || isSaving)
            }
        }
        .sheet(isPresented: $showExercisePicker) {
            ExercisePickerSheet(userId: userId) { selected in
                addExercises(selected)
            }
        }
        .onAppear {
            Task {
                await viewModel.loadRoutineForEdit(routineId: routineId, context: modelContext)
                routineName = viewModel.editingRoutineName
                exercises = viewModel.editingExercises
            }
        }
    }

    private var emptyExercisesState: some View {
        VStack(spacing: 20) {
            Spacer()
            Image(systemName: "list.bullet.clipboard")
                .font(.system(size: 44))
                .foregroundColor(Color(white: 0.2))
            Text("No exercises yet")
                .font(.system(size: 17, weight: .semibold))
                .foregroundColor(Color(white: 0.4))
            Text("Tap \"Add Exercise\" to build your routine")
                .font(.system(size: 14))
                .foregroundColor(Color(white: 0.3))
                .multilineTextAlignment(.center)
            Spacer()
        }
        .padding(.horizontal, 32)
    }

    private var exerciseList: some View {
        List {
            ForEach($exercises) { $exercise in
                ExerciseEditRow(exercise: $exercise) {
                    if let index = exercises.firstIndex(where: { $0.id == exercise.id }) {
                        withAnimation {
                            exercises.remove(at: index)
                        }
                    }
                }
                .listRowBackground(Color(hex: "#141414"))
                .listRowSeparatorTint(Color(white: 0.1))
                .listRowInsets(EdgeInsets(top: 4, leading: 16, bottom: 4, trailing: 16))
            }
            .onMove { from, to in
                exercises.move(fromOffsets: from, toOffset: to)
                for (index, _) in exercises.enumerated() {
                    exercises[index].orderIndex = index
                }
            }
            .onDelete { offsets in
                withAnimation {
                    exercises.remove(atOffsets: offsets)
                }
            }
        }
        .listStyle(.plain)
        .environment(\.editMode, .constant(.active))
        .scrollContentBackground(.hidden)
        .background(Color(hex: "#0A0A0A"))
    }

    private func addExercises(_ selected: [Exercise]) {
        let currentMax = exercises.map(\.orderIndex).max() ?? -1
        for (i, ex) in selected.enumerated() {
            let edit = RoutineExerciseEdit(
                id: -(exercises.count + i + 1),
                exerciseId: ex.id,
                exerciseName: ex.name,
                orderIndex: currentMax + i + 1,
                defaultSets: 3,
                defaultReps: 10
            )
            exercises.append(edit)
        }
    }

    private func saveRoutine() {
        guard !routineName.isEmpty else { return }
        isSaving = true
        Task {
            await viewModel.saveRoutineEdits(
                routineId: routineId,
                name: routineName,
                exercises: exercises,
                context: modelContext
            )
            isSaving = false
            withAnimation(.spring(response: 0.4, dampingFraction: 0.7)) {
                showSavedBanner = true
            }
            try? await Task.sleep(nanoseconds: 2_000_000_000)
            withAnimation {
                showSavedBanner = false
            }
        }
    }
}

// MARK: - Exercise Edit Row

struct ExerciseEditRow: View {
    @Binding var exercise: RoutineExerciseEdit
    let onDelete: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            // Drag handle
            Image(systemName: "line.3.horizontal")
                .font(.system(size: 16))
                .foregroundColor(Color(white: 0.3))

            VStack(alignment: .leading, spacing: 4) {
                Text(exercise.exerciseName)
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(.white)

                HStack(spacing: 16) {
                    // Sets stepper
                    HStack(spacing: 6) {
                        Text("Sets:")
                            .font(.system(size: 12))
                            .foregroundColor(Color(white: 0.5))
                        Stepper(value: $exercise.defaultSets, in: 1...20) {
                            Text("\(exercise.defaultSets)")
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundColor(Color(hex: "#0055FF"))
                                .frame(minWidth: 20)
                        }
                        .labelsHidden()
                        .scaleEffect(0.85)
                    }

                    // Reps stepper
                    HStack(spacing: 6) {
                        Text("Reps:")
                            .font(.system(size: 12))
                            .foregroundColor(Color(white: 0.5))
                        Stepper(value: $exercise.defaultReps, in: 1...100) {
                            Text("\(exercise.defaultReps)")
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundColor(Color(hex: "#0055FF"))
                                .frame(minWidth: 20)
                        }
                        .labelsHidden()
                        .scaleEffect(0.85)
                    }
                }
            }

            Spacer()

            Button(action: onDelete) {
                Image(systemName: "xmark.circle.fill")
                    .font(.system(size: 20))
                    .foregroundColor(Color(hex: "#FF3333").opacity(0.7))
            }
            .buttonStyle(PlainButtonStyle())
        }
        .padding(.vertical, 8)
    }
}

// MARK: - Exercise Picker Sheet

struct ExercisePickerSheet: View {
    let userId: Int
    let onSelect: ([Exercise]) -> Void
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss

    @State private var searchText: String = ""
    @State private var selectedMuscleGroup: String = "All"
    @State private var allExercises: [Exercise] = []
    @State private var selectedIds: Set<Int> = []
    @State private var isLoading: Bool = true

    private let muscleGroups = ["All", "Chest", "Back", "Shoulders", "Arms", "Legs", "Core", "Cardio", "Full Body"]

    var filteredExercises: [Exercise] {
        allExercises.filter { ex in
            let matchesSearch = searchText.isEmpty || ex.name.localizedCaseInsensitiveContains(searchText)
            let matchesGroup = selectedMuscleGroup == "All" || ex.muscleGroup == selectedMuscleGroup
            return matchesSearch && matchesGroup
        }
    }

    var body: some View {
        NavigationStack {
            ZStack {
                Color(hex: "#0A0A0A").ignoresSafeArea()

                VStack(spacing: 0) {
                    // Search bar
                    HStack(spacing: 10) {
                        Image(systemName: "magnifyingglass")
                            .foregroundColor(Color(white: 0.4))
                        TextField("", text: $searchText, prompt: Text("Search exercises...")
                            .foregroundColor(Color(white: 0.3)))
                            .foregroundColor(.white)
                            .autocorrectionDisabled()
                    }
                    .padding(.horizontal, 14)
                    .padding(.vertical, 12)
                    .background(Color(hex: "#141414"))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color(white: 0.12), lineWidth: 1))
                    .padding(.horizontal, 16)
                    .padding(.top, 12)
                    .padding(.bottom, 10)

                    // Muscle group filter chips
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(muscleGroups, id: \.self) { group in
                                MuscleGroupChip(
                                    label: group,
                                    isSelected: selectedMuscleGroup == group,
                                    action: { selectedMuscleGroup = group }
                                )
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                    .padding(.bottom, 10)

                    Divider().background(Color(white: 0.1))

                    // Exercise list
                    if isLoading {
                        Spacer()
                        ProgressView()
                            .tint(Color(hex: "#0055FF"))
                        Spacer()
                    } else if filteredExercises.isEmpty {
                        Spacer()
                        VStack(spacing: 12) {
                            Image(systemName: "magnifyingglass")
                                .font(.system(size: 32))
                                .foregroundColor(Color(white: 0.2))
                            Text("No exercises found")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(Color(white: 0.4))
                        }
                        Spacer()
                    } else {
                        List(filteredExercises) { exercise in
                            ExercisePickerRow(
                                exercise: exercise,
                                isSelected: selectedIds.contains(exercise.id),
                                onToggle: {
                                    if selectedIds.contains(exercise.id) {
                                        selectedIds.remove(exercise.id)
                                    } else {
                                        selectedIds.insert(exercise.id)
                                    }
                                }
                            )
                            .listRowBackground(Color(hex: "#141414"))
                            .listRowSeparatorTint(Color(white: 0.08))
                            .listRowInsets(EdgeInsets(top: 2, leading: 16, bottom: 2, trailing: 16))
                        }
                        .listStyle(.plain)
                        .scrollContentBackground(.hidden)
                    }

                    // Add selected button
                    if !selectedIds.isEmpty {
                        Button {
                            let selected = allExercises.filter { selectedIds.contains($0.id) }
                            onSelect(selected)
                            dismiss()
                        } label: {
                            Text("Add \(selectedIds.count) Exercise\(selectedIds.count == 1 ? "" : "s")")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .frame(height: 52)
                                .background(
                                    LinearGradient(
                                        colors: [Color(hex: "#0055FF"), Color(hex: "#0033CC")],
                                        startPoint: .leading,
                                        endPoint: .trailing
                                    )
                                )
                                .clipShape(RoundedRectangle(cornerRadius: 14))
                        }
                        .padding(.horizontal, 16)
                        .padding(.bottom, 16)
                        .transition(.move(edge: .bottom).combined(with: .opacity))
                    }
                }
            }
            .navigationTitle("Add Exercises")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") { dismiss() }
                        .foregroundColor(Color(hex: "#0055FF"))
                }
            }
        }
        .preferredColorScheme(.dark)
        .onAppear {
            Task {
                allExercises = (try? modelContext.fetch(FetchDescriptor<Exercise>(sortBy: [SortDescriptor(\.name)]))) ?? []
                isLoading = false
            }
        }
    }
}

struct MuscleGroupChip: View {
    let label: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(label)
                .font(.system(size: 13, weight: isSelected ? .semibold : .regular))
                .foregroundColor(isSelected ? .white : Color(white: 0.6))
                .padding(.horizontal, 14)
                .padding(.vertical, 7)
                .background(
                    isSelected
                        ? LinearGradient(colors: [Color(hex: "#0055FF"), Color(hex: "#0044CC")], startPoint: .leading, endPoint: .trailing)
                        : LinearGradient(colors: [Color(hex: "#1E1E1E"), Color(hex: "#1E1E1E")], startPoint: .leading, endPoint: .trailing)
                )
                .clipShape(Capsule())
                .overlay(Capsule().stroke(isSelected ? Color.clear : Color(white: 0.12), lineWidth: 1))
        }
        .buttonStyle(PlainButtonStyle())
    }
}

struct ExercisePickerRow: View {
    let exercise: Exercise
    let isSelected: Bool
    let onToggle: () -> Void

    var body: some View {
        Button(action: onToggle) {
            HStack(spacing: 14) {
                ZStack {
                    Circle()
                        .fill(isSelected ? Color(hex: "#0055FF") : Color(hex: "#1E1E1E"))
                        .frame(width: 28, height: 28)
                    if isSelected {
                        Image(systemName: "checkmark")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(.white)
                    }
                }
                .animation(.spring(response: 0.3, dampingFraction: 0.7), value: isSelected)

                VStack(alignment: .leading, spacing: 3) {
                    Text(exercise.name)
                        .font(.system(size: 15, weight: .medium))
                        .foregroundColor(.white)
                    HStack(spacing: 8) {
                        Text(exercise.muscleGroup)
                            .font(.system(size: 12))
                            .foregroundColor(Color(hex: "#0055FF").opacity(0.9))
                        if !exercise.equipment.isEmpty {
                            Text("·")
                                .foregroundColor(Color(white: 0.3))
                            Text(exercise.equipment)
                                .font(.system(size: 12))
                                .foregroundColor(Color(white: 0.4))
                        }
                    }
                }
                Spacer()
            }
            .padding(.vertical, 8)
            .contentShape(Rectangle())
        }
        .buttonStyle(PlainButtonStyle())
    }
}

#Preview {
    NavigationStack {
        RoutineEditView(routineId: 1, userId: 1)
    }
    .modelContainer(for: [], inMemory: true)
}
