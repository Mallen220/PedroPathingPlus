plugins {
    alias(libs.plugins.androidLibrary)
    id("maven-publish")
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

// Publish to mavenLocal so JitPack (and local users) can get the generated POM/JAR.
// Create a publication from the Android 'release' component. JitPack will run
// `./gradlew ... publishToMavenLocal` and expect a POM named <artifactId>-<version>.pom.
afterEvaluate {
    publishing {
        publications {
            create<org.gradle.api.publish.maven.MavenPublication>("maven") {
                artifactId = "PedroPathingPlus"
                // Use the project group and version as provided (JitPack sets -Pgroup/-Pversion when invoking)
                // Attach the AAR produced by the Android library module. The AAR is created by the "assembleRelease" task.
                val aarFile = file("$buildDir/outputs/aar/${project.name}-release.aar")
                artifact(aarFile) {
                    builtBy(tasks.named("assembleRelease"))
                }

                // Set minimal coordinates if not defined
                if (project.group.toString().isNotEmpty()) {
                    groupId = project.group.toString()
                }
                if (project.version.toString().isNotEmpty()) {
                    version = project.version.toString()
                }

                // Provide a simple POM customization (optional)
                pom {
                    name.set("${project.name}")
                    description.set("PedroPathingPlus Android library")
                    url.set("https://github.com/${project.findProperty("githubOwner") ?: "Mallen220"}/${project.findProperty("githubRepo") ?: "PedroPathingPlus"}")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("Mallen220")
                            name.set("Mallen220")
                        }
                    }
                }
            }
        }
        repositories {
            mavenLocal()
        }
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
