plugins {
    id("java")
    id("io.papermc.paperweight.userdev")
}

group = "eu.cafestube"
version = "1.0-SNAPSHOT"


dependencies {
    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")

    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(project(":paper:versioned"))
    compileOnly(project(":"))

}


tasks {
    assemble {
        dependsOn(reobfJar)
    }

}


tasks.test {
    useJUnitPlatform()
}