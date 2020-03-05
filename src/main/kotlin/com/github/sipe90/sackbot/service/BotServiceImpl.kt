package com.github.sipe90.sackbot.service

import club.minnced.jda.reactor.asMono
import club.minnced.jda.reactor.createManager
import club.minnced.jda.reactor.on
import club.minnced.jda.reactor.toInputStream
import club.minnced.jda.reactor.toMono
import com.github.sipe90.sackbot.SackException
import com.github.sipe90.sackbot.bot.BotCommand
import com.github.sipe90.sackbot.config.BotConfig
import com.github.sipe90.sackbot.util.stripExtension
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.ShutdownEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import java.util.EnumSet
import javax.annotation.PreDestroy

@Service
final class BotServiceImpl(
    private val config: BotConfig,
    private val fileService: AudioFileService,
    commands: List<BotCommand>
) : BotService {

    private final val logger = LoggerFactory.getLogger(javaClass)

    private val cmdSplitRegex = Regex("\\s+")
    private val eventManager = createManager()

    private val commandsMap = commands.associateBy(BotCommand::commandPrefix)
    private val helpCommand = commandsMap["help"] ?: throw SackException("Help command not found")
    private val playCommand = commandsMap[""] ?: throw SackException("Play command not found")

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

        eventManager.on<GuildMessageReceivedEvent>()
            .subscribe(this::onGuildMessageReceived)

        eventManager.on<PrivateMessageReceivedEvent>()
            .subscribe(this::onPrivateMessageReceived)

        jda = JDABuilder(config.token)
            .setEventManager(eventManager)
            .setStatus(OnlineStatus.DO_NOT_DISTURB)
            .setEnabledCacheFlags(EnumSet.of(CacheFlag.VOICE_STATE))
            .build()
    }

    private fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.author.isBot) return

        val msg = event.message.contentRaw

        if (!msg.startsWith(config.chat.commandPrefix)) return

        logger.debug("Processing guild message from {}: {}", event.author, msg)

        val cmdStr = msg.substring(1)
        val cmd = cmdSplitRegex.split(cmdStr).toTypedArray()

        logger.trace("Command: {}", cmd)

        val botCommand = commandsMap[cmd[0]] ?: playCommand
        if (botCommand.canProcess(*cmd)) {
            logger.trace("BotCommand {} processing command {}", botCommand.javaClass, cmd)
            botCommand.process(event, *cmd).log().subscribe { event.channel.sendMessage(it) }
        }
    }

    private fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {
        if (event.author.isBot || !config.chat.allowDm) return

        val msg = event.message.contentRaw

        event.author.openPrivateChannel().asMono().zipWith(Mono.defer<String> {
            logger.debug("Processing private message from {}: {}", event.author, msg)

            if (event.message.attachments.isNotEmpty()) return@defer handleUploads(event).next()

            if (!msg.startsWith(config.chat.commandPrefix)) {
                return@defer helpCommand.process(event)
            }

            val cmdStr = msg.substring(1)
            val cmd = cmdSplitRegex.split(cmdStr).toTypedArray()

            logger.trace("Command: {}", cmd)

            val botCommand = commandsMap[cmd[0]] ?: playCommand
            if (botCommand.canProcess(*cmd)) {
                logger.trace("BotCommand {} processing command {}", botCommand.javaClass, cmd)
                return@defer botCommand.process(event, *cmd)
            }
            return@defer Mono.empty<String>()
        }).log().subscribe { it.t1.sendMessage(it.t2).queue() }
    }

    private fun handleUploads(event: PrivateMessageReceivedEvent): Flux<String> {
        return event.message.attachments.toFlux().flatMap map@{ attachment ->
            logger.info("Processing attachment {}", attachment.fileName)

            if (attachment.fileExtension != "wav" && attachment.fileExtension != "mp3") {
                return@map "Could not upload file `${attachment.fileName}`: Unknown extension. Only mp3 and wav are supported.".toMono()
            }
            if (attachment.size > 1_000_000) {
                return@map "Could not upload file `${attachment.fileName}`: File size cannot exceed 1MB".toMono()
            }

            val audioName = stripExtension(attachment.fileName)

            return@map fileService.getAudioFilePathByName(attachment.fileName)
                .hasElement()
                .flatMap { exists ->
                    if (exists) return@flatMap "File `${audioName}` already exists".toMono()

                    return@flatMap attachment.toInputStream()
                        .flatMap { stream -> fileService.saveAudioFile(attachment.fileName, stream) }
                        .flatMap { "Saved audio file `${audioName}`".toMono() }
                }
        }
    }

    @PreDestroy
    fun cleanUp() {
        logger.info("Shutting down JDA")
        jda.shutdown()
    }
}

