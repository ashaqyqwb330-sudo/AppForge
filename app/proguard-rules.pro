# AppForge ProGuard Rules
-keepattributes *Annotation*

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# SQLite
-keep class org.sqlite.** { *; }
-keep class com.appforge.domain.model.** { *; }

# Keep custom views
-keep class com.appforge.ui.components.** { *; }
