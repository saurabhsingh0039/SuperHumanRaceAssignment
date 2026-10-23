package com.saurabharunima.superhumanraceassignment.testing

import com.saurabharunima.superhumanraceassignment.data.local.SessionStorage
import com.saurabharunima.superhumanraceassignment.data.model.ChatMessage
import com.saurabharunima.superhumanraceassignment.data.model.Profile
import com.saurabharunima.superhumanraceassignment.data.model.User
import com.saurabharunima.superhumanraceassignment.data.model.UserStats
import com.saurabharunima.superhumanraceassignment.data.remote.ChatDto
import com.saurabharunima.superhumanraceassignment.data.remote.ChatRuleDto
import com.saurabharunima.superhumanraceassignment.data.remote.LeagueDto
import com.saurabharunima.superhumanraceassignment.data.remote.MockDataDto
import com.saurabharunima.superhumanraceassignment.data.remote.MockDataSource
import com.saurabharunima.superhumanraceassignment.data.remote.UserDto
import com.saurabharunima.superhumanraceassignment.data.repository.ChatRepository
import com.saurabharunima.superhumanraceassignment.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** Replaces Dispatchers.Main so viewModelScope runs on the test scheduler. */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(val dispatcher: TestDispatcher = StandardTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

class FakeChatRepository : ChatRepository {
    var greetingResult: Result<ChatMessage> = Result.success(ChatMessage("g", "Hi", fromUser = false, timestamp = 0L))
    var replyDelayMs = 0L
    /** When set, every reply returns this instead of an echo. */
    var replyOverride: Result<ChatMessage>? = null
    val replyCalls = mutableListOf<String>()

    override suspend fun greeting(): Result<ChatMessage> = greetingResult

    override suspend fun reply(userText: String): Result<ChatMessage> {
        replyCalls += userText
        delay(replyDelayMs)
        return replyOverride
            ?: Result.success(ChatMessage("bot-${replyCalls.size}", "echo: $userText", fromUser = false, timestamp = 0L))
    }
}

fun profileOf(name: String, id: String = "u1", email: String = "ava@example.com") =
    Profile(User(id, name, email, avatarUrl = null), UserStats(xp = 0, seeds = 0, gems = 0, streakDays = 0))

class FakeUserRepository(
    var profile: Result<Profile> = Result.success(profileOf("Ava Green")),
) : UserRepository {
    val updateCalls = mutableListOf<String>()
    var updateFailure: Throwable? = null

    override suspend fun getProfile(): Result<Profile> = profile

    override suspend fun updateName(name: String): Result<Profile> {
        updateCalls += name
        updateFailure?.let { return Result.failure(it) }
        val updated = profile.getOrThrow().let { it.copy(user = it.user.copy(name = name)) }
        profile = Result.success(updated)
        return Result.success(updated)
    }
}

class FakeSessionStorage(initial: String? = null) : SessionStorage {
    private val state = MutableStateFlow(initial)
    override val userId: Flow<String?> = state
    override suspend fun save(userId: String) { state.value = userId }
    override suspend fun clear() { state.value = null }
    val current: String? get() = state.value
}

fun userDto(id: String, name: String, email: String, password: String = "password123") =
    UserDto(id = id, name = name, email = email, password = password, xp = 0, seeds = 0, gems = 0, streakDays = 0)

class FakeMockDataSource(
    private val users: List<UserDto> = listOf(
        userDto("u1", "Ava Green", "ava@example.com"),
        userDto("u2", "Ben Park", "ben@example.com"),
    ),
    private val chat: ChatDto = ChatDto(
        greeting = "Hello from Ora",
        fallback = "I didn't get that",
        rules = listOf(
            ChatRuleDto(listOf("carbon footprint", "co2"), "CO2 answer"),
            ChatRuleDto(listOf("transport"), "Transport answer"),
        ),
    ),
) : MockDataSource {
    override suspend fun load() = MockDataDto(
        currentUserId = "u1",
        league = LeagueDto("Bronze", advanceTop = 10, endsInHours = 5),
        users = users,
        posts = emptyList(),
        dailyLogs = emptyList(),
        friends = emptyList(),
        chat = chat,
    )
}
