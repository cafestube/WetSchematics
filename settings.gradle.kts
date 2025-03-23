rootProject.name = "WetSchematics"

buildscript {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
    dependencies {
        classpath("io.papermc.paperweight.userdev:io.papermc.paperweight.userdev.gradle.plugin:2.0.0-beta.16")
    }
}

include("paper:versioned")
include("paper:versioned:v1_21_1")
include("paper:versioned:v1_21_3")
include("paper:versioned:v1_21_4")
include("paper")
