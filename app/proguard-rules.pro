# ============================================================
# kotlinx.serialization
# ============================================================
-keep class kotlinx.serialization.** { *; }
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Сохраняем сгенерированные сериализаторы для наших DTO
-keep,includedescriptorclasses class ru.internet.spygame.data.model.**$$serializer { *; }
-keepclassmembers class ru.internet.spygame.data.model.** {
    *** Companion;
}
-keepclasseswithmembers class ru.internet.spygame.data.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ============================================================
# Hilt / Dagger
# ============================================================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }

# ============================================================
# Room
# ============================================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ============================================================
# DataStore
# ============================================================
-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences { *; }

# ============================================================
# Общие правила
# ============================================================
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
