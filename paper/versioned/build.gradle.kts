plugins {
    id("java")
}

group = "eu.cafestube"
version = "1.0-SNAPSHOT"


dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly("io.papermc.paper:paper-api:26.2.build.121-stable")
    compileOnly(project(":"))

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.test {
    useJUnitPlatform()
}