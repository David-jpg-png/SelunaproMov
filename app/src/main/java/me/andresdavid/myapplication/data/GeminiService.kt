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
     * Generates a personalized holistic wellness tip specifically focused on menstrual health,
     * cycle symptoms, and user condition profile.
     */
    suspend fun generateCycleAdvice(
        userName: String,
        phase: String,
        symptoms: List<String>,
        moods: List<String>,
        conditions: List<String>,
        goal: String = "",
        flowLevel: String = "",
        contraceptive: String = "",
        notes: String = ""
    ): String {
        return try {
            val prompt = """
                Actúa como Seluna AI, una experta especialista en salud menstrual, ciclo hormonal y bienestar ginecológico femenino.
                Tu objetivo principal es dar consejos y ayuda específica e individualizada sobre la menstruación, la fase del ciclo y la salud corporal de la usuaria $userName, basándote rigurosamente en la información que ella ha ingresado en la aplicación.

                DATOS INGRESADOS POR LA USUARIA:
                - Nombre: $userName
                - Objetivo personal: ${goal.ifBlank { "Seguimiento general del ciclo" }}
                - Enfoque / Fase actual: ${translateApproach(phase)}
                - Método anticonceptivo: ${translateContraceptive(contraceptive)}
                - Condiciones / Diagnósticos médicos registrados: ${conditions.ifEmpty { listOf("Ninguno registrado") }.joinToString(", ")}
                - Nivel de flujo menstrual hoy: ${flowLevel.ifBlank { "Sin flujo registrado hoy" }}
                - Síntomas físicos registrados: ${symptoms.ifEmpty { listOf("Sin síntomas reportados hoy") }.joinToString(", ")}
                - Estado de ánimo: ${moods.ifEmpty { listOf("Tranquila") }.joinToString(", ")}
                - Notas personales del día: ${notes.ifBlank { "Sin notas adicionales" }}

                INSTRUCCIONES DE RESPUESTA:
                1. Ofrécele una recomendación clara, empática y muy específica relacionada con su menstruación, flujo, síntomas (como cólicos, hinchazón o dolor) o condiciones médicas (como SOP o endometriosis si aplica).
                2. Sugiere acciones prácticas inmediatas (remedios caseros seguros, infusiones, termoterapia, hábitos de hidratación, alimentación o estiramientos).
                3. Mantén un tono cálido, humano, conciso y profesional (máximo 3 oraciones).
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            response.text ?: "Escucha a tu cuerpo, mantén una hidratación constante y descansa adecuadamente durante tu ciclo."
        } catch (e: Exception) {
            e.printStackTrace()
            "Tómate un momento para descansar, aplicar calor suave en la zona abdominal e hidratarte muy bien hoy."
        }
    }

    /**
     * Answers custom health or cycle questions asked by the user, incorporating their complete profile & logs context.
     */
    suspend fun askGemini(
        question: String,
        userProfile: UserProfile? = null,
        todayLog: DailyLogItem? = null
    ): String {
        return try {
            val userName = userProfile?.name?.ifBlank { "Usuaria" } ?: "Usuaria"
            val goal = userProfile?.goal ?: ""
            val approach = userProfile?.approach ?: "TRACK_CYCLE"
            val contraceptive = userProfile?.contraceptive ?: ""
            val periodDays = userProfile?.periodDays ?: 5
            val conditions = userProfile?.conditions ?: emptyList()

            val flow = todayLog?.flowLevel ?: ""
            val symptoms = todayLog?.symptoms ?: emptyList()
            val moods = todayLog?.moods ?: emptyList()
            val notes = todayLog?.notes ?: ""

            val prompt = """
                Eres Seluna AI, una asistente experta y especializada en salud menstrual, ciclo hormonal y ginecología.
                Responde la duda de la usuaria brindándole ayuda específica y consejos precisos sobre su menstruación y bienestar corporal, personalizando tu respuesta según los datos que ella misma ha ingresado en la app.

                DATOS DEL PERFIL Y REGISTRO DE LA USUARIA ($userName):
                - Enfoque ginecológico: ${translateApproach(approach)}
                - Objetivo: ${goal.ifBlank { "Bienestar y ciclo menstrual" }}
                - Duración media de menstruación: $periodDays días
                - Método anticonceptivo: ${translateContraceptive(contraceptive)}
                - Diagnósticos/Condiciones médicas: ${conditions.ifEmpty { listOf("Ninguno") }.joinToString(", ")}
                - Flujo menstrual actual: ${flow.ifBlank { "No registrado" }}
                - Síntomas reportados hoy: ${symptoms.ifEmpty { listOf("Ninguno") }.joinToString(", ")}
                - Estado de ánimo: ${moods.ifEmpty { listOf("No registrado") }.joinToString(", ")}
                - Notas personales de la usuaria: ${notes.ifBlank { "Ninguna" }}

                PREGUNTA O DUDA DE LA USUARIA:
                "$question"

                INSTRUCCIONES DE RESPUESTA:
                - Responde directamente su pregunta orientándola de forma clara sobre su salud menstrual y ginecológica.
                - Si sus síntomas o condiciones registradas (por ejemplo: cólicos, SOP, anticonceptivos, flujo) tienen relación con su duda, menciónalos de forma natural para darle un consejo verdaderamente personalizado.
                - Responde en máximo 3-4 oraciones con tono profesional, cercano, seguro y empático.
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            response.text ?: "No pude procesar la consulta en este momento."
        } catch (e: Exception) {
            e.printStackTrace()
            "Ocurrió un error al procesar tu consulta con Gemini AI. Revisa tu conexión a internet."
        }
    }

    private fun translateApproach(approach: String): String = when (approach) {
        "CONCEIVE" -> "Buscando concebir"
        "PREVENT_PREGNANCY" -> "Prevenir embarazo"
        "TRACK_CYCLE" -> "Conocer y registrar mi ciclo"
        else -> approach.ifBlank { "Seguimiento general del ciclo" }
    }

    private fun translateContraceptive(contraceptive: String): String = when (contraceptive) {
        "PILLS" -> "Pastillas anticonceptivas"
        "IUD" -> "Dispositivo Intrauterino (DIU)"
        "IMPLANT" -> "Implante subdérmico"
        "NONE" -> "Sin método anticonceptivo"
        else -> contraceptive.ifBlank { "Natural / Sin registrar" }
    }
}
