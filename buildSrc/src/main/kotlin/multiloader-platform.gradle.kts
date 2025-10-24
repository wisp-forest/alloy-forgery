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
    "shadowCommon"(project(":common", "transformProduction$currentPlatformDisplayName")) { this.setTransitive(false) }
}

//--

tasks.shadowJar {
    if (currentPlatform != "fabric") {
        exclude("fabric.mod.json")
    }
    exclude("architectury.common.json")

    configurations = mutableListOf<FileCollection>(project.configurations["shadowCommon"]);
    archiveClassifier.set("dev-shadow")
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