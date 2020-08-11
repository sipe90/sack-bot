package com.github.sipe90.sackbot.audio

import com.sedmelluq.discord.lavaplayer.tools.io.NonSeekableInputStream
import java.io.ByteArrayInputStream

class ByteArraySeekableInputStream(data: ByteArray) : NonSeekableInputStream(ByteArrayInputStream(data))