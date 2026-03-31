plugins {
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
    id("com.modrinth.minotaur") version "2.+"
    `maven-publish`
}

version = "2.4.0+26.1"
group = "dev.ashhhleyyy"

repositories {
    // needed for placeholder-api
    maven {
        name = "NucleoidMC"
        url = uri("https://maven.nucleoid.xyz/")
    }
    // permissions api
    maven {
        name = "Sonatype OSS"
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
    }
}

dependencies {
    // Minecraft
    minecraft(libs.minecraft)

    // Fabric
    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)

    // placeholder-api
    implementation(libs.placeholder.api)
    include(libs.placeholder.api)

    // fabric-api-permissions
    implementation(libs.fabric.permissions)
    include(libs.fabric.permissions)
}

loom {
    runtimeOnlyLog4j.set(true)
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25

    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.name}" }
    }
}

modrinth {
    projectId.set("player-pronouns")
    dependencies {
        required.project("fabric-api")
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }

    repositories {
        if (System.getenv("MAVEN_URL") != null) {
            maven {
                name = "ashhhleyyy"
                setUrl(System.getenv("MAVEN_URL"))
                credentials {
                    username = System.getenv("MAVEN_USERNAME")
                    password = System.getenv("MAVEN_PASSWORD")
                }
            }
        } else {
            mavenLocal()
        }
    }
}
