# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /opt/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}


# this is for ormlite, from https://github.com/campnic/ormlite-android-extras/blob/master/proguard.cfg

-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# OrmLite uses reflection
-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep enum com.j256.**
-keepclassmembers enum com.j256.** { *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }

-keepattributes *Annotation*

-keepclassmembers class * {
  public <init>(android.content.Context);
}

-keep class co.yishun.onemoment.app.data.**
-keepclassmembers class co.yishun.onemoment.app.data.** { *; }

-keep public class * extends com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper

# for others
-dontwarn org.androidannotations.api.rest.*
-dontwarn com.squareup.picasso.*
-dontwarn java.lang.invoke.*
-dontwarn net.orfjackal.retrolambda.**
-dontwarn com.squareup.okhttp.**
-dontwarn okio.**


# for reflect in Convert2
-keepnames class com.github.hiteshsondhi88.libffmpeg.FFmpegLoadLibraryAsyncTask
-keepclassmembers class com.github.hiteshsondhi88.libffmpeg.FFmpegLoadLibraryAsyncTask {
   <init>(...);
}

# for crash when login
-keepattributes Signature

#-keep class co.yishun.onemoment.app.net.**
#-keepclassmembers class co.yishun.onemoment.app.net.** { *; }
#-keep interface co.yishun.onemoment.app.net.**
#-keepclassmembers interface co.yishun.onemoment.app.net.** { *; }

# from gson http://google-gson.googlecode.com/svn/trunk/examples/android-proguard-example/proguard.cfg

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class co.yishun.onemoment.app.net.result.** { *; }
-keep class co.yishun.onemoment.app.net.request.sync.** { *; }

##---------------End: proguard configuration for Gson  ----------