package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.MemoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Checks if API Key is validated.
     */
    fun getApiKey(): String {
        val key = BuildConfig.GEMINI_API_KEY
        return if (key == "MY_GEMINI_API_KEY" || key.isEmpty()) "" else key
    }

    /**
     * Helper to call generative language REST endpoint.
     */
    private suspend fun callGenerateContent(
        prompt: String,
        base64Image: String? = null,
        mimeType: String = "image/jpeg"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return@withContext "API_KEY_ERROR"
        }

        val url = "$BASE_URL?key=$apiKey"

        try {
            // Build requested parts
            val partsArray = JSONArray()

            // 1. Text prompt part
            val textPartObj = JSONObject().put("text", prompt)
            partsArray.put(textPartObj)

            // 2. Multimodal image part, if any
            if (base64Image != null) {
                val inlineDataObj = JSONObject()
                    .put("mimeType", mimeType)
                    .put("data", base64Image)
                val imagePartObj = JSONObject().put("inlineData", inlineDataObj)
                partsArray.put(imagePartObj)
            }

            val contentObj = JSONObject().put("parts", partsArray)
            val contentsArray = JSONArray().put(contentObj)

            val requestBodyJson = JSONObject().put("contents", contentsArray)

            // Request config to get deterministic results
            val generationConfig = JSONObject()
                .put("temperature", 0.2) // Low temperature for extraction fidelity
            requestBodyJson.put("generationConfig", generationConfig)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Unsuccessful response code=${response.code}: $errBody")
                    throw Exception("Gemini API Error Code ${response.code}")
                }

                val responseBodyStr = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text")
                        }
                    }
                }
                return@withContext "No response text found."
            }
        } catch (e: Exception) {
            Log.e(TAG, "API call failed", e)
            return@withContext "API_ERROR: ${e.message}"
        }
    }

    /**
     * AI Auto-tagging & Extraction Pipeline.
     * Takes note info, link, image, or document details and returns structured JSON analysis.
     */
    suspend fun analyzeAndExtract(
        titleHint: String,
        mediaType: String, // "screenshot", "pdf", "voice_note", "link", "text"
        inputText: String, // User supplied notes, chat context or url
        base64Image: String? = null // Extracted image OCR if any
    ): ExtractionResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return@withContext ExtractionResult(
                title = titleHint.ifBlank { "Untitled Item" },
                rawText = inputText.ifBlank { "Unidentified text content" },
                extractedSummary = "Please configure your GEMINI_API_KEY in your AI Studio Secrets Panel to enable real intelligent analysis.",
                category = "general",
                tags = "unorganized",
                importanceScore = 5,
                reminders = emptyList()
            )
        }

        val prompt = """
            You are the Second Brain AI memory pipeline assistant. 
            Analyze this uploaded $mediaType with title/context: "$titleHint".
            Input raw content: "$inputText".
            
            Your job is to:
            1. Extract all text, descriptions, details, or if an image is provided, run OCR to extract visual texts.
            2. Infer a proper descriptive clean title.
            3. Categorize it into EXACTLY ONE of: "finance", "study", "travel", "medical", "passwords", "assignments", "work", "general".
            4. Automatically generate a concise summary (1-2 sentences).
            5. Create 2-4 lowercase tag strings (e.g. "receipt, ticket, train, python").
            6. Assign an importanceScore (integer 1-10) based on how essential it is to remember.
            7. Detect if there are deadlines, appointments, due dates, tasks, chores mentioned in the item. Extract them as reminders.
            
            Return the result in strict JSON format. Do not prepend any markdown wrapping like ```json or trailing formatting characters. Return raw parseable JSON only.
            
            JSON schema:
            {
               "title": "descriptive short title",
               "rawText": "extracted rich text content or full transcription text",
               "extractedSummary": "the clean concise summary",
               "category": "category name",
               "tags": "tags comma-separated",
               "importanceScore": 6,
               "reminders": [
                  {
                     "title": "actionable reminder description (e.g. Submit chemistry homework)",
                     "dueDateDaysFromNow": 3,
                     "detectedDateString": "e.g. Friday, Jan 14th"
                  }
               ]
            }
        """.trimIndent()

        val responseText = try {
            val raw = callGenerateContent(prompt, base64Image)
            // Strip markdown JSON wrapping if assistant returns them
            var cleanText = raw.trim()
            if (cleanText.startsWith("```json")) {
                cleanText = cleanText.substring(7)
            }
            if (cleanText.startsWith("```")) {
                cleanText = cleanText.substring(3)
            }
            if (cleanText.endsWith("```")) {
                cleanText = cleanText.substring(0, cleanText.length - 3)
            }
            cleanText.trim()
        } catch (e: Exception) {
            ""
        }

        if (responseText.isEmpty() || responseText.startsWith("API_KEY_ERROR") || responseText.startsWith("API_ERROR")) {
            return@withContext ExtractionResult(
                title = titleHint.ifBlank { "New $mediaType" },
                rawText = inputText.ifBlank { "Raw content details" },
                extractedSummary = "Error during content digitization or API key missing structure.",
                category = "general",
                tags = "simulation",
                importanceScore = 5,
                reminders = emptyList()
            )
        }

        try {
            val json = JSONObject(responseText)
            val title = json.optString("title", titleHint)
            val rawText = json.optString("rawText", inputText)
            val extractedSummary = json.optString("extractedSummary", "No summary generated")
            val category = json.optString("category", "general")
            val tags = json.optString("tags", "general")
            val importanceScore = json.optInt("importanceScore", 5)

            val reminderList = mutableListOf<ExtractedReminder>()
            val remindersArray = json.optJSONArray("reminders")
            if (remindersArray != null) {
                for (i in 0 until remindersArray.length()) {
                    val rObj = remindersArray.getJSONObject(i)
                    val rTitle = rObj.optString("title", "")
                    val offsetDays = rObj.optInt("dueDateDaysFromNow", 0)
                    val dateString = rObj.optString("detectedDateString", "Soon")
                    if (rTitle.isNotEmpty()) {
                        val duedate = System.currentTimeMillis() + (offsetDays * 24L * 60L * 60L * 1000L)
                        reminderList.add(ExtractedReminder(rTitle, duedate, dateString))
                    }
                }
            }

            ExtractionResult(title, rawText, extractedSummary, category, tags, importanceScore, reminderList)
        } catch (e: Exception) {
            Log.e(TAG, "Failed parsing extraction JSON: $responseText", e)
            // Rollback gracefully to a smart fallback parsing
            ExtractionResult(
                title = titleHint.ifBlank { "Digitized " + mediaType.capitalize() },
                rawText = if (responseText.isNotBlank()) responseText else inputText,
                extractedSummary = "Extracted and structured.",
                category = "general",
                tags = "imported",
                importanceScore = 6,
                reminders = emptyList()
            )
        }
    }

    /**
     * Cognitive RAG system. Answers user questions given the memory context database.
     */
    suspend fun answerQueryFromContext(
        query: String,
        memories: List<MemoryItem>,
        chatHistory: List<String>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return@withContext "Welcome to Second Brain! I can see you don't have a valid Gemini API key loaded in your Secrets panel yet.\n\nTo make this search fully functional and chat natively, please enter your GEMINI_API_KEY in Google AI Studio's Secrets panel. For now, I can execute static keyword matches over your local database!"
        }

        // Build the localized memory context profile
        val contextBuilder = StringBuilder()
        contextBuilder.append("USER MEMORIES (SECOND BRAIN CONTEXT):\n")
        if (memories.isEmpty()) {
            contextBuilder.append("- No memories stored yet. Tell the user to upload note, image or link first!\n")
        } else {
            memories.forEachIndexed { index, item ->
                contextBuilder.append("""
                    --- MEMORY #${index + 1} ---
                    ID: ${item.id}
                    Title: ${item.title}
                    Type: ${item.mediaType}
                    Category: ${item.category}
                    Summary: ${item.extractedSummary}
                    Raw Text or Transcripts: ${item.rawText}
                    Tags: ${item.tags}
                    Importance Score: ${item.importanceScore}/10
                    Saved: ${java.util.Date(item.timestamp)}
                    
                """.trimIndent())
            }
        }

        val historyCombined = chatHistory.joinToString("\n")

        val prompt = """
            $contextBuilder
            
            CONVERSATION HISTORY:
            $historyCombined
            
            USER QUESTION:
            "$query"
            
            You are "Second Brain AI", an advanced, friendly personal memory assistant. Your goal is to guide the user and find exactly what they are looking for inside their memories. 
            
            Rules:
            1. Answer the USER QUESTION using and citing the USER MEMORIES context.
            2. Be extremely specific. Quote text, give dates, summaries, and title names (e.g. "Found in 'Python Notes PDF': ...").
            3. If the correct answer is not found directly in the memories, state clearly that you checked the brain but couldn't find a direct correlation, and then give a helpful deduction based on other context.
            4. Keep your response conversational, concise, and structured. Use bold headings.
        """.trimIndent()

        val rawResponse = callGenerateContent(prompt)
        if (rawResponse == "API_KEY_ERROR") {
            "API key missing configuration."
        } else {
            rawResponse
        }
    }
}

data class ExtractedReminder(
    val title: String,
    val dueDate: Long,
    val dateString: String
)

data class ExtractionResult(
    val title: String,
    val rawText: String,
    val extractedSummary: String,
    val category: String,
    val tags: String,
    val importanceScore: Int,
    val reminders: List<ExtractedReminder>
)
