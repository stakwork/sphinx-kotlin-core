buildscript {
//    apply(from = rootProject.file("gradle/dependencies.gradle"))

    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10")
//        classpath(KAplugin.androidGradle)
//        classpath KAplugin.google.hilt
//        classpath KAplugin.gradleVersions
//        classpath KAplugin.kotlin.gradle
//        classpath KAplugin.square.exhaustive

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

plugins {
    kotlin("multiplatform") version "1.5.10"
}

group = "chat.sphinx"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    
    sourceSets {
        val kotlinVersion = "1.5.1"
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
                implementation("com.benasher44:uuid:0.4.0")
                implementation("com.soywiz.korlibs.krypto:krypto:2.2.0")
                implementation("org.jetbrains.kotlinx:kotlinx-io:0.1.16")
                implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-io-jvm:0.1.16")
            }
        }
        val jvmTest by getting
        val nativeMain by getting
        val nativeTest by getting
    }
}

val projectDirGenRoot = "$buildDir/generated/projectdir/kotlin"

kotlin.sourceSets.named("commonTest") {
    this.kotlin.srcDir(projectDirGenRoot)
}