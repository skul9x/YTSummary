# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep all models used for network requests
-keep class com.skul9x.ytsummary.model.** { *; }
-keep class com.skul9x.ytsummary.api.** { *; }
-keep class com.skul9x.ytsummary.transcript.model.** { *; }

# Keep Kotlinx Serialization behavior
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKd
-keep,allowobfuscation,allowshrinking class kotlinx.serialization.internal.**
-keepclassmembers class kotlinx.serialization.internal.** {
    *** Companion;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <init>(...);
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# General Keep rules
-keep class kotlin.Metadata { *; }
-keepattributes Signature
-keepattributes Exceptions

# Room database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep class * {
    @androidx.room.Dao <methods>;
    @androidx.room.Insert <methods>;
    @androidx.room.Query <methods>;
    @androidx.room.Update <methods>;
    @androidx.room.Delete <methods>;
}

# OkHttp/Retrofit basics if used
-keep class okhttp3.** { *; }
-keep class retrofit2.** { *; }
-keep class java.lang.invoke.MethodHandles
-keep class java.lang.invoke.MethodHandles$Lookup

-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn com.google.crypto.tink.**
-dontwarn com.google.api.client.**
-dontwarn org.joda.time.**

# Advanced R8/Optimization rules
-repackageclasses ''
-allowaccessmodification
-overloadaggressively

# Optimization level
-optimizationpasses 5

# Assume no side effects for Log methods in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}
