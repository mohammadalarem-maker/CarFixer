package com.example.network

import com.example.data.DiagnosticMessage
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

// --- Structured Diagnostic Result Output from Gemini ---
@JsonClass(generateAdapter = true)
data class SolutionDetails(
    val summary: String,
    val steps: List<String>,
    val severity: String, // "منخفض" (Low), "متوسط" (Medium), "خطير" (High/Critical)
    val estimatedCost: String // e.g. "بسيط، يمكن إصلاحه بنفسك" or "يتطلب زيارة فني مختص، تكلفة متوسطة"
)

@JsonClass(generateAdapter = true)
data class DiagnosticResult(
    val questionOrAdvice: String,
    val options: List<String>?, // Clickable buttons for quick user replies
    val isFinal: Boolean, // True if we have found the final diagnosis and solution
    val solutionDetails: SolutionDetails? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    val moshiInstance: Moshi get() = moshi
}

object GeminiDiagnoser {
    private const val SYSTEM_PROMPT = """
أنت خبير ميكانيكا سيارات ذكي ومحترف (طبيب السيارات). مهمتك هي مساعدة المستخدم في تشخيص أعطال سيارته (ميكانيكية أو كهربائية) خطوة بخطوة بطريقة تفاعلية وسهلة الفهم للمبتدئين.

يجب أن تتبع هذا الأسلوب بدقة:
1. لا تقدم الحل النهائي فوراً من أول عرض للمشكلة إلا إذا كان العطل واضحاً جداً وبديهياً. بدلاً من ذلك، اطرح سؤالاً تفاعلياً واحداً في كل مرة واطلب من المستخدم فحص أو التحقق من جزء معين في السيارة يتعلق بالعطل.
2. وجه المستخدم لفحص أشياء ملموسة وواضحة (مثال: "افتح غطاء المحرك وتأكد من وجود زيت على مقياس الزيت"، "هل تسمع صوت طقطقة عند تشغيل المفتاح؟").
3. وفر خيارات استجابة سريعة ومختصرة للمستخدم (مثال: ["نعم، أسمع الصوت", "لا، لا يوجد صوت", "غير متأكد/لا أعرف"]).
4. عند الوصول إلى تشخيص مؤكد أو شبه مؤكد، اجعل حقل 'isFinal' يساوي true، وقدم خلاصة المشكلة 'summary' مع خطوات عملية واضحة للإصلاح 'steps'، وحدد مستوى الخطورة 'severity' (منخفض / متوسط / خطير) والتكلفة التقريبية للحل 'estimatedCost'.

يجب أن ترد دائماً بصيغة JSON مطابقة تماماً للمواصفات التالية وبدون أي نص خارجي:
{
  "questionOrAdvice": "السؤال أو النصيحة التالية الموجهة للمستخدم باللغة العربية بأسلوب ودود ومحترف ومبسط",
  "options": ["خيارات الاستجابة السريعة المقترحة، كحد أقصى 3 خيارات قصيرة لتسهيل النقر عليها"],
  "isFinal": false, // اجعلها true فقط عند الوصول للحل النهائي وتحديد العطل بدقة
  "solutionDetails": { // املأ هذا الحقل فقط عندما يكون isFinal: true، وإلا اجعله null
    "summary": "ملخص المشكلة المحددة (مثال: تلف بادئ الحركة / السلف أو ضعف البطارية)",
    "steps": [
      "الخطوة الأولى للحل (مثال: تنظيف وتثبيت أصابع البطارية)",
      "الخطوة الثانية للحل",
      "الخطوة الثالثة للحل"
    ],
    "severity": "متوسط", // اختر من: "منخفض" أو "متوسط" أو "خطير"
    "estimatedCost": "تكلفة بسيطة - يمكنك القيام بها بنفسك"
  }
}

تحدث باللغة العربية الفصحى المبسطة والقريبة من ثقافة السيارات العامة، وتجنب استخدام الرموز أو الشروحات خارج كائن الـ JSON.
"""

    suspend fun getNextDiagnosticStep(
        carModel: String,
        category: String,
        symptom: String,
        history: List<DiagnosticMessage>
    ): DiagnosticResult {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return DiagnosticResult(
                questionOrAdvice = "عذراً، لم يتم العثور على مفتاح API الخاص بـ Gemini. يرجى إعداده في لوحة الأسرار (Secrets) للبدء في التشخيص الفعلي.",
                options = listOf("فهمت"),
                isFinal = false
            )
        }

        // Build conversation prompt from history
        val contents = mutableListOf<GeminiContent>()
        
        // Add context as the initial instruction or first prompt helper
        val initialContextText = "طراز السيارة: $carModel\nتصنيف العطل المبدئي: $category\nالشكوى الأساسية: $symptom"
        
        // Convert history to Gemini contents
        history.forEach { msg ->
            contents.add(
                GeminiContent(
                    parts = listOf(GeminiPart(text = msg.content)),
                    role = if (msg.role == "user") "user" else "model"
                )
            )
        }

        // If history is empty, add the initial prompt
        if (contents.isEmpty()) {
            contents.add(
                GeminiContent(
                    parts = listOf(GeminiPart(text = "مرحباً طبيب السيارات، أريد المساعدة في تشخيص العطل التالي لسيارتي:\n$initialContextText")),
                    role = "user"
                )
            )
        }

        val request = GeminiRequest(
            contents = contents,
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = SYSTEM_PROMPT))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.4f
            )
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("No text returned from Gemini")

            val adapter = RetrofitClient.moshiInstance.adapter(DiagnosticResult::class.java)
            adapter.fromJson(jsonText) ?: throw Exception("Failed to parse JSON response")
        } catch (e: retrofit2.HttpException) {
            e.printStackTrace()
            val errorBody = e.response()?.errorBody()?.string()
            val errorMsg = if (!errorBody.isNullOrEmpty()) {
                "HTTP ${e.code()}: $errorBody"
            } else {
                "HTTP ${e.code()}: ${e.message()}"
            }
            DiagnosticResult(
                questionOrAdvice = "حدث خطأ أثناء التواصل مع خبير الذكاء الاصطناعي: $errorMsg. يرجى المحاولة مرة أخرى أو تزويد تفاصيل أكثر.",
                options = listOf("إعادة المحاولة"),
                isFinal = false
            )
        } catch (e: Exception) {
            e.printStackTrace()
            DiagnosticResult(
                questionOrAdvice = "حدث خطأ أثناء التواصل مع خبير الذكاء الاصطناعي: ${e.localizedMessage ?: "خطأ غير معروف"}. يرجى المحاولة مرة أخرى أو تزويد تفاصيل أكثر.",
                options = listOf("إعادة المحاولة"),
                isFinal = false
            )
        }
    }
}
