package com.github.sipe90.sackbot.audio

import com.sedmelluq.discord.lavaplayer.container.MediaContainer
import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry

object ContainerRegistries {

    val all = MediaContainerRegistry.DEFAULT_REGISTRY

    val audio = MediaContainerRegistry(
            listOf(
                    MediaContainer.MP3.probe,
                    MediaContainer.OGG.probe,
                    MediaContainer.WAV.probe
            ))

    val wav = MediaContainerRegistry(listOf(MediaContainer.WAV.probe))
}