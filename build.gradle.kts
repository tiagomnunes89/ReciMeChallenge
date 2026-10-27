import dev.detekt.gradle.extensions.DetektExtension

// Plugins are declared here (apply false) so every module shares one plugin classloader.
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
    apply(plugin = "dev.detekt")
    extensions.configure<DetektExtension> {
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        buildUponDefaultConfig.set(true)
        autoCorrect.set(false)
        parallel.set(true)
    }
}
