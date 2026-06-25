package com.example.data

import com.example.network.GeminiDiagnoser
import com.example.network.RetrofitClient
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class DiagnosticRepository(private val diagnosticDao: DiagnosticDao) {

    val allSessions: Flow<List<DiagnosticSession>> = diagnosticDao.getAllSessions()

    fun getSessionById(id: Int): Flow<DiagnosticSession?> = diagnosticDao.getSessionById(id)

    private val listType = Types.newParameterizedType(List::class.java, DiagnosticMessage::class.java)
    private val messagesAdapter = RetrofitClient.moshiInstance.adapter<List<DiagnosticMessage>>(listType)

    suspend fun createNewSession(carModel: String, symptom: String, category: String): Long = withContext(Dispatchers.IO) {
        val initialMessages = listOf(
            DiagnosticMessage(role = "user", content = "السيارة: $carModel | المشكلة: $symptom")
        )
        val session = DiagnosticSession(
            carModel = carModel,
            symptom = symptom,
            category = category,
            status = "نشط",
            messagesJson = messagesAdapter.toJson(initialMessages)
        )
        val sessionId = diagnosticDao.insertSession(session)
        
        // Fetch the first AI response in the background to initialize the conversation
        fetchNextStep(sessionId.toInt(), initialMessages)
        
        sessionId
    }

    suspend fun addMessageToSession(sessionId: Int, userMessage: String) = withContext(Dispatchers.IO) {
        // Retrieve current messages
        val session = diagnosticDao.getSessionById(sessionId).first()
        if (session != null) {
            val currentMessages = messagesAdapter.fromJson(session.messagesJson)?.toMutableList() ?: mutableListOf()
            currentMessages.add(DiagnosticMessage(role = "user", content = userMessage))
            
            // Update session with user message
            val updatedSession = session.copy(
                messagesJson = messagesAdapter.toJson(currentMessages)
            )
            diagnosticDao.updateSession(updatedSession)
            
            // Fetch next diagnostic step from Gemini
            fetchNextStep(sessionId, currentMessages)
        }
    }

    private suspend fun fetchNextStep(sessionId: Int, currentMessages: List<DiagnosticMessage>) {
        // Need to load session once to extract context
        // Query database on background dispatcher
        val session = withContext(Dispatchers.IO) {
            diagnosticDao.getSessionById(sessionId).first()
        } ?: return

        try {
            // Get next diagnostic result from Gemini
            val response = GeminiDiagnoser.getNextDiagnosticStep(
                carModel = session.carModel,
                category = session.category,
                symptom = session.symptom,
                history = currentMessages
            )

            val updatedMessages = currentMessages.toMutableList()
            updatedMessages.add(
                DiagnosticMessage(
                    role = "assistant",
                    content = response.questionOrAdvice,
                    options = response.options
                )
            )

            val nextSession = session.copy(
                status = if (response.isFinal) "مكتمل" else "نشط",
                messagesJson = messagesAdapter.toJson(updatedMessages),
                diagnosisSummary = if (response.isFinal) {
                    // Serialize solutionDetails to display on UI
                    val solutionAdapter = RetrofitClient.moshiInstance.adapter(com.example.network.DiagnosticResult::class.java)
                    solutionAdapter.toJson(response)
                } else {
                    session.diagnosisSummary
                }
            )

            diagnosticDao.updateSession(nextSession)
        } catch (e: Exception) {
            if (e.message != "Success") {
                e.printStackTrace()
                // Add error message as assistant response
                val updatedMessages = currentMessages.toMutableList()
                updatedMessages.add(
                    DiagnosticMessage(
                        role = "assistant",
                        content = "عذراً، حدث خطأ في النظام: ${e.localizedMessage ?: "فشل الاتصال بخبير الذكاء الاصطناعي"}. يرجى محاولة كتابة ردك مجدداً."
                    )
                )
                val errorSession = session.copy(
                    messagesJson = messagesAdapter.toJson(updatedMessages)
                )
                diagnosticDao.updateSession(errorSession)
            }
        }
    }

    suspend fun deleteSession(sessionId: Int) = withContext(Dispatchers.IO) {
        diagnosticDao.deleteSessionById(sessionId)
    }
}
