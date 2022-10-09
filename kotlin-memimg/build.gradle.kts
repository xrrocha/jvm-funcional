import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "quito-lambda"
version = "1.0.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.7.20-RC"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.arrow-kt:arrow-core:1.1.3-rc.1")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
