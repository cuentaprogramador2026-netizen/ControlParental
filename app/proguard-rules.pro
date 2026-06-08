# ─────────────────────────────────────────────────────────────
# ControlParental - ProGuard / R8 Rules
# Versión robusta para release en Google Play
# ─────────────────────────────────────────────────────────────

# --- AndroidX y Jetpack ---
-keep class androidx.** { *; }
-keep class * extends androidx.** { *; }
-dontwarn androidx.**

# --- Hilt / Dagger ---
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }
-keep class dagger.hilt.internal.** { *; }
-dontwarn dagger.hilt.**
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }
-keepclassmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModelMap <fields>;
}

# --- Compose ---
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-keepclassmembers class * {
    @androidx.compose.runtime.Immutable <fields>;
}

# --- Firebase ---
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.messaging.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
-keepattributes *Annotation*

# --- Firestore (mantener modelos de datos) ---
-keep class com.controlparental.app.domain.model.** { *; }
-keepclassmembers class com.controlparental.app.domain.model.** {
    <fields>;
    <init>(...);
}

# --- DataStore (Preferences + Proto) ---
-keep class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite { *; }
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# --- Corrutinas ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# --- Service, Receiver, Activity ---
-keep class * extends android.app.Service { *; }
-keep class * extends android.content.BroadcastReceiver { *; }
-keep class * extends android.app.Activity { *; }
-keep class * extends android.app.admin.DeviceAdminReceiver { *; }
-keep class * extends android.app.Application { *; }

# --- Mantener clases de nuestra app ---
-keep class com.controlparental.app.** { *; }
-keepclassmembers class com.controlparental.app.** {
    <fields>;
    <init>(...);
}

# --- WindowManager / System UI ---
-keep class android.view.WindowManager$LayoutParams { *; }
-keep class android.view.View { *; }

# --- UsageStats ---
-keep class android.app.usage.UsageEvents { *; }
-keep class android.app.usage.UsageEvents$Event { *; }
-keep class android.app.usage.UsageStatsManager { *; }

# --- Security / Crypto ---
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# --- OkHttp / Retrofit (si se agregan en el futuro) ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# --- Gson (si se agrega en el futuro) ---
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# --- Kotlin Reflection ---
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# --- Evitar stripping de Service notifications ---
-keepclassmembers class * {
    @android.annotation.SuppressLint <fields>;
}

# --- Serialización manual con org.json (nuestra app) ---
-keepclassmembers class * {
    @kotlin.jvm.JvmOverloads <methods>;
}

# --- Keep Parcelable ---
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# --- Keep enum methods ---
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- Keep R8 from stripping AndroidX annotations ---
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature, Exceptions

# --- Optimizaciones específicas para release ---
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
