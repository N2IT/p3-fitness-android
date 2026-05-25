import Foundation
import Security

final class APIKeyManager {
    static let shared = APIKeyManager()

    private let serviceName = "com.p3fitness.apikeys"
    private let providerDefaultsKey = "p3fitness_selected_provider"

    var selectedProvider: String {
        get { UserDefaults.standard.string(forKey: providerDefaultsKey) ?? "anthropic" }
        set { UserDefaults.standard.set(newValue, forKey: providerDefaultsKey) }
    }

    private init() {}

    // MARK: - Save

    func save(key: String, for provider: String) {
        guard let data = key.data(using: .utf8) else { return }

        // Delete existing key first
        delete(for: provider)

        let query: [String: Any] = [
            kSecClass as String:            kSecClassGenericPassword,
            kSecAttrService as String:      serviceName,
            kSecAttrAccount as String:      provider,
            kSecValueData as String:        data,
            kSecAttrAccessible as String:   kSecAttrAccessibleWhenUnlocked
        ]

        let status = SecItemAdd(query as CFDictionary, nil)
        if status != errSecSuccess {
            print("APIKeyManager: Failed to save key for \(provider), status: \(status)")
        }
    }

    // MARK: - Load

    func load(for provider: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String:        kSecClassGenericPassword,
            kSecAttrService as String:  serviceName,
            kSecAttrAccount as String:  provider,
            kSecReturnData as String:   true,
            kSecMatchLimit as String:   kSecMatchLimitOne
        ]

        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)

        guard status == errSecSuccess,
              let data = result as? Data,
              let key = String(data: data, encoding: .utf8) else {
            return nil
        }
        return key
    }

    // MARK: - Delete

    func delete(for provider: String) {
        let query: [String: Any] = [
            kSecClass as String:        kSecClassGenericPassword,
            kSecAttrService as String:  serviceName,
            kSecAttrAccount as String:  provider
        ]
        SecItemDelete(query as CFDictionary)
    }

    // MARK: - Helpers

    var availableProviders: [String] {
        ["anthropic", "openai", "google"]
    }

    func providerDisplayName(_ provider: String) -> String {
        switch provider {
        case "anthropic": return "Anthropic (Claude)"
        case "openai":    return "OpenAI (GPT)"
        case "google":    return "Google (Gemini)"
        default:          return provider.capitalized
        }
    }
}
