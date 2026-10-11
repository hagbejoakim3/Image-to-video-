package com.example.engine

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiScriptService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val isApiKeyAvailable: Boolean
        get() = BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

    suspend fun generateOrEnhanceScript(
        prompt: String,
        style: String = "Naturel & Percutant",
        avatarRole: String = "Présentateur"
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!isApiKeyAvailable) {
            // Graceful fallback: return a smartly formatted script tailored to style and role
            return@withContext Result.success(getSmartLocalScript(prompt, style, avatarRole))
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${BuildConfig.GEMINI_API_KEY}"

            val systemPrompt = "Tu es un scénariste expert en vidéos parlantes et avatars virtuels animés. " +
                    "Génère un script parlé court (entre 2 et 4 phrases percutantes, 30 à 60 mots maximum) en français, " +
                    "spécialement écrit pour être prononcé oralement par un avatar de type '$avatarRole' dans un style '$style'. " +
                    "Ne renvoie QUE le texte à prononcer, sans guillemets, sans didascalies, sans émoticônes."

            val userText = if (prompt.isBlank()) {
                "Génère un discours captivant pour commencer la vidéo."
            } else {
                "Améliore et transforme ce texte en script oral accrocheur: \"$prompt\""
            }

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray()
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray()
                    partsArray.put(JSONObject().apply {
                        put("text", "$systemPrompt\n\n$userText")
                    })
                    put("parts", partsArray)
                }
                contentsArray.put(contentObj)
                put("contents", contentsArray)

                val genConfig = JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 200)
                }
                put("generationConfig", genConfig)
            }

            val request = Request.Builder()
                .url(endpoint)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.success(getSmartLocalScript(prompt, style, avatarRole))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val generatedText = parts?.optJSONObject(0)?.optString("text")?.trim()

            if (!generatedText.isNullOrBlank()) {
                Result.success(generatedText)
            } else {
                Result.success(getSmartLocalScript(prompt, style, avatarRole))
            }
        } catch (e: Exception) {
            Result.success(getSmartLocalScript(prompt, style, avatarRole))
        }
    }

    private fun getSmartLocalScript(userText: String, style: String, role: String): String {
        if (userText.isNotBlank()) {
            return when {
                style.contains("Sérieux", ignoreCase = true) ->
                    "Information essentielle à retenir : $userText. Restez attentifs aux prochaines évolutions."
                style.contains("Futuriste", ignoreCase = true) || style.contains("Cyber", ignoreCase = true) ->
                    "Transmission décryptée : $userText. Les données indiquent une nouvelle ère imminente."
                style.contains("Joyeux", ignoreCase = true) || style.contains("Dynamique", ignoreCase = true) ->
                    "Excellente nouvelle aujourd'hui ! $userText. Partagez cette énergie autour de vous !"
                else ->
                    "Bienvenue dans cette vidéo ! $userText. Merci pour votre écoute et à très bientôt !"
            }
        }

        return when {
            role.contains("Journal", ignoreCase = true) || role.contains("Lucas", ignoreCase = true) ->
                "Flash spécial en direct de notre plateau ! Aujourd'hui, une révolution technologique permet d'animer chaque photo en vidéo vivante. Restez avec nous pour tous les détails."
            role.contains("Cyber", ignoreCase = true) ->
                "Initialisation de l'hologramme réussie. Ici le secteur cybernétique. Votre réalité visuelle vient de franchir une nouvelle dimension numérique."
            role.contains("Emma", ignoreCase = true) || role.contains("Créat", ignoreCase = true) ->
                "Coucou à tous ! Je suis super contente de vous retrouver aujourd'hui pour cette toute nouvelle vidéo animée. Dites-moi en commentaire ce que vous en pensez !"
            else ->
                "Bonjour à tous et bienvenue. Grâce à notre intelligence artificielle, chaque image prend vie et s'exprime avec clarté et conviction."
        }
    }
}
