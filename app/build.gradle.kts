plugins {
    alias(libs.plugins.androidLibrary)
}

android {
    namespace = "com.pedropathingplus"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    compileOnly(libs.ftc.robotcore)
    compileOnly(libs.ftc.hardware)
    compileOnly(libs.ftc.robotserver)
    compileOnly(libs.ftc.common)

    implementation(libs.pedro.pathing)
    implementation(libs.solvers.core)
    implementation(libs.solvers.pedro)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
