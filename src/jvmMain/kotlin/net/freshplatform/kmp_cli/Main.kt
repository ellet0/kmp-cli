package net.freshplatform.kmp_cli

import com.varabyte.kotter.foundation.input.Completions
import com.varabyte.kotter.foundation.input.input
import com.varabyte.kotter.foundation.input.onInputEntered
import com.varabyte.kotter.foundation.input.runUntilInputEntered
import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.cyan
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) = runBlocking {

    session {
        run {
            var folderName by liveVarOf("app")
            section {
                textLine("The folder you choose here will be created under your current path.")
                textLine("You can enter `.` if you want to use the current directory.\n")
                textLine("Specify a folder for your project:")
                text("> "); input(Completions(folderName))

            }.runUntilInputEntered {
                onInputEntered {
                    if (input.trim().isNotBlank()) {
                        folderName = input
                    }
                }
            }

            val isFolderExists = File(folderName).exists()
            if (isFolderExists) {
                section {
                    text("There is already a folder with name $folderName. Please delete it or choose another name first.")
                }.run { }
                return@session
            }

            var useComposeMultiplatform by liveVarOf(false)
            section {
                text("Would you use "); cyan { text("Compose Multiplatform") }; textLine(" to share UI? (Y/n)")
                text("> "); input(Completions("yes", "no"), initialText = "y")

            }.runUntilInputEntered {
                onInputEntered { useComposeMultiplatform = "yes".startsWith(input.lowercase()) }
            }

            if (!useComposeMultiplatform) {
                section {
                    text("Sorry but only Compose multiplatform is supported for now.")
                }.run { }
                return@session
            }

            var targetPlatforms by liveVarOf<TargetPlatforms?>(TargetPlatforms.All)

            section {
                textLine("Which platforms are you planning to support? ")
                text("> "); input(Completions("all", "desktop", "mobile"))

            }.runUntilInputEntered {
                onInputEntered {
                    try {
                        if (input.trim().isNotBlank()) {
                            targetPlatforms = TargetPlatforms.byValueName(input)
                        }
                    } catch (_: IllegalArgumentException) {
                        targetPlatforms = null
                    }
                }
            }

            if (targetPlatforms == null) {
                section {
                    text("Please enter a valid value, desktop, mobile or all")
                }.run { }
                return@session
            }

            var projectId by liveVarOf("org.example.${folderName}")
            section {
                textLine("Note: The group ID should uniquely identify your project and organization.")
                textLine("'io.github.(username).(projectname)' can work for a hobby project.")
                textLine("What is the group ID for your project?")
                text("> "); input(Completions(projectId))

            }.runUntilInputEntered {
                onInputEntered {
                    projectId = input
                }
            }

            val repo = when (targetPlatforms) {
                TargetPlatforms.Mobile -> Constants.ComposeMultiplatform.MOBILE
                TargetPlatforms.Desktop -> Constants.ComposeMultiplatform.DESKTOP
                TargetPlatforms.All -> Constants.ComposeMultiplatform.ALL
                null -> throw KotlinNullPointerException("targetPlatforms is not supposed to be null")
            }

            try {
                val command = "git clone $repo $folderName"
                val currentDirectory = System.getProperty("user.dir")
                runBlocking {
                    command.runCommand(File(currentDirectory))
                    val newProject = Paths.get(currentDirectory).resolve(folderName)
                    val gitFolder = newProject.resolve(".git")
                    gitFolder.toFile().delete()
                }
            } catch (e: Exception) {
                println("Failure: $e")
            }
        }
    }
}

suspend fun String.runCommand(workingDir: File): String? {
    val s = this
    return withContext(Dispatchers.IO) {
        try {
            val parts = s.split("\\s".toRegex())
            val processBuilder = ProcessBuilder(*parts.toTypedArray())
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            processBuilder.waitFor(60, TimeUnit.MINUTES)
            processBuilder.inputStream.bufferedReader().readText()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}