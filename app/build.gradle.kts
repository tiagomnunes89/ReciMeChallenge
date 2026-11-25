import java.net.URI

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kover)
}

val releaseKeystorePath: String? = providers.environmentVariable("RELEASE_KEYSTORE_PATH").orNull

val releaseApiBaseUrl: String? = providers.gradleProperty("RECIME_API_BASE_URL")
    .orElse(providers.environmentVariable("RECIME_API_BASE_URL"))
    .orNull

val mockApiBaseUrl: String = "https://mock.recime.invalid/"

val releaseApiBaseUrlForBuild: String = releaseApiBaseUrl
    ?.trim()
    ?.takeIf(String::isNotBlank)
    ?.let { if (it.endsWith("/")) it else "$it/" }
    .orEmpty()

android {
    namespace = "com.recime.challenge"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.recime.challenge"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = providers.environmentVariable("VERSION_CODE").map(String::toInt).getOrElse(1)
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        jniLibs {
            keepDebugSymbols += "**/libandroidx.graphics.path.so"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    signingConfigs {
        if (releaseKeystorePath != null) {
            create("release") {
                storeFile = file(releaseKeystorePath)
                storePassword = providers.environmentVariable("RELEASE_KEYSTORE_PASSWORD").get()
                keyAlias = providers.environmentVariable("RELEASE_KEY_ALIAS").get()
                keyPassword = providers.environmentVariable("RELEASE_KEY_PASSWORD").get()
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"$mockApiBaseUrl\"")
        }
        release {
            buildConfigField("String", "API_BASE_URL", "\"$releaseApiBaseUrlForBuild\"")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
    }

}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

val validateReleaseApiBaseUrl = tasks.register("validateReleaseApiBaseUrl") {
    description = "Validates the production API URL for release builds."
    val configuredBaseUrl: String? = releaseApiBaseUrl
    doLast {
        val configured = configuredBaseUrl?.trim().orEmpty()
        check(configured.isNotBlank()) {
            "Release builds need the production API URL: pass -PRECIME_API_BASE_URL=https://... " +
                "or set the RECIME_API_BASE_URL environment variable."
        }
        val uri = runCatching { URI(configured) }.getOrNull()
        check(
            uri != null &&
                uri.scheme?.equals("https", ignoreCase = true) == true &&
                !uri.host.isNullOrBlank() &&
                uri.userInfo == null &&
                uri.fragment == null,
        ) {
            "Release API URL must be an absolute HTTPS URL without credentials or fragments."
        }
    }
}

tasks.matching { it.name == "generateReleaseBuildConfig" }.configureEach {
    dependsOn(validateReleaseApiBaseUrl)
}

composeCompiler {
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose_stability.conf"))
    if (providers.gradleProperty("composeReports").isPresent) {
        reportsDestination.set(layout.buildDirectory.dir("compose-reports"))
        metricsDestination.set(layout.buildDirectory.dir("compose-metrics"))
    }
}

dependencies {
    implementation(projects.designsystem)
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)

    debugImplementation(libs.okhttp.logging)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.paging.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.compose.ui.test.junit4)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}

kover {
    reports {
        filters {
            includes {
                classes(
                    "com.recime.challenge.ui.home.HomeViewModel",
                    "com.recime.challenge.ui.favorites.FavoritesViewModel",
                    "com.recime.challenge.ui.detail.RecipeDetailViewModel",
                    "com.recime.challenge.ui.home.HomePagingSource",
                    "com.recime.challenge.ui.home.RecipeQueriesKt",
                    "com.recime.challenge.data.repository.DefaultRecipeRepository",
                    "com.recime.challenge.data.local.RecipeLocalDataSource",
                )
            }
        }
        verify {
            rule {
                minBound(80)
            }
        }
    }
}
