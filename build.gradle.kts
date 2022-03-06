plugins {
    kotlin("multiplatform") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("com.squareup.sqldelight")
}

sqldelight {
    database("SphinxDatabase") {
        packageName = "chat.sphinx.concepts.coredb"
//        schemaOutputDirectory = file("build/dbs")
    }
    linkSqlite = false
}

group = "chat.sphinx"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

configurations {
    all {
        exclude(
            "io.matthewnelson.kotlin-components",
            "kmp-tor-macosx64"
        )
    }
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
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
        val okioVersion = "3.0.0"
        val klockVersion = "2.5.1"

        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
                api("com.benasher44:uuid:0.4.0")
                api("com.soywiz.korlibs.krypto:krypto:2.4.12")
                api("org.jetbrains.kotlinx:kotlinx-io:0.1.16")
                api("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")
                implementation("com.squareup.okio:okio:3.0.0")
                implementation("com.squareup.sqldelight:coroutines-extensions:1.5.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
                implementation("io.ktor:ktor-client-core:1.6.7")
                implementation("io.ktor:ktor-client-cio:1.6.7")
                implementation("com.squareup.okio:okio:$okioVersion")
                implementation("com.soywiz.korlibs.klock:klock:$klockVersion")
                implementation("io.matthewnelson.kotlin-components:kmp-tor:0.4.6.10+0.1.0-alpha4")
            }
        }
        val commonTest by getting {
            dependencies {
                api(kotlin("test"))

                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
                implementation("com.squareup.okio:okio-fakefilesystem:$okioVersion")
            }
        }
        val jvmMain by getting {
            val okHttpVersion = "4.9.3"

            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-io-jvm:0.1.16")
                api("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")
                implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
                implementation("com.squareup.okhttp3:logging-interceptor:$okHttpVersion")
                implementation("org.jsoup:jsoup:1.14.3")
                implementation("com.squareup.sqldelight:sqlite-driver:1.5.3")
                implementation("io.socket:socket.io-client:1.0.0")
                implementation("io.socket:engine.io-client:1.0.0")
                implementation("org.cryptonode.jncryptor:jncryptor:1.2.0")
            }
        }
        val jvmTest by getting
        val nativeMain by getting {
            dependencies {
                implementation("com.squareup.sqldelight:native-driver:1.5.3")
            }
        }
        val nativeTest by getting
    }
}

val projectDirGenRoot = "$buildDir/generated/projectdir/kotlin"

kotlin.sourceSets.named("commonTest") {
    this.kotlin.srcDir(projectDirGenRoot)
}