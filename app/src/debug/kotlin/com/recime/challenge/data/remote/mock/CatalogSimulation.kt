package com.recime.challenge.data.remote.mock

import com.recime.challenge.data.remote.model.RecipeDto

internal fun List<RecipeDto>.simulateCatalog(size: Int): List<RecipeDto> {
    if (isEmpty() || size <= this.size) return this
    return List(size) { index ->
        val seed = this[index % this.size]
        val copy = index / this.size
        if (copy == 0) seed else seed.copy(id = "${seed.id}-$copy", title = "${seed.title} #${copy + 1}")
    }
}
