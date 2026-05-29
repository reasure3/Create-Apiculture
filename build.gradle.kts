import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-library`
    `maven-publish`
    idea

    id("net.neoforged.gradle.userdev") version "7.1.36"

    kotlin("jvm") version "2.3.0"
}

val minecraftVersion: String by project
val minecraftVersionRange: String by project
val neoVersion: String by project
val loaderVersionRange: String by project
val modId: String by project
val modName: String by project
val modLicense: String by project
val modVersion: String by project
val modGroupId: String by project
val kotlinForForgeVersion: String by project
val createVersion: String by project
val createVersionRange: String by project
val ponderVersion: String by project
val flywheelVersion: String by project
val registrateVersion: String by project
val jeiVersion: String by project
val jadeVersion: String by project
val jadeAddonsVersion: String by project

tasks.named<Wrapper>("wrapper") {
    distributionType = Wrapper.DistributionType.BIN
}

version = modVersion
group = modGroupId

base {
    archivesName.set(modId)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

sourceSets {
    main {
        resources {
            srcDir("src/generated/resources")

            exclude("**/*.bbmodel")
            exclude("src/generated/**/.cache")
        }
    }
}

repositories {
    mavenCentral()

    maven {
        name = "Create Maven"
        url = uri("https://maven.createmod.net")
        content {
            includeGroup("com.simibubi.create")
            includeGroup("net.createmod.ponder")
            includeGroup("dev.engine-room.flywheel")
        }
    }

    maven {
        name = "Registrate Maven"
        url = uri("https://maven.ithundxr.dev/snapshots")
        content {
            includeGroup("com.tterrag.registrate")
        }
    }

    maven {
        name = "Kotlin for Forge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
        content {
            includeGroup("thedarkcolour")
        }
    }

    maven {
        name = "Jared's Maven"
        url = uri("https://maven.blamejared.com/")
        content {
            includeGroup("mezz.jei")
        }
    }

    maven {
        name = "ModMaven"
        url = uri("https://modmaven.dev")
        content {
            includeGroup("mezz.jei")
        }
    }

    maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
        content {
            includeGroup("maven.modrinth")
        }
    }
}

configurations {
    runtimeClasspath {
        extendsFrom(localRuntime.get())
    }
}

dependencies {
    implementation("net.neoforged:neoforge:$neoVersion")
    implementation("thedarkcolour:kotlinforforge-neoforge:$kotlinForForgeVersion")

    implementation("com.simibubi.create:create-$minecraftVersion:$createVersion:slim") {
        isTransitive = false
    }
    implementation("net.createmod.ponder:ponder-neoforge:$ponderVersion+mc$minecraftVersion")
    compileOnly("dev.engine-room.flywheel:flywheel-neoforge-api-$minecraftVersion:$flywheelVersion")
    runtimeOnly("dev.engine-room.flywheel:flywheel-neoforge-$minecraftVersion:$flywheelVersion")
    implementation("com.tterrag.registrate:Registrate:$registrateVersion")

    compileOnly("mezz.jei:jei-$minecraftVersion-neoforge-api:$jeiVersion")

    // Runtime-only convenience mods for local run configurations.
    localRuntime("mezz.jei:jei-$minecraftVersion-neoforge:$jeiVersion")
    localRuntime("maven.modrinth:jade:$jadeVersion")
    localRuntime("maven.modrinth:jade-addons-forge:$jadeAddonsVersion")
}

runs {
    configureEach {
        systemProperties.put("forge.logging.markers", "REGISTRIES")
        systemProperties.put("forge.logging.console.level", "debug")

        workingDirectory.set(layout.projectDirectory.dir("run").dir(name))

        modSource(sourceSets.main.get())
    }

    named("client") {
        systemProperties.put("neoforge.enabledGameTestNamespaces", modId)
    }

    named("server") {
        systemProperties.put("neoforge.enabledGameTestNamespaces", modId)
        arguments.add("--nogui")
    }

    named("gameTestServer") {
        systemProperties.put("neoforge.enabledGameTestNamespaces", modId)
    }

    named("data") {
        arguments.addAll(
            "--mod", modId,
            "--all",
            "--output", file("src/generated/resources/").absolutePath,
            "--existing", file("src/main/resources/").absolutePath
        )
    }
}

tasks.withType<ProcessResources>().configureEach {
    val replaceProperties = mapOf(
        "minecraftVersion" to minecraftVersion,
        "minecraftVersionRange" to minecraftVersionRange,
        "neoVersion" to neoVersion,
        "loaderVersionRange" to loaderVersionRange,
        "createVersionRange" to createVersionRange,
        "modId" to modId,
        "modName" to modName,
        "modLicense" to modLicense,
        "modVersion" to modVersion,
    )

    inputs.properties(replaceProperties)

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(replaceProperties)
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = layout.projectDirectory.dir("repo").asFile.toURI()
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<Jar>().configureEach {
    includeEmptyDirs = false
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
