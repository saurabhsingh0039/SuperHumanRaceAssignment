package com.saurabharunima.superhumanraceassignment.data.remote

import android.content.Context
import com.saurabharunima.superhumanraceassignment.core.DispatcherProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/** Seam for tests: swap in a fake that returns a hand-built MockDataDto. */
interface MockDataSource {
    suspend fun load(): MockDataDto
}

@Singleton
class AssetMockDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider,
) : MockDataSource {

    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private var cached: MockDataDto? = null

    override suspend fun load(): MockDataDto = mutex.withLock {
        cached ?: withContext(dispatchers.io) {
            context.assets.open(FILE_NAME).bufferedReader().use {
                json.decodeFromString<MockDataDto>(it.readText())
            }
        }.also { cached = it }
    }

    private companion object {
        const val FILE_NAME = "mock_data.json"
    }
}
