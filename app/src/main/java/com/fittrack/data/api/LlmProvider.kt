package com.fittrack.data.api

enum class LlmProvider(
    val displayName: String,
    val description: String,
    val model: String,
    val keyHint: String,
    val signupUrl: String,
    val pricingNote: String
) {
    ANTHROPIC(
        displayName = "Anthropic Claude",
        description = "Claude Haiku — fast, affordable, great at structured output",
        model = "claude-haiku-4-5-20251001",
        keyHint = "sk-ant-...",
        signupUrl = "https://console.anthropic.com/",
        pricingNote = "Free tier available · Pay-as-you-go from \$0.25/M input tokens"
    ),
    OPENAI(
        displayName = "OpenAI",
        description = "GPT-4o Mini — fast and cost-effective",
        model = "gpt-4o-mini",
        keyHint = "sk-...",
        signupUrl = "https://platform.openai.com/signup",
        pricingNote = "Free tier available · Pay-as-you-go from \$0.15/M input tokens"
    ),
    GOOGLE(
        displayName = "Google Gemini",
        description = "Gemini 2.0 Flash — generous free tier",
        model = "gemini-2.0-flash",
        keyHint = "AIza...",
        signupUrl = "https://aistudio.google.com/apikey",
        pricingNote = "Generous free tier · Pay-as-you-go from \$0.075/M input tokens"
    );

    val apiEndpoint: String
        get() = when (this) {
            ANTHROPIC -> "https://api.anthropic.com/v1/messages"
            OPENAI -> "https://api.openai.com/v1/chat/completions"
            GOOGLE -> "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"
        }
}
