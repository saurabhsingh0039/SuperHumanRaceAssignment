package com.saurabharunima.superhumanraceassignment.data.repository.mock

import com.saurabharunima.superhumanraceassignment.core.NETWORK_DELAY_MS
import com.saurabharunima.superhumanraceassignment.core.suspendRunCatching
import com.saurabharunima.superhumanraceassignment.data.model.Friend
import com.saurabharunima.superhumanraceassignment.data.remote.MockDataSource
import com.saurabharunima.superhumanraceassignment.data.repository.FriendsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class MockFriendsRepository @Inject constructor(
    private val source: MockDataSource,
    private val store: UserStore,
) : FriendsRepository {

    override suspend fun getNearby(lat: Double, lng: Double): Result<List<Friend>> = suspendRunCatching {
        delay(NETWORK_DELAY_MS)
        val users = store.all().associateBy { it.id }
        source.load().friends.mapNotNull { f ->
            users[f.userId]?.let { u ->
                Friend(u.id, u.name, u.avatarUrl, lat + f.dLat, lng + f.dLng, f.active)
            }
        }
    }
}
