plugins {
    kotlin("jvm") version "1.9.23"
    `maven-publish`
}

group = "com.lanlinju"
version = "1.0.4"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
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
            groupId = "com.lanlinju"
            artifactId = "bencode"
            version = rootProject.version.toString()

            from(components["java"])
        }
    }
}
