package com.saurabharunima.superhumanraceassignment.data.repository.mock

import com.saurabharunima.superhumanraceassignment.core.NETWORK_DELAY_MS
import com.saurabharunima.superhumanraceassignment.core.NotFoundException
import com.saurabharunima.superhumanraceassignment.core.suspendRunCatching
import com.saurabharunima.superhumanraceassignment.data.model.Comment
import com.saurabharunima.superhumanraceassignment.data.model.Post
import com.saurabharunima.superhumanraceassignment.data.remote.MockDataSource
import com.saurabharunima.superhumanraceassignment.data.remote.toPosts
import com.saurabharunima.superhumanraceassignment.data.repository.FeedRepository
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class MockFeedRepository @Inject constructor(
    private val source: MockDataSource,
    private val store: UserStore,
    private val clock: Clock,
) : FeedRepository {

    private val mutex = Mutex()
    private var posts: List<Post>? = null

    private suspend fun ensure(): List<Post> =
        posts ?: source.load().toPosts(clock.millis()).also { posts = it }

    override suspend fun getPosts(): Result<List<Post>> = suspendRunCatching {
        delay(NETWORK_DELAY_MS)
        mutex.withLock { ensure().sortedByDescending { it.timestamp } }
    }

    override suspend fun toggleLike(postId: String): Result<Post> = suspendRunCatching {
        delay(NETWORK_DELAY_MS / 2)
        mutate(postId) { p ->
            val liked = !p.likedByMe
            p.copy(likedByMe = liked, likeCount = (p.likeCount + if (liked) 1 else -1).coerceAtLeast(0))
        }
    }

    override suspend fun addComment(postId: String, text: String): Result<Post> = suspendRunCatching {
        require(text.isNotBlank()) { "Comment cannot be empty" }
        delay(NETWORK_DELAY_MS / 2)
        val me = store.currentUserId()
        val myName = store.all().firstOrNull { it.id == me }?.name.orEmpty()
        mutate(postId) { p ->
            p.copy(
                comments = p.comments + Comment(
                    id = "c_${UUID.randomUUID()}",
                    userId = me,
                    userName = myName,
                    text = text.trim(),
                    timestamp = clock.millis(),
                ),
            )
        }
    }

    private suspend fun mutate(postId: String, transform: (Post) -> Post): Post = mutex.withLock {
        val list = ensure()
        val old = list.firstOrNull { it.id == postId } ?: throw NotFoundException("Post")
        val updated = transform(old)
        posts = list.map { if (it.id == postId) updated else it }
        updated
    }
}
