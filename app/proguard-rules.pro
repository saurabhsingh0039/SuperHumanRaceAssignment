# kotlinx.serialization: keep generated serializers and the mock-data DTOs
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **.*$Companion { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.saurabharunima.superhumanraceassignment.data.remote.**$$serializer { *; }
-keepclassmembers class com.saurabharunima.superhumanraceassignment.data.remote.** { *** Companion; }
-keep class com.saurabharunima.superhumanraceassignment.data.remote.*Dto { *; }

# osmdroid
-dontwarn org.osmdroid.**
-keep class org.osmdroid.** { *; }

# Stack traces readable in Play Console
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile