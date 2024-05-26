package me.elephant1214.nogrief.locale

import me.elephant1214.nogrief.NoGrief
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

enum class Locale(val id: String) {
    EN_US("en-us");

    fun fromJar(): InputStream = NoGrief.getResource("locales/${this.id}.json")
        ?: error("The locale \"${this.id}\" was not found in the plugin jar. This should not be possible!")

    fun fromDir(): Path = localesDir.resolve("${this.id}.json")

    companion object {
        private val localesDir: Path =
            NoGrief.dataDir.resolve("locales").apply { if (!this@apply.exists()) this@apply.createDirectories() }
    }
}
