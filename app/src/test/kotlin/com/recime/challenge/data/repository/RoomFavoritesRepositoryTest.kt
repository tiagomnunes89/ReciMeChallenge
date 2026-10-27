package com.recime.challenge.data.repository

import com.recime.challenge.data.local.ReciMeDatabase
import com.recime.challenge.domain.model.DietaryAttribute
import com.recime.challenge.testing.createInMemoryDatabase
import com.recime.challenge.testing.recipeEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [35])
@RunWith(RobolectricTestRunner::class)
class RoomFavoritesRepositoryTest {

    private val database: ReciMeDatabase = createInMemoryDatabase()
    private var now: Long = 0L
    private val repository = RoomFavoritesRepository(database) { ++now }

    @Before
    fun setUp() = runTest {
        database.recipeDao().upsertAll(listOf(recipeEntity("1"), recipeEntity("2"), recipeEntity("3")))
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `starts with no favorites`() = runTest {
        assertEquals(emptySet<String>(), repository.observeFavoriteIds().first())
        assertEquals(emptyList<String>(), repository.observeFavoriteRecipes().first().map { it.id })
    }

    @Test
    fun `toggle adds and then removes a favorite`() = runTest {
        repository.toggleFavorite("1")
        repository.toggleFavorite("2")
        assertEquals(setOf("1", "2"), repository.observeFavoriteIds().first())

        repository.toggleFavorite("1")
        assertEquals(setOf("2"), repository.observeFavoriteIds().first())
    }

    @Test
    fun `favorite recipes come from the cache, most recent first`() = runTest {
        repository.toggleFavorite("2")
        repository.toggleFavorite("1")

        val actualRecipes = repository.observeFavoriteRecipes().first()

        assertEquals(listOf("1", "2"), actualRecipes.map { it.id })
        assertEquals("Recipe 1", actualRecipes.first().title)
        assertEquals(setOf(DietaryAttribute.VEGETARIAN), actualRecipes.first().dietaryAttributes)
    }

    @Test
    fun `refreshing a cached recipe keeps it favorited and updates its data`() = runTest {
        repository.toggleFavorite("1")

        database.recipeDao().upsert(recipeEntity("1", title = "Renamed"))

        assertEquals(setOf("1"), repository.observeFavoriteIds().first())
        assertEquals(listOf("Renamed"), repository.observeFavoriteRecipes().first().map { it.title })
    }

    @Test
    fun `a recipe that is not cached cannot be favorited`() = runTest {
        val actualFailure = runCatching { repository.toggleFavorite("missing") }.exceptionOrNull()

        assertTrue(actualFailure != null)
        assertEquals(emptySet<String>(), repository.observeFavoriteIds().first())
    }
}
