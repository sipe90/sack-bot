plugins {
    id("maven-publish")
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.jetbrains.kotlin.plugin.spring") version "1.8.20"
    id("com.google.cloud.tools.jib") version "3.3.1"
    id("net.researchgate.release") version "3.0.2"
    id("com.diffplug.spotless") version "6.25.0"
    kotlin("jvm") version "2.0.20"
}

group = "com.github.sipe90"

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.session:spring-session-data-mongodb")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.4.12")

    implementation("net.dv8tion:JDA:5.1.1")
    implementation("com.github.minndevelopment:jda-reactor:1.6.0")
    implementation("dev.arbjerg:lavaplayer:2.2.2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.security:spring-security-test")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

kotlin {
    jvmToolchain(21)
}

jib {
    from {
        image = "eclipse-temurin:21"
    }
    to {
        image = "sipe90/${project.name}:${project.version}"
    }
}

spotless {
    kotlin {
        ktlint()
    }
}

release {
    git.requireBranch.set("master")
    tagTemplate.set("v\$version")
}

tasks.test {
    useJUnitPlatform()
}

tasks.afterReleaseBuild {
    dependsOn(tasks.jib)
}

tasks.jib {
    dependsOn(tasks.bootJar)
}
