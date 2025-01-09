plugins {
    id("java")
}

allprojects {
    apply(plugin = "java")
    
    group = "com.project"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation(platform("org.junit:junit-bom:5.10.0"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }

    tasks.test {
        useJUnitPlatform()
    }
}

project(":Server") {
    dependencies {
        implementation(project(":Common"))
    }

    tasks.jar {
        manifest {
            attributes["Main-Class"] = "TicTacToeServer"
        }
        
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        
        from({
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
        })
        
        from(project(":Common").sourceSets.main.get().output)
    }
}

project(":Client") {
    dependencies {
        implementation(project(":Common"))
    }

    tasks.jar {
        manifest {
            attributes["Main-Class"] = "client.TicTacToeClient"
        }
        
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        
        from({
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
        })
        
        from(project(":Common").sourceSets.main.get().output)
    }
}

project(":Common")