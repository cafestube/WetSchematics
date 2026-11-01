plugins {
    id("java")
    id("io.papermc.paperweight.userdev")
}

group = "eu.cafestube"
version = "1.0-SNAPSHOT"


dependencies {
    paperweight.paperDevBundle("26.2.build.+")

    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(project(":paper:versioned"))
    compileOnly(project(":"))

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()
}