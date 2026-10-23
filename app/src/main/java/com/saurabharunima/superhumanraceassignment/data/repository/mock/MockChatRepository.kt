package com.saurabharunima.superhumanraceassignment.data.repository.mock

import com.saurabharunima.superhumanraceassignment.core.CHAT_TYPING_DELAY_MS
import com.saurabharunima.superhumanraceassignment.core.suspendRunCatching
import com.saurabharunima.superhumanraceassignment.data.model.ChatMessage
import com.saurabharunima.superhumanraceassignment.data.remote.MockDataSource
import com.saurabharunima.superhumanraceassignment.data.repository.ChatRepository
import com.saurabharunima.superhumanraceassignment.domain.KeywordResponder
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class MockChatRepository @Inject constructor(
    private val source: MockDataSource,
    private val clock: Clock,
) : ChatRepository {

    private fun bot(text: String) =
        ChatMessage(UUID.randomUUID().toString(), text, fromUser = false, timestamp = clock.millis())

    override suspend fun greeting(): Result<ChatMessage> = suspendRunCatching {
        bot(source.load().chat.greeting)
    }

    override suspend fun reply(userText: String): Result<ChatMessage> = suspendRunCatching {
        delay(CHAT_TYPING_DELAY_MS)
        val chat = source.load().chat
        val responder = KeywordResponder(chat.rules.map { it.keywords to it.reply }, chat.fallback)
        bot(responder.respond(userText))
    }
}
