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

fabricApi {
    configureDataGeneration {
        modId.set(rootProject.property("mod_id") as String)
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