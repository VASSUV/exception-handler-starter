plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.dependency.management)
    id("java-library")
    id("maven-publish")
}

group = "ru.vassuv"
version = "0.0.1-SNAPSHOT"

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
}
