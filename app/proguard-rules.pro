# Add any ProGuard configurations here
-keep class com.sakib.devinfo.** { *; }
-keepclassmembers class ** {
    @android.webkit.JavascriptInterface <methods>;
}

# Preserve line numbers for debugging
-keepattributes LineNumberTable,SourceFile
-renamesourcefileattribute SourceFile

# Android Architecture Components
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }

# Kotlin
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# Material Design
-keep class com.google.android.material.** { *; }

# System info related classes
-keep class android.os.** { *; }
-keep class java.io.** { *; }

# Coroutines (avoid stripping debug metadata used in stack traces)
-keepclassmembers class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-dontwarn kotlinx.coroutines.**

# If using reflection on sensor classes (generally not needed, but safe keep)
-keep class android.hardware.Sensor { *; }
-keep class android.hardware.SensorManager { *; }

# Keep annotation attributes (useful if future libraries depend on them)
-keepattributes *Annotation*

# Guidance: Remove overly broad keeps (like entire package) once stable to improve shrinking.
