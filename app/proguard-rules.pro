# Room Database generated implementations & entities
-keep class * extends androidx.room.RoomDatabase
-keep class com.rayven0421.ashnotes.data.local.** { *; }
-dontwarn androidx.room.paging.**

# Dagger Hilt ViewModels & Dependency Injection
-keepclassmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# Navigation routes & screens
-keep class com.rayven0421.ashnotes.navigation.** { *; }

# Kotlinx Serialization (Keep serializable classes, polymorphic serializers & discriminator names)
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * implements kotlinx.serialization.KSerializer {
    public static ** INSTANCE;
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class * {
    *** Companion;
}
-keepclassmembers class * {
    *** serializer(...);
}
-keep class * implements kotlinx.serialization.KSerializer
-keepclassmembers class * {
    synthetic static kotlinx.serialization.KSerializer[] get$childSerializers(...);
}

# Domain models & Core Common
-keep class com.rayven0421.ashnotes.domain.model.** { *; }
-keep class com.rayven0421.ashnotes.core.common.** { *; }

# AndroidX Credentials & Google Identity (Credential Manager / Sign-in)
-keep class androidx.credentials.CredentialManager { *; }
-keep class androidx.credentials.GetCredentialRequest { *; }
-keep class androidx.credentials.GetCredentialResponse { *; }
-keep class androidx.credentials.CustomCredential { *; }
-keep class androidx.credentials.Credential { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
