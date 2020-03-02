package com.github.sipe90.sackbot.component

import marytts.LocalMaryInterface
import marytts.MaryInterface
import marytts.util.data.audio.MaryAudioUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
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

    // TODO: Remove blocking
    fun textToSpeech(text: String): Path {
        logger.debug("Converting text to speech: {}", text)
        val audio = maryTts.generateAudio(text)
        val tempFile = Files.createTempFile("t2s_", ".wav")
        logger.debug("Created temp file {}", tempFile)
        MaryAudioUtils.writeWavFile(MaryAudioUtils.getSamplesAsDoubleArray(audio), tempFile.toString(), audio.format)
        return tempFile
    }
}