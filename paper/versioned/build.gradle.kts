plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.5.5" apply false
}

group = "eu.cafestube"
version = "1.0-SNAPSHOT"


dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
    implementation(project(":"))

}

tasks.test {
    useJUnitPlatform()
}