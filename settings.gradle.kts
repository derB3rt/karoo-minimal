pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // GitHub Packages requires a read:packages token, even for this public package.
        maven {
            url = uri("https://maven.pkg.github.com/hammerheadnav/karoo-ext")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull
                    ?: System.getenv("GPR_USER") ?: ""
                password = providers.gradleProperty("gpr.key").orNull
                    ?: System.getenv("GPR_TOKEN") ?: ""
            }
        }
    }
}
rootProject.name = "KarooMinimal"
include(":app")
