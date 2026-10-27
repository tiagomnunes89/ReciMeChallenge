package com.recime.challenge.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeQueryTest {

    @Test
    fun `the default query is unfiltered`() = assertTrue(RecipeQuery().isUnfiltered)

    @Test
    fun `blank text is unfiltered`() =
        assertTrue(RecipeQuery(text = "  ", instructionContains = " ").isUnfiltered)

    @Test
    fun `every filter makes the query filtered`() {
        val filteredQueries = listOf(
            RecipeQuery(text = "pasta"),
            RecipeQuery(vegetarianOnly = true),
            RecipeQuery(servings = 2),
            RecipeQuery(includeIngredients = listOf("garlic")),
            RecipeQuery(excludeIngredients = listOf("meat")),
            RecipeQuery(instructionContains = "boil"),
        )

        filteredQueries.forEach { assertFalse(it.isUnfiltered) }
    }
}
