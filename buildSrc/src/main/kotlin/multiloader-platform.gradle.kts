import io.wispforest.helpers.Extensions.currentPlatform
import io.wispforest.helpers.Extensions.currentPlatformDisplayName
import org.gradle.kotlin.dsl.get

plugins {
    id("multiloader-base")
    id("com.github.johnrengelman.shadow")
}

val common by configurations.creating
val shadowCommon by configurations.creating

configurations {
    common
    shadowCommon // Don't use shadow from the shadow plugin since it *excludes* files.
    "compileClasspath" { extendsFrom(common) }
    "runtimeClasspath" { extendsFrom(common) }
}

dependencies {
    "common"(project(":common", "namedElements")) { this.setTransitive(false) }
}

//--

var generatedResources = file("src/generated/resources")

sourceSets {
    main {
        resources {
            srcDir(generatedResources)
            exclude(".cache/**")
        }
    }
}

if (currentPlatform == "fabric") {
    fabricApi {
        configureDataGeneration {
            modId.set(rootProject.property("mod_id") as String)
            outputDirectory = generatedResources
            client = true
        }
    }
} else {
    loom {
        runs {
            create("data-generation"){
                clientData()

                name("Data Generation")

                programArgs.addAll(
                    mutableListOf(
                        "--all", "--mod", rootProject.property("mod_id") as String, "--output", generatedResources.absolutePath
                    )
                )
            }
        }
    }
}

//--

tasks.shadowJar {
    if (currentPlatform != "fabric") {
        exclude("fabric.mod.json")
    }
    exclude("architectury.common.json")

    configurations = mutableListOf<FileCollection>(project.configurations["shadowCommon"]);
    archiveClassifier.set("dev-shadow")
    destinationDirectory.set(destinationDirectory.get().dir("shadow"))
}

tasks.remapJar {
    inputFile.set(tasks.shadowJar.get().archiveFile)
    dependsOn(tasks.shadowJar)
    archiveClassifier.set("")

    if (currentPlatform == "fabric") {
        injectAccessWidener = true
    } else {
        atAccessWideners.add("${rootProject.property("mod_id")}.accesswidener")
    }
}

tasks.getByName("sourcesJar", AbstractArchiveTask::class) {
    val commonSources = project(":common").tasks.getByName("sourcesJar", AbstractArchiveTask::class)
    dependsOn(commonSources)
    from(commonSources.archiveFile.map { zipTree(it) })
}

with(components["java"] as AdhocComponentWithVariants) {
    withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) { skip() }
}