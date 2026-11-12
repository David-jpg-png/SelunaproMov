package me.andresdavid.myapplication.data

import com.google.firebase.Firebase
import com.google.firebase.vertexai.vertexAI

object GeminiService {

    // Initialize Gemini 1.5 Flash using Firebase Vertex AI
    private val generativeModel by lazy {
        Firebase.vertexAI.generativeModel(
            modelName = "gemini-1.5-flash"
        )
    }

    /**
     * Generates a personalized holistic wellness tip for Seluna users based on their profile and daily log.
     */
    suspend fun generateCycleAdvice(
        userName: String,
        phase: String,
        symptoms: List<String>,
        moods: List<String>,
        conditions: List<String>
    ): String {
        return try {
            val prompt = """
                Actúa como Seluna AI, una experta en salud holística y bienestar femenino.
                Genera un consejo empático, positivo y breve (máximo 3 oraciones) para la usuaria $userName.
                
                Datos actuales:
                - Fase del ciclo: $phase
                - Síntomas: ${symptoms.ifEmpty { listOf("Ninguno registrado") }.joinToString()}
                - Ánimo: ${moods.ifEmpty { listOf("Tranquila") }.joinToString()}
                - Diagnósticos: ${conditions.ifEmpty { listOf("Ninguna") }.joinToString()}
                
                Habla de forma cálida, sin tecnicismos excesivos y brinda una recomendación práctica.
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            response.text ?: "Escucha a tu cuerpo, mantén una hidratación constante y tómate pausas activas durante el día."
        } catch (e: Exception) {
            e.printStackTrace()
            "Tómate un momento para descansar, hidratarte y conectar con tu ritmo natural hoy."
        }
    }

    /**
     * Answers custom health or cycle questions asked by the user.
     */
    suspend fun askGemini(question: String): String {
        return try {
            val prompt = """
                Eres Seluna AI, un asistente de bienestar femenino educativo y empático.
                Responde la siguiente duda de la usuaria en máximo 4 oraciones con tono profesional y cercano:
                
                Pregunta: $question
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            response.text ?: "No pude procesar la consulta en este momento."
        } catch (e: Exception) {
            e.printStackTrace()
            "Ocurrió un error al procesar tu consulta con la IA. Revisa tu conexión a internet."
        }
    }
}
