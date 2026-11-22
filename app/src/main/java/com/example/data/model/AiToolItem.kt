package com.example.data.model

data class AiToolItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val promptTemplate: String,
    val category: ConversationCategory,
    val badge: String
)

object RoyalAiToolsCatalog {
    val tools: List<AiToolItem> = listOf(
        AiToolItem(
            id = "writing",
            title = "AI Writing",
            subtitle = "Executive prose & editorial craft",
            description = "Draft briefings, announcements, luxury copy, and structured essays with refined tone control.",
            promptTemplate = "Draft an executive memo on ",
            category = ConversationCategory.WRITING,
            badge = "EDITORIAL"
        ),
        AiToolItem(
            id = "coding",
            title = "AI Coding",
            subtitle = "Kotlin, Compose & system architecture",
            description = "Architect modular Kotlin features, debug edge cases, and review clean MVVM patterns.",
            promptTemplate = "Review and architect a Kotlin Compose module for ",
            category = ConversationCategory.CODING,
            badge = "ENGINEER"
        ),
        AiToolItem(
            id = "translation",
            title = "AI Translation",
            subtitle = "Nuanced multilingual precision",
            description = "Translate communications across languages while preserving formal register and cultural intent.",
            promptTemplate = "Translate into formal Spanish while keeping technical terminology: ",
            category = ConversationCategory.TRANSLATION,
            badge = "LINGUIST"
        ),
        AiToolItem(
            id = "summarization",
            title = "AI Summarization",
            subtitle = "Distill long briefs into decisions",
            description = "Extract key insights, risks, and next actions from lengthy reports or meeting notes.",
            promptTemplate = "Summarize the key decisions and action items from: ",
            category = ConversationCategory.SUMMARIZATION,
            badge = "BRIEFING"
        ),
        AiToolItem(
            id = "learning",
            title = "AI Learning",
            subtitle = "Adaptive tutoring & concept mastery",
            description = "Break down complex mathematics, science, and engineering concepts into step-by-step mental models.",
            promptTemplate = "Explain the intuition and real-world applications of ",
            category = ConversationCategory.LEARNING,
            badge = "ACADEMY"
        ),
        AiToolItem(
            id = "vision",
            title = "AI Image Understanding",
            subtitle = "Visual context & document analysis",
            description = "Attach diagrams, screenshots, or photos from your gallery to stage multimodal inspection sessions.",
            promptTemplate = "Analyze the attached image and outline its key elements: ",
            category = ConversationCategory.VISION,
            badge = "VISION"
        )
    )
}
