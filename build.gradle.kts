plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.6.10"
    id("com.squareup.sqldelight")
}

sqldelight {
    database("SphinxDatabase") {
        packageName = "chat.sphinx.database.core"
        sourceFolders = listOf("coredb")
//        schemaOutputDirectory = file("build/dbs")
    }
    database("SphinxSettingsDatabase") {
        packageName = "chat.sphinx.database.settings"
        sourceFolders = listOf("settingsdb")
//        schemaOutputDirectory = file("build/dbs")
    }
    linkSqlite = false
}

group = "chat.sphinx"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

//configurations {
//    all {
//        exclude(
//            "io.matthewnelson.kotlin-components",
//            "kmp-tor-macosx64"
//        )
//    }
//}

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
//    val hostOs = System.getProperty("os.name")
//    val isMingwX64 = hostOs.startsWith("Windows")
//    val nativeTarget = when {
//        hostOs == "Mac OS X" -> macosX64("native")
//        hostOs == "Linux" -> linuxX64("native")
//        isMingwX64 -> mingwX64("native")
//        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
//    }

    
    sourceSets {
        val kotlinVersion = "1.5.1"
        val okioVersion = "3.0.0"
        val klockVersion = "2.5.1"
        val sqlDelightVersion = "1.5.1"
        val kmpTorVersion = "0.4.6.10+0.1.0-beta1"

        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
                api("com.benasher44:uuid:0.4.0")
                api("com.soywiz.korlibs.krypto:krypto:2.4.12")
                api("org.jetbrains.kotlinx:kotlinx-io:0.1.16")
                api("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")
                api("com.squareup.sqldelight:coroutines-extensions:$sqlDelightVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
                implementation("com.squareup.okio:okio:3.0.0")
                implementation("io.ktor:ktor-client-core:1.6.7")
                implementation("io.ktor:ktor-client-cio:1.6.7")
                implementation("com.squareup.okio:okio:$okioVersion")
                implementation("com.soywiz.korlibs.klock:klock:$klockVersion")
                api("io.matthewnelson.kotlin-components:kmp-tor:$kmpTorVersion")

                implementation("com.russhwolf:multiplatform-settings:0.8.1")

//                implementation(project(":kmp-tor:library:kmp-tor"))
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
            val netlayerVersion = "0.7.2"

            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-io-jvm:0.1.16")
                api("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")
                api("com.squareup.sqldelight:sqlite-driver:$sqlDelightVersion")
                implementation("io.socket:socket.io-client:1.0.0")
                implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
                implementation("com.squareup.okhttp3:logging-interceptor:$okHttpVersion")
                implementation("org.jsoup:jsoup:1.14.3")
                implementation("io.socket:engine.io-client:1.0.0")
                implementation("org.cryptonode.jncryptor:jncryptor:1.2.0")
                implementation("com.github.bisq-network.netlayer:tor.external:$netlayerVersion")
                implementation("com.github.bisq-network.netlayer:tor.native:$netlayerVersion")
            }
        }
        val jvmTest by getting
//        val nativeMain by getting {
//            dependencies {
//                api("com.squareup.sqldelight:native-driver:$sqlDelightVersion")
//                implementation("io.matthewnelson.kotlin-components:kmp-tor-manager-jvm:$kmpTorVersion")
//            }
//        }
//        val nativeTest by getting
    }
}

val projectDirGenRoot = "$buildDir/generated/projectdir/kotlin"

kotlin.sourceSets.named("commonTest") {
    this.kotlin.srcDir(projectDirGenRoot)
}