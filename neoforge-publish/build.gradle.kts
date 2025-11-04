import org.gradle.jvm.tasks.Jar;
import org.gradle.accessors.dm.LibrariesForLibs

val libs get() = the<LibrariesForLibs>()

plugins {
    id("net.neoforged.moddev") version "2.0.42-beta"
    id("maven-publish")
    id("java-library")
}

apply(plugin = "maven-publish")

var targetProject = rootProject.project("neoforge")

version = targetProject.version
group = targetProject.group

repositories {
    mavenCentral()
    gradlePluginPortal()
    
    // Neoforge Lib
    maven("https://maven.neoforged.net/releases/")
    // --

    // Fabric API Event Lib
    maven("https://maven.su5ed.dev/releases")
    // --
    
    // oωo (owo-lib) and Endec Lib
    maven("https://maven.wispforest.io/releases")
    // --
}

dependencies {
    implementation(libs.endec)
    implementation(libs.endec.netty)
    implementation(libs.endec.gson)
    implementation(libs.endec.jankson)

    implementation(libs.owolib.neo)
    implementation(libs.jankson)

    api(libs.ffapi.base) { exclude(group = "fabric-api")  }
}

neoForge {
    version = libs.versions.neoforge.asProvider().get()

    validateAccessTransformers = true

    // TODO: UNKNOWN TO HOW BEST TO HANDLE THIS IF WE ARE JUST CONVERTING AW FROM COMMON
//    accessTransformers {
//        from(targetProject.file("src/main/resources/META-INF/accesstransformer.cfg"))
//        publish(targetProject.file("src/main/resources/META-INF/accesstransformer.cfg"))
//    }

    interfaceInjectionData {
        from(targetProject.file("src/main/resources/interfaces.json"))
        publish(targetProject.file("src/main/resources/interfaces.json"))
    }
}

val targetJavaVersion = Integer.parseInt(libs.versions.java.get())
tasks.withType<JavaCompile>().configureEach {
    this.options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible()) {
        this.options.release = targetJavaVersion
    }
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
    base.archivesName.set(targetProject.base.archivesName.get())
    withSourcesJar()

    val data: MutableMap<String, Set<PublishArtifact>> = mutableMapOf();

    val projectName:String = "test" // rootProject.property("mod_id");

    for (cfg in targetProject.configurations) {
        //if (cfg.name.equals("runtimeElements")) continue;

        with(cfg.artifacts) {
            val publishArtifact = this.filter {
                    publishArtifact -> return@filter publishArtifact.file.name.contains(projectName);
            }.toSet()

            data[cfg.name] = publishArtifact

            if (publishArtifact.isNotEmpty()) this.removeAll(publishArtifact);
        }
    }

    for (cfg in project.configurations) {
        with(cfg.artifacts) {
            this.filter {
                    publishArtifact -> return@filter publishArtifact.file.name.contains(projectName);
            }.toSet().let {
                    publishArtifact -> this.removeAll(publishArtifact)
            }

            data[cfg.name]?.let { this.addAll(it) }
        }
    }
}

val ENV = System.getenv()

publishing {
    publications {
        create<MavenPublication>("mavenCommon") {
            this.from(components["java"])

            val name = targetProject.name

            artifactId = "${rootProject.property("mod_id")}${(if (name.isEmpty()) "" else "-${name.replace("-mojmap", "")}")}"
        }
    }
    repositories {
        maven {
            var mavenUrl = ENV["MAVEN_URL"]
            if (mavenUrl != null) {
                url = uri(mavenUrl)
                credentials {
                    username = ENV["MAVEN_USER"]
                    password = ENV["MAVEN_PASSWORD"]
                }
            }
        }
    }
}