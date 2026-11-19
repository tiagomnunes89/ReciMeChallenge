package com.recime.challenge.ui.home

import com.recime.challenge.domain.model.RecipeQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeQueriesTest {

    private val inputFilters = MutableStateFlow(SearchFilters())

    private fun TestScope.collectQueries(): List<RecipeQuery> {
        val actualQueries = mutableListOf<RecipeQuery>()
        backgroundScope.launch { inputFilters.toDebouncedQueries().collect { actualQueries += it } }
        runCurrent()
        return actualQueries
    }

    @Test
    fun `the unfiltered query is emitted immediately`() = runTest {
        val actualQueries = collectQueries()

        assertEquals(listOf(RecipeQuery()), actualQueries)
    }

    @Test
    fun `text edits wait for the debounce window`() = runTest {
        val actualQueries = collectQueries()

        inputFilters.value = SearchFilters(searchText = "pasta")
        advanceTimeBy(SEARCH_DEBOUNCE_MS - 1)
        assertEquals(listOf(RecipeQuery()), actualQueries)
        advanceTimeBy(1)
        runCurrent()

        assertEquals(listOf(RecipeQuery(), RecipeQuery(text = "pasta")), actualQueries)
    }

    @Test
    fun `rapid typing only emits the latest text`() = runTest {
        val actualQueries = collectQueries()

        inputFilters.value = SearchFilters(searchText = "p")
        advanceTimeBy(SEARCH_DEBOUNCE_MS / 2)
        inputFilters.value = SearchFilters(searchText = "pa")
        advanceTimeBy(SEARCH_DEBOUNCE_MS)
        runCurrent()

        assertEquals(listOf(RecipeQuery(), RecipeQuery(text = "pa")), actualQueries)
    }

    @Test
    fun `the vegetarian tab is applied without waiting`() = runTest {
        val actualQueries = collectQueries()

        inputFilters.value = SearchFilters(vegetarianOnly = true)
        runCurrent()

        assertEquals(listOf(RecipeQuery(), RecipeQuery(vegetarianOnly = true)), actualQueries)
    }

    @Test
    fun `toggling the tab while text is pending applies both immediately`() = runTest {
        val actualQueries = collectQueries()
        inputFilters.value = SearchFilters(searchText = "pasta")
        advanceTimeBy(SEARCH_DEBOUNCE_MS)
        runCurrent()

        inputFilters.value = SearchFilters(searchText = "pasta", vegetarianOnly = true)
        runCurrent()

        assertEquals(RecipeQuery(text = "pasta", vegetarianOnly = true), actualQueries.last())
    }

    @Test
    fun `clearing every filter is applied without waiting`() = runTest {
        val actualQueries = collectQueries()
        inputFilters.value = SearchFilters(searchText = "pasta")
        advanceTimeBy(SEARCH_DEBOUNCE_MS)
        runCurrent()

        inputFilters.value = SearchFilters()
        runCurrent()

        assertEquals(RecipeQuery(), actualQueries.last())
    }
}
