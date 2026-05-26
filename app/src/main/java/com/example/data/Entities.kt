package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val mediaType: String, // "screenshot", "pdf", "voice_note", "link", "text"
    val rawText: String, // Extracted raw content (OCR text, note, transcript)
    val extractedSummary: String, // AI-generated summary
    val category: String, // "finance", "study", "travel", "medical", "passwords", "assignments", "general"
    val tags: String, // Comma separated list of tags
    val importanceScore: Int = 5, // 1 to 10
    val timestamp: Long = System.currentTimeMillis(),
    val mediaUri: String? = null // Path or simulation url of resource
)

@Entity(tableName = "reminders")
data class ReminderItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val memoryId: Int, // Refers to the memory it was extracted from (or 0 if stand-alone)
    val title: String,
    val dueDate: Long,
    val isCompleted: Boolean = false,
    val detectedDateString: String // e.g., "April 15th", "Friday"
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user" or "assistant"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
