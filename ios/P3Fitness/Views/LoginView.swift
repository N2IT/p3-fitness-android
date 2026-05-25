import SwiftUI
import SwiftData

struct LoginView: View {
    let onLogin: (Int) -> Void
    @State private var viewModel = LoginViewModel()
    @Environment(\.modelContext) private var modelContext

    @State private var username: String = ""
    @State private var unitPreference: String = "lbs"
    @State private var showError: Bool = false
    @State private var logoScale: CGFloat = 0.7
    @State private var logoOpacity: Double = 0
    @State private var formOpacity: Double = 0
    @State private var formOffset: CGFloat = 40

    var body: some View {
        ZStack {
            // Dark gradient background
            LinearGradient(
                colors: [Color(hex: "#0A0A0A"), Color(hex: "#0D1A33")],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 0) {
                    // Logo area
                    VStack(spacing: 8) {
                        Text("P3")
                            .font(.system(size: 80, weight: .black, design: .rounded))
                            .foregroundColor(.white)
                            .shadow(color: Color(hex: "#0055FF").opacity(0.5), radius: 20, x: 0, y: 8)

                        Text("FITNESS")
                            .font(.system(size: 22, weight: .bold, design: .rounded))
                            .foregroundColor(Color(hex: "#0055FF"))
                            .kerning(8)

                        Text("Track. Progress. Perform.")
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(Color(white: 0.5))
                            .kerning(1)
                            .padding(.top, 4)
                    }
                    .scaleEffect(logoScale)
                    .opacity(logoOpacity)
                    .padding(.top, 80)
                    .padding(.bottom, 60)

                    // Form card
                    VStack(spacing: 24) {
                        // Username field
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Username")
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundColor(Color(white: 0.6))
                                .kerning(0.5)

                            HStack {
                                Image(systemName: "person.fill")
                                    .foregroundColor(Color(hex: "#0055FF"))
                                    .frame(width: 20)

                                TextField("", text: $username, prompt: Text("Enter your username")
                                    .foregroundColor(Color(white: 0.3)))
                                    .foregroundColor(.white)
                                    .autocorrectionDisabled()
                                    .textInputAutocapitalization(.never)
                            }
                            .padding(.horizontal, 16)
                            .padding(.vertical, 14)
                            .background(Color(hex: "#141414"))
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(
                                        username.isEmpty ? Color(white: 0.15) : Color(hex: "#0055FF").opacity(0.6),
                                        lineWidth: 1
                                    )
                            )
                        }

                        // Units toggle
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Units")
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundColor(Color(white: 0.6))
                                .kerning(0.5)

                            HStack(spacing: 0) {
                                ForEach(["lbs", "kg"], id: \.self) { unit in
                                    Button(action: {
                                        withAnimation(.easeInOut(duration: 0.2)) {
                                            unitPreference = unit
                                        }
                                    }) {
                                        Text(unit.uppercased())
                                            .font(.system(size: 15, weight: .semibold))
                                            .foregroundColor(unitPreference == unit ? .white : Color(white: 0.5))
                                            .frame(maxWidth: .infinity)
                                            .padding(.vertical, 12)
                                            .background(
                                                unitPreference == unit
                                                    ? LinearGradient(
                                                        colors: [Color(hex: "#0055FF"), Color(hex: "#0044CC")],
                                                        startPoint: .leading,
                                                        endPoint: .trailing
                                                      )
                                                    : LinearGradient(
                                                        colors: [Color.clear, Color.clear],
                                                        startPoint: .leading,
                                                        endPoint: .trailing
                                                      )
                                            )
                                            .cornerRadius(10)
                                    }
                                }
                            }
                            .padding(4)
                            .background(Color(hex: "#141414"))
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color(white: 0.15), lineWidth: 1)
                            )
                        }

                        // Error message
                        if showError, let errorMsg = viewModel.errorMessage {
                            HStack(spacing: 8) {
                                Image(systemName: "exclamationmark.triangle.fill")
                                    .foregroundColor(Color(hex: "#FF3333"))
                                    .font(.system(size: 14))
                                Text(errorMsg)
                                    .font(.system(size: 13, weight: .medium))
                                    .foregroundColor(Color(hex: "#FF3333"))
                            }
                            .padding(.horizontal, 16)
                            .padding(.vertical, 10)
                            .background(Color(hex: "#FF3333").opacity(0.1))
                            .cornerRadius(8)
                            .overlay(
                                RoundedRectangle(cornerRadius: 8)
                                    .stroke(Color(hex: "#FF3333").opacity(0.3), lineWidth: 1)
                            )
                            .transition(.opacity.combined(with: .scale(scale: 0.95)))
                        }

                        // Get Started button
                        Button(action: handleLogin) {
                            ZStack {
                                if viewModel.isLoading {
                                    ProgressView()
                                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                        .scaleEffect(0.9)
                                } else {
                                    HStack(spacing: 8) {
                                        Text("Get Started")
                                            .font(.system(size: 17, weight: .bold))
                                            .foregroundColor(.white)
                                        Image(systemName: "arrow.right")
                                            .font(.system(size: 15, weight: .semibold))
                                            .foregroundColor(.white)
                                    }
                                }
                            }
                            .frame(maxWidth: .infinity)
                            .frame(height: 56)
                            .background(
                                LinearGradient(
                                    colors: username.isEmpty
                                        ? [Color(white: 0.2), Color(white: 0.15)]
                                        : [Color(hex: "#0055FF"), Color(hex: "#0033CC")],
                                    startPoint: .leading,
                                    endPoint: .trailing
                                )
                            )
                            .cornerRadius(14)
                            .shadow(
                                color: username.isEmpty ? .clear : Color(hex: "#0055FF").opacity(0.4),
                                radius: 12, x: 0, y: 6
                            )
                        }
                        .disabled(username.isEmpty || viewModel.isLoading)
                        .animation(.easeInOut(duration: 0.2), value: username.isEmpty)
                    }
                    .padding(24)
                    .background(
                        RoundedRectangle(cornerRadius: 20)
                            .fill(Color(hex: "#141414"))
                            .overlay(
                                RoundedRectangle(cornerRadius: 20)
                                    .stroke(Color(white: 0.12), lineWidth: 1)
                            )
                    )
                    .padding(.horizontal, 24)
                    .opacity(formOpacity)
                    .offset(y: formOffset)

                    Spacer(minLength: 40)
                }
            }
        }
        .preferredColorScheme(.dark)
        .onAppear {
            withAnimation(.spring(response: 0.6, dampingFraction: 0.8).delay(0.1)) {
                logoScale = 1.0
                logoOpacity = 1.0
            }
            withAnimation(.easeOut(duration: 0.5).delay(0.4)) {
                formOpacity = 1.0
                formOffset = 0
            }
        }
        .onChange(of: viewModel.loggedInUserId) { _, newId in
            if let id = newId {
                withAnimation(.easeInOut(duration: 0.3)) {
                    onLogin(id)
                }
            }
        }
        .onChange(of: viewModel.errorMessage) { _, error in
            if error != nil {
                withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                    showError = true
                }
            } else {
                showError = false
            }
        }
    }

    private func handleLogin() {
        showError = false
        viewModel.username = username
        viewModel.unitPreference = unitPreference
        Task {
            await viewModel.login(context: modelContext)
        }
    }
}

#Preview {
    LoginView(onLogin: { _ in })
        .modelContainer(for: [], inMemory: true)
}
