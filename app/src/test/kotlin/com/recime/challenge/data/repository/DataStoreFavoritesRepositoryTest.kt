package com.recime.challenge.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreFavoritesRepositoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun TestScope.repository(file: File = tmp.newFile("favorites.preferences_pb").also { it.delete() }) =
        DataStoreFavoritesRepository(
            PreferenceDataStoreFactory.create(
                scope = backgroundScope,
                produceFile = { file },
            ),
        )

    @Test
    fun `starts with no favorites`() = runTest(UnconfinedTestDispatcher()) {
        assertEquals(emptySet<String>(), repository().observeFavoriteIds().first())
    }

    @Test
    fun `toggle adds and then removes a favorite`() = runTest(UnconfinedTestDispatcher()) {
        val repository = repository()

        repository.toggleFavorite("1")
        repository.toggleFavorite("2")
        assertEquals(setOf("1", "2"), repository.observeFavoriteIds().first())

        repository.toggleFavorite("1")
        assertEquals(setOf("2"), repository.observeFavoriteIds().first())
    }
}
