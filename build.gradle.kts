import org.jetbrains.kotlin.ir.backend.js.compile
import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml
import xyz.jpenilla.resourcefactory.paper.paperPluginYaml

plugins {
    kotlin("jvm") version("1.9.23")
    id("io.papermc.paperweight.userdev") version("1.6.3")
    id("xyz.jpenilla.run-paper") version("2.2.4")
    id("xyz.jpenilla.resource-factory-bukkit-convention") version("1.1.1")
}

group = "me.elephant1214.nogrief"
version = "1.0.0-SNAPSHOT"
description = "Test plugin for paperweight-userdev"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.purpurmc.org/snapshots")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    // implementation("org.purpurmc.purpur:purpur-api:1.20.6-R0.1-SNAPSHOT")
    paperweight.devBundle("org.purpurmc.purpur", "1.20.6-R0.1-SNAPSHOT")
    // paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")
    // paperweight.foliaDevBundle("1.20.6-R0.1-SNAPSHOT")
    // paperweight.devBundle("com.example.paperfork", "1.20.6-R0.1-SNAPSHOT")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release = 21
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "21"

        all {
            kotlinOptions.freeCompilerArgs += listOf(
                "-opt-in=kotlin.io.path.ExperimentalPathApi"
            )
        }
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
}

paperPluginYaml {
    main = "me.elephant1214.nogrief.NoGriefKt"
    authors.add("Elephant_1214")
    apiVersion = "1.20.6"
    version = rootProject.version.toString()
}