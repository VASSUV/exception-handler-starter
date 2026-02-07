plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.dependency.management)
    id("java-library")
    id("maven-publish")
}

group = "ru.vassuv"
version = "0.0.1-SNAPSHOT"

val githubOwner = providers.gradleProperty("githubOwner").orElse(System.getenv("GITHUB_OWNER"))
val githubRepo = providers.gradleProperty("githubRepo").orElse(System.getenv("GITHUB_REPO"))
val githubUser = providers.gradleProperty("githubUser").orElse(System.getenv("GITHUB_ACTOR"))
val githubToken = providers.gradleProperty("githubToken").orElse(System.getenv("GITHUB_TOKEN"))

val gitlabProjectId = providers.gradleProperty("gitlabProjectId").orElse(System.getenv("GITLAB_PROJECT_ID"))
val gitlabUser = providers.gradleProperty("gitlabUser").orElse(System.getenv("GITLAB_USER"))
val gitlabToken = providers.gradleProperty("gitlabToken").orElse(System.getenv("GITLAB_TOKEN"))

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

        if (gitlabProjectId.orNull != null) {
            maven {
                name = "GitLabPackages"
                url = uri("https://gitlab.com/api/v4/projects/${gitlabProjectId.get()}/packages/maven")
                credentials {
                    username = gitlabUser.orNull
                    password = gitlabToken.orNull
                }
            }
        }
    }
}
