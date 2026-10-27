# Release shrinking rules for the app module.
# Hilt, Retrofit, OkHttp, kotlinx.serialization and Compose ship consumer rules,
# so only project-specific reflection needs to be kept here.

# MockRecipeApi decodes DTOs with kotlinx.serialization; keep generated serializers.
-keepclassmembers class com.recime.challenge.data.remote.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.recime.challenge.data.remote.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep line numbers for readable crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
