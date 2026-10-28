plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.o2dialer.app"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.o2dialer.app"
        minSdk = 26
        targetSdk = 36
        // Always rises with every GitHub run, so each build installs as an update over the previous one.
        versionCode = 100 + (System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 0)
        versionName = "7.0"
        // Address of your license server (https://...). Empty = licensing is OFF and the app works exactly as before.
        // Set it with the LICENSE_URL GitHub secret (the workflow passes it in) or -PLICENSE_URL=... locally.
        val licenseUrl = (System.getenv("LICENSE_URL") ?: (project.findProperty("LICENSE_URL") as String?) ?: "").trim()
        buildConfigField("String", "LICENSE_URL", "\"" + licenseUrl.replace("\\", "").replace("\"", "") + "\"")
    }
    // One fixed signing key for every build: Android only installs an APK as an update
    // when it is signed with the same key as the installed app.
    signingConfigs {
        create("o2dialer") {
            storeFile = file("o2dialer.keystore")
            storeType = "pkcs12"
            storePassword = "o2dialer-sign"
            keyAlias = "o2dialer"
            keyPassword = "o2dialer-sign"
        }
    }
    buildTypes {
        getByName("debug") { signingConfig = signingConfigs.getByName("o2dialer") }
        // The release build is far faster than debug: R8 optimises the code and Compose is not in debug mode.
        getByName("release") {
            signingConfig = signingConfigs.getByName("o2dialer")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.8" }
    lint {
        abortOnError = true
        checkReleaseBuilds = true
    }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}
dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20231013")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.core:core-ktx:1.12.0")
    // Installs the baseline profiles that ship inside Compose, so the app starts and scrolls fast from the first launch.
    implementation("androidx.profileinstaller:profileinstaller:1.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
