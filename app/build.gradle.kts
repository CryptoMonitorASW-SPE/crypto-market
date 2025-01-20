import io.github.andreabrighi.gradle.gitsemver.conventionalcommit.ConventionalCommit
import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.task.NodeTask


plugins {
    id("org.danilopianini.git-sensitive-semantic-versioning") version "3.1.7"
    // Apply the Node.js plugin
    id("com.github.node-gradle.node") version "7.1.0"
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        // Add the plugin to the classpath
        classpath("io.github.andreabrighi:conventional-commit-strategy-for-git-sensitive-semantic-versioning-gradle-plugin:1.0.15")
    }
}

gitSemVer {
    maxVersionLength.set(20)
    commitNameBasedUpdateStrategy(ConventionalCommit::semanticVersionUpdate)
}

node {
    version.set("22.13.0")

    // Download a local Node.js distribution (instead of using a global one)
    download.set(true)

    // If you have a specific version of npm to use, uncomment and set it:
    // npmVersion.set("9.6.6")

    // This is the directory where the plugin will look for package.json
    nodeProjectDir.set(file(project.projectDir))
}

tasks.register<NpmTask>("runBackend") {
    dependsOn("npm_install")
    args.set(listOf("run", "dev"))
}

tasks.register<NpmTask>("runFrontend") {
    dependsOn("npm_install")
    args.set(listOf("run", "serve"))
}

tasks.register("printVersion") {
    doLast {
        println("Project version: ${project.version}")
    }
}