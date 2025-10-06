plugins {
    java
    `java-library`
    kotlin("jvm") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "8.0.0"
    id("com.willfp.libreforge-gradle-plugin") version "1.0.2"
}

group = "com.mcstarrysky"
version = findProperty("version")!!
val libreforgeVersion = findProperty("libreforge-version")

base {
    archivesName.set(project.name)
}

dependencies {
    implementation(project(":eco-core:core-plugin"))
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "com.github.johnrengelman.shadow")

    repositories {
        mavenCentral()

        maven("https://repo.auxilor.io/repository/maven-public/")
        maven {
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        maven("https://repo.tabooproject.org/repository/releases/")
    }

    dependencies {
        compileOnly("com.willfp:eco:6.75.0")
        compileOnly("io.papermc.paper:paper-api:1.19.2-R0.1-SNAPSHOT")
        compileOnly("cc.polarastrum:aiyatsbus:1.0.17:api")
        compileOnly("com.github.ben-manes.caffeine:caffeine:3.1.5")
        compileOnly("org.jetbrains:annotations:23.0.0")
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.1.0")
    }

    java {
        withSourcesJar()
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }

    tasks {
        shadowJar {
            relocate("com.willfp.libreforge.loader", "com.mcstarrysky.aiyatsbus.libreforge.loader")
            exclude("kotlin/**")
        }

        compileKotlin {
            kotlinOptions {
                jvmTarget = "17"
            }
        }

        compileJava {
            options.isDeprecation = true
            options.encoding = "UTF-8"

            dependsOn(clean)
        }

        processResources {
            filesMatching(listOf("**plugin.yml", "**eco.yml")) {
                expand(
                    "version" to project.version,
                    "libreforgeVersion" to libreforgeVersion,
                    "pluginName" to rootProject.name
                )
            }
        }

        build {
            dependsOn(shadowJar)
        }
    }
}

gradle.buildFinished {
    buildDir.deleteRecursively()
}
