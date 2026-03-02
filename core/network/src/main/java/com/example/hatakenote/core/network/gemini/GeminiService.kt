package com.example.hatakenote.core.network.gemini

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor(
    private val generativeModel: GenerativeModel,
) {
    private val chat by lazy {
        generativeModel.startChat(
            history = listOf(
                content(role = "user") {
                    text(SYSTEM_PROMPT)
                },
                content(role = "model") {
                    text("はい、承知しました。家庭菜園のアドバイザーとして、野菜の育て方や病害虫対策、肥料のタイミングなど、畑仕事に関する質問にお答えします。どのようなことでもお気軽にご質問ください。")
                }
            )
        )
    }

    suspend fun sendMessage(message: String): Result<String> {
        return try {
            val response = chat.sendMessage(message)
            val text = response.text
            if (text != null) {
                Result.success(text)
            } else {
                Result.failure(Exception("Empty response"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val SYSTEM_PROMPT = """
あなたは家庭菜園のアドバイザーです。以下の点に注意して回答してください：

1. 野菜の育て方、病害虫対策、肥料のタイミング、収穫時期など、家庭菜園に関する質問に答えてください
2. 日本の気候や季節に合わせたアドバイスを心がけてください
3. 初心者にもわかりやすい言葉で説明してください
4. 具体的な数値（水やりの量、肥料の量、間隔など）を含めると親切です
5. 連作障害や輪作についても必要に応じてアドバイスしてください
6. 回答は簡潔にまとめつつ、必要な情報は漏らさないようにしてください

家庭菜園に関係ない質問には「すみません、家庭菜園に関する質問にお答えしています」と返答してください。
"""
    }
}
