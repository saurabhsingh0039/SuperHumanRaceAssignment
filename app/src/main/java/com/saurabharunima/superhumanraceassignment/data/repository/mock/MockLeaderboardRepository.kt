package com.saurabharunima.superhumanraceassignment.data.repository.mock

import com.saurabharunima.superhumanraceassignment.core.NETWORK_DELAY_MS
import com.saurabharunima.superhumanraceassignment.core.suspendRunCatching
import com.saurabharunima.superhumanraceassignment.data.model.Leaderboard
import com.saurabharunima.superhumanraceassignment.data.model.League
import com.saurabharunima.superhumanraceassignment.data.remote.MockDataSource
import com.saurabharunima.superhumanraceassignment.data.remote.toRankedEntries
import com.saurabharunima.superhumanraceassignment.data.repository.LeaderboardRepository
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class MockLeaderboardRepository @Inject constructor(
    private val source: MockDataSource,
    private val store: UserStore,
    private val clock: Clock,
) : LeaderboardRepository {

    override suspend fun getLeaderboard(): Result<Leaderboard> = suspendRunCatching {
        delay(NETWORK_DELAY_MS)
        val data = source.load()
        Leaderboard(
            league = League(
                name = data.league.name,
                advanceTop = data.league.advanceTop,
                endsAtMillis = clock.millis() + data.league.endsInHours * 3_600_000L,
            ),
            entries = store.all().toRankedEntries(store.currentUserId()),
        )
    }
}
