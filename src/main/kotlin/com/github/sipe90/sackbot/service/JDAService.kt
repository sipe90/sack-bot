package com.github.sipe90.sackbot.service

import club.minnced.jda.reactor.createManager
import club.minnced.jda.reactor.on
import com.github.sipe90.sackbot.config.BotConfig
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.ShutdownEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
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

        jda = JDABuilder.create(
            config.token,
            GatewayIntent.DIRECT_MESSAGES,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_VOICE_STATES
        )
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .enableCache(CacheFlag.VOICE_STATE)
            .disableCache(CacheFlag.ACTIVITY, CacheFlag.EMOTE, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
            .setEventManager(eventManager)
            .setAudioSendFactory(NativeAudioSendFactory())
            .setStatus(OnlineStatus.DO_NOT_DISTURB)
            .build()
    }

    fun registerCommands(commandData: List<CommandData>) {
        logger.info("Registering global slash commands")
        jda.awaitReady()
            .updateCommands()
            .addCommands(commandData).queue {
                logger.info("Global slash commands registered, updating command privileges")
                jda.retrieveCommands().queue { commands ->
                    val restrictedCommands = commands.filter { cmd -> !cmd.isDefaultEnabled }
                    logger.debug("Restricted commands: {}", restrictedCommands)
                    jda.guilds.forEach { guild ->
                        val ownerId = guild.ownerId
                        val adminRole = getAdminRole(guild.id)

                        val commandPrivileges = mutableListOf(CommandPrivilege.enableUser(ownerId))
                        if (adminRole != null) {
                            commandPrivileges.add(CommandPrivilege.enableRole(adminRole.id))
                        }

                        val commandPrivilegesMap = restrictedCommands.associate { cmd -> cmd.id to commandPrivileges }
                        logger.info("Updating command privileges for guild {}", guild.id)
                        guild.updateCommandPrivileges(commandPrivilegesMap).queue()
                    }
                }
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
        logger.info("Shutting down JDA")
        jda.shutdown()
    }
}

