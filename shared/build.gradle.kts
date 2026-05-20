import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinSerialization)

    id("com.vanniktech.maven.publish") version "0.30.0"
    id("signing")
}

group = "io.github.iam-venkateshwarlu"
version = "1.0.0"

kotlin {

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->

        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {

        commonMain.dependencies {

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)

            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.koin.core)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.kotlinx.datetime)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.android)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {

    namespace = "com.example.cryptoapp.shared"

    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

mavenPublishing {

    publishToMavenCentral(
        SonatypeHost.CENTRAL_PORTAL
    )

    signAllPublications()

    pom {

        name.set("CryptoApp")

        description.set(
            "Kotlin Multiplatform crypto library"
        )

        inceptionYear.set("2026")

        url.set(
            "https://github.com/iam-venkateshwarlu/my-kmp-library"
        )

        licenses {

            license {

                name.set(
                    "The Apache License, Version 2.0"
                )

                url.set(
                    "https://www.apache.org/licenses/LICENSE-2.0.txt"
                )
            }
        }

        developers {

            developer {

                id.set("iam-venkateshwarlu")

                name.set("Venkateshwarlu")

                url.set(
                    "https://github.com/iam-venkateshwarlu"
                )
            }
        }

        scm {

            url.set(
                "https://github.com/iam-venkateshwarlu/my-kmp-library"
            )

            connection.set(
                "scm:git:git://github.com/iam-venkateshwarlu/my-kmp-library.git"
            )

            developerConnection.set(
                "scm:git:ssh://git@github.com/iam-venkateshwarlu/my-kmp-library.git"
            )
        }
    }
}

signing {
    useInMemoryPgpKeys(
        providers.gradleProperty("signingKey").orNull,
        providers.gradleProperty("signingPassword").orNull
    )
}