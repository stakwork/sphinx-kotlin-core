
rootProject.name = "sphinx-kotlin-core"

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
        maven(url = "https://jitpack.io")
        maven(url = "https://plugins.gradle.org/m2/")
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.squareup.sqldelight") {
                useModule("com.squareup.sqldelight:gradle-plugin:1.5.0")
            }
        }
    }
}
