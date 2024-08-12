import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree.Companion.test

plugins {
    kotlin("jvm") version "2.0.0"
}

group = "org.dgawlik"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}


kotlin {
    jvmToolchain(21)
}