package com.github.sipe90.sackbot.service

import club.minnced.jda.reactor.createManager
import club.minnced.jda.reactor.on
import com.github.sipe90.sackbot.config.BotConfig
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.ShutdownEvent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.EnumSet
import javax.annotation.PreDestroy

@Service
class JDAService(
    private val config: BotConfig
) {

    private final val logger = LoggerFactory.getLogger(javaClass)

    final val eventManager = createManager()

    private lateinit var jda: JDA

    init {
        eventManager.on<ReadyEvent>()
            .next()
            .map { it.jda }
            .doOnSuccess {
                it.presence.setStatus(OnlineStatus.ONLINE)
                it.presence.activity = Activity.of(config.activity.getDiscordType(), config.activity.text)
                logger.info("Sackbot is ready to meme")
            }
            .subscribe()

        eventManager.on<ShutdownEvent>()
            .subscribe {
                it.jda.httpClient.connectionPool().evictAll()
            }

        jda = JDABuilder(config.token)
            .setEventManager(eventManager)
            .setAudioSendFactory(NativeAudioSendFactory())
            .setStatus(OnlineStatus.DO_NOT_DISTURB)
            .setEnabledCacheFlags(EnumSet.of(CacheFlag.VOICE_STATE))
            .build()
    }

    fun getMutualGuilds(userId: String) = jda.getMutualGuilds(jda.getUserById(userId))

    fun isMutualGuild(guildId: String, userId: String) =
        getMutualGuilds(userId).any { it.id == guildId }

    @PreDestroy
    fun cleanUp() {
        logger.info("Shutting down JDA")
        jda.shutdown()
    }
}

