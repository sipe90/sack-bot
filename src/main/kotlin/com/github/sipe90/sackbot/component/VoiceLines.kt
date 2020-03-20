package com.github.sipe90.sackbot.component

import com.github.sipe90.sackbot.config.BotConfig
import com.github.sipe90.sackbot.util.stripExtension
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList

@Component
class VoiceLines(private val config: BotConfig) {

    private final val voiceLines: Map<String, Map<String, Path>>

    init {
        voiceLines = config.voices.filter { it.value.enabled }
            .mapValues {
                getVoiceFiles(it.value.path)
                    .associateBy { file -> stripExtension(file.fileName.toString()) }
            }
    }

    fun voiceIsAvailable(voice: String): Boolean = config.voices[voice]?.enabled == true

    fun getVoices(): Set<String> = config.voices.filterValues { it.enabled }.keys

    fun getVoiceLines(voice: String): Set<String> {
        if (!voiceIsAvailable(voice)) throw IllegalArgumentException("Invalid voice")
        return voiceLines[voice]!!.keys
    }

    fun getPaths(voice: String, lines: List<String>): List<Path> {
        val voiceConfig = config.voices[voice] ?: throw IllegalArgumentException("Invalid voice")
        if (!voiceConfig.enabled) throw IllegalArgumentException("Voice is disabled")

        return lines.map { voiceConfig.substitutions.getOrDefault(it, it) }
            .mapNotNull { voiceLines[voice]?.get(it) }
    }

    private fun getVoiceFiles(folder: String): List<Path> = Files.list(Paths.get(folder)).toList()
}