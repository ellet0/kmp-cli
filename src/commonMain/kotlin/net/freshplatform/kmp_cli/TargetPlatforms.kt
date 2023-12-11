package net.freshplatform.kmp_cli

enum class TargetPlatforms(
    val valueName: String,
) {
    Mobile("mobile"),
    Desktop("desktop"),
    All("all");

    companion object {
        fun byValueName(valueName: String): TargetPlatforms {
            TargetPlatforms.entries.forEach {
                if (valueName == it.valueName) {
                    return it
                }
            }
            throw IllegalArgumentException("Could not find an element.")
        }
    }
}