package com.github.sipe90.sackbot.audio

import com.sedmelluq.discord.lavaplayer.tools.io.ExtendedBufferedInputStream
import com.sedmelluq.discord.lavaplayer.tools.io.SeekableInputStream
import com.sedmelluq.discord.lavaplayer.track.info.AudioTrackInfoProvider
import java.io.ByteArrayInputStream
import java.io.IOException

class SeekableBufferedInputStream(bytes: ByteArray) : SeekableInputStream(bytes.size.toLong(), 0) {

    private val bufferedStream = ExtendedBufferedInputStream(ByteArrayInputStream(bytes))

    private var position = 0

    init {
        bufferedStream.mark(0)
    }

    override fun read(): Int {
        val result: Int = bufferedStream.read()
        if (result >= 0) {
            position++
        }
        return result
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val read: Int = bufferedStream.read(b, off, len)
        position += read
        return read
    }

    override fun skip(n: Long): Long {
        val skipped = bufferedStream.skip(n).toInt()
        position += skipped
        return skipped.toLong()
    }

    override fun available() = bufferedStream.available()

    override fun mark(readlimit: Int) {
        throw IOException("mark/reset not supported")
    }

    override fun reset() {
        throw IOException("mark/reset not supported")
    }

    override fun markSupported() = false

    override fun close() {
        try {
            bufferedStream.close()
        } catch (e: IOException) {

        }
    }

    override fun getPosition() = position.toLong()

    override fun canSeekHard() = true

    override fun seekHard(pos: Long) {
        if (position == pos.toInt()) return
        if (position < pos) {
            skip(pos - position)
            return
        }
        bufferedStream.reset()
        bufferedStream.discardBuffer()
        skip(pos)
    }

    override fun getTrackInfoProviders() = emptyList<AudioTrackInfoProvider>()
}