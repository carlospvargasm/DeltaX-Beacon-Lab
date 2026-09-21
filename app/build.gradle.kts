plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android { namespace="com.deltax.beaconlab"; compileSdk=35
 defaultConfig { applicationId="com.deltax.estimotelab.cvm20260921"; minSdk=26; targetSdk=35; versionCode=5; versionName="2.0-estimote-diagnostic" }
 compileOptions { sourceCompatibility=JavaVersion.VERSION_17; targetCompatibility=JavaVersion.VERSION_17 }
}
kotlin { jvmToolchain(17) }