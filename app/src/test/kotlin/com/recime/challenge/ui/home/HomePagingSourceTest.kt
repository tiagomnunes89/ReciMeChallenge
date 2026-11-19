package com.recime.challenge.ui.home

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import com.recime.challenge.domain.model.DEFAULT_PAGE_SIZE
import com.recime.challenge.domain.model.RecipePage
import com.recime.challenge.domain.model.RecipeQuery
import com.recime.challenge.testing.FakeRecipeRepository
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class HomePagingSourceTest {

    private val repository = FakeRecipeRepository()

    private fun pagingSource(query: RecipeQuery = RecipeQuery()) =
        HomePagingSource(repository, query)

    private fun refreshParams(): LoadParams<String> =
        LoadParams.Refresh(key = null, loadSize = DEFAULT_PAGE_SIZE, placeholdersEnabled = false)

    private fun appendParams(key: String): LoadParams<String> =
        LoadParams.Append(key = key, loadSize = DEFAULT_PAGE_SIZE, placeholdersEnabled = false)

    @Test
    fun `refresh loads the first cursor and append advances the cursor`() = runTest {
        repository.recipes = previewRecipes
        val source = pagingSource()

        source.load(refreshParams())
        val secondPage = source.load(appendParams(key = "2")) as LoadResult.Page

        assertEquals(listOf(null, "2"), repository.pages.map { it.first })
        assertEquals(previewRecipes, secondPage.data)
    }

    @Test
    fun `the page size requested by paging is forwarded as the limit`() = runTest {
        pagingSource().load(
            LoadParams.Refresh(key = null, loadSize = 7, placeholdersEnabled = false),
        )

        assertEquals(listOf(null to 7), repository.pages)
    }

    @Test
    fun `the cursor returned by the repository becomes the next key and there is no previous key`() = runTest {
        repository.pagesByCursor = mapOf(null to RecipePage(previewRecipes, nextCursor = "cursor-2"))

        val actualPage = pagingSource().load(refreshParams()) as LoadResult.Page

        assertEquals("cursor-2", actualPage.nextKey)
        assertNull(actualPage.prevKey)
    }

    @Test
    fun `the last page has no next key`() = runTest {
        val actualPage = pagingSource().load(refreshParams()) as LoadResult.Page

        assertNull(actualPage.nextKey)
    }

    @Test
    fun `a repository failure becomes a paging error`() = runTest {
        val expectedFailure = IOException("offline")
        repository.searchFailure = expectedFailure

        val actualResult = pagingSource().load(refreshParams())

        assertSame(expectedFailure, (actualResult as LoadResult.Error).throwable)
    }

    @Test
    fun `a canceled call is propagated instead of becoming a paging error`() = runTest {
        repository.searchFailure = IOException("Canceled")

        val actualError: Throwable? = runCatching { pagingSource().load(refreshParams()) }.exceptionOrNull()

        assertTrue(actualError is CancellationException)
    }

    @Test
    fun `cancellation is propagated instead of becoming a paging error`() = runTest {
        repository.searchFailure = CancellationException("cancelled")

        val actualError = runCatching { pagingSource().load(refreshParams()) }.exceptionOrNull()

        assertTrue(actualError is CancellationException)
    }

    @Test
    fun `refresh always starts at the top`() {
        val state = PagingState(
            pages = listOf(
                LoadResult.Page(previewRecipes, prevKey = null, nextKey = "2"),
                LoadResult.Page(previewRecipes, prevKey = null, nextKey = "3"),
            ),
            anchorPosition = 2,
            config = PagingConfig(pageSize = DEFAULT_PAGE_SIZE),
            leadingPlaceholderCount = 0,
        )

        assertNull(pagingSource().getRefreshKey(state))
    }

    @Test
    fun `the query is forwarded to the repository`() = runTest {
        val inputQuery = RecipeQuery(text = "pasta")

        pagingSource(inputQuery).load(refreshParams())

        assertEquals(listOf(inputQuery), repository.queries)
    }
}
