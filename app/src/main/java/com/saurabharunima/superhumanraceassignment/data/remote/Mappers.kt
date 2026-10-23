package com.saurabharunima.superhumanraceassignment.data.remote

import com.saurabharunima.superhumanraceassignment.data.model.Comment
import com.saurabharunima.superhumanraceassignment.data.model.DailyLog
import com.saurabharunima.superhumanraceassignment.data.model.LeaderEntry
import com.saurabharunima.superhumanraceassignment.data.model.Post
import com.saurabharunima.superhumanraceassignment.data.model.TransportMode
import com.saurabharunima.superhumanraceassignment.data.model.User
import com.saurabharunima.superhumanraceassignment.data.model.UserStats
import java.time.LocalDate

fun UserDto.toUser() = User(id = id, name = name, email = email, avatarUrl = avatarUrl)

fun UserDto.toStats() = UserStats(xp = xp, seeds = seeds, gems = gems, streakDays = streakDays)

fun MockDataDto.toPosts(nowMillis: Long): List<Post> {
    val names = users.associate { it.id to it.name }
    return posts.map { p ->
        Post(
            id = p.id,
            userId = p.userId,
            userName = names[p.userId].orEmpty(),
            text = p.text,
            co2SavedKg = p.co2SavedKg,
            timestamp = nowMillis - p.minutesAgo * 60_000L,
            likeCount = p.likeCount,
            likedByMe = p.likedByMe,
            comments = p.comments.map { c ->
                Comment(
                    id = c.id,
                    userId = c.userId,
                    userName = names[c.userId].orEmpty(),
                    text = c.text,
                    timestamp = nowMillis - c.minutesAgo * 60_000L,
                )
            },
        )
    }
}

fun DailyLogDto.toDomain(today: LocalDate) = DailyLog(
    date = today.minusDays(daysAgo.toLong()),
    mode = TransportMode.valueOf(mode),
    km = km,
)

/**
 * Competition ranking: ties share a rank (1, 2, 2, 4). Ties break alphabetically for display order.
 */
fun List<UserDto>.toRankedEntries(currentUserId: String): List<LeaderEntry> {
    val sorted = sortedWith(compareByDescending<UserDto> { it.xp }.thenBy { it.name })
    return sorted.map { u ->
        LeaderEntry(
            rank = sorted.indexOfFirst { it.xp == u.xp } + 1,
            userId = u.id,
            name = u.name,
            avatarUrl = u.avatarUrl,
            xp = u.xp,
            gems = u.gems,
            isCurrentUser = u.id == currentUserId,
        )
    }
}
