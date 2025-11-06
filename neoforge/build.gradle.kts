import io.wispforest.helpers.Utils
import io.wispforest.helpers.Extensions.modrinth
import io.wispforest.helpers.Extensions.modrinthImplementation

plugins {
    id("multiloader-platform")
    id("com.github.johnrengelman.shadow")
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