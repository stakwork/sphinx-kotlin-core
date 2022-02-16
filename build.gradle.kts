buildscript {
//    apply(from = rootProject.file("gradle/dependencies.gradle"))

    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10")
//        classpath("com.squareup.sqldelight:gradle-plugin:1.5.3")
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
    id("com.squareup.sqldelight")
}

sqldelight {
    database("CtxDatabase") {
        packageName = "chat.sphinx.concepts.coredb"
        schemaOutputDirectory = file("build/dbs")
    }
    linkSqlite = false
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
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
                api("com.benasher44:uuid:0.4.0")
                api("com.soywiz.korlibs.krypto:krypto:2.4.12")
                api("org.jetbrains.kotlinx:kotlinx-io:0.1.16")
                api("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")
                implementation("com.squareup.okio:okio:3.0.0")
                implementation("com.squareup.sqldelight:coroutines-extensions:1.5.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
            }
        }
        val commonTest by getting {
            dependencies {
                api(kotlin("test"))

                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
            }
        }
        val jvmMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-io-jvm:0.1.16")
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