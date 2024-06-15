import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    kotlin("jvm") version("1.9.24")
    kotlin("plugin.serialization") version("1.9.24")
    id("io.papermc.paperweight.userdev") version("1.7.1")
    id("xyz.jpenilla.run-paper") version("2.3.0")
}

group = "me.elephant1214.nogrief"
version = "1.0.0-SNAPSHOT"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

repositories {
    mavenCentral()
    maven("https://repo.purpurmc.org/snapshots")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://www.jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("cloud.commandframework:cloud-paper:1.8.4")
    implementation("cloud.commandframework:cloud-annotations:1.8.4")
    implementation("cloud.commandframework:cloud-minecraft-extras:1.8.4")
    implementation("com.github.Elephant1214:CCFUtils:0a75e37d55")
    implementation("com.github.Elephant1214:InventoryMenus:7ce62cb02b")
    implementation("xyz.jpenilla:squaremap-api:1.2.4")
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
            freeCompilerArgs += "-opt-in=kotlin.io.path.ExperimentalPathApi"
            freeCompilerArgs += "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
        }
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filesMatching(listOf("paper-plugin.yml", "plugin.yml")) {
            expand(props)
        }
    }
}
