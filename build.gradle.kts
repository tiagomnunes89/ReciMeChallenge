import io.gitlab.arturbosch.detekt.extensions.DetektExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kover) apply false
}

subprojects {
    pluginManager.apply("io.gitlab.arturbosch.detekt")
    extensions.configure<DetektExtension> {
        config.setFrom(rootProject.layout.projectDirectory.file("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        autoCorrect = false
        parallel = true
        source.from("src/debug/kotlin", "src/testDebug/kotlin")
    }
}
