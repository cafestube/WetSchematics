import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.papermc.paperweight.userdev.attribute.Obfuscation

plugins {
    id("java-library")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("maven-publish")
}


group = "eu.cafestube"
version = "1.0-SNAPSHOT"

val versionSpecific = configurations.create("versionSpecific") {
    description = "Version Adapters to include in the JAR"
    isCanBeConsumed = false
    isCanBeResolved = true
    shouldResolveConsistentlyWith(configurations["runtimeClasspath"])
    attributes {
        attribute(Obfuscation.OBFUSCATION_ATTRIBUTE, objects.named(Obfuscation.OBFUSCATED))
    }
}

configurations.implementation {
    extendsFrom(versionSpecific)
}

val versions = listOf("v1_20_1", "v1_20_2")



dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")

    compileOnly(project(":"))
    versions.forEach {
        versionSpecific(project(":paper:versioned:$it"))
    }
    implementation(project(":paper:versioned"))

}

tasks.named<ShadowJar>("shadowJar") {
//    dependsOn(versions.map { project.project(":$it") }.map { it.tasks.named("build") })
    from(Callable {
        versionSpecific.resolve()
            .map { f ->
                zipTree(f).matching {
                    exclude("META-INF/")
                }
            }
    })
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
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