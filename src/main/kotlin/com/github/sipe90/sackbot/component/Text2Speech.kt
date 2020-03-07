package com.github.sipe90.sackbot.component

import club.minnced.jda.reactor.toMono
import marytts.LocalMaryInterface
import marytts.MaryInterface
import marytts.util.data.audio.MaryAudioUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale

@Component
class Text2Speech {

    private final val logger = LoggerFactory.getLogger(javaClass)

    private final val maryTts: MaryInterface = LocalMaryInterface()

    init {
        maryTts.locale = Locale.UK
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