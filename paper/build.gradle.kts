import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.papermc.paperweight.userdev.attribute.Obfuscation

plugins {
    id("java-library")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("maven-publish")
}


group = "eu.cafestube"
version = "2.0.9-SNAPSHOT"

val versions = listOf("v1_21_1", "v1_21_3", "v1_21_4")

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.12.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")

    compileOnly(project(":"))
    versions.forEach {
        implementation(project(":paper:versioned:$it"))
    }
    implementation(project(":paper:versioned"))

}

tasks.named<ShadowJar>("shadowJar") {
    dependsOn(versions.map { ":paper:versioned:${it}:build" })
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
}

base {
    archivesName.set("WetSchematicsPaper")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "$group"
            artifactId = "WetSchematicsPaper"
            version = "${project.version}"

            artifact(tasks["shadowJar"])
            artifact(tasks["sourcesJar"])
        }
        repositories {
            maven {
                name = "cafestubeRepository"
                credentials(PasswordCredentials::class)
                url = uri("https://repo.cafestu.be/repository/maven-public-snapshots/")
            }
        }
    }
}

tasks {
    shadowJar {
        archiveClassifier.set("")
    }
    jar {
        archiveClassifier.set("api-only")
    }
    build {
        dependsOn(shadowJar)
    }
}

tasks.test {
    useJUnitPlatform()
}