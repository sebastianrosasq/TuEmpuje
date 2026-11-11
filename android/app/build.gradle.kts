import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
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
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("boolean", "FIREBASE_LOCAL", (backend == "local").toString())
        buildConfigField("String", "FIREBASE_PROJECT_ID", quoted(firebaseValue("projectId")))
        buildConfigField("String", "FIREBASE_APPLICATION_ID", quoted(firebaseValue("applicationId")))
        buildConfigField("String", "FIREBASE_API_KEY", quoted(firebaseValue("apiKey")))
    }

    buildFeatures {
        compose = true
        buildConfig = true
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
    implementation(platform("com.google.firebase:firebase-bom:34.10.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
