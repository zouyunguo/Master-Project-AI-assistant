plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij.platform") version "2.0.1"
    id("org.openjfx.javafxplugin") version "0.0.13"
}

group = "MP25"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("org.json:json:20240303")
    implementation("com.github.javaparser:javaparser-core:3.25.4")
    implementation("com.vladsch.flexmark:flexmark-all:0.64.0")

    intellijPlatform {
        intellijIdeaCommunity("2024.2.3")
        bundledPlugins(listOf(/* Plugin Dependencies */))
        instrumentationTools()
    }
}

javafx {
    version = "20"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.web", "javafx.swing")
}

// Configure IntelliJ Platform Plugin
intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        version = project.version.toString()
        ideaVersion {
            sinceBuild = "232"
            untilBuild = "242.*"
        }
    }

    publishing {
        token = System.getenv("PUBLISH_TOKEN")
    }

    signing {
        certificateChain = System.getenv("CERTIFICATE_CHAIN")
        privateKey = System.getenv("PRIVATE_KEY")
        password = System.getenv("PRIVATE_KEY_PASSWORD")
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}
