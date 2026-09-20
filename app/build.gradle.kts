plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

android {
    namespace = "com.deltax.beaconlab"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.deltax.quickshare.cvm20260920"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "1.0-quickshare"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
kotlin { jvmToolchain(17) }
