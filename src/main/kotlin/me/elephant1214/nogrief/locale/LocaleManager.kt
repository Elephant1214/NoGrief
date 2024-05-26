package me.elephant1214.nogrief.locale

import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import me.elephant1214.nogrief.NoGrief
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import java.io.InputStream
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

private typealias LocaleMessages = LinkedHashMap<String, String>

object LocaleManager {
    private var _locale: Locale = NoGrief.cfg.locale
    private val _messages = LocaleMessages()

    fun get(message: String, vararg placeholders: TagResolver): Component =
        NoGrief.MINI_MESSAGE.deserialize(this._messages[message]!!, *placeholders)

    fun changeLocale(new: Locale) {
        this._locale = new
        NoGrief.cfg.locale = this._locale
        NoGrief.saveCfg()
        this.reload()
    }

    fun reload() {
        this._messages.clear()
        this.loadMessages()
    }

    fun loadMessages() {
        val locale = this._locale.fromDir()
        if (!locale.exists()) {
            val jarLocale = this._locale.fromJar()
            locale.createFile()
            jarLocale.copyTo(locale.outputStream())
        }
        this._messages.putAll(decodeMessages(locale.inputStream()))
    }

    private fun decodeMessages(stream: InputStream): Map<String, String> =
        NoGrief.PRETTY_JSON.decodeFromStream(MapSerializer(String.serializer(), String.serializer()), stream)

    fun saveMessages() {
        val locale = this._locale.fromDir()
        if (!locale.exists()) {
            val jarLocale = this._locale.fromJar()
            locale.createFile()
            jarLocale.copyTo(locale.outputStream())
        }

        NoGrief.PRETTY_JSON.encodeToStream(this._messages, locale.outputStream())
    }
}