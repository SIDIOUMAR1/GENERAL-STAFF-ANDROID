pluginManagement {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven { url = uri("https://jitpack.io") }
        maven {
            url =uri("https://maven.google.com")
        }

        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven { url = uri("https://jitpack.io") }
        maven {
            url =uri("https://maven.google.com")
        }

        gradlePluginPortal()
    }
}

rootProject.name = "Genral Staff"
include(":app")
 