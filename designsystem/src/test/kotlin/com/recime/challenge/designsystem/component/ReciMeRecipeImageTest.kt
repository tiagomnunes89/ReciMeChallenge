package com.recime.challenge.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReciMeRecipeImageTest {
    @Test
    fun `accepts absolute HTTPS image URLs`() {
        assertEquals(
            "https://images.example.com/meal.jpg",
            safeImageUrl("https://images.example.com/meal.jpg"),
        )
    }

    @Test
    fun `accepts trusted Android asset image URLs`() {
        assertEquals(
            "file:///android_asset/images/spaghetti.jpg",
            safeImageUrl("file:///android_asset/images/spaghetti.jpg"),
        )
    }

    @Test
    fun `rejects insecure or ambiguous image URLs`() {
        assertNull(safeImageUrl("http://images.example.com/meal.jpg"))
        assertNull(safeImageUrl("file:///tmp/meal.jpg"))
        assertNull(safeImageUrl("content://media/meal"))
        assertNull(safeImageUrl("/meal.jpg"))
        assertNull(safeImageUrl("https://"))
        assertNull(safeImageUrl("file:///android_asset/images/../secret.jpg"))
        assertNull(safeImageUrl(null))
    }
}
