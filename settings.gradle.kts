pluginManagement {
    plugins {
        kotlin("jvm") version "2.1.21"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "I-TTS"
include("core")
include("selfhost")
