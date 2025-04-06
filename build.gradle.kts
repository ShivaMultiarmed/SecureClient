import org.gradle.api.file.DuplicatesStrategy.EXCLUDE

plugins {
    kotlin("jvm") version "2.0.0"
    application
}

group = "mikhail.shell.web.application"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("io.ktor:ktor-server-core:2.3.4")
    implementation("io.ktor:ktor-server-netty:2.3.4")
    implementation("io.ktor:ktor-server-websockets:2.3.4")

    // Ktor Client WebSockets (for Alice & Bob)
    implementation("io.ktor:ktor-client-core:2.3.4")
    implementation("io.ktor:ktor-client-cio:2.3.4")
    implementation("io.ktor:ktor-client-websockets:2.3.4")
}


tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "mikhail.shell.education.security.client.MainKt"
        )
    }
    from (sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from ({
        configurations.runtimeClasspath.get().filter { it.exists() }.map { zipTree(it) }
    })
}
tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

tasks.create<Jar>("estream") {
    archiveFileName = "estream-client.jar"
    manifest {
        attributes(
            "Main-Class" to "mikhail.shell.education.security.client.transfer.MainKt"
        )
    }
    from(sourceSets.main.get().output) {
        include("mikhail/shell/education/security/client/common/**")
        include("mikhail/shell/education/security/client/transfer/**")
    }
    dependsOn(configurations.runtimeClasspath)
    duplicatesStrategy = EXCLUDE
    from(
        {
            configurations.runtimeClasspath.get().filter { it.exists() }.map { zipTree(it) }
        }
    )
}