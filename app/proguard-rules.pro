
-keepclassmembers class com.recime.challenge.data.remote.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.recime.challenge.data.remote.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
