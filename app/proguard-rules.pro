# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Room entities
-keep class com.p3.recibop3.data.entity.** { *; }

# Keep signature for Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}
