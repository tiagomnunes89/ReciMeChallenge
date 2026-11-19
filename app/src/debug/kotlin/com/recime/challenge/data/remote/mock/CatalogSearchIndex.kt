package com.recime.challenge.data.remote.mock

import com.recime.challenge.data.remote.model.RecipeDto
import java.util.Locale

internal class CatalogSearchIndex {

    private var indexedRecipes: List<RecipeDto> = emptyList()
    private var indexedCatalogIdentity: Int = 0
    private var indexedCatalogSize: Int = -1
    private var servingIndex: Map<Int, Set<Int>> = emptyMap()
    private var vegetarianIndex: Set<Int> = emptySet()
    private var textIndex: Map<String, Set<Int>> = emptyMap()
    private var ingredientIndex: Map<String, Set<Int>> = emptyMap()
    private var instructionIndex: Map<String, Set<Int>> = emptyMap()

    @Synchronized
    fun search(
        recipes: List<RecipeDto>,
        text: String?,
        vegetarian: Boolean?,
        servings: Int?,
        includeIngredients: List<String>?,
        excludeIngredients: List<String>?,
        instruction: String?,
    ): List<RecipeDto> {
        rebuildIfNeeded(recipes)

        val candidates = mutableSetOf<Int>()
        candidates += indexedRecipes.indices

        vegetarian.takeIf { it == true }?.let {
            candidates.retainAll(vegetarianIndex)
        }
        servings?.let { serving ->
            candidates.retainAll(servingIndex[serving].orEmpty())
        }

        text?.normalizeForSearch()?.let { query ->
            if (query.length >= TRIGRAM_MIN_LENGTH) {
                candidates.retainAll(combinedPosting(textIndex, query))
            }
        }

        instruction?.normalizeForSearch()?.let { query ->
            if (query.length >= TRIGRAM_MIN_LENGTH) {
                candidates.retainAll(postingCandidates(instructionIndex, query))
            }
        }

        includeIngredients.orEmpty()
            .mapNotNull { it.normalizeForSearch() }
            .forEach { ingredient ->
                if (ingredient.length >= TRIGRAM_MIN_LENGTH) {
                    candidates.retainAll(postingCandidates(ingredientIndex, ingredient))
                }
            }

        return candidates
            .sorted()
            .map { indexedRecipes[it] }
            .filter { recipe ->
                recipe.matches(
                    text = text,
                    vegetarian = vegetarian,
                    servings = servings,
                    includeIngredients = includeIngredients,
                    excludeIngredients = excludeIngredients,
                    instruction = instruction,
                )
            }
    }

    private fun rebuildIfNeeded(recipes: List<RecipeDto>) {
        val catalogIdentity = System.identityHashCode(recipes)
        val unchangedImmutableCatalog =
            indexedCatalogIdentity == catalogIdentity &&
                indexedCatalogSize == recipes.size &&
                recipes !is MutableList
        val unchangedMutableCatalog =
            indexedCatalogIdentity == catalogIdentity &&
                indexedCatalogSize == recipes.size &&
                indexedRecipes == recipes
        if (unchangedImmutableCatalog || unchangedMutableCatalog) return

        indexedRecipes = recipes.toList()
        indexedCatalogIdentity = catalogIdentity
        indexedCatalogSize = recipes.size
        val servings = mutableMapOf<Int, MutableSet<Int>>()
        val vegetarian = mutableSetOf<Int>()
        val textPostings = mutableMapOf<String, MutableSet<Int>>()
        val ingredientPostings = mutableMapOf<String, MutableSet<Int>>()
        val instructionPostings = mutableMapOf<String, MutableSet<Int>>()

        indexedRecipes.forEachIndexed { index, recipe ->
            servings.getOrPut(recipe.servings) { mutableSetOf() }.add(index)
            if (recipe.dietaryAttributes.any { it.equals(VEGETARIAN, ignoreCase = true) }) {
                vegetarian.add(index)
            }
            recipe.title.normalizeForSearch()?.let { normalized ->
                addTrigrams(textPostings, normalized, index)
            }
            recipe.description.normalizeForSearch()?.let { normalized ->
                addTrigrams(textPostings, normalized, index)
            }
            recipe.ingredients.forEach { ingredient ->
                ingredient.normalizeForSearch()?.let { normalized ->
                    addTrigrams(ingredientPostings, normalized, index)
                }
            }
            recipe.instructions.forEach { step ->
                step.normalizeForSearch()?.let { normalized ->
                    addTrigrams(instructionPostings, normalized, index)
                }
            }
        }

        servingIndex = servings
        vegetarianIndex = vegetarian
        textIndex = textPostings
        ingredientIndex = ingredientPostings
        instructionIndex = instructionPostings
    }

    private fun combinedPosting(
        postingIndex: Map<String, Set<Int>>,
        query: String,
    ): Set<Int> = postingCandidates(postingIndex, query)

    private fun postingCandidates(
        postingIndex: Map<String, Set<Int>>,
        query: String,
    ): Set<Int> {
        val normalized = query.normalizeForSearch() ?: return emptySet()
        val trigrams = normalized.toTrigrams()
        if (trigrams.isEmpty()) return emptySet()

        var candidates: Set<Int>? = null
        for (trigram in trigrams) {
            val posting = postingIndex[trigram].orEmpty()
            candidates = if (candidates == null) posting else candidates.intersect(posting)
            if (candidates.isEmpty()) break
        }
        return candidates.orEmpty()
    }

    private fun addTrigrams(
        postings: MutableMap<String, MutableSet<Int>>,
        normalizedValue: String,
        recipeIndex: Int,
    ) {
        normalizedValue.toTrigrams().forEach { trigram ->
            postings.getOrPut(trigram) { mutableSetOf() }.add(recipeIndex)
        }
    }

    private fun RecipeDto.matches(
        text: String?,
        vegetarian: Boolean?,
        servings: Int?,
        includeIngredients: List<String>?,
        excludeIngredients: List<String>?,
        instruction: String?,
    ): Boolean =
        listOf(this).filterBy(
            text = text,
            vegetarian = vegetarian,
            servings = servings,
            includeIngredients = includeIngredients,
            excludeIngredients = excludeIngredients,
            instruction = instruction,
        ).isNotEmpty()

    private companion object {
        const val VEGETARIAN = "vegetarian"
    }
}

private const val TRIGRAM_MIN_LENGTH = 3

private fun String.normalizeForSearch(): String? =
    trim().lowercase(Locale.ROOT).takeIf(String::isNotBlank)

private fun String.toTrigrams(): Set<String> =
    if (length < TRIGRAM_MIN_LENGTH) {
        emptySet()
    } else {
        (0..length - TRIGRAM_MIN_LENGTH)
            .map { index -> substring(index, index + TRIGRAM_MIN_LENGTH) }
            .toSet()
    }
