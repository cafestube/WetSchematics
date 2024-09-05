rootProject.name = "WetSchematics"

buildscript {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
    dependencies {
        classpath("io.papermc.paperweight.userdev:io.papermc.paperweight.userdev.gradle.plugin:1.7.2")
    }
}

include("paper:versioned")
include("paper:versioned:v1_20_1")
include("paper:versioned:v1_20_2")
include("paper:versioned:v1_20_4")
include("paper:versioned:v1_20_6")
include("paper:versioned:v1_21_1")
include("paper")
