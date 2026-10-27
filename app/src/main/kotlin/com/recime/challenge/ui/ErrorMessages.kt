package com.recime.challenge.ui

import androidx.annotation.StringRes
import com.recime.challenge.R
import com.recime.challenge.domain.model.RecipeNotFoundException

@StringRes
internal fun Throwable.toUserMessageRes(): Int = when (this) {
    is RecipeNotFoundException -> R.string.common_error_recipe_unavailable
    else -> R.string.common_error_generic
}
