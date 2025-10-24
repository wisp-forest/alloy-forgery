import io.wispforest.helpers.Extensions.fabricModule

plugins {
    id("multiloader-mojmap")
    id("multiloader-publishing")
}

architectury {
    common((rootProject.property("enabled_platforms") as String).split(","))
}

dependencies {
    // Core Libs
    modImplementation(libs.fabric.loader)
    compileOnly(libs.mixin.extras.common)
    annotationProcessor(libs.mixin.extras.common)
    // --

    // General Libs
    fabricModule(this::modCompileOnlyApi, "fabric-api-base")
    // --
}

sourceSets {
    main {
        resources.srcDirs.add(File("src/generated"))
    }
}
