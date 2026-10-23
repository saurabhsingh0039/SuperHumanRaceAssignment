package com.saurabharunima.superhumanraceassignment.data.model

import java.time.LocalDate

data class User(val id: String, val name: String, val email: String, val avatarUrl: String?)

/** Single source for header chips and Profile cards. */
data class UserStats(val xp: Int, val seeds: Int, val gems: Int, val streakDays: Int)

enum class SocialProvider(val label: String) { APPLE("Apple"), GOOGLE("Google"), FACEBOOK("Facebook") }

data class Profile(val user: User, val stats: UserStats)

data class Comment(
    val id: String,
    val userId: String,
    val userName: String,
    val text: String,
    val timestamp: Long,
)

data class Post(
    val id: String,
    val userId: String,
    val userName: String,
    val text: String,
    val co2SavedKg: Double,
    val timestamp: Long,
    val likeCount: Int,
    val likedByMe: Boolean,
    val comments: List<Comment>,
)

data class League(val name: String, val advanceTop: Int, val endsAtMillis: Long)

data class LeaderEntry(
    val rank: Int,
    val userId: String,
    val name: String,
    val avatarUrl: String?,
    val xp: Int,
    val gems: Int,
    val isCurrentUser: Boolean,
)

data class Leaderboard(val league: League, val entries: List<LeaderEntry>)

enum class TransportMode(val label: String) {
    WALK("Walk"), CYCLE("Cycle"), BUS("Bus"), TRAIN("Train"), CAR("Car")
}

data class DailyLog(val date: LocalDate, val mode: TransportMode, val km: Double)

enum class ReportPeriod(val days: Int) { WEEKLY(7), MONTHLY(30) }

data class DailyCo2(val date: LocalDate, val savedKg: Double, val emittedKg: Double)

data class ModeUsage(val mode: TransportMode, val km: Double, val share: Double)

data class Report(
    val period: ReportPeriod,
    val totalSavedKg: Double,
    val totalEmittedKg: Double,
    val daily: List<DailyCo2>,
    val modeUsage: List<ModeUsage>,
)

data class Friend(
    val userId: String,
    val name: String,
    val avatarUrl: String?,
    val lat: Double,
    val lng: Double,
    val active: Boolean,
)

data class ChatMessage(val id: String, val text: String, val fromUser: Boolean, val timestamp: Long)

enum class ActivityType { STORIES, QUIZ, MOBILITY, WELLNESS, ROUTINE, INVITE }

data class Activity(
    val id: String,
    val type: ActivityType,
    val title: String,
    val xpReward: Int,
    val isNew: Boolean,
    val needsActivation: Boolean,
    val completed: Boolean,
)

data class TodayActivities(val activities: List<Activity>, val completedToday: Boolean)
