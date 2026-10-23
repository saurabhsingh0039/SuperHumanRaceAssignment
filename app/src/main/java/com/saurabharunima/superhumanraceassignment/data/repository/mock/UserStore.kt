package com.saurabharunima.superhumanraceassignment.data.repository.mock

import com.saurabharunima.superhumanraceassignment.core.NotFoundException
import com.saurabharunima.superhumanraceassignment.data.remote.MockDataSource
import com.saurabharunima.superhumanraceassignment.data.remote.UserDto
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** In-memory users + session shared by all mock repositories. */
@Singleton
class UserStore @Inject constructor(private val source: MockDataSource) {
    private val mutex = Mutex()
    private var users: List<UserDto>? = null
    private val _session = MutableStateFlow<String?>(null)
    val session: StateFlow<String?> = _session.asStateFlow()

    private suspend fun ensure(): List<UserDto> =
        users ?: source.load().users.also { users = it }

    suspend fun all(): List<UserDto> = mutex.withLock { ensure() }

    suspend fun add(user: UserDto) = mutex.withLock { users = ensure() + user }

    suspend fun update(id: String, transform: (UserDto) -> UserDto): UserDto = mutex.withLock {
        val list = ensure()
        val old = list.firstOrNull { it.id == id } ?: throw NotFoundException("User")
        val updated = transform(old)
        users = list.map { if (it.id == id) updated else it }
        updated
    }

    fun setSession(userId: String?) {
        _session.value = userId
    }

    /** Signed-in user, or the mock default so screens work before login during development. */
    suspend fun currentUserId(): String = _session.value ?: source.load().currentUserId
}
