plugins {
    id("java-library")
    id("maven-publish")
}

group = "eu.cafestube"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
        maven {
            name = "cafestubeRepository"
            url = uri("https://repo.cafestu.be/repository/maven/")
        }
        maven("https://repo.papermc.io/repository/maven-public/")
    }

}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "$group"
            artifactId = "WetSchematics"
            version = "${project.version}"

            artifact(tasks["jar"])
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

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")


    api("com.github.steveice10:opennbt:1.6")
    implementation("org.jetbrains:annotations:24.0.1")

}

tasks.test {
    useJUnitPlatform()
}