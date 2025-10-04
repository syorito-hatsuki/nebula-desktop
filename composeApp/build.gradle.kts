import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    kotlin("plugin.serialization") version "2.2.0"
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            implementation("io.ktor:ktor-client-core:3.3.0")
            implementation("io.ktor:ktor-client-cio:3.3.0")
            implementation("io.ktor:ktor-client-content-negotiation:3.3.0")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.0")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

            implementation("org.apache.commons:commons-compress:1.23.0")

            implementation("io.github.kdroidfilter:composenativetray:0.9.4")

            val version = "0.11.0"
            implementation("io.github.vinceglb:filekit-core:${version}")
            implementation("io.github.vinceglb:filekit-dialogs:${version}")
            implementation("io.github.vinceglb:filekit-dialogs-compose:${version}")
        }
    }
}


compose.desktop {
    application {
        mainClass = "dev.syoritohatsuki.nebuladesktop.MainKt"

        buildTypes.release {
            proguard { isEnabled = false }
        }

        nativeDistributions {
            targetFormats(
                *setOf(
                    TargetFormat.AppImage,
                    TargetFormat.Deb,
                    TargetFormat.Dmg,
                    TargetFormat.Exe,
                    TargetFormat.Msi,
                    TargetFormat.Pkg,
                    TargetFormat.Rpm,
                ).toTypedArray()
            )

            val version = "2025.10.1"
            packageVersion = version
            packageName = "dev.syoritohatsuki.nebuladesktop"

            windows {
                packageVersion = version.substring(2)
            }
        }
    }
}
