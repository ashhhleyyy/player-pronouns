plugins {
    id("fabric-loom") version "0.9.45"
    id("io.github.juuxel.loom-quiltflower") version "1.2.1"
    `maven-publish`
}

version = "1.0.0"
group = "io.github.ashisbored"

repositories {
    // needed for placeholder-api
    maven {
        name = "NucleoidMC"
        url = uri("https://maven.nucleoid.xyz/")
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
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16

    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(16)
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.name}" }
    }
}
