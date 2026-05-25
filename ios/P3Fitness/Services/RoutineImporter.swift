import Foundation

struct ImportedRoutine {
    var name: String
    var exercises: [ImportedExercise]
}

struct ImportedExercise {
    var name: String
    var sets: Int
    var reps: Int
    var muscleGroup: String
    var equipment: String
}

enum ImporterError: LocalizedError {
    case noAPIKey(String)
    case networkError(String)
    case parseError(String)
    case urlFetchError(String)

    var errorDescription: String? {
        switch self {
        case .noAPIKey(let provider): return "No API key set for \(provider). Please add one in Settings."
        case .networkError(let msg): return "Network error: \(msg)"
        case .parseError(let msg): return "Failed to parse LLM response: \(msg)"
        case .urlFetchError(let msg): return "Failed to fetch URL: \(msg)"
        }
    }
}

final class RoutineImporter {

    // MARK: - Public entry points

    static func importFromURL(_ urlString: String, apiKeyManager: APIKeyManager) async throws -> ImportedRoutine {
        guard let url = URL(string: urlString) else {
            throw ImporterError.urlFetchError("Invalid URL: \(urlString)")
        }
        let (data, _) = try await URLSession.shared.data(from: url)
        let html = String(data: data, encoding: .utf8) ?? ""
        let text = stripHTML(html)
        let truncated = String(text.prefix(8000)) // keep tokens manageable
        return try await sendToLLM(content: truncated, apiKeyManager: apiKeyManager)
    }

    static func importFromText(_ text: String, apiKeyManager: APIKeyManager) async throws -> ImportedRoutine {
        return try await sendToLLM(content: text, apiKeyManager: apiKeyManager)
    }

    // MARK: - LLM dispatch

    private static func sendToLLM(content: String, apiKeyManager: APIKeyManager) async throws -> ImportedRoutine {
        let provider = apiKeyManager.selectedProvider
        guard let apiKey = apiKeyManager.load(for: provider), !apiKey.isEmpty else {
            throw ImporterError.noAPIKey(provider)
        }

        let systemPrompt = """
        You are a fitness assistant. Parse the following workout description and output ONLY valid JSON in this exact format:
        {
          "routineName": "Routine Name",
          "exercises": [
            {"name": "Exercise Name", "sets": 3, "reps": 10, "muscleGroup": "Chest", "equipment": "Barbell"}
          ]
        }
        muscleGroup must be one of: Chest, Back, Shoulders, Arms, Legs, Core, Other.
        equipment must be one of: Barbell, Dumbbell, Cable, Machine, Bodyweight.
        Output ONLY the JSON object, no markdown fences, no extra text.
        """

        let jsonText: String
        switch provider {
        case "openai":
            jsonText = try await callOpenAI(apiKey: apiKey, systemPrompt: systemPrompt, userContent: content)
        case "google":
            jsonText = try await callGemini(apiKey: apiKey, systemPrompt: systemPrompt, userContent: content)
        default: // anthropic
            jsonText = try await callAnthropic(apiKey: apiKey, systemPrompt: systemPrompt, userContent: content)
        }

        return try parseRoutineJSON(jsonText)
    }

    // MARK: - Anthropic

    private static func callAnthropic(apiKey: String, systemPrompt: String, userContent: String) async throws -> String {
        let url = URL(string: "https://api.anthropic.com/v1/messages")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "content-type")
        request.setValue(apiKey, forHTTPHeaderField: "x-api-key")
        request.setValue("2023-06-01", forHTTPHeaderField: "anthropic-version")

        let body: [String: Any] = [
            "model": "claude-haiku-4-5-20251001",
            "max_tokens": 1024,
            "system": systemPrompt,
            "messages": [
                ["role": "user", "content": userContent]
            ]
        ]
        request.httpBody = try JSONSerialization.data(withJSONObject: body)

        let (data, response) = try await URLSession.shared.data(for: request)
        try checkHTTP(response, data: data, provider: "Anthropic")

