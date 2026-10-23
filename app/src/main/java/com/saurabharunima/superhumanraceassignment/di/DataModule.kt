package com.saurabharunima.superhumanraceassignment.di

import com.saurabharunima.superhumanraceassignment.core.DefaultDispatcherProvider
import com.saurabharunima.superhumanraceassignment.core.DispatcherProvider
import com.saurabharunima.superhumanraceassignment.data.local.DataStoreSessionStorage
import com.saurabharunima.superhumanraceassignment.data.local.DataStoreSettingsRepository
import com.saurabharunima.superhumanraceassignment.data.local.DeviceLocationProvider
import com.saurabharunima.superhumanraceassignment.data.local.LocationProvider
import com.saurabharunima.superhumanraceassignment.data.local.SessionStorage
import com.saurabharunima.superhumanraceassignment.data.remote.AssetMockDataSource
import com.saurabharunima.superhumanraceassignment.data.remote.MockDataSource
import com.saurabharunima.superhumanraceassignment.data.repository.ActivityRepository
import com.saurabharunima.superhumanraceassignment.data.repository.AuthRepository
import com.saurabharunima.superhumanraceassignment.data.repository.ChatRepository
import com.saurabharunima.superhumanraceassignment.data.repository.FeedRepository
import com.saurabharunima.superhumanraceassignment.data.repository.FriendsRepository
import com.saurabharunima.superhumanraceassignment.data.repository.LeaderboardRepository
import com.saurabharunima.superhumanraceassignment.data.repository.ReportRepository
import com.saurabharunima.superhumanraceassignment.data.repository.SettingsRepository
import com.saurabharunima.superhumanraceassignment.data.repository.UserRepository
import com.saurabharunima.superhumanraceassignment.data.repository.mock.MockActivityRepository
import com.saurabharunima.superhumanraceassignment.data.repository.mock.MockAuthRepository
import com.saurabharunima.superhumanraceassignment.data.repository.mock.MockChatRepository
import com.saurabharunima.superhumanraceassignment.data.repository.mock.MockFeedRepository
import com.saurabharunima.superhumanraceassignment.data.repository.mock.MockFriendsRepository
import com.saurabharunima.superhumanraceassignment.data.repository.mock.MockLeaderboardRepository
import com.saurabharunima.superhumanraceassignment.data.repository.mock.MockReportRepository
import com.saurabharunima.superhumanraceassignment.data.repository.mock.MockUserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindingsModule {
    @Binds abstract fun dispatchers(impl: DefaultDispatcherProvider): DispatcherProvider
    @Binds abstract fun dataSource(impl: AssetMockDataSource): MockDataSource
    @Binds abstract fun session(impl: DataStoreSessionStorage): SessionStorage
    @Binds abstract fun activities(impl: MockActivityRepository): ActivityRepository
    @Binds abstract fun location(impl: DeviceLocationProvider): LocationProvider
    @Binds abstract fun auth(impl: MockAuthRepository): AuthRepository
    @Binds abstract fun user(impl: MockUserRepository): UserRepository
    @Binds abstract fun feed(impl: MockFeedRepository): FeedRepository
    @Binds abstract fun leaderboard(impl: MockLeaderboardRepository): LeaderboardRepository
    @Binds abstract fun report(impl: MockReportRepository): ReportRepository
    @Binds abstract fun friends(impl: MockFriendsRepository): FriendsRepository
    @Binds abstract fun chat(impl: MockChatRepository): ChatRepository
    @Binds abstract fun settings(impl: DataStoreSettingsRepository): SettingsRepository
}

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {
    @Provides @Singleton fun clock(): Clock = Clock.systemDefaultZone()
}
