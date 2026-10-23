package com.saurabharunima.superhumanraceassignment.ui.chat

import com.saurabharunima.superhumanraceassignment.core.UiState
import com.saurabharunima.superhumanraceassignment.data.model.ChatMessage
import com.saurabharunima.superhumanraceassignment.testing.FakeChatRepository
import com.saurabharunima.superhumanraceassignment.testing.MainDispatcherRule
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private val clock = Clock.fixed(Instant.ofEpochMilli(1_000_000L), ZoneOffset.UTC)
    private lateinit var repo: FakeChatRepository

    @Before fun setUp() { repo = FakeChatRepository() }

    private fun viewModel() = ChatViewModel(repo, clock)

    @Test fun `greeting loads and markdown is stripped`() = runTest(mainRule.dispatcher) {
        repo.greetingResult = Result.success(ChatMessage("g", "**Hi** there", fromUser = false, timestamp = 0L))
        val vm = viewModel()
        assertTrue(vm.state.value.start is UiState.Loading)
        advanceUntilIdle()
        assertTrue(vm.state.value.start is UiState.Success)
        assertEquals("Hi there", vm.state.value.messages.single().text)
    }

    @Test fun `greeting failure shows error and retry recovers`() = runTest(mainRule.dispatcher) {
        repo.greetingResult = Result.failure(Exception("boom"))
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(UiState.Error("boom"), vm.state.value.start)

        repo.greetingResult = Result.success(ChatMessage("g", "Hi", fromUser = false, timestamp = 0L))
        vm.retryStart()
        advanceUntilIdle()
        assertTrue(vm.state.value.start is UiState.Success)
    }

    @Test fun `blank input is ignored`() = runTest(mainRule.dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onDraftChange("   ")
        vm.onSend()
        advanceUntilIdle()
        assertEquals(1, vm.state.value.messages.size)
        assertTrue(repo.replyCalls.isEmpty())
    }

    @Test fun `send shows typing, then the reply, and clears the draft`() = runTest(mainRule.dispatcher) {
        repo.replyDelayMs = 700
        val vm = viewModel()
        advanceUntilIdle()

        vm.onDraftChange("hello")
        vm.onSend()
        runCurrent()
        assertTrue(vm.state.value.typing)
        assertEquals("", vm.state.value.draft)
        assertEquals(2, vm.state.value.messages.size)
        assertTrue(vm.state.value.messages.last().fromUser)

        advanceUntilIdle()
        assertFalse(vm.state.value.typing)
        assertEquals("echo: hello", vm.state.value.messages.last().text)
        assertFalse(vm.state.value.messages.last().fromUser)
    }

    @Test fun `a second send while typing is ignored`() = runTest(mainRule.dispatcher) {
        repo.replyDelayMs = 700
        val vm = viewModel()
        advanceUntilIdle()

        vm.onDraftChange("a")
        vm.onSend()
        runCurrent()
        vm.onSuggestion("b")
        advanceUntilIdle()

        assertEquals(listOf("a"), repo.replyCalls)
    }

    @Test fun `refresh during a pending reply drops the late reply`() = runTest(mainRule.dispatcher) {
        repo.replyDelayMs = 700
        val vm = viewModel()
        advanceUntilIdle()

        vm.onDraftChange("a")
        vm.onSend()
        runCurrent()
        vm.refresh()
        advanceUntilIdle()

        assertFalse(vm.state.value.typing)
        assertEquals("Hi", vm.state.value.messages.single().text)
    }

    @Test fun `reply failure sets an error and retry resends without duplicating the question`() = runTest(mainRule.dispatcher) {
        repo.replyOverride = Result.failure(Exception("net"))
        val vm = viewModel()
        advanceUntilIdle()

        vm.onDraftChange("hello")
        vm.onSend()
        advanceUntilIdle()
        assertEquals("net", vm.state.value.replyError)
        assertFalse(vm.state.value.typing)

        repo.replyOverride = null
        vm.retryReply()
        advanceUntilIdle()
        assertNull(vm.state.value.replyError)
        assertEquals(3, vm.state.value.messages.size) // greeting, question, reply
        assertEquals("echo: hello", vm.state.value.messages.last().text)
    }
}
