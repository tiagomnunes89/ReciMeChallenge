package com.recime.challenge.data.remote.mock

import com.recime.challenge.data.remote.model.RecipeDto
import org.junit.Assert.assertEquals
import org.junit.Test

class RecipeFilteringTest {

    private val pasta = recipe(
        id = "pasta",
        title = "Garlic Pasta",
        servings = 2,
        ingredients = listOf("spaghetti", "garlic"),
        instructions = listOf("Boil the pasta."),
        attributes = listOf("vegetarian"),
    )
    private val salad = recipe(
        id = "salad",
        title = "Chicken Salad",
        servings = 4,
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

    @Test
    fun `search index matches short substrings`() {
        val index = CatalogSearchIndex()

        val actual = index.search(
            recipes = all,
            text = "pa",
            vegetarian = null,
            servings = null,
            includeIngredients = null,
            excludeIngredients = null,
            instruction = null,
        )

        assertEquals(listOf("pasta"), actual.map(RecipeDto::id))
    }

    @Test
    fun `search index preserves filtering and catalog order for a large catalog`() {
        val index = CatalogSearchIndex()
        val catalog = (1..10_000).map { number ->
            recipe(
                id = "recipe-$number",
                title = "Recipe $number",
                servings = if (number % 2 == 0) 4 else 2,
                ingredients = listOf(if (number % 2 == 0) "garlic" else "onion"),
                instructions = listOf("Prepare recipe $number."),
            )
        }

        val actual = index.search(
            recipes = catalog,
            text = null,
            vegetarian = null,
            servings = 4,
            includeIngredients = listOf("garlic"),
            excludeIngredients = null,
            instruction = "prepare",
        )

        assertEquals(5_000, actual.size)
        assertEquals("recipe-2", actual.first().id)
        assertEquals("recipe-10000", actual.last().id)
    }

    @Test
    fun `search index rebuilds when mutable catalog changes at the same size`() {
        val index = CatalogSearchIndex()
        val catalog = mutableListOf(pasta, salad)

        val initial = index.search(
            recipes = catalog,
            text = "pasta",
            vegetarian = null,
            servings = null,
            includeIngredients = null,
            excludeIngredients = null,
            instruction = null,
        )
        catalog[0] = recipe(
            id = "pasta",
            title = "Garlic Bread",
            servings = 2,
            ingredients = listOf("bread", "garlic"),
            instructions = listOf("Toast the bread."),
            attributes = listOf("vegetarian"),
        )
        val updated = index.search(
            recipes = catalog,
            text = "pasta",
            vegetarian = null,
            servings = null,
            includeIngredients = null,
            excludeIngredients = null,
            instruction = null,
        )

        assertEquals(listOf("pasta"), initial.map(RecipeDto::id))
        assertEquals(emptyList<String>(), updated.map(RecipeDto::id))
    }

    private fun recipe(
        id: String,
        title: String,
        servings: Int,
        ingredients: List<String>,
        instructions: List<String>,
        attributes: List<String> = emptyList(),
    ) = RecipeDto(id, title, "$title description", servings, ingredients, instructions, attributes)
}
