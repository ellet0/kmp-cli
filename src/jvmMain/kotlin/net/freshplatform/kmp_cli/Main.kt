package net.freshplatform.kmp_cli

import com.varabyte.kotter.foundation.input.Completions
import com.varabyte.kotter.foundation.input.input
import com.varabyte.kotter.foundation.input.onInputEntered
import com.varabyte.kotter.foundation.input.runUntilInputEntered
import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import kotlinx.coroutines.*
import net.freshplatform.kmp_cli.sections.projectFolderNameSection
import net.freshplatform.kmp_cli.sections.useComposeMultiplatformSection
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) = runBlocking {
    session {
        run {
            val projectFolderName = projectFolderNameSection()
            val useComposeMultiplatform = useComposeMultiplatformSection()

            if (!useComposeMultiplatform) {
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

            val repo = when (targetPlatforms) {
                TargetPlatforms.Mobile -> Constants.ComposeMultiplatform.MOBILE
                TargetPlatforms.Desktop -> Constants.ComposeMultiplatform.DESKTOP
                TargetPlatforms.All -> Constants.ComposeMultiplatform.ALL
                null -> throw KotlinNullPointerException("targetPlatforms is not supposed to be null")
            }

            try {
                val coroutineScope = CoroutineScope(Dispatchers.IO)
                val command = "git clone $repo $projectFolderName"
                val currentDirectory = System.getProperty("user.dir")
                val job = coroutineScope.launch {
                    command.runCommand(File(currentDirectory))
                    val newProject = Paths.get(currentDirectory).resolve(projectFolderName)
                    val gitFolder = newProject.resolve(".git")
                    gitFolder.toFile().delete()
                }
                section {
                    text("Loading...")
                }.run {
                    job.join()
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