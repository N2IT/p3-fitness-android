package com.fittrack.data.api

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ImportedExercise(
    val name: String,
    val sets: Int,
    val reps: Int,
    val muscleGroup: String = "",
    val equipment: String = ""
)

data class ImportedRoutine(
    val name: String,
    val exercises: List<ImportedExercise>
)

sealed class ImportState {
    data object Idle : ImportState()
    data object Loading : ImportState()
    data class Preview(val routine: ImportedRoutine) : ImportState()
    data class Error(val message: String) : ImportState()
    data object Success : ImportState()
}

class RoutineImporter(private val apiKeyManager: ApiKeyManager) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun importFromUrl(url: String, onResult: (ImportState) -> Unit) {
        val provider = apiKeyManager.getSelectedProvider()
        val apiKey = apiKeyManager.getApiKey(provider)
        if (apiKey.isNullOrBlank()) {
            onResult(ImportState.Error("No API key configured for ${provider.displayName}. Add your key in Settings."))
            return
        }

        onResult(ImportState.Loading)

        try {
            val pageRequest = Request.Builder().url(url).build()
            val pageResponse = client.newCall(pageRequest).execute()
            val html = pageResponse.body?.string() ?: ""

            if (html.isBlank()) {
                onResult(ImportState.Error("Could not fetch content from URL"))
                return
            }

            val textContent = html
                .replace(Regex("<script[^>]*>[\\s\\S]*?</script>"), "")
                .replace(Regex("<style[^>]*>[\\s\\S]*?</style>"), "")
                .replace(Regex("<[^>]+>"), " ")
                .replace(Regex("\\s+"), " ")
                .take(8000)

            val routine = callLlmApi(provider, apiKey, buildUrlPrompt(textContent))
            if (routine != null) {
                onResult(ImportState.Preview(routine))
            } else {
                onResult(ImportState.Error("Could not extract a workout routine from this page"))
            }
        } catch (e: Exception) {
            onResult(ImportState.Error(e.message ?: "Import failed"))
        }
    }

    fun importFromText(text: String, onResult: (ImportState) -> Unit) {
        val provider = apiKeyManager.getSelectedProvider()
        val apiKey = apiKeyManager.getApiKey(provider)
        if (apiKey.isNullOrBlank()) {
            onResult(ImportState.Error("No API key configured for ${provider.displayName}. Add your key in Settings."))
            return
        }

        if (text.isBlank()) {
            onResult(ImportState.Error("Please enter a workout description"))
            return
        }

        onResult(ImportState.Loading)

        try {
            val routine = callLlmApi(provider, apiKey, buildTextPrompt(text))
            if (routine != null) {
                onResult(ImportState.Preview(routine))
            } else {
                onResult(ImportState.Error("Could not generate a routine from that description"))
            }
        } catch (e: Exception) {
            onResult(ImportState.Error(e.message ?: "Generation failed"))
        }
    }

    private fun callLlmApi(provider: LlmProvider, apiKey: String, userMessage: String): ImportedRoutine? {
        return when (provider) {
            LlmProvider.ANTHROPIC -> callAnthropicApi(apiKey, userMessage)
            LlmProvider.OPENAI -> callOpenAiApi(apiKey, userMessage)
            LlmProvider.GOOGLE -> callGoogleApi(apiKey, userMessage)
        }
    }

    // ── Anthropic Claude ──

    private fun callAnthropicApi(apiKey: String, userMessage: String): ImportedRoutine? {
        val requestBody = JSONObject().apply {
            put("model", LlmProvider.ANTHROPIC.model)
            put("max_tokens", 2048)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userMessage)
                })
            })
        }

        val request = Request.Builder()
            .url(LlmProvider.ANTHROPIC.apiEndpoint)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: return null

        if (!response.isSuccessful) {
            val errorJson = try { JSONObject(responseBody) } catch (_: Exception) { null }
            val errorMsg = errorJson?.optJSONObject("error")?.optString("message") ?: "API error: ${response.code}"
            throw Exception(errorMsg)
        }

        val json = JSONObject(responseBody)
        val content = json.getJSONArray("content")
        val textBlock = (0 until content.length())
            .map { content.getJSONObject(it) }
            .firstOrNull { it.getString("type") == "text" }
            ?.getString("text") ?: return null

        return parseRoutineJson(textBlock)
    }

    // ── OpenAI ──

    private fun callOpenAiApi(apiKey: String, userMessage: String): ImportedRoutine? {
        val requestBody = JSONObject().apply {
            put("model", LlmProvider.OPENAI.model)
            put("max_tokens", 2048)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userMessage)
                })
            })
        }

        val request = Request.Builder()
            .url(LlmProvider.OPENAI.apiEndpoint)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: return null

        if (!response.isSuccessful) {
            val errorJson = try { JSONObject(responseBody) } catch (_: Exception) { null }
            val errorMsg = errorJson?.optJSONObject("error")?.optString("message") ?: "API error: ${response.code}"
            throw Exception(errorMsg)
        }

        val json = JSONObject(responseBody)
        val choices = json.getJSONArray("choices")
        if (choices.length() == 0) return null
        val text = choices.getJSONObject(0)
            .getJSONObject("message")
            .getString("content")

        return parseRoutineJson(text)
    }

    // ── Google Gemini ──

    private fun callGoogleApi(apiKey: String, userMessage: String): ImportedRoutine? {
        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", userMessage)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("maxOutputTokens", 2048)
            })
        }

        val url = "${LlmProvider.GOOGLE.apiEndpoint}?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: return null

        if (!response.isSuccessful) {
            val errorJson = try { JSONObject(responseBody) } catch (_: Exception) { null }
            val errorMsg = errorJson?.optJSONObject("error")?.optString("message") ?: "API error: ${response.code}"
            throw Exception(errorMsg)
        }

        val json = JSONObject(responseBody)
        val candidates = json.getJSONArray("candidates")
        if (candidates.length() == 0) return null
        val text = candidates.getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")

        return parseRoutineJson(text)
    }

    // ── Shared parsing ──

    private fun parseRoutineJson(text: String): ImportedRoutine? {
        val jsonStr = Regex("\\{[\\s\\S]*\\}").find(text)?.value ?: return null

        return try {
            val json = JSONObject(jsonStr)
            val name = json.optString("name", "Imported Routine")
            val exercisesArray = json.getJSONArray("exercises")

            val exercises = (0 until exercisesArray.length()).map { i ->
                val ex = exercisesArray.getJSONObject(i)
                ImportedExercise(
                    name = ex.getString("name"),
                    sets = ex.optInt("sets", 3),
                    reps = ex.optInt("reps", 10),
                    muscleGroup = ex.optString("muscleGroup", ""),
                    equipment = ex.optString("equipment", "")
                )
            }

            ImportedRoutine(name = name, exercises = exercises)
        } catch (_: Exception) {
            null
        }
    }

    private fun buildUrlPrompt(content: String): String = """
Extract the workout routine from this webpage content and return it as JSON.

Return ONLY a JSON object with this exact structure (no other text):
{
  "name": "Routine Name",
  "exercises": [
    {"name": "Exercise Name", "sets": 3, "reps": 10, "muscleGroup": "Chest", "equipment": "Barbell"}
  ]
}

muscleGroup must be one of: Chest, Back, Shoulders, Arms, Legs, Core
equipment must be one of: Barbell, Dumbbell, Cable, Machine, Bodyweight

Webpage content:
$content
""".trimIndent()

    private fun buildTextPrompt(text: String): String = """
Generate a complete workout routine based on this description and return it as JSON.

Return ONLY a JSON object with this exact structure (no other text):
{
  "name": "Routine Name",
  "exercises": [
    {"name": "Exercise Name", "sets": 3, "reps": 10, "muscleGroup": "Chest", "equipment": "Barbell"}
  ]
}

muscleGroup must be one of: Chest, Back, Shoulders, Arms, Legs, Core
equipment must be one of: Barbell, Dumbbell, Cable, Machine, Bodyweight

Choose appropriate sets and reps for each exercise. Include 4-8 exercises.

Description: $text
""".trimIndent()
}
