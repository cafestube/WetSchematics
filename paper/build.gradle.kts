plugins {
    id("java-library")
}

group = "eu.cafestube"
version = "1.0-SNAPSHOT"

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")

    api(project(":"))
    implementation(project(":paper:versioned"))
    implementation(project(":paper:versioned:v1_20_1"))
    implementation(project(":paper:versioned:v1_20_2"))

}

tasks.test {
    useJUnitPlatform()
}