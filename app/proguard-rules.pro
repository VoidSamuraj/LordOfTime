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

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#-libraryjars libs
-keepattributes InnerClasses,Signature

# Sdk
-keep public interface com.zendesk.sdk.** { *; }
-keep public class com.zendesk.sdk.** { *; }


# Appcompat and support

-keep interface android.support.v7.** { *; }
-keep class android.support.v7.** { *; }
-keep interface android.support.v4.** { *; }
-keep class android.support.v4.** { *; }


-keep class com.voidsamurai.lordoftime.bd.*{*;}
-keep class com.crashlytics.** { *; }
-keep class com.voidsamurai.lordoftime.AuthActivity
-dontwarn com.crashlytics.**
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature
-keep class com.crashlytics.android.**
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**
