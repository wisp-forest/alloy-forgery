import io.wispforest.helpers.Extensions.fabricModule

plugins {
    id("multiloader-mojmap")
    id("multiloader-publishing")
}

dependencies {
    // Core Libs
    compileOnly(libs.mixin.extras.common)
    annotationProcessor(libs.mixin.extras.common)
    // --

    // General Libs
    fabricModule(this::modCompileOnlyApi, "fabric-api-base")
    // --
}
