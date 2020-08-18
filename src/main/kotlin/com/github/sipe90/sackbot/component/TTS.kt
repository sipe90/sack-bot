package com.github.sipe90.sackbot.component

import com.github.sipe90.sackbot.config.BotConfig
import com.github.sipe90.sackbot.exception.ValidationException
import marytts.LocalMaryInterface
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioSystem

@Component
class TTS(private val config: BotConfig) {

    private final val logger = LoggerFactory.getLogger(javaClass)

    private final val maryTts = LocalMaryInterface()

    private final val phrases = ArrayList<String>()

    init {
        val phrasesFile = config.tts.phrasesFile

        if (StringUtils.isNotEmpty(phrasesFile)) {
            val path = Paths.get(phrasesFile)
            logger.info("Loading phrases from {}", phrasesFile)
            if (Files.isRegularFile(path) && Files.isReadable(path)) {
                try {
                    phrases.addAll(Files.readAllLines(path, StandardCharsets.UTF_8))
                } catch (e: IOException) {
                    logger.error("Failed to read phrases from file", e)
                }
            } else {
                logger.error("Could not load phrases. Check the that the file exists and it is readable.")
            }
        }
    }

    fun isEnabled() = true

    fun getAvailableVoices(): MutableSet<String> = maryTts.availableVoices

    fun isRandomEnabled() = phrases.isNotEmpty()

    fun getMaxTextLength() = config.tts.maxLength

    fun randomPhraseToSpeech(voice: String): Mono<ByteArray> {
        if (phrases.isEmpty()) return Mono.empty()
        return textToSpeech(voice, phrases[(0 until phrases.size).random()])
    }

    fun textToSpeech(voice: String, text: String): Mono<ByteArray> {
        if (!getAvailableVoices().contains(voice)) {
            throw ValidationException("Invalid voice: $voice")
        }

        val txt = if (text.length > config.tts.maxLength) text.slice(0 until config.tts.maxLength) else text

        logger.debug("Converting text to speech using voice {}: {}", voice, txt)
        maryTts.voice = voice

        return maryTts.generateAudio(txt).toMono()
                .map {
                    val baos = ByteArrayOutputStream()
                    AudioSystem.write(it, AudioFileFormat.Type.WAVE, baos)
                    baos.toByteArray()
                }
    }
}