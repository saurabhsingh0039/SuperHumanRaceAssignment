package com.saurabharunima.superhumanraceassignment.ui.settings

import com.saurabharunima.superhumanraceassignment.core.UiState
import com.saurabharunima.superhumanraceassignment.testing.FakeUserRepository
import com.saurabharunima.superhumanraceassignment.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    @Test fun `load fills the name draft`() = runTest(mainRule.dispatcher) {
        val vm = SettingsViewModel(FakeUserRepository())
        advanceUntilIdle()
        assertTrue(vm.state.value.profile is UiState.Success)
        assertEquals("Ava Green", vm.state.value.nameDraft)
    }

    @Test fun `load failure exposes an error`() = runTest(mainRule.dispatcher) {
        val vm = SettingsViewModel(FakeUserRepository(Result.failure(Exception("offline"))))
        advanceUntilIdle()
        assertEquals(UiState.Error("offline"), vm.state.value.profile)
    }

    @Test fun `a too-short name is rejected without calling the repository`() = runTest(mainRule.dispatcher) {
        val repo = FakeUserRepository()
        val vm = SettingsViewModel(repo)
        advanceUntilIdle()
        vm.onNameChange("A")
        vm.saveName()
        advanceUntilIdle()
        assertNotNull(vm.state.value.nameError)
        assertTrue(repo.updateCalls.isEmpty())
    }

    @Test fun `saving a valid name trims it and confirms`() = runTest(mainRule.dispatcher) {
        val repo = FakeUserRepository()
        val vm = SettingsViewModel(repo)
        advanceUntilIdle()
        vm.onNameChange("  Ava G ")
        vm.saveName()
        advanceUntilIdle()
        assertEquals(listOf("Ava G"), repo.updateCalls)
        assertEquals("Name updated", vm.state.value.message)
        assertEquals("Ava G", vm.state.value.nameDraft)
        assertFalse(vm.state.value.saving)
        vm.messageShown()
        assertNull(vm.state.value.message)
    }

    @Test fun `a failed save reports the error and stops saving`() = runTest(mainRule.dispatcher) {
        val repo = FakeUserRepository().apply { updateFailure = Exception("server down") }
        val vm = SettingsViewModel(repo)
        advanceUntilIdle()
        vm.onNameChange("Ava G")
        vm.saveName()
        advanceUntilIdle()
        assertEquals("server down", vm.state.value.message)
        assertFalse(vm.state.value.saving)
    }
}
