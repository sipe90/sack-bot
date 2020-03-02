package com.github.sipe90.sackbot.service

import club.minnced.jda.reactor.asMono
import club.minnced.jda.reactor.createManager
import club.minnced.jda.reactor.on
import club.minnced.jda.reactor.toInputStream
import com.github.sipe90.sackbot.config.BotConfig
import com.github.sipe90.sackbot.util.stripExtension
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.Event
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
import reactor.util.function.Tuples
import java.util.EnumSet
import javax.annotation.PreDestroy

@Service
final class BotServiceImpl(
    private val config: BotConfig,
    private val fileService: AudioFileService,
    private val playerService: AudioPlayerService
) : BotService {

    private final val logger = LoggerFactory.getLogger(javaClass)

    private val cmdSplitRegex = Regex("\\s+")
    private val eventManager = createManager()

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
        val cmd = cmdSplitRegex.split(cmdStr)
        val voiceChannel = event.member?.voiceState?.channel

        when (cmd[0]) {
            "help" -> helpCommand(event, event.channel)
            "info" -> infoCommand(cmd, event, event.channel)
            "list" -> listCommand(event.channel)
            "random" -> randomCommand(voiceChannel, event.channel)
            "say" -> sayCommand(cmd, voiceChannel, event.channel)
            "volume" -> volumeCommand(cmd, event.guild, event.channel)
            else -> playFileCommand(cmd, voiceChannel, event.channel)
        }.subscribe()
    }

    private fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {
        if (event.author.isBot || !config.chat.allowDm) return

        val msg = event.message.contentRaw

        event.author.openPrivateChannel().asMono().flatMap {
            logger.debug("Processing private message from {}: {}", event.author, msg)

            if (event.message.attachments.isNotEmpty()) return@flatMap handleUploads(event, it).next()

            if (!msg.startsWith(config.chat.commandPrefix)) {
                return@flatMap helpCommand(event, it)
            }

            val cmdStr = msg.substring(1)
            val cmd = cmdSplitRegex.split(cmdStr)

            val voiceChannel = event.author.mutualGuilds
                .flatMap { guild -> guild.voiceChannels }
                .find { vc -> vc.members.map { m -> m.id }.contains(event.author.id) }

            return@flatMap when (cmd[0]) {
                "help" -> helpCommand(event, it)
                "info" -> infoCommand(cmd, event, it)
                "list" -> listCommand(it)
                "random" -> randomCommand(voiceChannel, event.channel)
                "say" -> sayCommand(cmd, voiceChannel, it)
                "volume" -> volumeCommand(cmd, voiceChannel?.guild, it)
                else -> playFileCommand(cmd, voiceChannel, it)
            }
        }.subscribe()
    }

    private fun handleUploads(event: PrivateMessageReceivedEvent, channel: MessageChannel): Flux<Message> {
        return event.message.attachments.toFlux().flatMap map@{ attachment ->
            logger.info("Processing attachment {}", attachment.fileName)
            if (attachment.fileExtension != "wav" && attachment.fileExtension != "mp3") return@map channel.sendMessage("Could not upload file `${attachment.fileName}`: Unknown extension. Only mp3 and wav are supported.")
                .asMono()
            if (attachment.size > 1_000_000) return@map channel.sendMessage("Could not upload file `${attachment.fileName}`: File size cannot exceed 1MB")
                .asMono()

            val audioName = stripExtension(attachment.fileName)

            return@map fileService.getAudioFilePathByName(attachment.fileName)
                .hasElement()
                .flatMap { exists ->
                    if (exists) return@flatMap channel.sendMessage("File `${audioName}` already exists")
                        .asMono()
                    return@flatMap attachment.toInputStream()
                        .flatMap { stream -> fileService.saveAudioFile(attachment.fileName, stream) }
                        .flatMap { channel.sendMessage("Saved audio file `${audioName}`").asMono() }
                }
        }
    }

    private fun helpCommand(event: Event, channel: MessageChannel): Mono<Message> {
        return Mono.fromCallable {
            StringBuilder().append("The following chat commands are available:\n")
                .append("```")
                .append(helpLine("help", "Prints this text"))
                //.append(helpLine("info", "N/A"))
                .append(helpLine("list", "Lists all playable sound names"))
                .append(helpLine("random", "Play a random sound"))
                .append(helpLine("say <text>", "Text to speech"))
                .append(helpLine("volume <1-100>", "Set sound playback volume"))
                .append(helpLine("<sound_name>", "Plays a sound with the given name"))
                .append("```\n")
                .append("You can also send me new audio files via a private message.")
        }.flatMap { channel.sendMessage(it).asMono() }
    }

    private fun helpLine(cmd: String, text: String): StringBuilder {
        return StringBuilder("\n")
            .append(config.chat.commandPrefix)
            .append(cmd)
            .append("                -- ".substring(cmd.length - 1))
            .append(text)
    }

    private fun volumeCommand(
        cmd: List<String>,
        guild: Guild?,
        channel: MessageChannel
    ): Mono<Message> {
        if (cmd.size != 2) return channel.sendMessage("Invalid volume command. Correct format is `${config.chat.commandPrefix}volume <1-100>`")
            .asMono()
        if (guild == null) return channel.sendMessage("Could not find the bot you are looking for").asMono()
        val volumeStr = cmd[1]

        return try {
            val volume = volumeStr.toInt().coerceAtLeast(1).coerceAtMost(100)
            playerService.setVolume(guild.id, volume)
            channel.sendMessage("Setting volume to `$volume%`").asMono()
        } catch (e: NumberFormatException) {
            channel.sendMessage("Could not parse volume").asMono()
        }
    }

    private fun playFileCommand(
        cmd: List<String>,
        voiceChannel: VoiceChannel?,
        channel: MessageChannel
    ): Mono<Message> {
        if (cmd.size != 1) {
            return channel.sendMessage("Invalid play command. Correct format is `${config.chat.commandPrefix}<soundName>`")
                .asMono()
        }

        if (voiceChannel == null) return channel.sendMessage("Could not find a voice channel to play in").asMono()

        val audioFileName = cmd[0]

        return fileService.getAudioFilePathByName(audioFileName).flatMap {
            playerService.playInChannel(it.toString(), voiceChannel)
            channel.sendMessage("Playing sound file `$audioFileName` in voice channel `#${voiceChannel.name}`").asMono()
        }.switchIfEmpty(channel.sendMessage("Could not find a sound file with given name").asMono())
    }

    private fun sayCommand(
        cmd: List<String>,
        voiceChannel: VoiceChannel?,
        channel: MessageChannel
    ): Mono<Message> {
        if (cmd.size < 2) {
            return channel.sendMessage("Invalid say command. Correct format is `${config.chat.commandPrefix}say <text>`")
                .asMono()
        }

        if (voiceChannel == null) return channel.sendMessage("Could not find a voice channel to play in").asMono()

        val text = cmd.subList(1, cmd.size).joinToString(" ")

        playerService.playTtsInChannel(text, voiceChannel)
        return channel.sendMessage("Playing text to speech in voice channel `#${voiceChannel.name}`").asMono()
    }

    private fun listCommand(
        channel: MessageChannel
    ): Mono<Message> {
        return Mono.just("List of available sounds to play:\n```")
            .concatWith(fileService.getAudioFiles().map { "\n$it" })
            .concatWith(Mono.just("\n```"))
            .reduce(StringBuilder(), { sb, str -> sb.append(str) })
            .flatMap { channel.sendMessage(it).asMono() }
    }

    private fun randomCommand(voiceChannel: VoiceChannel?, channel: MessageChannel): Mono<Message> {
        if (voiceChannel == null) return channel.sendMessage("Could not find a voice channel to play in").asMono()
        val paths = fileService.getAudioFiles()
        return paths
            .count()
            .map { (0..it).random() }
            .flatMap { paths.take(it).last() }
            .flatMap { fileService.getAudioFilePathByName(it).map { p -> Tuples.of(it, p) } }
            .flatMap {
                playerService.playInChannel(it.t2.toString(), voiceChannel)
                channel.sendMessage("Playing random sound file `${it.t1}` in voice channel `#${voiceChannel.name}`")
                    .asMono()
            }
    }

    private fun infoCommand(
        cmd: List<String>,
        event: Event,
        channel: MessageChannel
    ): Mono<Message> {
        return channel.sendMessage("Not implemented").asMono()
    }

    @PreDestroy
    fun cleanUp() {
        logger.info("Shutting down JDA")
        jda.shutdown()
    }
}

