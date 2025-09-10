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
