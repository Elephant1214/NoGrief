import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version("1.9.24")
    kotlin("plugin.serialization") version("1.9.24")
    id("io.papermc.paperweight.userdev") version("1.7.1")
    id("xyz.jpenilla.run-paper") version("2.3.0")
    id("io.github.goooler.shadow") version("8.1.7")
}

group = "me.elephant1214.nogrief"
version = "1.0.0-SNAPSHOT"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.purpurmc.org/snapshots")
}

configurations.implementation {
    extendsFrom(configurations.getByName("shadow"))
}

dependencies {
    shadow(kotlin("stdlib-jdk8"))
    shadow("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    paperweight.devBundle("org.purpurmc.purpur", "1.20.6-R0.1-SNAPSHOT")
}

tasks {
    runServer {
        minecraftVersion("1.20.6")
        dependsOn(jar)
    }
    
    compileJava {
        options.encoding = "UTF-8"
        options.release = 21
    }
    withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "21"
            freeCompilerArgs += "-Xopt-in=kotlin.io.path.ExperimentalPathApi"
            freeCompilerArgs += "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi"
        }
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    shadowJar {
        configurations = listOf(project.configurations.getByName("shadow"))
        relocate("org.jetbrains.kotlin", "me.elephant1214.nogrief.kotlin")
        relocate("org.jetbrains.kotlinx", "me.elephant1214.nogrief.kotlinx")
        mergeServiceFiles()
    }
    jar {
        dependsOn(shadowJar)
    }
    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filesMatching(listOf("paper-plugin.yml", "plugin.yml")) {
            expand(props)
        }
    }
}
