package com.saurabharunima.superhumanraceassignment.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class MockDataDto(
    val currentUserId: String,
    val league: LeagueDto,
    val users: List<UserDto>,
    val posts: List<PostDto>,
    val dailyLogs: List<DailyLogDto>,
    val friends: List<FriendDto>,
    val chat: ChatDto,
    val activities: List<ActivityDto> = emptyList(),
)

@Serializable
data class LeagueDto(val name: String, val advanceTop: Int, val endsInHours: Int)

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val password: String,
    val avatarUrl: String? = null,
    val xp: Int,
    val seeds: Int,
    val gems: Int,
    val streakDays: Int,
)

@Serializable
data class PostDto(
    val id: String,
    val userId: String,
    val text: String,
    val co2SavedKg: Double,
    val minutesAgo: Int,
    val likeCount: Int,
    val likedByMe: Boolean = false,
    val comments: List<CommentDto> = emptyList(),
)

@Serializable
data class CommentDto(val id: String, val userId: String, val text: String, val minutesAgo: Int)

/** daysAgo keeps mock data fresh: dates are resolved against today at read time. */
@Serializable
data class DailyLogDto(val daysAgo: Int, val mode: String, val km: Double)

/** Offsets from the user's location, so friends appear nearby wherever the device is. */
@Serializable
data class FriendDto(val userId: String, val dLat: Double, val dLng: Double, val active: Boolean)

@Serializable
data class ChatDto(val greeting: String, val fallback: String, val rules: List<ChatRuleDto>)

@Serializable
data class ChatRuleDto(val keywords: List<String>, val reply: String)

@Serializable
data class ActivityDto(
    val id: String,
    val type: String,
    val title: String,
    val xpReward: Int = 0,
    val isNew: Boolean = false,
    val needsActivation: Boolean = false,
)
