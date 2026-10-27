package com.recime.challenge.ui.home

sealed interface HomeEvent {

    data class FiltersChanged(val filters: SearchFilters) : HomeEvent
}
