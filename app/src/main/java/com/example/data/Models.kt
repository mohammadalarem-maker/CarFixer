package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

data class DiagnosticMessage(
    val role: String, // "user" or "assistant"
    val content: String,
    val options: List<String>? = null // Quick reply buttons for Arabic response, e.g. "نعم", "لا", "غير متأكد"
)

@Entity(tableName = "diagnostic_sessions")
data class DiagnosticSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val carModel: String,
    val symptom: String,
    val category: String, // "كهرباء" (Electrical), "ميكانيك" (Mechanical), "عام" (General)
    val status: String, // "نشط" (Active), "مكتمل" (Completed)
    val timestamp: Long = System.currentTimeMillis(),
    val messagesJson: String = "[]", // List<DiagnosticMessage> serialized
    val diagnosisSummary: String? = null // Final solution summary
)

class Converters {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    
    private val type = Types.newParameterizedType(List::class.java, DiagnosticMessage::class.java)
    private val adapter = moshi.adapter<List<DiagnosticMessage>>(type)

    @TypeConverter
    fun fromJson(json: String): List<DiagnosticMessage> {
        return try {
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun toJson(list: List<DiagnosticMessage>): String {
        return try {
            adapter.toJson(list)
        } catch (e: Exception) {
            "[]"
        }
    }
}
