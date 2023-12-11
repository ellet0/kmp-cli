package net.freshplatform.kmp_cli.sections

import com.varabyte.kotter.foundation.input.Completions
import com.varabyte.kotter.foundation.input.input
import com.varabyte.kotter.foundation.input.onInputEntered
import com.varabyte.kotter.foundation.input.runUntilInputEntered
import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.text.cyan
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.Session

fun Session.useComposeMultiplatformSection(): Boolean {
    var useComposeMultiplatform by liveVarOf(false)
    section {
        text("Would you use "); cyan { text("Compose Multiplatform") }; textLine(" to share UI? (Y/n)")
        text("> "); input(Completions("yes", "no"), initialText = "y")

    }.runUntilInputEntered {
        onInputEntered { useComposeMultiplatform = "yes".startsWith(input.lowercase()) }
    }
    return useComposeMultiplatform
}