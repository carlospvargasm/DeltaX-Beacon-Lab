plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

android {
    namespace = "com.deltax.beaconlab"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.deltax.beaconlab.receiver2026"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "0.3"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
kotlin { jvmToolchain(17) }
