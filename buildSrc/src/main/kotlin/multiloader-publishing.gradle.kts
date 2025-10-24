import io.wispforest.helpers.Extensions.libs

plugins {
    id("java-library")
    id("maven-publish")
}

val ENV = System.getenv()

publishing {
    publications {
        create<MavenPublication>("mavenCommon") {
            val name = project.name
            artifactId = "${rootProject.property("mod_id")}${(if(name.isEmpty()) "" else "-${name.replace("-mojmap", "")}")}"
            afterEvaluate {
                this@create.from(components["java"])
            }
        }

        if (project.name == "common") {
            create<MavenPublication>("mavenMojmap") {
                val name = project.name

                version = "${rootProject.property("mod_version")}+${libs.versions.minecraft.asProvider().get()}-mojmap"
                artifactId = "${rootProject.property("mod_id")}-${name}"

                afterEvaluate {
                    this@create.from(components["java"])

                    this@create.setArtifacts(emptyList<Any>())

                    val mojmapJarTask = project.tasks.named("mojmapJar");
                    artifact(mojmapJarTask) {
                        builtBy(mojmapJarTask)
                        classifier = ""
                    }
                    val mojmapSourcesJarTask = project.tasks.named("mojmapSourcesJar");
                    artifact(mojmapSourcesJarTask) {
                        builtBy(mojmapSourcesJarTask)
                        classifier = "sources"
                    }
                }
            }
        }
    }

    repositories {
        maven {
            url = uri(ENV["MAVEN_URL"]!!)
            credentials {
                username = ENV["MAVEN_USER"]
                password = ENV["MAVEN_PASSWORD"]
            }
        }
    }
}