        guard let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
              let content = json["content"] as? [[String: Any]],
              let first = content.first,
              let text = first["text"] as? String else {
            throw ImporterError.parseError("Unexpected Anthropic response structure")
        }
        return text
    }

    // MARK: - OpenAI

    private static func callOpenAI(apiKey: String, systemPrompt: String, userContent: String) async throws -> String {
        let url = URL(string: "https://api.openai.com/v1/chat/completions")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "content-type")
        request.setValue("Bearer \(apiKey)", forHTTPHeaderField: "Authorization")

        let body: [String: Any] = [
            "model": "gpt-4o-mini",
            "max_tokens": 1024,
            "messages": [
                ["role": "system", "content": systemPrompt],
                ["role": "user", "content": userContent]
            ]
        ]
        request.httpBody = try JSONSerialization.data(withJSONObject: body)

        let (data, response) = try await URLSession.shared.data(for: request)
        try checkHTTP(response, data: data, provider: "OpenAI")

        guard let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
              let choices = json["choices"] as? [[String: Any]],
              let first = choices.first,
              let message = first["message"] as? [String: Any],
              let text = message["content"] as? String else {
            throw ImporterError.parseError("Unexpected OpenAI response structure")
        }
        return text
    }

    // MARK: - Google Gemini

    private static func callGemini(apiKey: String, systemPrompt: String, userContent: String) async throws -> String {
        let urlString = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=\(apiKey)"
        let url = URL(string: urlString)!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "content-type")

        let combinedPrompt = "\(systemPrompt)\n\n\(userContent)"
        let body: [String: Any] = [
            "contents": [
                ["parts": [["text": combinedPrompt]]]
            ],
            "generationConfig": ["maxOutputTokens": 1024]
        ]
        request.httpBody = try JSONSerialization.data(withJSONObject: body)

        let (data, response) = try await URLSession.shared.data(for: request)
        try checkHTTP(response, data: data, provider: "Google")

        guard let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
              let candidates = json["candidates"] as? [[String: Any]],
              let first = candidates.first,
              let content = first["content"] as? [String: Any],
              let parts = content["parts"] as? [[String: Any]],
              let part = parts.first,
              let text = part["text"] as? String else {
            throw ImporterError.parseError("Unexpected Gemini response structure")
        }
        return text
    }

    // MARK: - Parsing

    private static func parseRoutineJSON(_ raw: String) throws -> ImportedRoutine {
        // Strip markdown fences if present
        var cleaned = raw.trimmingCharacters(in: .whitespacesAndNewlines)
        if cleaned.hasPrefix("```") {
            cleaned = cleaned
                .components(separatedBy: "\n")
                .dropFirst()
                .joined(separator: "\n")
            if cleaned.hasSuffix("```") {
                cleaned = String(cleaned.dropLast(3)).trimmingCharacters(in: .whitespacesAndNewlines)
            }
        }

        guard let data = cleaned.data(using: .utf8),
              let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            throw ImporterError.parseError("Response is not valid JSON")
        }

        let routineName = json["routineName"] as? String ?? "Imported Routine"
        let rawExercises = json["exercises"] as? [[String: Any]] ?? []

        let exercises: [ImportedExercise] = rawExercises.compactMap { ex in
            guard let name = ex["name"] as? String else { return nil }
            let sets = ex["sets"] as? Int ?? 3
            let reps = ex["reps"] as? Int ?? 10
            let muscleGroup = ex["muscleGroup"] as? String ?? "Other"
            let equipment = ex["equipment"] as? String ?? "Bodyweight"
            return ImportedExercise(name: name, sets: sets, reps: reps, muscleGroup: muscleGroup, equipment: equipment)
        }

        return ImportedRoutine(name: routineName, exercises: exercises)
    }

    // MARK: - Helpers

    private static func checkHTTP(_ response: URLResponse, data: Data, provider: String) throws {
        guard let http = response as? HTTPURLResponse else { return }
        guard (200..<300).contains(http.statusCode) else {
            let body = String(data: data, encoding: .utf8) ?? ""
            throw ImporterError.networkError("\(provider) returned HTTP \(http.statusCode): \(body.prefix(200))")
        }
    }

    private static func stripHTML(_ html: String) -> String {
        // Remove script/style blocks first
        var result = html
        let blockTags = ["script", "style", "head", "nav", "footer", "header"]
        for tag in blockTags {
            while let startRange = result.range(of: "<\(tag)", options: .caseInsensitive),
                  let endRange = result.range(of: "</\(tag)>", options: .caseInsensitive) {
                let removeRange = startRange.lowerBound..<endRange.upperBound
                result.removeSubrange(removeRange)
            }
        }
        // Remove all remaining HTML tags
        result = result.replacingOccurrences(of: "<[^>]+>", with: " ", options: .regularExpression)
        // Collapse whitespace
        result = result.components(separatedBy: .whitespacesAndNewlines)
            .filter { !$0.isEmpty }
            .joined(separator: " ")
        // Decode common HTML entities
        result = result
            .replacingOccurrences(of: "&amp;", with: "&")
            .replacingOccurrences(of: "&lt;", with: "<")
            .replacingOccurrences(of: "&gt;", with: ">")
            .replacingOccurrences(of: "&nbsp;", with: " ")
            .replacingOccurrences(of: "&#39;", with: "'")
            .replacingOccurrences(of: "&quot;", with: "\"")
        return result
    }
}
