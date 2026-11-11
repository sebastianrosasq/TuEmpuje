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
)

object FirebaseEnvironment {
    private var services: FirebaseServices? = null

    @Synchronized
    fun localDemo(context: Context): FirebaseServices? {
        if (!BuildConfig.DEBUG) return null
        services?.let { return it }

        val options = FirebaseOptions.Builder()
            .setProjectId("demo-tuempuje")
            .setApplicationId("1:123456789:android:0000000000000000")
            .setApiKey("demo-local-only")
            .build()
        val app = FirebaseApp.getApps(context).firstOrNull { it.name == "tuempuje-local" }
            ?: FirebaseApp.initializeApp(context, options, "tuempuje-local")
        val auth = FirebaseAuth.getInstance(app).apply { useEmulator("10.0.2.2", 9099) }
        val firestore = FirebaseFirestore.getInstance(app).apply {
            useEmulator("10.0.2.2", 8080)
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                .build()
        }
        return FirebaseServices(auth, firestore).also { services = it }
    }
}
