package com.github.sipe90.sackbot.component

import club.minnced.jda.reactor.toMono
import com.github.sipe90.sackbot.config.BotConfig
import marytts.LocalMaryInterface
import marytts.MaryInterface
import marytts.util.data.audio.MaryAudioUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Locale

@Component
class TTS(config: BotConfig) {

    private final val logger = LoggerFactory.getLogger(javaClass)

    private final val maryTts: MaryInterface = LocalMaryInterface()

    private final val phrases = ArrayList<String>()

    init {
        val voice = config.tts.voice
        val phrasesFile = config.tts.phrasesFile

        maryTts.locale = Locale.UK

        if (StringUtils.isNotEmpty(voice)) {
            if (!maryTts.availableVoices.contains(voice)) {
                logger.warn("Invalid voice name given: {}. Available voices are: {}", voice, maryTts.availableVoices)
                logger.info("Using default voice: {}", maryTts.voice)
            } else {
                maryTts.voice = voice
            }
        } else {
            logger.info("Using default voice: {}", maryTts.voice)
        }

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

    fun randomPhraseToSpeech(): Mono<Path> {
        if (phrases.isEmpty()) return Mono.empty()
        return textToSpeech(phrases[(0 until phrases.size).random()])
    }

    fun textToSpeech(text: String): Mono<Path> {
        logger.debug("Converting text to speech: {}", text)
        return Mono.fromCallable { Files.createTempFile("t2s_", ".wav") }
            .zipWith(maryTts.generateAudio(text).toMono())
            .map {
                MaryAudioUtils.writeWavFile(
                    MaryAudioUtils.getSamplesAsDoubleArray(it.t2),
                    it.t1.toString(),
                    it.t2.format
                )
                return@map it.t1
            }
    }
}