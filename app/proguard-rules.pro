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

# ── Javalin ──
-keep class io.javalin.** { *; }
-dontwarn io.javalin.**

# ── Jetty (embedded server) ──
-keep class org.eclipse.jetty.** { *; }
-dontwarn org.eclipse.jetty.**
-keepclassmembers class org.eclipse.jetty.** {
    public <init>(...);
}

# ── SLF4J / Logging ──
-keep class org.slf4j.** { *; }
-dontwarn org.slf4j.**

# ── Jackson (JSON & XML) ──
-keep class com.fasterxml.jackson.** { *; }
-keepclassmembers class com.fasterxml.jackson.** {
    *;
}
-dontwarn com.fasterxml.jackson.**
# Keep your data classes used for serialization
-keep class omega.sunkey.sunkensonic.server.responses.** { *; }
-keep class omega.sunkey.sunkensonic.server.Response { *; }
-keep class omega.sunkey.sunkensonic.server.Root { *; }
-keep class omega.sunkey.sunkensonic.server.Error { *; }
-keep class omega.sunkey.sunkensonic.server.FailedResponse { *; }

# ── Room ──
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# ── StAX API (XML) ──
-keep class javax.xml.stream.** { *; }
-dontwarn javax.xml.stream.**

# ── Woodstox (XML Parser) ──
-keep class com.ctc.wstx.** { *; }
-dontwarn com.ctc.wstx.**
-keep class org.codehaus.stax2.** { *; }
-dontwarn org.codehaus.stax2.**
