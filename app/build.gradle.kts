import io.github.andreabrighi.gradle.gitsemver.conventionalcommit.ConventionalCommit

plugins {
    alias(libs.plugins.git.sensitive.semantic.versioning)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    application
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("io.github.andreabrighi:conventional-commit-strategy-for-git-sensitive-semantic-versioning-gradle-plugin:1.0.15")
    }
}

gitSemVer {
    maxVersionLength.set(20)
    commitNameBasedUpdateStrategy(ConventionalCommit::semanticVersionUpdate)
}

repositories {
    mavenCentral()
}

dependencies {
    // Dependencies for testing
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.konsist)

    // Dependencies for runtime
    testRuntimeOnly(libs.junit.platform.launcher)

    // Dependencies for the application
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.logback.classic)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")
    implementation(libs.dotenv.kotlin)
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.ktor.server.content.negotiation.jvm)
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "it.unibo.MainKt"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom("config/detekt/detekt.yaml")
}

tasks.register("printVersion") {
    val version = project.version
    doLast {
        println("Project version: $version")
    }
}

tasks.jar {
    archiveFileName.set("app.jar")
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }

    // Include all runtime dependencies into the JAR file
    from(
        configurations.runtimeClasspath
            .get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) },
    )

    from(sourceSets.main.get().output)

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
