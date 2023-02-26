package com.github.sipe90.sackbot.service

import club.minnced.jda.reactor.createManager
import club.minnced.jda.reactor.on
import club.minnced.jda.reactor.toFlux
import com.github.sipe90.sackbot.config.BotConfig
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.events.session.ShutdownEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import java.time.Duration

@Service
class JDAService(
    @Value("\${sackbot.environment}")
    private val environment: String,
    private val config: BotConfig,
) {

    private val logger = KotlinLogging.logger {}

    final val eventManager = createManager()

    private lateinit var jda: JDA

    @PostConstruct
    private fun init() {
        eventManager.on<ReadyEvent>()
            .next()
            .map { it.jda }
            .doOnSuccess {
                it.presence.setStatus(OnlineStatus.ONLINE)
                it.presence.activity = Activity.of(config.activity.getDiscordType(), config.activity.text)
                logger.info { "Sackbot is ready to meme" }
            }
            .subscribe()

        eventManager.on<ShutdownEvent>()
            .subscribe {
                it.jda.httpClient.connectionPool.evictAll()
            }

        jda = JDABuilder.create(
            config.token,
            GatewayIntent.DIRECT_MESSAGES,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_VOICE_STATES,
        )
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .enableCache(CacheFlag.VOICE_STATE)
            .disableCache(
                CacheFlag.ACTIVITY,
                CacheFlag.EMOJI,
                CacheFlag.STICKER,
                CacheFlag.CLIENT_STATUS,
                CacheFlag.ONLINE_STATUS,
                CacheFlag.SCHEDULED_EVENTS,
            )
            .setEventManager(eventManager)
            .setAudioSendFactory(NativeAudioSendFactory())
            .setStatus(OnlineStatus.DO_NOT_DISTURB)
            .build()
    }

    fun registerCommands(commandData: List<CommandData>) {
        if (environment == "development") {
            registerGuildCommands(commandData).subscribe()
        } else {
            registerGlobalCommands(commandData).subscribe()
        }
    }

    private fun registerGlobalCommands(commandData: List<CommandData>): Flux<Command> {
        logger.info { "Updating global slash commands" }
        return jda.awaitReady()
            .updateCommands()
            .addCommands(commandData).toFlux().doOnComplete {
                logger.info { "Global slash commands updated" }
            }
    }

    private fun registerGuildCommands(commandData: List<CommandData>): Flux<Command> {
        logger.info("Updating guild slash commands")
        return jda.awaitReady().guilds.toFlux().flatMap { guild ->
            logger.info { "Updating guild slash commands for guild ${guild.name} (${guild.id})" }
            guild.updateCommands().addCommands(commandData).toFlux()
                .onErrorResume {
                    logger.error(it) { "Failed to update command for guild ${guild.name} (${guild.id})" }
                    Mono.empty()
                }
                .doOnComplete { logger.info { "Updated commands for guild ${guild.name} (${guild.id})" } }
        }
    }

    fun getUser(userId: String): User? = jda.getUserById(userId)

    fun getGuild(guildId: String): Guild? = jda.getGuildById(guildId)

    fun getMutualGuilds(userId: String): List<Guild> = jda.getMutualGuilds(jda.getUserById(userId))

    fun isMutualGuild(guildId: String, userId: String): Boolean =
        getMutualGuilds(userId).any { it.id == guildId }

    fun getAdminRole(guildId: String): Role? {
        val guild = getGuild(guildId) ?: throw IllegalArgumentException("Invalid guild id")
        return getAdminRole(guild)
    }

    fun getAdminRole(guild: Guild): Role? {
        if (config.adminRole == null) return null
        return guild.getRolesByName(config.adminRole, false).first()
    }

    fun hasAdminAccess(userId: String, guildId: String): Boolean {
        val user = getUser(userId) ?: throw IllegalArgumentException("Invalid user id")
        val guild = getGuild(guildId) ?: throw IllegalArgumentException("Invalid guild id")
        return hasAdminAccess(user, guild)
    }

    fun hasAdminAccess(user: User, guild: Guild): Boolean {
        val adminRole = getAdminRole(guild)

        if (guild.ownerId == user.id) return true
        if (adminRole == null) return false

        val member = guild.members.first { member -> member.user.id == user.id } ?: return false

        return member.roles.any { role -> role.id == adminRole.id }
    }

    @PreDestroy
    fun cleanUp() {
        logger.info { "Shutting down JDA" }
        jda.shutdown()
        if (!jda.awaitShutdown(Duration.ofSeconds(10))) {
            logger.warn { "JDA shutdown taking too long, forcing shutdown." }
            jda.shutdownNow()
        }
    }
}
