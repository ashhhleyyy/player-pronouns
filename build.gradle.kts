plugins {
    id("fabric-loom") version "0.12.+"
    id("io.github.juuxel.loom-quiltflower") version "1.7.+"
    `maven-publish`
}

version = "1.4.0+1.19"
group = "io.github.ashhhleyyy"

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
