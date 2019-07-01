import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    id("org.jetbrains.dokka") version "0.9.18"
    id("com.jfrog.bintray") version "1.8.4"
    id("maven-publish")

    kotlin("jvm") version "1.3.31"
}

group = "uk.co.paulcowie"
version = "0.0.4"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0-M1")

    implementation("org.jsoup:jsoup:1.12.1")
    implementation("com.beust:klaxon:5.0.1")
    implementation("io.ktor:ktor-client-apache:1.2.1")
    implementation("org.slf4j:slf4j-api:1.7.26")

    testCompile("org.slf4j:slf4j-simple:1.7.26")
    testCompile("junit:junit:4.12")
}

val dokka by tasks.getting(DokkaTask::class) {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}

val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    classifier = "javadoc"
    from(dokka)
}

val sourceJar by tasks.creating(Jar::class) {
    classifier = "sources"
    from(sourceSets["main"].allSource)
    from(sourceSets["test"].allSource)
}

val compileKotlin: KotlinCompile by tasks
val compileTestKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}


compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_KEY")
    setPublications("default")

    with(pkg){
        repo = "maven"
        name = "uk.co.paulcowie.twitterscraper-kt"

        with(version) {
            name = "0.0.4"
            released  = Date().toString()
        }
    }
}


publishing {
    publications {
        create<MavenPublication>("default") {
            from(components["java"])
            artifact(dokkaJar)
            artifact(sourceJar)
        }
    }
}