import SwiftUI
import SwiftData

struct ImportRoutineView: View {
    let userId: Int
    @State private var viewModel = ImportViewModel()
    @State private var apiKeyManager = APIKeyManager()
    @Environment(\.modelContext) private var modelContext

    @State private var selectedMode: ImportMode = .url
    @State private var urlInput: String = ""
    @State private var textInput: String = ""
    @State private var editedRoutineName: String = ""
    @State private var navigateToSettings: Bool = false

    var body: some View {
        ZStack {
            Color(hex: "#0A0A0A").ignoresSafeArea()

            ScrollView {
                VStack(spacing: 20) {
                    // Provider badge
                    providerBadge

                    // Mode picker
                    modePicker

                    // Input area
                    inputSection

                    // State views
                    Group {
                        switch viewModel.importState {
                        case .idle:
                            EmptyView()
                        case .loading:
                            loadingView
                        case .preview(let routine):
                            previewView(routine)
                        case .error(let message):
                            errorView(message)
                        case .success:
                            successView
                        }
                    }
                    .animation(.easeInOut(duration: 0.3), value: viewModel.importStateTag)

                    Color.clear.frame(height: 40)
                }
                .padding(.top, 16)
            }
        }
        .navigationTitle("Import Routine")
        .navigationBarTitleDisplayMode(.large)
        .navigationDestination(isPresented: $navigateToSettings) {
            SettingsView(userId: userId, onLogout: {})
        }
    }

    // MARK: - Provider Badge

