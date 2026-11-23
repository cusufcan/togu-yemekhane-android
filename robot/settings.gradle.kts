// Robot uygulamadan bağımsız bir derlemedir: uygulama derlenmese de robot çalışır.
// Çalıştırma: ./gradlew -p robot run --args="<çıktı klasörü>"
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories { mavenCentral() }
    versionCatalogs {
        create("libs") { from(files("../gradle/libs.versions.toml")) }
    }
}

rootProject.name = "robot"
