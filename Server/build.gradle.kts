plugins {
    id("java")
}

group = "com.project"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(project(":Common"))
}

tasks.test {
    useJUnitPlatform()
}

project(":Server") {
    tasks.jar {
        manifest {
            attributes["Main-Class"] = "TicTacToeServer"
        }
    }
}