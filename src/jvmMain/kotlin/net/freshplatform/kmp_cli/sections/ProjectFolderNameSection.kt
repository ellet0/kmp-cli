package net.freshplatform.kmp_cli.sections

import com.varabyte.kotter.foundation.input.Completions
import com.varabyte.kotter.foundation.input.input
import com.varabyte.kotter.foundation.input.onInputEntered
import com.varabyte.kotter.foundation.input.runUntilInputEntered
import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.Session
import java.io.File

fun Session.projectFolderNameSection(): String {
    var folderName by liveVarOf("app")
    var isFolderExists = false

    section {
        textLine("The folder you choose here will be created under your current path.")
        textLine("You can enter `.` if you want to use the current directory.\n")
    }.run {  }
    do {
        section {
            if (isFolderExists) {
                text("There is already a folder with name $folderName. Please delete it or choose another name first.")
            }
            textLine("Specify a folder for your project:")
            text("> "); input(Completions(folderName))

        }.runUntilInputEntered {
            onInputEntered {
                if (input.trim().isNotBlank()) {
                    folderName = input
                }
                isFolderExists = File(folderName).exists()
            }
        }

    } while (isFolderExists)

    return folderName
}