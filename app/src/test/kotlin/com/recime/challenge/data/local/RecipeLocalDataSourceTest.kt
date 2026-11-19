package com.recime.challenge.data.local

import com.recime.challenge.testing.createInMemoryDatabase
import com.recime.challenge.testing.recipeEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [35])
@RunWith(RobolectricTestRunner::class)
class RecipeLocalDataSourceTest {

    private val database: ReciMeDatabase = createInMemoryDatabase()
    private val dataSource = RecipeLocalDataSource(database)

    @After
    fun tearDown() = database.close()

    @Test
    fun `a saved recipe can be read by id`() = runTest {
        dataSource.upsert(recipeEntity("a"))

        assertEquals(recipeEntity("a"), dataSource.findById("a"))
    }

    @Test
    fun `saveAll upserts every recipe`() = runTest {
        dataSource.upsertAll(listOf(recipeEntity("a"), recipeEntity("b")))

        assertEquals(recipeEntity("a"), dataSource.findById("a"))
        assertEquals(recipeEntity("b"), dataSource.findById("b"))
    }

    @Test
    fun `findById returns null when the recipe is missing`() = runTest {
        assertNull(dataSource.findById("missing"))
    }

    @Test
    fun `findAll returns recipes ordered by title`() = runTest {
        dataSource.upsertAll(listOf(recipeEntity("b").copy(title = "B"), recipeEntity("a").copy(title = "A")))

        assertEquals(listOf("a", "b"), dataSource.findAll().map { it.id })
    }

    @Test
    fun `saveAll preserves snapshots that are absent from a later response`() = runTest {
        dataSource.upsertAll(listOf(recipeEntity("a"), recipeEntity("b"), recipeEntity("c")))

        dataSource.upsertAll(listOf(recipeEntity("a"), recipeEntity("c")))

        assertEquals(listOf("a", "b", "c"), dataSource.findAll().map { it.id })
        assertEquals(recipeEntity("b"), dataSource.findById("b"))
    }
}
