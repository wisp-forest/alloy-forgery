import io.wispforest.helpers.Utils
import io.wispforest.helpers.Extensions.modrinth
import io.wispforest.helpers.Extensions.modrinthImplementation

plugins {
    id("multiloader-platform")
    id("multiloader-publishing")
    id("com.github.johnrengelman.shadow")
}

architectury {
    platformSetupLoomIde()
    fabric {
        platformPackage = "fabric"
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

fabricApi {
    configureDataGeneration {
        modId.set(rootProject.property("mod_id") as String)
        outputDirectory = generatedResources
        client = true
    }
}

dependencies {
    // Core Libs
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    // --

    // General Libs
    modCompileOnly(libs.modmenu)
    modLocalRuntime(libs.modmenu)
    //--
}

repositories {}