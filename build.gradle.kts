plugins {
    `java-library`
}

group = "eu.cafestube"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")


    api("me.nullicorn:Nedit:2.2.0")

}

tasks.test {
    useJUnitPlatform()
}