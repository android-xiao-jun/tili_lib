-keep class com.example.record.analytics.init.PageTrackInitProvider { *; }
-keep class com.example.record.analytics.PageTracker { *; }
-keep class com.example.record.analytics.api.PageTrackUploader { *; }
-keep class com.example.record.analytics.api.PageTrackBridge { *; }

-keepclassmembers class * implements com.example.record.analytics.api.PageTrackUploader {
    public <methods>;
}

-keep class com.example.record.analytics.db.PageStayRecord { *; }
-keep class kotlin.Metadata { *; }
-keepattributes Signature,*Annotation*