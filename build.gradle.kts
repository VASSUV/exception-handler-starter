plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.dependency.management)
    id("java-library")
    id("maven-publish")
}

group = "ru.vassuv"
version = providers.gradleProperty("version").orElse("0.0.1-SNAPSHOT").get()

val githubOwner = providers.gradleProperty("githubOwner")
    .orElse(providers.environmentVariable("GITHUB_OWNER"))
val githubRepo = providers.gradleProperty("githubRepo")
    .orElse(providers.environmentVariable("GITHUB_REPO"))
val githubUser = providers.gradleProperty("githubUser")
    .orElse(providers.environmentVariable("GITHUB_ACTOR"))
val githubToken = providers.gradleProperty("githubToken")
    .orElse(providers.environmentVariable("GITHUB_TOKEN"))

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.spring.boot.autoconfigure)
    implementation(libs.spring.web)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.slf4j.api)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("exceptionhandler") {
            from(components["java"])
            artifactId = "exceptionhandler"
        }
    }
    repositories {
        if (githubOwner.orNull != null && githubRepo.orNull != null) {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/${githubOwner.get()}/${githubRepo.get()}")
                credentials {
                    username = githubUser.orNull
                    password = githubToken.orNull
                }
            }
        }
    }
}
