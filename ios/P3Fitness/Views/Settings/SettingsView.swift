import SwiftUI
import SwiftData

struct SettingsView: View {
    let userId: Int
    let onLogout: () -> Void
    @State private var viewModel = SettingsViewModel()
    @State private var apiKeyManager = APIKeyManager()
    @Environment(\.modelContext) private var modelContext

    @State private var selectedProvider: AIProvider = .anthropic
    @State private var apiKeyInput: String = ""
    @State private var showApiKey: Bool = false
    @State private var showLogoutConfirm: Bool = false
    @State private var showShareSheet: Bool = false
    @State private var exportURL: URL? = nil
    @State private var keySavedBanner: Bool = false
    @State private var unitSaveBanner: Bool = false

    var body: some View {
        ZStack {
            Color(hex: "#0A0A0A").ignoresSafeArea()

            ScrollView {
                VStack(spacing: 24) {
                    // User card
                    userCard

                    // Units section
                    settingsSection(title: "UNITS") {
                        unitsToggle
                    }

                    // AI Provider section
                    settingsSection(title: "AI PROVIDER") {
                        aiProviderSection
                    }

                    // Data section
                    settingsSection(title: "DATA") {
                        dataSection
                    }

                    // Navigation links
                    settingsSection(title: "ACHIEVEMENTS") {
                        NavigationLink(destination: AchievementsView(userId: userId)) {
                            SettingsRow(icon: "trophy.fill", iconColor: Color(hex: "#FF6600"), title: "Achievements") {
                                Image(systemName: "chevron.right")
                                    .font(.system(size: 13))
                                    .foregroundColor(Color(white: 0.3))
                            }
                        }
                        .buttonStyle(PlainButtonStyle())
                    }

                    // Sign out
                    Button {
                        showLogoutConfirm = true
                    } label: {
                        HStack(spacing: 12) {
                            Image(systemName: "rectangle.portrait.and.arrow.right")
                                .font(.system(size: 16))
                                .foregroundColor(Color(hex: "#FF3333"))
                            Text("Sign Out")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(Color(hex: "#FF3333"))
                            Spacer()
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 14)
                        .background(Color(hex: "#141414"))
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color(hex: "#FF3333").opacity(0.2), lineWidth: 1))
                    }
                    .padding(.horizontal, 16)

                    // App info
                    VStack(spacing: 4) {
                        Text("P3 Fitness")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(Color(white: 0.3))
                        Text("Version 1.0")
                            .font(.system(size: 12))
                            .foregroundColor(Color(white: 0.2))
                    }
                    .padding(.bottom, 32)
                }
                .padding(.top, 16)
            }

