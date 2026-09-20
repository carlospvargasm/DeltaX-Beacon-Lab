plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

android {
    namespace = "com.deltax.beaconlab"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.deltax.beaconlab.v2"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "0.2"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
kotlin { jvmToolchain(17) }
