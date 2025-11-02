import io.wispforest.helpers.Utils
import io.wispforest.helpers.Extensions.modrinth
import io.wispforest.helpers.Extensions.modrinthImplementation

plugins {
    id("multiloader-platform")
    id("com.github.johnrengelman.shadow")
}

architectury {
    platformSetupLoomIde()
    neoForge {
        platformPackage = "neoforge"
    }
}

var generatedResources = file("src/generated/resources")

sourceSets {
    main {
        resources {
            srcDir(generatedResources)
            exclude(".cache/**")
        }
    }
}

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
//        create("data-generation"){
//            clientData()
//
//            //forgeTemplate("dataClient")
//
//            name("Data Generation")
//
//            programArgs.addAll(
//                mutableListOf(
//                    "--all", "--mod", rootProject.property("mod_id") as String, "--output", generatedResources.absolutePath
//                )
//            )
//        }
    }
}

dependencies {
    // Core Libs
    neoForge(libs.neoforge)
    // --

    // Neoforge: Required as these are General Libs that are used by owo and can be used but must be added to runtime path due to not being mods
    forgeRuntimeLibrary(libs.endec)
    forgeRuntimeLibrary(libs.endec.netty)
    forgeRuntimeLibrary(libs.endec.gson)
    forgeRuntimeLibrary(libs.endec.jankson)
    forgeRuntimeLibrary(libs.jankson)
    // --
}

repositories {}