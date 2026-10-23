package com.saurabharunima.superhumanraceassignment.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Persists the signed-in user id across app restarts. Fake this in unit tests. */
interface SessionStorage {
    val userId: Flow<String?>
    suspend fun save(userId: String)
    suspend fun clear()
}

private val Context.sessionDataStore by preferencesDataStore(name = "session")

@Singleton
class DataStoreSessionStorage @Inject constructor(
    @ApplicationContext context: Context,
) : SessionStorage {
    private val store = context.sessionDataStore
    private val key = stringPreferencesKey("user_id")

    override val userId: Flow<String?> = store.data.map { it[key] }
    override suspend fun save(userId: String) { store.edit { it[key] = userId } }
    override suspend fun clear() { store.edit { it.remove(key) } }
}
