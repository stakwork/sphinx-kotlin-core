
rootProject.name = "sphinx-kotlin-core"

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        google()
        gradlePluginPortal()
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.squareup.sqldelight") {
                useModule("com.squareup.sqldelight:gradle-plugin:1.5.3")
            }
        }
    }
}
