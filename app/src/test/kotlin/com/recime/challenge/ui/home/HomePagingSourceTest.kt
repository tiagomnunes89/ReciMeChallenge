package com.recime.challenge.ui.home

import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import com.recime.challenge.domain.model.RecipeQuery
import com.recime.challenge.testing.FakeRecipeRepository
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class HomePagingSourceTest {

    private val repository = FakeRecipeRepository()

    private fun pagingSource(query: RecipeQuery = RecipeQuery()) =
        HomePagingSource(repository, query)

    @Test
    fun `refresh loads the first cursor and append advances the cursor`() = runTest {
        repository.recipes = previewRecipes
        val source = pagingSource()

        val firstPage = source.load(
            androidx.paging.PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false,
            ),
        ) as LoadResult.Page
        val secondPage = source.load(
            androidx.paging.PagingSource.LoadParams.Append(
                key = "2",
                loadSize = 20,
                placeholdersEnabled = false,
            ),
        ) as LoadResult.Page

        assertNull(repository.pages[0].first)
        assertEquals("2", repository.pages[1].first)
        assertNull(firstPage.prevKey)
        assertNull(firstPage.nextKey)
        assertEquals(listOf(null, "2"), repository.pages.map { it.first })
        assertEquals(previewRecipes, secondPage.data)
    }

    @Test
    fun `a repository failure becomes a paging error`() = runTest {
        val expectedFailure = IOException("offline")
        repository.searchFailure = expectedFailure

        val result = pagingSource().load(
            androidx.paging.PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false,
            ),
        )

        assertSame(expectedFailure, (result as LoadResult.Error).throwable)
    }

    @Test
    fun `refresh always starts at the top`() {
        val source = pagingSource()
        val state = PagingState(
            pages = listOf(
                LoadResult.Page(previewRecipes, prevKey = null, nextKey = "2"),
                LoadResult.Page(previewRecipes, prevKey = null, nextKey = "3"),
            ),
            anchorPosition = 2,
            config = androidx.paging.PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0,
        )

        assertNull(source.getRefreshKey(state))
    }

    @Test
    fun `the query is forwarded to the repository`() = runTest {
        val query = RecipeQuery(text = "pasta")

        pagingSource(query).load(
            androidx.paging.PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false,
            ),
        )

        assertEquals(listOf(query), repository.queries)
    }
}
