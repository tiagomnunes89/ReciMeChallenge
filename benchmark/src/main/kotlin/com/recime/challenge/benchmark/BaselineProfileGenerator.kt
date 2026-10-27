package com.recime.challenge.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() {
        baselineProfileRule.collect(
            packageName = "com.recime.challenge",
            includeInStartupProfile = true,
        ) {
            pressHome()
            startActivityAndWait()
            device.wait(Until.hasObject(By.text("Spaghetti Aglio e Olio")), 5_000)
            device.swipe(500, 1200, 500, 300, 20)
            device.swipe(500, 300, 500, 1200, 20)
            device.findObject(By.text("Spaghetti Aglio e Olio"))?.click()
        }
    }
}
