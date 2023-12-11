plugins {
    kotlin("multiplatform")
    application
}

group = "net.freshplatform"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

//dependencies {
//
//    testImplementation(kotlin("test"))
//}
//
tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)

    jvm()

    listOf(
        linuxX64(),
        mingwX64(),
        macosArm64(),
        macosX64(),
    ).forEach { nativeTarget ->
        nativeTarget.apply {
            binaries {
                executable {
                    entryPoint = "main"
                }
            }
        }
    }

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation("com.varabyte.kotter:kotter-jvm:1.1.1")
            }
        }
        val commonMain by getting {
            dependencies {
                implementation("com.varabyte.kotter:kotter:1.1.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            }
        }
    }
}

application {
    mainClass.set("${group}.kmp_cli.MainKt")
}