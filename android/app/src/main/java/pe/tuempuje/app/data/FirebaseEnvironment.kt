package pe.tuempuje.app.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import pe.tuempuje.app.BuildConfig

data class FirebaseServices(
    val auth: FirebaseAuth,
    val firestore: FirebaseFirestore,
    val isLocal: Boolean,
)

object FirebaseEnvironment {
    private var services: FirebaseServices? = null

    @Synchronized
    fun create(context: Context): FirebaseServices? {
        if (!BuildConfig.DEBUG) return null
        services?.let { return it }

        val isLocal = BuildConfig.FIREBASE_LOCAL
        val projectId = if (isLocal) "demo-tuempuje" else BuildConfig.FIREBASE_PROJECT_ID
        val applicationId = if (isLocal) "1:123456789:android:0000000000000000" else BuildConfig.FIREBASE_APPLICATION_ID
        val apiKey = if (isLocal) "demo-local-only" else BuildConfig.FIREBASE_API_KEY
        if (listOf(projectId, applicationId, apiKey).any { it.isBlank() }) return null

        val options = FirebaseOptions.Builder()
            .setProjectId(projectId)
            .setApplicationId(applicationId)
            .setApiKey(apiKey)
            .build()
        val appName = if (isLocal) "tuempuje-local" else "tuempuje-cloud"
        val app = FirebaseApp.getApps(context).firstOrNull { it.name == appName }
            ?: FirebaseApp.initializeApp(context, options, appName)
        val auth = FirebaseAuth.getInstance(app).apply {
            if (isLocal) useEmulator("10.0.2.2", 9099)
        }
        val firestore = FirebaseFirestore.getInstance(app).apply {
            if (isLocal) useEmulator("10.0.2.2", 8080)
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                .build()
        }
        return FirebaseServices(auth, firestore, isLocal).also { services = it }
    }
}
