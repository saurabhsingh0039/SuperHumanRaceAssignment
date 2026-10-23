package com.saurabharunima.superhumanraceassignment.data.repository

import com.saurabharunima.superhumanraceassignment.data.model.ChatMessage
import com.saurabharunima.superhumanraceassignment.data.model.TodayActivities
import com.saurabharunima.superhumanraceassignment.data.model.Friend
import com.saurabharunima.superhumanraceassignment.data.model.Leaderboard
import com.saurabharunima.superhumanraceassignment.data.model.Post
import com.saurabharunima.superhumanraceassignment.data.model.Profile
import com.saurabharunima.superhumanraceassignment.data.model.Report
import com.saurabharunima.superhumanraceassignment.data.model.ReportPeriod
import com.saurabharunima.superhumanraceassignment.data.model.SocialProvider
import com.saurabharunima.superhumanraceassignment.data.model.User
import kotlinx.coroutines.flow.StateFlow

/** Swap these implementations for Firebase later; ViewModels only see the interfaces. */
interface AuthRepository {
    /** Signed-in user id, or null. Drives the auth-gated nav graph. */
    val sessionUserId: StateFlow<String?>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun signUp(name: String, email: String, password: String): Result<User>
    /** Mock: every provider signs in as the default demo user. Replace with real OAuth later. */
    suspend fun loginWithProvider(provider: SocialProvider): Result<User>
    /** Re-establishes the saved session (if the user still exists). Never throws. */
    suspend fun restoreSession()
    suspend fun logout()
}

interface UserRepository {
    suspend fun getProfile(): Result<Profile>
    suspend fun updateName(name: String): Result<Profile>
}

interface FeedRepository {
    suspend fun getPosts(): Result<List<Post>>
    suspend fun toggleLike(postId: String): Result<Post>
    suspend fun addComment(postId: String, text: String): Result<Post>
}

interface LeaderboardRepository {
    suspend fun getLeaderboard(): Result<Leaderboard>
}

interface ReportRepository {
    suspend fun getReport(period: ReportPeriod): Result<Report>
}

interface FriendsRepository {
    /** Friends positioned relative to the given device location. */
    suspend fun getNearby(lat: Double, lng: Double): Result<List<Friend>>
}

interface ChatRepository {
    suspend fun greeting(): Result<ChatMessage>
    suspend fun reply(userText: String): Result<ChatMessage>
}

interface ActivityRepository {
    suspend fun getToday(): Result<TodayActivities>
    /** Unlocks an activity that needs activation (e.g. Wellness). */
    suspend fun activate(id: String): Result<TodayActivities>
    /** Awards XP and, on the first completion of the day, extends the streak. Idempotent per activity. */
    suspend fun complete(id: String): Result<TodayActivities>
}
