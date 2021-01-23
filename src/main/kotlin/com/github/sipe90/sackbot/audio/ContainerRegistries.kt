package com.github.sipe90.sackbot.audio

import com.sedmelluq.discord.lavaplayer.container.MediaContainer
import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry

object ContainerRegistries {
    val audio = MediaContainerRegistry(
        listOf(
            MediaContainer.MP3.probe,
            MediaContainer.OGG.probe,
            MediaContainer.WAV.probe
        )
    )
}