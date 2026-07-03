package moe.ouom.neriplayer.core.agent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class MusicAgentDecision(
    val shouldPlay: Boolean,
    val query: String,
    val reply: String,
    val queries: List<String> = if (query.isBlank()) emptyList() else listOf(query),
    val emotion: String = "",
    val scene: String = ""
) {
    fun searchQueries(): List<String> {
        return (queries + query)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }
}

class DeepSeekMusicAgentClient(
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val BASE_URL = "https://api.deepseek.com"
        private const val MODEL = "deepseek-chat"

        // TODO: 请在 local.properties 中配置 DEEPSEEK_API_KEY 并通过 BuildConfig 注入
        private const val DEEPSEEK_API_KEY = ""
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun decide(
        userMessage: String,
        history: List<Pair<String, String>>
    ): MusicAgentDecision = withContext(Dispatchers.IO) {
        if (DEEPSEEK_API_KEY.isBlank()) {
            return@withContext MusicAgentDecision(
                shouldPlay = false,
                query = "",
                reply = "DeepSeek 接口密钥为空。请先在音乐助手客户端文件里填写密钥。"
            )
        }

        val messages = JSONArray().apply {
            put(JSONObject().put("role", "system").put("content", systemPrompt()))
            history.takeLast(8).forEach { (role, content) ->
                put(JSONObject().put("role", role).put("content", content))
            }
            put(JSONObject().put("role", "user").put("content", userMessage))
        }
        val body = JSONObject()
            .put("model", MODEL)
            .put("temperature", 0.7)
            .put("messages", messages)
            .toString()
            .toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url("$BASE_URL/chat/completions")
            .header("Authorization", "Bearer $DEEPSEEK_API_KEY")
            .header("Content-Type", "application/json")
            .post(body)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return@withContext MusicAgentDecision(
                    shouldPlay = false,
                    query = "",
                    reply = "DeepSeek 请求失败，状态码：${response.code}"
                )
            }

            val content = JSONObject(raw)
                .optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content")
                .orEmpty()
            parseDecision(content, userMessage)
        }
    }

    private fun systemPrompt(): String {
        return """
            你是 NeriPlayer 内置的音乐助手，不只是点歌工具，还要根据用户表达做情绪分析和场景理解。
            你需要判断用户是否想听音乐，或是否适合主动用音乐安慰、陪伴、提振、放松用户。
            用户可能不会直接说“播放”，例如：“我今天上班迟到被老板扣工资了，心情不好”。这种情况应识别为沮丧、委屈、压力场景，并设置 should_play=true。
            当用户表达负面情绪时，优先选择能安慰、舒缓、陪伴的歌曲；当用户疲惫时选择放松歌曲；当用户需要动力时选择鼓舞歌曲；当用户点名歌曲或歌手时按点名内容处理。
            query 必须是最优先的一个具体音乐搜索词，尽量包含歌曲名或“歌手 歌曲名”，因为播放器会用它搜索单曲。
            queries 必须给出 1 到 3 个候选搜索词，按优先级排序，便于第一个搜不到时继续尝试。
            emotion 用中文简短概括用户情绪，例如“沮丧”“焦虑”“疲惫”“开心”“怀旧”。
            scene 用中文简短概括场景，例如“工作受挫”“通勤路上”“深夜学习”。
            reply 必须先用一句中文回应用户情绪，再说明你准备播放什么类型或哪首歌。
            reply 必须全部使用中文，除了歌曲名称、歌手名称、歌词原文可以保留原语言。
            reply 不要出现英文解释、英文操作状态、英文寒暄或英文标点风格。
            只返回 JSON，不要 Markdown，不要代码块：
            {"should_play":true/false,"emotion":"...","scene":"...","query":"...","queries":["..."],"reply":"..."}
        """.trimIndent()
    }

    private fun parseDecision(content: String, fallbackMessage: String): MusicAgentDecision {
        val jsonText = content.trim().let { text ->
            val start = text.indexOf('{')
            val end = text.lastIndexOf('}')
            if (start >= 0 && end > start) text.substring(start, end + 1) else text
        }

        return runCatching {
            val json = JSONObject(jsonText)
            val parsedQueries = json.optJSONArray("queries").toStringList()
            val query = json.optString("query").trim()
                .ifBlank { parsedQueries.firstOrNull().orEmpty() }
            MusicAgentDecision(
                shouldPlay = json.optBoolean("should_play", false),
                query = query,
                queries = parsedQueries,
                emotion = json.optString("emotion").trim(),
                scene = json.optString("scene").trim(),
                reply = json.optString("reply").trim().ifBlank { "好的，我来帮你选一首合适的歌。" }
            )
        }.getOrElse {
            MusicAgentDecision(
                shouldPlay = true,
                query = fallbackMessage.trim(),
                reply = content.ifBlank { "我来根据你的状态帮你找一首合适的歌。" }
            )
        }
    }
}

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            optString(index).trim().takeIf { it.isNotBlank() }?.let(::add)
        }
    }
}
