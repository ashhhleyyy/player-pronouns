plugins {
    id("fabric-loom") version "0.12.+"
    id("io.github.juuxel.loom-quiltflower") version "1.7.+"
    id("com.modrinth.minotaur") version "2.+"
    `maven-publish`
}

version = "1.5.0+1.19"
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
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

dependencies {
    // Minecraft
    minecraft(libs.minecraft)
    mappings(variantOf(libs.yarn) { classifier("v2") })

    // Fabric
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)

    // placeholder-api
    modImplementation(libs.placeholder.api)
    include(libs.placeholder.api)

    // more-codecs
    modImplementation(libs.more.codecs)
    include(libs.more.codecs)

    // fabric-api-permissions
    modImplementation(libs.fabric.permissions)
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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.name}" }
    }
}

modrinth {
    projectId.set("player-pronouns")
    uploadFile.set(tasks.remapJar.get())
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
