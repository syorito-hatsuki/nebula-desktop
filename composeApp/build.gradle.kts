import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.serialization)
}

val appVersion = rootProject.extra.get("app.version") as String

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            languageVersion.set(KotlinVersion.KOTLIN_2_2)
            freeCompilerArgs.addAll(
                "-Xjvm-default=all",
                "-opt-in=kotlin.RequiresOptIn",
                "-Xinline-classes",
                "-Xcontext-receivers"
            )
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
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
        }

        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutinesSwing)

                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.kotlinx.serialization.json)

                implementation(libs.composenativetray)
                implementation(libs.filekit.core)
                implementation(libs.filekit.dialogs)
                implementation(libs.filekit.dialogs.compose)

                implementation(libs.commons.compress)

                implementation(libs.pty4j)
            }
        }
    }
}

compose {
    desktop {
        application {
            mainClass = "dev.syoritohatsuki.nebuladesktop.MainKt"

            buildTypes {
                release {
                    proguard {
                        isEnabled = false
                    }
                }
            }

            nativeDistributions {

                modules("jdk.security.auth", "jdk.unsupported")

                targetFormats(
                    /* Temporally disabled until done actions/tasks for AppImage and Flatpak deploy */
                     TargetFormat.AppImage,
                    TargetFormat.Deb,
                    TargetFormat.Rpm,
                    TargetFormat.Dmg,
                    TargetFormat.Pkg,
                    TargetFormat.Exe,
                    TargetFormat.Msi,
                )

                packageName = "nebula-desktop"
                packageVersion = appVersion
                description = "Nebula Desktop VPN client"
                vendor = "Syorito Hatsuki"
                licenseFile.set(rootProject.file("LICENSE"))

                outputBaseDir.set(project.layout.buildDirectory.dir("compose/releases"))

                linux {
                    iconFile.set(project.file("src/jvmMain/resources/icon.svg"))
                    rpmLicenseType = "MIT"
                    appCategory = "Utility"
                }

                windows {
                    iconFile.set(project.file("src/jvmMain/resources/icon.ico"))
                    packageVersion = appVersion.substring(2)
                    shortcut = true
                    menuGroup = "Nebula"
                }

                macOS {
                    iconFile.set(project.file("src/jvmMain/resources/icon.icns"))
                }
            }
        }
    }
}
