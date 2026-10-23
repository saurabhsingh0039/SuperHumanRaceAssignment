package com.saurabharunima.superhumanraceassignment.data.repository.mock

import com.saurabharunima.superhumanraceassignment.core.NETWORK_DELAY_MS
import com.saurabharunima.superhumanraceassignment.core.NotFoundException
import com.saurabharunima.superhumanraceassignment.core.suspendRunCatching
import com.saurabharunima.superhumanraceassignment.data.model.Activity
import com.saurabharunima.superhumanraceassignment.data.model.ActivityType
import com.saurabharunima.superhumanraceassignment.data.model.TodayActivities
import com.saurabharunima.superhumanraceassignment.data.remote.MockDataSource
import com.saurabharunima.superhumanraceassignment.data.repository.ActivityRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** In-memory progress: resets on app restart (mock limitation). */
@Singleton
class MockActivityRepository @Inject constructor(
    private val source: MockDataSource,
    private val store: UserStore,
) : ActivityRepository {

    private val mutex = Mutex()
    private val completed = mutableSetOf<String>()
    private val activated = mutableSetOf<String>()

    override suspend fun getToday(): Result<TodayActivities> = suspendRunCatching {
        delay(NETWORK_DELAY_MS)
        snapshot()
    }

    override suspend fun activate(id: String): Result<TodayActivities> = suspendRunCatching {
        delay(NETWORK_DELAY_MS / 2)
        find(id)
        mutex.withLock { activated += id }
        snapshot()
    }

    override suspend fun complete(id: String): Result<TodayActivities> = suspendRunCatching {
        delay(NETWORK_DELAY_MS / 2)
        val dto = find(id)
        val (alreadyDone, firstOfDay) = mutex.withLock {
            val done = id in completed
            val first = !done && completed.isEmpty()
            if (!done) completed += id
            done to first
        }
        if (!alreadyDone) {
            store.update(store.currentUserId()) {
                it.copy(xp = it.xp + dto.xpReward, streakDays = it.streakDays + if (firstOfDay) 1 else 0)
            }
        }
        snapshot()
    }

    private suspend fun find(id: String) =
        source.load().activities.firstOrNull { it.id == id } ?: throw NotFoundException("Activity")

    private suspend fun snapshot(): TodayActivities {
        val (done, unlocked) = mutex.withLock { completed.toSet() to activated.toSet() }
        val list = source.load().activities.map { dto ->
            Activity(
                id = dto.id,
                type = ActivityType.valueOf(dto.type),
                title = dto.title,
                xpReward = dto.xpReward,
                isNew = dto.isNew && dto.id !in done,
                needsActivation = dto.needsActivation && dto.id !in unlocked,
                completed = dto.id in done,
            )
        }
        return TodayActivities(list, completedToday = done.isNotEmpty())
    }
}
