package com.recime.challenge.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

private const val PACKAGE_NAME = "com.recime.challenge"
private const val ITERATIONS = 10

class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldStartNone() = benchmark(CompilationMode.None())

    @Test
    fun coldStartPartial() = benchmark(CompilationMode.Partial())

    private fun benchmark(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(StartupTimingMetric()),
            iterations = ITERATIONS,
            startupMode = StartupMode.COLD,
            compilationMode = compilationMode,
            setupBlock = { pressHome() },
        ) { startActivityAndWait() }
    }
}

class ScrollBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollNone() = benchmark(CompilationMode.None())

    @Test
    fun scrollPartial() = benchmark(CompilationMode.Partial())

    private fun benchmark(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            iterations = ITERATIONS,
            compilationMode = compilationMode,
            setupBlock = {
                pressHome()
                startActivityAndWait()
                device.wait(Until.hasObject(By.res(PACKAGE_NAME, "recipe_grid")), 5_000)
                device.wait(Until.hasObject(By.textContains("Serves")), 5_000)
            },
        ) {
            device.swipe(500, 1200, 500, 300, 20)
            device.swipe(500, 300, 500, 1200, 20)
            device.swipe(500, 1200, 500, 300, 20)
            device.swipe(500, 300, 500, 1200, 20)
        }
    }
}
