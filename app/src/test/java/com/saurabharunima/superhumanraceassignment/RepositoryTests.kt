package com.saurabharunima.superhumanraceassignment.data

import com.saurabharunima.superhumanraceassignment.core.EmailAlreadyUsedException
import com.saurabharunima.superhumanraceassignment.core.InvalidCredentialsException
import com.saurabharunima.superhumanraceassignment.data.repository.mock.MockAuthRepository
import com.saurabharunima.superhumanraceassignment.data.repository.mock.MockChatRepository
import com.saurabharunima.superhumanraceassignment.data.repository.mock.UserStore
import com.saurabharunima.superhumanraceassignment.testing.FakeMockDataSource
import com.saurabharunima.superhumanraceassignment.testing.FakeSessionStorage
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MockAuthRepositoryTest {
    private val storage = FakeSessionStorage()
    private val repo = MockAuthRepository(UserStore(FakeMockDataSource()), storage)

    @Test fun `valid login starts and persists a session`() = runTest {
        val result = repo.login("ava@example.com", "password123")
        assertTrue(result.isSuccess)
        assertEquals("u1", repo.sessionUserId.value)
        assertEquals("u1", storage.current)
    }

    @Test fun `login email is case-insensitive`() = runTest {
        assertTrue(repo.login("AVA@Example.com", "password123").isSuccess)
    }

    @Test fun `wrong password fails and leaves no session`() = runTest {
        val result = repo.login("ava@example.com", "nope")
        assertTrue(result.exceptionOrNull() is InvalidCredentialsException)
        assertNull(repo.sessionUserId.value)
        assertNull(storage.current)
    }

    @Test fun `sign up creates a trimmed user, signs in, and rejects a duplicate email`() = runTest {
        val created = repo.signUp(" New User ", "new@example.com", "password123")
        assertEquals("New User", created.getOrThrow().name)
        assertTrue(repo.sessionUserId.value!!.startsWith("u_"))

        val duplicate = repo.signUp("Other", "NEW@example.com", "password123")
        assertTrue(duplicate.exceptionOrNull() is EmailAlreadyUsedException)
    }

    @Test fun `restoreSession restores a known user and clears an unknown one`() = runTest {
        storage.save("u2")
        repo.restoreSession()
        assertEquals("u2", repo.sessionUserId.value)

        val ghostStorage = FakeSessionStorage("ghost")
        val ghostRepo = MockAuthRepository(UserStore(FakeMockDataSource()), ghostStorage)
        ghostRepo.restoreSession()
        assertNull(ghostRepo.sessionUserId.value)
        assertNull(ghostStorage.current)
    }

    @Test fun `logout clears memory and storage`() = runTest {
        repo.login("ava@example.com", "password123")
        repo.logout()
        assertNull(repo.sessionUserId.value)
        assertNull(storage.current)
    }
}

class MockChatRepositoryTest {
    private val clock = Clock.fixed(Instant.ofEpochMilli(5_000L), ZoneOffset.UTC)
    private val repo = MockChatRepository(FakeMockDataSource(), clock)

    @Test fun `greeting comes from the mock data as a bot message`() = runTest {
        val message = repo.greeting().getOrThrow()
        assertEquals("Hello from Ora", message.text)
        assertFalse(message.fromUser)
        assertEquals(5_000L, message.timestamp)
    }

    @Test fun `reply follows keyword rules`() = runTest {
        assertEquals("CO2 answer", repo.reply("what is my carbon footprint").getOrThrow().text)
        assertEquals("Transport answer", repo.reply("transport tips").getOrThrow().text)
    }

    @Test fun `unmatched input gets the fallback with a unique id`() = runTest {
        val a = repo.reply("zzz").getOrThrow()
        val b = repo.reply("zzz").getOrThrow()
        assertEquals("I didn't get that", a.text)
        assertNotNull(a.id)
        assertTrue(a.id != b.id)
    }
}
