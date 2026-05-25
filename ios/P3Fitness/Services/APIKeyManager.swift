import Foundation
import Security
import Observation

// MARK: - AI Provider enum

enum AIProvider: String, CaseIterable, Codable {
    case anthropic = "anthropic"
    case openai    = "openai"
    case google    = "google"

    var displayName: String {
        switch self {
        case .anthropic: return "Anthropic (Claude)"
        case .openai:    return "OpenAI (GPT-4o)"
        case .google:    return "Google (Gemini)"
        }
    }

    var keychainKey: String { rawValue }
}

// MARK: - APIKeyManager

@Observable
final class APIKeyManager {
    static let shared = APIKeyManager()

    private let serviceName = "com.p3fitness.apikeys"
    private let providerDefaultsKey = "p3fitness_selected_provider"

    // MARK: - Selected Provider

    var currentProvider: AIProvider {
        get {
            let raw = UserDefaults.standard.string(forKey: providerDefaultsKey) ?? AIProvider.anthropic.rawValue
            return AIProvider(rawValue: raw) ?? .anthropic
        }
        set {
            UserDefaults.standard.set(newValue.rawValue, forKey: providerDefaultsKey)
        }
    }

    var selectedProvider: String {
        get { currentProvider.rawValue }
        set { currentProvider = AIProvider(rawValue: newValue) ?? .anthropic }
    }

    var currentProviderName: String {
        currentProvider.displayName
    }

    var hasActiveKey: Bool {
        loadKey(for: currentProvider) != nil
    }

    // MARK: - Init (allows instantiation as @State)

    init() {}

    // MARK: - Save

    func save(key: String, for provider: String) {
        guard let p = AIProvider(rawValue: provider) else { return }
        saveKey(key, for: p)
    }

    func saveKey(_ key: String, for provider: AIProvider) {
        guard let data = key.data(using: .utf8) else { return }
        clearKey(for: provider) // Delete existing first

        let query: [String: Any] = [
            kSecClass as String:          kSecClassGenericPassword,
            kSecAttrService as String:    serviceName,
            kSecAttrAccount as String:    provider.keychainKey,
            kSecValueData as String:      data,
            kSecAttrAccessible as String: kSecAttrAccessibleWhenUnlocked
        ]
        let status = SecItemAdd(query as CFDictionary, nil)
        if status != errSecSuccess {
            print("APIKeyManager: Failed to save key for \(provider), status: \(status)")
        }
    }

    // MARK: - Load

    func load(for provider: String) -> String? {
        guard let p = AIProvider(rawValue: provider) else { return nil }
        return loadKey(for: p)
    }

    func loadKey(for provider: AIProvider) -> String? {
        let query: [String: Any] = [
            kSecClass as String:       kSecClassGenericPassword,
            kSecAttrService as String: serviceName,
            kSecAttrAccount as String: provider.keychainKey,
            kSecReturnData as String:  true,
            kSecMatchLimit as String:  kSecMatchLimitOne
        ]
        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)
        guard status == errSecSuccess,
              let data = result as? Data,
              let key = String(data: data, encoding: .utf8) else { return nil }
        return key
    }

    // MARK: - Delete / Clear

    func delete(for provider: String) {
        guard let p = AIProvider(rawValue: provider) else { return }
        clearKey(for: p)
    }

    func clearKey(for provider: AIProvider) {
        let query: [String: Any] = [
            kSecClass as String:       kSecClassGenericPassword,
            kSecAttrService as String: serviceName,
            kSecAttrAccount as String: provider.keychainKey
        ]
        SecItemDelete(query as CFDictionary)
    }

    // MARK: - Helpers

    var availableProviders: [String] {
        AIProvider.allCases.map { $0.rawValue }
    }

    func providerDisplayName(_ provider: String) -> String {
        AIProvider(rawValue: provider)?.displayName ?? provider.capitalized
    }
}
