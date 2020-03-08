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
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Locale

@Component
class Text2Speech(config: BotConfig) {

    private final val logger = LoggerFactory.getLogger(javaClass)

    private final val maryTts: MaryInterface = LocalMaryInterface()

    private final val phrases = ArrayList<String>()

    init {
        maryTts.locale = Locale.UK

        val phrasesFile = config.tts.phrasesFile
        if (StringUtils.isNotEmpty(phrasesFile)) {
            val path = Paths.get(phrasesFile)
            if (Files.isRegularFile(path) && Files.isReadable(path)) {
                phrases.addAll(Files.readAllLines(path, StandardCharsets.UTF_8))
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