import Foundation
import SwiftData
import Observation

@Observable
final class LoginViewModel {
    var username: String = ""
    var unitPreference: String = "lbs"
    var isLoading: Bool = false
    var errorMessage: String? = nil
    var loggedInUserId: Int? = nil

    // MARK: - Login / Create user

    @MainActor
    func login(context: ModelContext) async {
        let trimmedName = username.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedName.isEmpty else {
            errorMessage = "Please enter a username."
            return
        }

        isLoading = true
        errorMessage = nil

        do {
            // Try to find an existing user with this username
            let descriptor = FetchDescriptor<User>(
                predicate: #Predicate<User> { $0.username == trimmedName }
            )
            let existing = try context.fetch(descriptor)

            if let user = existing.first {
                // Found existing user — log in
                loggedInUserId = user.id
            } else {
                // Create a new user
                let newUser = User(username: trimmedName, unitPreference: unitPreference)
                context.insert(newUser)
                try context.save()
                loggedInUserId = newUser.id
            }
        } catch {
            errorMessage = "Failed to sign in: \(error.localizedDescription)"
        }

        isLoading = false
    }

    // MARK: - Unit toggle

    func toggleUnit() {
        unitPreference = (unitPreference == "lbs") ? "kg" : "lbs"
    }
}
