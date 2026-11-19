package com.recime.challenge.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.recime.challenge.R
import com.recime.challenge.designsystem.theme.ReciMeTheme
import com.recime.challenge.domain.model.Recipe
import java.io.IOException
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.ContinuationInterceptor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@Config(sdk = [35], qualifiers = "w411dp-h1600dp")
@RunWith(RobolectricTestRunner::class)
class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun string(id: Int) = RuntimeEnvironment.getApplication().getString(id)

    // Robolectric drops the async message AndroidUiDispatcher uses to clear its scheduled flag
    // after the search field takes focus, so a later Paging collection never calls load.
    @Before
    fun resetUiDispatcher() {
        val dispatcher = AndroidUiDispatcher.Main[ContinuationInterceptor] ?: return
        listOf("scheduledTrampolineDispatch", "scheduledFrameDispatch").forEach { fieldName ->
            val field = dispatcher.javaClass.getDeclaredField(fieldName)
            field.isAccessible = true
            field.setBoolean(dispatcher, false)
        }
    }

    private fun setScreen(
        recipes: @Composable () -> LazyPagingItems<Recipe> = { rememberPreviewPagingItems() },
        filters: SearchFilters = SearchFilters(),
        onFiltersChange: (SearchFilters) -> Unit = {},
        onRecipeClick: (String) -> Unit = {},
    ) {
        composeRule.setContent {
            ReciMeTheme {
                HomeScreen(
                    filters = filters,
                    recipes = recipes(),
                    onFiltersChange = onFiltersChange,
                    onRecipeClick = onRecipeClick,
                )
            }
        }
    }

    @Test
    fun `success lists the recipes`() {
        setScreen()

        composeRule.onNodeWithText("Spaghetti Aglio e Olio").assertIsDisplayed()
        composeRule.onNodeWithText("Chicken Caesar Salad").assertIsDisplayed()
    }

    @Test
    fun `empty result shows the not found state`() {
        setScreen(recipes = { rememberPreviewPagingItems(recipes = emptyList()) })

        composeRule.onNodeWithText(string(R.string.home_empty_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.home_empty_message)).assertIsDisplayed()
    }

    @Test
    fun `refresh failure shows an error and retry loads the recipes`() {
        setScreen(recipes = { rememberFlakyPagingItems() })

        composeRule.onNodeWithText(string(R.string.common_error_offline)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.common_try_again)).performClick()

        composeRule.onNodeWithText("Spaghetti Aglio e Olio").assertIsDisplayed()
    }

    @Test
    fun `append failure keeps the list and offers a retry`() {
        setScreen(
            recipes = {
                rememberPreviewPagingItems(append = LoadState.Error(IOException("page 2 failed")))
            },
        )

        composeRule.onNodeWithText("Spaghetti Aglio e Olio").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.common_error_generic)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.common_try_again)).assertIsDisplayed()
    }

    @Test
    fun `refresh failure keeps the loaded recipes and offers a retry`() {
        lateinit var items: LazyPagingItems<Recipe>
        composeRule.setContent {
            ReciMeTheme {
                items = rememberRefreshFailureItems()
                HomeScreen(
                    filters = SearchFilters(),
                    recipes = items,
                    onFiltersChange = {},
                    onRecipeClick = {},
                )
            }
        }

        composeRule.onNodeWithText("Spaghetti Aglio e Olio").assertIsDisplayed()
        items.refresh()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Spaghetti Aglio e Olio").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.common_error_offline)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.common_try_again)).assertIsDisplayed()
    }

    @Test
    fun `clicking a recipe reports its id`() {
        var clicked: String? = null
        setScreen(onRecipeClick = { clicked = it })

        composeRule.onNodeWithText("Chicken Caesar Salad").performClick()

        assertEquals("2", clicked)
    }

    @Test
    fun `typing in the search bar updates the text filter`() {
        var filters: SearchFilters? = null
        setScreen(onFiltersChange = { filters = it })

        composeRule.onNode(hasSetTextAction()).performTextInput("sushi")

        assertEquals(SearchFilters(searchText = "sushi"), filters)
    }

    @Test
    fun `vegetarian tab enables the vegetarian filter`() {
        var filters: SearchFilters? = null
        setScreen(onFiltersChange = { filters = it })

        composeRule.onNodeWithText(string(R.string.home_tab_vegetarian)).performClick()

        assertTrue(filters?.vegetarianOnly == true)
    }

    @Test
    fun `see more reveals the advanced filters and see less hides them`() {
        setScreen()

        composeRule.onNodeWithText(string(R.string.home_filter_servings)).assertDoesNotExist()
        composeRule.onNodeWithText(string(R.string.home_filters_more)).performClick()

        composeRule.onNodeWithText(string(R.string.home_filter_servings)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.home_filter_include)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.home_filter_exclude)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.home_filter_instructions)).assertIsDisplayed()

        composeRule.onNodeWithText(string(R.string.home_filters_less)).performClick()

        composeRule.onNodeWithText(string(R.string.home_filter_servings)).assertDoesNotExist()
    }

    @Test
    fun `vegetarian filter never hides loaded recipes on the client`() {
        setScreen(filters = SearchFilters(vegetarianOnly = true))

        composeRule.onNodeWithText("Spaghetti Aglio e Olio").assertIsDisplayed()
        composeRule.onNodeWithText("Chicken Caesar Salad").assertIsDisplayed()
    }

    @Composable
    private fun rememberRefreshFailureItems(): LazyPagingItems<Recipe> {
        val flow = remember {
            val hasLoaded = AtomicBoolean(false)
            Pager(PagingConfig(pageSize = 20)) { RefreshFailurePagingSource(hasLoaded) }.flow
        }
        return flow.collectAsLazyPagingItems()
    }

    private class RefreshFailurePagingSource(
        private val hasLoaded: AtomicBoolean,
    ) : PagingSource<Int, Recipe>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Recipe> {
            if (!hasLoaded.compareAndSet(false, true)) return LoadResult.Error(UnknownHostException())
            return LoadResult.Page(previewRecipes, prevKey = null, nextKey = null)
        }

        override fun getRefreshKey(state: PagingState<Int, Recipe>): Int? = null
    }

    @Composable
    private fun rememberFlakyPagingItems(): LazyPagingItems<Recipe> {
        val flow = remember {
            val source = FlakyPagingSource()
            Pager(PagingConfig(pageSize = 20)) { source }.flow
        }
        return flow.collectAsLazyPagingItems()
    }

    private class FlakyPagingSource : PagingSource<Int, Recipe>() {
        private var hasFailed = false

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Recipe> {
            if (hasFailed) return LoadResult.Page(previewRecipes, prevKey = null, nextKey = null)
            hasFailed = true
            return LoadResult.Error(UnknownHostException())
        }

        override fun getRefreshKey(state: PagingState<Int, Recipe>): Int? = null
    }
}
