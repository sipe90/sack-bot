package com.github.sipe90.sackbot.component

import com.github.sipe90.sackbot.config.BotConfig
import com.github.sipe90.sackbot.util.stripExtension
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.streams.toList

@Component
class VoiceLines(private val config: BotConfig) {

    private final val voiceLines: Map<String, Map<String, Path>>

    init {
        voiceLines = if (config.voice.enabled) {
            config.voice.voices
                    .mapValues {
                        getVoiceFiles(it.value.path)
                                .associateByTo(TreeMap()) { file -> stripExtension(file.fileName.toString()) }
                    }
        } else {
            emptyMap()
        }
    }

    fun isEnabled(): Boolean = config.voice.enabled

    fun getVoices(): Set<String> = config.voice.voices.keys

    fun getVoiceLines(): Map<String, Set<String>> {
        return voiceLines.mapValues { it.value.keys }
    }

    fun getVoiceLines(voice: String): Set<String> {
        return (voiceLines[voice] ?: throw IllegalArgumentException("Invalid voice")).keys
    }

    fun getPaths(voice: String, lines: List<String>): List<Path> {
        val voiceConfig = config.voice.voices[voice] ?: throw IllegalArgumentException("Invalid voice")
        return lines.map { voiceConfig.substitutions?.getOrDefault(it, it) ?: it }
                .mapNotNull { voiceLines[voice]?.get(it) }
    }

    private fun getVoiceFiles(folder: String): List<Path> = Files.list(Paths.get(folder)).toList()
}