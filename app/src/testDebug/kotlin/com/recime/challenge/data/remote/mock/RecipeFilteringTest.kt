package com.recime.challenge.data.remote.mock

import com.recime.challenge.data.remote.model.RecipeDto
import org.junit.Assert.assertEquals
import org.junit.Test

class RecipeFilteringTest {

    private val pasta = recipe(
        id = "pasta", title = "Garlic Pasta", servings = 2,
        ingredients = listOf("spaghetti", "garlic"),
        instructions = listOf("Boil the pasta."),
        attributes = listOf("vegetarian"),
    )
    private val salad = recipe(
        id = "salad", title = "Chicken Salad", servings = 4,
        ingredients = listOf("chicken breast", "lettuce"),
        instructions = listOf("Grill the chicken."),
    )
    private val all = listOf(pasta, salad)

    private fun filter(
        text: String? = null,
        vegetarian: Boolean? = null,
        servings: Int? = null,
        include: List<String>? = null,
        exclude: List<String>? = null,
        instruction: String? = null,
    ) = all.filterBy(text, vegetarian, servings, include, exclude, instruction).map { it.id }

    @Test
    fun `no filters returns everything`() = assertEquals(listOf("pasta", "salad"), filter())

    @Test
    fun `text matches title ignoring case`() = assertEquals(listOf("salad"), filter(text = "CHICKEN"))

    @Test
    fun `vegetarian filter keeps only vegetarian recipes`() =
        assertEquals(listOf("pasta"), filter(vegetarian = true))

    @Test
    fun `vegetarian false does not filter`() =
        assertEquals(listOf("pasta", "salad"), filter(vegetarian = false))

    @Test
    fun `servings filter is exact`() = assertEquals(listOf("salad"), filter(servings = 4))

    @Test
    fun `included ingredients must all be present`() {
        assertEquals(listOf("pasta"), filter(include = listOf("garlic", "spaghetti")))
        assertEquals(emptyList<String>(), filter(include = listOf("garlic", "lettuce")))
    }

    @Test
    fun `excluded ingredients remove matching recipes`() =
        assertEquals(listOf("pasta"), filter(exclude = listOf("chicken")))

    @Test
    fun `instruction text is searched in the steps`() =
        assertEquals(listOf("salad"), filter(instruction = "grill"))

    @Test
    fun `filters are combined with AND`() =
        assertEquals(emptyList<String>(), filter(vegetarian = true, servings = 4))

    private fun recipe(
        id: String,
        title: String,
        servings: Int,
        ingredients: List<String>,
        instructions: List<String>,
        attributes: List<String> = emptyList(),
    ) = RecipeDto(id, title, "$title description", servings, ingredients, instructions, attributes)
}
