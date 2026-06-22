plugins {
    kotlin("jvm") version "2.3.21"
    id("com.gradleup.shadow") version "9.4.2"
}

group = "dev.boecker"
version = "0.1.0"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.boecker.dev/releases/")
    maven("https://repo.hypera.dev/snapshots/")
}

dependencies {
    implementation("net.minestom:minestom:2026.06.20-26.1.2")
    implementation("dev.hollowcube:polar:1.16.0")

    implementation("dev.boecker.cherrycave.permissions:minestom:0.1.1")

    implementation("ch.qos.logback:logback-classic:1.5.34")
    implementation("io.github.oshai:kotlin-logging-jvm:8.0.4")
}

kotlin {
    jvmToolchain(25)
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "dev.boecker.cclobby.LauncherKt"
        }
    }

    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("")
    }
}