    private var providerBadge: some View {
        Button {
            navigateToSettings = true
        } label: {
            HStack(spacing: 10) {
                Image(systemName: "sparkles")
                    .font(.system(size: 14))
                    .foregroundColor(Color(hex: "#FF6600"))
                VStack(alignment: .leading, spacing: 1) {
                    Text("AI Provider")
                        .font(.system(size: 11))
                        .foregroundColor(Color(white: 0.4))
                    Text(apiKeyManager.currentProviderName)
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(.white)
                }
                Spacer()
                Text(apiKeyManager.hasActiveKey ? "Configured" : "Tap to configure")
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(apiKeyManager.hasActiveKey ? Color(hex: "#00CC66") : Color(hex: "#FF3333"))
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .background(
                        (apiKeyManager.hasActiveKey ? Color(hex: "#00CC66") : Color(hex: "#FF3333")).opacity(0.1)
                    )
                    .clipShape(Capsule())
                Image(systemName: "chevron.right")
                    .font(.system(size: 12))
                    .foregroundColor(Color(white: 0.3))
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(Color(hex: "#141414"))
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color(white: 0.1), lineWidth: 1))
        }
        .buttonStyle(PlainButtonStyle())
        .padding(.horizontal, 16)
    }

    // MARK: - Mode Picker

    private var modePicker: some View {
        HStack(spacing: 1) {
            ForEach(ImportMode.allCases, id: \.self) { mode in
                Button {
                    withAnimation(.easeInOut(duration: 0.2)) {
                        selectedMode = mode
                        viewModel.importState = .idle
                    }
                } label: {
                    HStack(spacing: 8) {
                        Image(systemName: mode == .url ? "link" : "text.alignleft")
                            .font(.system(size: 14))
                        Text(mode.rawValue)
                            .font(.system(size: 15, weight: selectedMode == mode ? .semibold : .regular))
                    }
                    .foregroundColor(selectedMode == mode ? .white : Color(white: 0.5))
                    .frame(maxWidth: .infinity)
                    .frame(height: 42)
                    .background(
                        selectedMode == mode
                            ? LinearGradient(colors: [Color(hex: "#0055FF"), Color(hex: "#0044CC")], startPoint: .leading, endPoint: .trailing)
                            : LinearGradient(colors: [Color.clear, Color.clear], startPoint: .leading, endPoint: .trailing)
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 10))
                }
                .buttonStyle(PlainButtonStyle())
            }
        }
        .padding(4)
        .background(Color(hex: "#141414"))
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color(white: 0.1), lineWidth: 1))
        .padding(.horizontal, 16)
    }

    // MARK: - Input Section

    private var inputSection: some View {
        VStack(spacing: 14) {
            if selectedMode == .url {
                // URL input
                VStack(alignment: .leading, spacing: 8) {
                    Text("WORKOUT URL")
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundColor(Color(white: 0.4))
                        .tracking(2)

                    HStack(spacing: 10) {
                        Image(systemName: "link")
                            .foregroundColor(Color(hex: "#0055FF"))
                            .frame(width: 20)
                        TextField("", text: $urlInput, prompt: Text("https://...")
                            .foregroundColor(Color(white: 0.3)))
                            .foregroundColor(.white)
                            .autocorrectionDisabled()
                            .textInputAutocapitalization(.never)
                            .keyboardType(.URL)
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 14)
                    .background(Color(hex: "#141414"))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(urlInput.isEmpty ? Color(white: 0.15) : Color(hex: "#0055FF").opacity(0.4), lineWidth: 1)
                    )
                }

                importButton(
                    title: "Import from URL",
                    icon: "arrow.down.circle.fill",
                    isEnabled: !urlInput.isEmpty && apiKeyManager.hasActiveKey
                ) {
                    Task {
                        await viewModel.importFromURL(urlInput, userId: userId, context: modelContext)
                    }
                }

            } else {
                // Text input
                VStack(alignment: .leading, spacing: 8) {
                    Text("DESCRIBE YOUR ROUTINE")
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundColor(Color(white: 0.4))
                        .tracking(2)

                    ZStack(alignment: .topLeading) {
                        TextEditor(text: $textInput)
                            .foregroundColor(.white)
                            .font(.system(size: 15))
                            .frame(minHeight: 140)
                            .scrollContentBackground(.hidden)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 12)

                        if textInput.isEmpty {
                            Text("e.g. 5x5 Stronglifts: Squat, Bench, Barbell Row, Overhead Press, Deadlift...")
                                .foregroundColor(Color(white: 0.25))
                                .font(.system(size: 15))
                                .padding(.horizontal, 16)
                                .padding(.top, 16)
                                .allowsHitTesting(false)
                        }
                    }
                    .background(Color(hex: "#141414"))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(textInput.isEmpty ? Color(white: 0.15) : Color(hex: "#0055FF").opacity(0.4), lineWidth: 1)
                    )
                }

                importButton(
                    title: "Generate Routine",
                    icon: "wand.and.stars",
                    isEnabled: !textInput.isEmpty && apiKeyManager.hasActiveKey
                ) {
                    Task {
                        await viewModel.importFromText(textInput, userId: userId, context: modelContext)
                    }
                }
            }

            if !apiKeyManager.hasActiveKey {
                HStack(spacing: 8) {
                    Image(systemName: "info.circle.fill")
                        .foregroundColor(Color(hex: "#FF6600"))
                        .font(.system(size: 14))
                    Text("Configure an AI provider above to use this feature")
                        .font(.system(size: 13))
                        .foregroundColor(Color(white: 0.5))
                }
                .padding(.horizontal, 14)
                .padding(.vertical, 10)
                .background(Color(hex: "#FF6600").opacity(0.08))
                .clipShape(RoundedRectangle(cornerRadius: 10))
                .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color(hex: "#FF6600").opacity(0.15), lineWidth: 1))
            }
        }
        .padding(.horizontal, 16)
    }

    @ViewBuilder
    private func importButton(title: String, icon: String, isEnabled: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack(spacing: 10) {
                Image(systemName: icon)
                    .font(.system(size: 16))
                Text(title)
                    .font(.system(size: 16, weight: .semibold))
            }
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .frame(height: 52)
            .background(
                isEnabled
                    ? LinearGradient(colors: [Color(hex: "#0055FF"), Color(hex: "#0033CC")], startPoint: .leading, endPoint: .trailing)
                    : LinearGradient(colors: [Color(white: 0.2), Color(white: 0.15)], startPoint: .leading, endPoint: .trailing)
            )
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .shadow(color: isEnabled ? Color(hex: "#0055FF").opacity(0.35) : .clear, radius: 10, y: 4)
        }
        .disabled(!isEnabled)
        .animation(.easeInOut(duration: 0.2), value: isEnabled)
    }

    // MARK: - Loading View

    private var loadingView: some View {
        VStack(spacing: 20) {
            ZStack {
                Circle()
                    .stroke(Color(white: 0.1), lineWidth: 3)
                    .frame(width: 60, height: 60)
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: Color(hex: "#0055FF")))
                    .scaleEffect(1.5)
            }

            VStack(spacing: 6) {
                Text("Analyzing workout...")
                    .font(.system(size: 17, weight: .semibold))
                    .foregroundColor(.white)
                Text("This may take a few seconds")
                    .font(.system(size: 13))
                    .foregroundColor(Color(white: 0.5))
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 40)
        .padding(.horizontal, 16)
        .background(Color(hex: "#141414"))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(white: 0.1), lineWidth: 1))
        .padding(.horizontal, 16)
    }

    // MARK: - Preview View

    @ViewBuilder
    private func previewView(_ routine: ImportedRoutine) -> some View {
        VStack(alignment: .leading, spacing: 16) {
            // Header
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("PREVIEW")
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundColor(Color(hex: "#00CC66"))
                        .tracking(2)
                    Text("Review your imported routine")
                        .font(.system(size: 13))
                        .foregroundColor(Color(white: 0.5))
                }
                Spacer()
                Image(systemName: "checkmark.circle.fill")
                    .font(.system(size: 24))
                    .foregroundColor(Color(hex: "#00CC66"))
            }

            Divider().background(Color(white: 0.1))

            // Editable routine name
            VStack(alignment: .leading, spacing: 6) {
                Text("ROUTINE NAME")
                    .font(.system(size: 10, weight: .semibold))
                    .foregroundColor(Color(white: 0.4))
                    .tracking(2)
                TextField("", text: $editedRoutineName, prompt: Text(routine.name)
                    .foregroundColor(Color(white: 0.3)))
                    .foregroundColor(.white)
                    .font(.system(size: 16, weight: .semibold))
                    .padding(.horizontal, 14)
                    .padding(.vertical, 12)
                    .background(Color(hex: "#1E1E1E"))
                    .clipShape(RoundedRectangle(cornerRadius: 10))
                    .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color(white: 0.12), lineWidth: 1))
            }

            // Exercise list
            VStack(alignment: .leading, spacing: 8) {
                Text("\(routine.exercises.count) EXERCISES")
                    .font(.system(size: 10, weight: .semibold))
                    .foregroundColor(Color(white: 0.4))
                    .tracking(2)

                ForEach(Array(routine.exercises.enumerated()), id: \.offset) { index, ex in
                    ImportedExerciseRow(index: index + 1, exercise: ex)
                }
            }

            Divider().background(Color(white: 0.1))

            // Action buttons
            HStack(spacing: 12) {
                Button {
                    viewModel.importState = .idle
                    editedRoutineName = ""
                } label: {
                    Text("Try Again")
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(Color(white: 0.6))
                        .frame(maxWidth: .infinity)
                        .frame(height: 48)
                        .background(Color(hex: "#1E1E1E"))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }

                Button {
                    let name = editedRoutineName.isEmpty ? routine.name : editedRoutineName
                    Task {
                        await viewModel.saveImportedRoutine(
                            routine: routine,
                            customName: name,
                            userId: userId,
                            context: modelContext
                        )
                    }
                } label: {
                    HStack(spacing: 8) {
                        Image(systemName: "square.and.arrow.down.fill")
                            .font(.system(size: 14))
                        Text("Save Routine")
                            .font(.system(size: 15, weight: .bold))
                    }
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 48)
                    .background(
                        LinearGradient(colors: [Color(hex: "#0055FF"), Color(hex: "#0033CC")], startPoint: .leading, endPoint: .trailing)
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
            }
        }
        .padding(16)
        .background(Color(hex: "#141414"))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "#00CC66").opacity(0.2), lineWidth: 1))
        .padding(.horizontal, 16)
        .onAppear {
            editedRoutineName = routine.name
        }
    }

    // MARK: - Error View

    private func errorView(_ message: String) -> some View {
        VStack(spacing: 16) {
            ZStack {
                Circle()
                    .fill(Color(hex: "#FF3333").opacity(0.1))
                    .frame(width: 64, height: 64)
                Image(systemName: "exclamationmark.circle.fill")
                    .font(.system(size: 28))
                    .foregroundColor(Color(hex: "#FF3333"))
            }

            VStack(spacing: 8) {
                Text("Import Failed")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.white)
                Text(message)
                    .font(.system(size: 13))
                    .foregroundColor(Color(white: 0.5))
                    .multilineTextAlignment(.center)
                    .lineSpacing(3)
            }

            Button {
                viewModel.importState = .idle
            } label: {
                Text("Try Again")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(.white)
                    .frame(width: 140, height: 44)
                    .background(Color(hex: "#1E1E1E"))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color(white: 0.12), lineWidth: 1))
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 32)
        .padding(.horizontal, 24)
        .background(Color(hex: "#141414"))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "#FF3333").opacity(0.2), lineWidth: 1))
        .padding(.horizontal, 16)
    }

    // MARK: - Success View

    private var successView: some View {
        VStack(spacing: 20) {
            ZStack {
                Circle()
                    .fill(Color(hex: "#00CC66").opacity(0.15))
                    .frame(width: 80, height: 80)
                Image(systemName: "checkmark.circle.fill")
                    .font(.system(size: 40))
                    .foregroundColor(Color(hex: "#00CC66"))
            }

            VStack(spacing: 8) {
                Text("Routine Saved!")
                    .font(.system(size: 22, weight: .bold))
                    .foregroundColor(.white)
                Text("Your routine has been imported\nand is ready to use")
                    .font(.system(size: 14))
                    .foregroundColor(Color(white: 0.5))
                    .multilineTextAlignment(.center)
            }

            Button {
                viewModel.importState = .idle
                urlInput = ""
                textInput = ""
                editedRoutineName = ""
            } label: {
                Text("Import Another")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(Color(hex: "#0055FF"))
                    .frame(width: 160, height: 44)
                    .background(Color(hex: "#0055FF").opacity(0.1))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color(hex: "#0055FF").opacity(0.3), lineWidth: 1))
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 32)
        .background(Color(hex: "#141414"))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "#00CC66").opacity(0.2), lineWidth: 1))
        .padding(.horizontal, 16)
    }
}

