plugins {
    kotlin("jvm") version "1.9.23"
    `maven-publish`
}

group = "com.lanli"
version = "1.0.3"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.lanli"
            artifactId = "bencode"
            version = rootProject.version.toString()

            from(components["java"])
        }
    }
}
