package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SecondBrainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val memoryDao = db.memoryDao()
    private val reminderDao = db.reminderDao()
    private val chatDao = db.chatDao()

    // Plan Management
    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    // Loading State
    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // Search Query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Core Data Streams from Room
    val allMemories: StateFlow<List<MemoryItem>> = memoryDao.getAllMemoriesFlow()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReminders: StateFlow<List<ReminderItem>> = reminderDao.getAllRemindersFlow()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatHistory: StateFlow<List<ChatMessage>> = chatDao.getChatMessagesFlow()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search results combining query + memory stream
    val searchedMemories: StateFlow<List<MemoryItem>> = combine(allMemories, searchQuery) { memories, query ->
        if (query.isBlank()) {
            memories
        } else {
            memories.filter { item ->
                item.title.contains(query, ignoreCase = true) ||
                        item.rawText.contains(query, ignoreCase = true) ||
                        item.extractedSummary.contains(query, ignoreCase = true) ||
                        item.category.contains(query, ignoreCase = true) ||
                        item.tags.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Statistics used for Admin dashboard simulation
    val aiTokensUsed = MutableStateFlow(42150)
    val activeUsersCount = MutableStateFlow(1284)

    init {
        // Pre-populate some starter memories if database is completely empty
        viewModelScope.launch(Dispatchers.IO) {
            val existing = memoryDao.getAllMemories()
            if (existing.isEmpty()) {
                prepopulateDemoData()
            }
        }
    }

    /**
     * Toggles premium state.
     */
    fun togglePremium() {
        _isPremium.update { !it }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Real upload process with standard / preset images and notes.
     */
    fun processUpload(
        titleHint: String,
        mediaType: String, // "screenshot", "pdf", "voice_note", "link", "text"
        inputText: String,
        base64Image: String? = null,
        onComplete: (Boolean) -> Unit = {}
    ) {
        // Enforce Free Tier limits: max 4 records unless premium
        if (!isPremium.value && allMemories.value.size >= 4) {
            onComplete(false)
            return
        }

        viewModelScope.launch {
            _isUploading.value = true
            try {
                // Call Gemini for industrial OCR / transcription, categorization, tags, and summary!
                val result = GeminiClient.analyzeAndExtract(
                    titleHint = titleHint,
                    mediaType = mediaType,
                    inputText = inputText,
                    base64Image = base64Image
                )

                aiTokensUsed.update { it + 2350 } // simulated cost tracking

                // Build a clean memory entry
                val newMemory = MemoryItem(
                    title = result.title,
                    mediaType = mediaType,
                    rawText = result.rawText,
                    extractedSummary = result.extractedSummary,
                    category = result.category,
                    tags = result.tags,
                    importanceScore = result.importanceScore,
                    mediaUri = if (base64Image != null) "base64_rendered" else null
                )

                // Save to Room DB and capture the generated ID
                val generatedId = withContext(Dispatchers.IO) {
                    memoryDao.insertMemory(newMemory)
                }

                // If any deadlines / dates are detected, insert reminders associated to this memory!
                result.reminders.forEach { extracted ->
                    val r = ReminderItem(
                        memoryId = generatedId.toInt(),
                        title = extracted.title,
                        dueDate = extracted.dueDate,
                        detectedDateString = extracted.dateString
                    )
                    withContext(Dispatchers.IO) {
                        reminderDao.insertReminder(r)
                    }
                }

                onComplete(true)
            } catch (e: Exception) {
                Log.e("SecondBrainViewModel", "Failed to process upload pipeline", e)
                onComplete(false)
            } finally {
                _isUploading.value = false
            }
        }
    }

    /**
     * Delete memory and its associated reminders.
     */
    fun deleteMemory(item: MemoryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            memoryDao.deleteMemory(item)
            // Cleanup standalone reminders derived from it
            val reminders = allReminders.value.filter { it.memoryId == item.id }
            reminders.forEach { reminderDao.deleteReminder(it) }
        }
    }

    /**
     * Create local custom reminder.
     */
    fun insertManualReminder(title: String, dueDateString: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val offsetDays = when {
                dueDateString.contains("today", ignoreCase = true) -> 0
                dueDateString.contains("tomorrow", ignoreCase = true) -> 1
                else -> 3
            }
            val dueDateMillis = System.currentTimeMillis() + (offsetDays * 24L * 60L * 60L * 1000L)
            reminderDao.insertReminder(
                ReminderItem(
                    memoryId = 0,
                    title = title,
                    dueDate = dueDateMillis,
                    detectedDateString = dueDateString
                )
            )
        }
    }

    fun toggleReminderStatus(id: Int, isCompleted: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            reminderDao.updateReminderStatus(id, isCompleted)
        }
    }

    fun deleteReminder(reminder: ReminderItem) {
        viewModelScope.launch(Dispatchers.IO) {
            reminderDao.deleteReminder(reminder)
        }
    }

    /**
     * Chat response utilizing on-device cognitive RAG!
     */
    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return

        viewModelScope.launch {
            // Save user message to database
            val userMsg = ChatMessage(sender = "user", text = userText)
            withContext(Dispatchers.IO) {
                chatDao.insertMessage(userMsg)
            }

            _isChatLoading.value = true

            try {
                // Collect conversation history
                val messages = chatHistory.value
                val formattedHistory = messages.joinToString("\n") { "${it.sender.uppercase()}: ${it.text}" }

                // Gather global memory bank
                val memories = withContext(Dispatchers.IO) { memoryDao.getAllMemories() }

                // Execute Gemini RAG query
                val aiResponseStr = GeminiClient.answerQueryFromContext(
                    query = userText,
                    memories = memories,
                    chatHistory = listOf(formattedHistory)
                )

                aiTokensUsed.update { it + 1850 } // simulation

                // Log the AI answer in database
                val botMsg = ChatMessage(sender = "assistant", text = aiResponseStr)
                withContext(Dispatchers.IO) {
                    chatDao.insertMessage(botMsg)
                }
            } catch (e: Exception) {
                val errorMsg = ChatMessage(sender = "assistant", text = "Sorry, I had trouble retrieving your memory bank right now: ${e.message}")
                withContext(Dispatchers.IO) {
                    chatDao.insertMessage(errorMsg)
                }
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.clearHistory()
        }
    }

    /**
     * Simulates a direct share sheet/import from WhatsApp with automatic chunking.
     */
    fun simulateWhatsAppImport(chatLog: String) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                // Send WhatsApp chat log to Gemini to extract memory anchors
                val prompt = """
                    You are parsing a snippet of exported WhatsApp group/individual chat log.
                    Look through it and identify important dates, addresses, link endpoints, notes, study instructions, or receipts shared.
                    Split them into separate actionable items, and extract the summary and details for each category.
                    
                    Chat log to analyze:
                    "$chatLog"
                    
                    Return a JSON containing extracted memory entries. Make it parseable.
                    Format:
                    {
                      "memories": [
                         {
                           "title": "Clean descriptive title",
                           "rawText": "Actual fragment or aggregated context of what was said",
                           "category": "finance/study/travel/medical/passwords/assignments/general",
                           "tags": "whatsapp, group, keyword"
                         }
                      ]
                    }
                """.trimIndent()

                val apiResponse = GeminiClient.getApiKey().let { key ->
                    if (key.isNotEmpty()) {
                        // Real processing
                        val response = GeminiClient.answerQueryFromContext(prompt, emptyList(), emptyList())
                        var cleanText = response.trim()
                        if (cleanText.startsWith("```json")) cleanText = cleanText.substring(7)
                        if (cleanText.startsWith("```")) cleanText = cleanText.substring(3)
                        if (cleanText.endsWith("```")) cleanText = cleanText.substring(0, cleanText.length - 3)
                        cleanText.trim()
                    } else {
                        // Standalone simulation fallback
                        "AUTO_FALLBACK"
                    }
                }

                if (apiResponse == "AUTO_FALLBACK" || apiResponse.startsWith("Welcome") || !apiResponse.contains("memories")) {
                    // Fallback local mock simulation parser
                    val lines = chatLog.trim().lines()
                    val textCombined = lines.joinToString(" ")
                    val mockAutoItem = MemoryItem(
                        title = "WhatsApp Buzz: Import Link",
                        mediaType = "link",
                        rawText = textCombined,
                        extractedSummary = "Imported chat records referencing Amit, exams, and links.",
                        category = "study",
                        tags = "whatsapp, imported",
                        importanceScore = 7
                    )
                    withContext(Dispatchers.IO) {
                        memoryDao.insertMemory(mockAutoItem)
                        reminderDao.insertReminder(
                            ReminderItem(
                                memoryId = 0,
                                title = "Exam preparation tasks (from WhatsApp chat)",
                                dueDate = System.currentTimeMillis() + (2 * 24L * 60L * 60L * 1000L),
                                detectedDateString = "Friday"
                            )
                        )
                    }
                } else {
                    // Real extraction from WhatsApp chat snippet
                    try {
                        val mainJson = org.json.JSONObject(apiResponse)
                        val items = mainJson.optJSONArray("memories")
                        if (items != null) {
                            for (i in 0 until items.length()) {
                                val item = items.getJSONObject(i)
                                val memoryItem = MemoryItem(
                                    title = item.optString("title", "Imported Note"),
                                    mediaType = "text",
                                    rawText = item.optString("rawText", ""),
                                    extractedSummary = "Automatically parsed from imported chat.",
                                    category = item.optString("category", "general"),
                                    tags = item.optString("tags", "whatsapp"),
                                    importanceScore = 6
                                )
                                withContext(Dispatchers.IO) {
                                    memoryDao.insertMemory(memoryItem)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("SecondBrainViewModel", "Failed to parse WhatsApp JSON: $apiResponse", e)
                    }
                }
            } finally {
                _isUploading.value = false
            }
        }
    }

    /**
     * Clear all database records.
     */
    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            memoryDao.clearAll()
            chatDao.clearHistory()
        }
    }

    private suspend fun prepopulateDemoData() {
        val demo1 = MemoryItem(
            title = "Aadhaar Card Screenshot",
            mediaType = "screenshot",
            rawText = "GOVERNMENT OF INDIA. UIDAI. Name: Alex Sam. Year of Birth: 1998. Card No: 4835 1284 4215.",
            extractedSummary = "Digitized national ID card screenshot showing card number and year of birth (1998).",
            category = "passwords",
            tags = "national_id, identity, scanner",
            importanceScore = 9
        )
        val demo2 = MemoryItem(
            title = "Python Internship Notes PDF",
            mediaType = "pdf",
            rawText = "Overview of Python structures. Variables, functions, scope resolution. Assignment: Build custom HTTP server. Deadline: Friday, June 5th 2026. Send portfolio links to hr-interns@pythondev.org",
            extractedSummary = "Course curriculum for programming internship outline presenting code requirements and submission instructions.",
            category = "study",
            tags = "study, programming, deadline",
            importanceScore = 8
        )
        val demo3 = MemoryItem(
            title = "Voice note: Gym locations",
            mediaType = "voice_note",
            rawText = "Hey Alex, Amit here. The gym location is near the Metro Gate 2, right behind the central food court.",
            extractedSummary = "Voice transcript of directions forwarded by Amit locating fitness club relative to transit hubs.",
            category = "travel",
            tags = "amit, directions, transit",
            importanceScore = 6
        )

        val id1 = memoryDao.insertMemory(demo1)
        val id2 = memoryDao.insertMemory(demo2)
        val id3 = memoryDao.insertMemory(demo3)

        // Add matching demo reminders
        reminderDao.insertReminder(
            ReminderItem(
                memoryId = id2.toInt(),
                title = "Submit Python Custom HTTP Server Application",
                dueDate = System.currentTimeMillis() + (10 * 24L * 60L * 60L * 1000L),
                detectedDateString = "Friday, June 5th"
            )
        )
    }
}
