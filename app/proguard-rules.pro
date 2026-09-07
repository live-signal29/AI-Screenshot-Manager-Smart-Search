# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# Room Database rules
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keepclassmembers class * {
    @androidx.room.Dao *;
}

# Keep Entities
-keep class com.example.data.local.** { *; }

# Google Play Services & AdMob
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# ML Kit Text Recognition
-keep class com.google.mlkit.vision.** { *; }
-dontwarn com.google.mlkit.vision.**