// MARK: - Imported Exercise Row

struct ImportedExerciseRow: View {
    let index: Int
    let exercise: ImportedExercise

    var body: some View {
        HStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(Color(hex: "#0055FF").opacity(0.15))
                    .frame(width: 32, height: 32)
                Text("\(index)")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(Color(hex: "#0055FF"))
            }

            VStack(alignment: .leading, spacing: 3) {
                Text(exercise.name)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(.white)
                HStack(spacing: 8) {
                    Text("\(exercise.sets)×\(exercise.reps)")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(Color(hex: "#0055FF").opacity(0.9))
                    if !exercise.muscleGroup.isEmpty {
                        Text("·")
                            .foregroundColor(Color(white: 0.3))
                        Text(exercise.muscleGroup)
                            .font(.system(size: 12))
                            .foregroundColor(Color(white: 0.4))
                    }
                }
            }

            Spacer()

            if !exercise.equipment.isEmpty {
                Text(exercise.equipment)
                    .font(.system(size: 11, weight: .medium))
                    .foregroundColor(Color(white: 0.4))
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color(hex: "#1E1E1E"))
                    .clipShape(Capsule())
            }
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 10)
        .background(Color(hex: "#1E1E1E"))
        .clipShape(RoundedRectangle(cornerRadius: 10))
    }
}

// MARK: - Import State Tag (for animation value)

extension ImportState: Equatable {
    static func == (lhs: ImportState, rhs: ImportState) -> Bool {
        switch (lhs, rhs) {
        case (.idle, .idle): return true
        case (.loading, .loading): return true
        case (.preview, .preview): return true
        case (.error, .error): return true
        case (.success, .success): return true
        default: return false
        }
    }
}

extension ImportViewModel {
    var importStateTag: Int {
        switch importState {
        case .idle: return 0
        case .loading: return 1
        case .preview: return 2
        case .error: return 3
        case .success: return 4
        }
    }
}

#Preview {
    NavigationStack {
        ImportRoutineView(userId: 1)
    }
    .modelContainer(for: [], inMemory: true)
}
