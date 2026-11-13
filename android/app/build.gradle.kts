import java.util.Properties
import groovy.json.JsonSlurper

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

val backend = providers.gradleProperty("tuempujeBackend").orElse("local").get()
require(backend == "local" || backend == "cloud") {
    "tuempujeBackend debe ser local o cloud"
}
val firebaseProperties = Properties().apply {
    val config = rootProject.file("firebase.local.properties")
    if (backend == "cloud" && config.isFile) config.inputStream().use { load(it) }
}
fun firebaseValue(name: String): String = firebaseProperties.getProperty(name)?.trim().orEmpty()
fun quoted(value: String): String = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
val googleWebClientId = if (backend == "cloud") runCatching {
    val config = JsonSlurper().parse(rootProject.file("app/google-services.json")) as Map<*, *>
    val info = config["project_info"] as Map<*, *>
    require(info["project_id"] == firebaseValue("projectId"))
    val clients = config["client"] as List<*>
    val client = clients.map { it as Map<*, *> }.first {
        val clientInfo = it["client_info"] as Map<*, *>
        val androidInfo = clientInfo["android_client_info"] as Map<*, *>
        androidInfo["package_name"] == "pe.tuempuje.app"
    }
    (client["oauth_client"] as? List<*>)?.map { it as Map<*, *> }
        ?.firstOrNull { (it["client_type"] as? Number)?.toInt() == 3 }
        ?.get("client_id") as? String ?: ""
}.getOrDefault("") else ""
if (backend == "cloud") {
    require(listOf("projectId", "applicationId", "apiKey").all { firebaseValue(it).isNotEmpty() }) {
        "Para el entorno cloud configura projectId, applicationId y apiKey en android/firebase.local.properties"
    }
}

android {
    namespace = "pe.tuempuje.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "pe.tuempuje.app"
        manifestPlaceholders["authHost"] = (if (backend == "local") "demo-tuempuje" else firebaseValue("projectId")) + ".firebaseapp.com"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("boolean", "FIREBASE_LOCAL", (backend == "local").toString())
        buildConfigField("String", "FIREBASE_PROJECT_ID", quoted(firebaseValue("projectId")))
        buildConfigField("String", "FIREBASE_APPLICATION_ID", quoted(firebaseValue("applicationId")))
        buildConfigField("String", "FIREBASE_API_KEY", quoted(firebaseValue("apiKey")))
        buildConfigField("String", "FIREBASE_WEB_CLIENT_ID", quoted(googleWebClientId))
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    bundle {
        language { enableSplit = false }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.5.0-alpha18")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    implementation("androidx.navigation:navigation-compose:2.9.8")
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")
    implementation("app.rive:rive-android:11.16.3")
    implementation("androidx.credentials:credentials:1.6.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.6.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation(platform("com.google.firebase:firebase-bom:34.10.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-functions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
