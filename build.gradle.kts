plugins {
    id("maven-publish")
    id("org.springframework.boot") version "2.6.6"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.jetbrains.kotlin.plugin.spring") version "1.6.20"
    id("com.google.cloud.tools.jib") version "3.2.1"
    id("net.researchgate.release") version "2.8.1"
    kotlin("jvm") version "1.6.20"
}

group = "com.github.sipe90"

java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    jcenter()
    maven {
        name = "m2-dv8tion"
        url = uri("https://m2.dv8tion.net/releases")
    }
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    implementation("ch.qos.logback:logback-classic:1.2.11")

    implementation("org.dizitart:potassium-nitrite:3.4.3")
    implementation("net.dv8tion:JDA:5.0.0-alpha.9")
    implementation("com.github.minndevelopment:jda-reactor:1.5.0")
    implementation("com.github.walkyst:lavaplayer-fork:1.3.97")
    implementation("com.sedmelluq:jda-nas:1.1.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.security:spring-security-test")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

jib {
    to {
        image = "sipe90/${project.name}:${project.version}"
    }
}

release {
    tagTemplate = "v${version}"
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileKotlin {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.afterReleaseBuild {
    dependsOn(tasks.jib)
}

tasks.jib {
    dependsOn(tasks.bootJar)
}
