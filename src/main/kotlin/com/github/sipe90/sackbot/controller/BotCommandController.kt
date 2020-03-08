package com.github.sipe90.sackbot.controller

import com.github.sipe90.sackbot.service.AudioFileService
import com.github.sipe90.sackbot.service.AudioPlayerService
import com.github.sipe90.sackbot.service.BotService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class BotCommandController(
    private val audioFileService: AudioFileService,
    private val audioPlayerService: AudioPlayerService,
    private val botService: BotService
) {

    // fun getSoundsList(principal: Principal): Flux<String> = audioFileService.getAudioFiles()
}