            // Banners
            if keySavedBanner {
                bannerOverlay(message: "API key saved!", icon: "checkmark.circle.fill", color: Color(hex: "#00CC66"))
            }
            if unitSaveBanner {
                bannerOverlay(message: "Units updated!", icon: "checkmark.circle.fill", color: Color(hex: "#00CC66"))
            }
        }
        .navigationTitle("Profile")
        .navigationBarTitleDisplayMode(.large)
        .confirmationDialog("Sign Out?", isPresented: $showLogoutConfirm, titleVisibility: .visible) {
            Button("Sign Out", role: .destructive, action: onLogout)
            Button("Cancel", role: .cancel) {}
        }
        .sheet(isPresented: $showShareSheet) {
            if let url = exportURL {
                ShareSheet(items: [url])
            }
        }
        .onAppear {
            Task {
                await viewModel.loadUserData(userId: userId, context: modelContext)
                selectedProvider = apiKeyManager.currentProvider
                apiKeyInput = ""
            }
        }
    }

    // MARK: - User Card

    private var userCard: some View {
        HStack(spacing: 16) {
            ZStack {
                Circle()
                    .fill(
                        LinearGradient(
                            colors: [Color(hex: "#0055FF"), Color(hex: "#0033CC")],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: 64, height: 64)
                Text(viewModel.username.prefix(1).uppercased())
                    .font(.system(size: 28, weight: .black))
                    .foregroundColor(.white)
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(viewModel.username)
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(.white)
                Text("Member since \(formattedMemberDate)")
                    .font(.system(size: 13))
                    .foregroundColor(Color(white: 0.5))
                HStack(spacing: 16) {
                    Label("\(viewModel.totalWorkouts) workouts", systemImage: "dumbbell.fill")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(Color(hex: "#0055FF").opacity(0.9))
                }
            }

            Spacer()
        }
        .padding(20)
        .background(Color(hex: "#141414"))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(white: 0.1), lineWidth: 1))
        .padding(.horizontal, 16)
    }

    // MARK: - Units Toggle

    private var unitsToggle: some View {
        HStack {
            Image(systemName: "scalemass.fill")
                .font(.system(size: 16))
                .foregroundColor(Color(hex: "#0055FF"))
                .frame(width: 28)

            Text("Unit System")
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.white)

            Spacer()

            HStack(spacing: 2) {
                ForEach(["lbs", "kg"], id: \.self) { unit in
                    Button {
                        viewModel.unitPreference = unit
                        Task {
                            await viewModel.saveUnitPreference(userId: userId, context: modelContext)
                        }
                        withAnimation {
                            unitSaveBanner = true
                        }
                        Task {
                            try? await Task.sleep(nanoseconds: 2_000_000_000)
                            withAnimation { unitSaveBanner = false }
                        }
                    } label: {
                        Text(unit.uppercased())
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(viewModel.unitPreference == unit ? .white : Color(white: 0.5))
                            .padding(.horizontal, 14)
                            .padding(.vertical, 7)
                            .background(
                                viewModel.unitPreference == unit
                                    ? LinearGradient(colors: [Color(hex: "#0055FF"), Color(hex: "#0044CC")], startPoint: .leading, endPoint: .trailing)
                                    : LinearGradient(colors: [Color.clear, Color.clear], startPoint: .leading, endPoint: .trailing)
                            )
                            .clipShape(Capsule())
                    }
                    .buttonStyle(PlainButtonStyle())
                }
            }
            .padding(3)
            .background(Color(hex: "#1E1E1E"))
            .clipShape(Capsule())
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(Color(hex: "#141414"))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    // MARK: - AI Provider Section

    private var aiProviderSection: some View {
        VStack(spacing: 12) {
            // Provider picker
            HStack {
                Image(systemName: "sparkles")
                    .font(.system(size: 16))
                    .foregroundColor(Color(hex: "#FF6600"))
                    .frame(width: 28)
                Text("Provider")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(.white)
                Spacer()
                Picker("", selection: $selectedProvider) {
                    ForEach(AIProvider.allCases, id: \.self) { provider in
                        Text(provider.displayName).tag(provider)
                    }
                }
                .pickerStyle(.menu)
                .tint(Color(hex: "#0055FF"))
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(Color(hex: "#141414"))
            .clipShape(RoundedRectangle(cornerRadius: 12))

            // API Key input
            VStack(alignment: .leading, spacing: 8) {
                Text("\(selectedProvider.displayName) API Key")
                    .font(.system(size: 13, weight: .medium))
                    .foregroundColor(Color(white: 0.5))
                    .padding(.horizontal, 4)

                HStack(spacing: 10) {
                    Image(systemName: "key.fill")
                        .font(.system(size: 14))
                        .foregroundColor(Color(white: 0.4))

                    Group {
                        if showApiKey {
                            TextField("", text: $apiKeyInput, prompt: Text("Enter API key...")
                                .foregroundColor(Color(white: 0.25)))
                                .foregroundColor(.white)
                                .autocorrectionDisabled()
                                .textInputAutocapitalization(.never)
                        } else {
                            SecureField("", text: $apiKeyInput, prompt: Text("Enter API key...")
                                .foregroundColor(Color(white: 0.25)))
                                .foregroundColor(.white)
                        }
                    }
                    .font(.system(size: 14, design: .monospaced))

                    Button {
                        showApiKey.toggle()
                    } label: {
                        Image(systemName: showApiKey ? "eye.slash" : "eye")
                            .font(.system(size: 14))
                            .foregroundColor(Color(white: 0.4))
                    }
                }
                .padding(.horizontal, 14)
                .padding(.vertical, 12)
                .background(Color(hex: "#141414"))
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(apiKeyInput.isEmpty ? Color(white: 0.12) : Color(hex: "#0055FF").opacity(0.3), lineWidth: 1)
                )
            }

            // Save / Clear buttons
            HStack(spacing: 12) {
                Button {
                    apiKeyManager.clearKey(for: selectedProvider)
                    apiKeyInput = ""
                } label: {
                    Text("Clear Key")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(Color(hex: "#FF3333").opacity(0.8))
                        .frame(maxWidth: .infinity)
                        .frame(height: 42)
                        .background(Color(hex: "#FF3333").opacity(0.08))
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                        .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color(hex: "#FF3333").opacity(0.15), lineWidth: 1))
                }

                Button {
                    guard !apiKeyInput.isEmpty else { return }
                    apiKeyManager.saveKey(apiKeyInput, for: selectedProvider)
                    apiKeyInput = ""
                    withAnimation {
                        keySavedBanner = true
                    }
                    Task {
                        try? await Task.sleep(nanoseconds: 2_000_000_000)
                        withAnimation { keySavedBanner = false }
                    }
                } label: {
                    Text("Save Key")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 42)
                        .background(
                            apiKeyInput.isEmpty
                                ? Color(white: 0.2)
                                : LinearGradient(colors: [Color(hex: "#0055FF"), Color(hex: "#0033CC")], startPoint: .leading, endPoint: .trailing)
                        )
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                }
                .disabled(apiKeyInput.isEmpty)
            }
        }
    }

    // MARK: - Data Section

    private var dataSection: some View {
        Button {
            Task {
                if let url = await viewModel.exportWorkoutCSV(userId: userId, context: modelContext) {
                    exportURL = url
                    showShareSheet = true
                }
            }
        } label: {
            SettingsRow(icon: "square.and.arrow.up", iconColor: Color(hex: "#0055FF"), title: "Export Workout Data (CSV)") {
                Image(systemName: "chevron.right")
                    .font(.system(size: 13))
                    .foregroundColor(Color(white: 0.3))
            }
        }
        .buttonStyle(PlainButtonStyle())
    }

    // MARK: - Section wrapper

    private func settingsSection<Content: View>(title: String, @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(title)
                .font(.system(size: 11, weight: .semibold))
                .foregroundColor(Color(white: 0.4))
                .tracking(2)
                .padding(.horizontal, 16)

            content()
                .padding(.horizontal, 16)
        }
    }

    // MARK: - Banner Overlay

    private func bannerOverlay(message: String, icon: String, color: Color) -> some View {
        VStack {
            Spacer()
            HStack(spacing: 10) {
                Image(systemName: icon)
                    .foregroundColor(color)
                Text(message)
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(.white)
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 14)
            .background(Color(hex: "#1E1E1E"))
            .clipShape(Capsule())
            .shadow(color: .black.opacity(0.4), radius: 12, y: 4)
            .padding(.bottom, 40)
            .transition(.move(edge: .bottom).combined(with: .opacity))
        }
        .ignoresSafeArea()
    }

    private var formattedMemberDate: String {
        let fmt = DateFormatter()
        fmt.dateFormat = "MMM yyyy"
        return fmt.string(from: viewModel.memberSince ?? Date())
    }
}

// MARK: - Settings Row

struct SettingsRow<Trailing: View>: View {
    let icon: String
    let iconColor: Color
    let title: String
    @ViewBuilder let trailing: () -> Trailing

    var body: some View {
        HStack(spacing: 12) {
            ZStack {
                RoundedRectangle(cornerRadius: 8)
                    .fill(iconColor.opacity(0.15))
                    .frame(width: 32, height: 32)
                Image(systemName: icon)
                    .font(.system(size: 15))
                    .foregroundColor(iconColor)
            }
            Text(title)
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.white)
            Spacer()
            trailing()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(Color(hex: "#141414"))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

// MARK: - Share Sheet

struct ShareSheet: UIViewControllerRepresentable {
    let items: [Any]

    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: items, applicationActivities: nil)
    }

    func updateUIViewController(_ uvc: UIActivityViewController, context: Context) {}
}

#Preview {
    NavigationStack {
        SettingsView(userId: 1, onLogout: {})
    }
    .modelContainer(for: [], inMemory: true)
}
