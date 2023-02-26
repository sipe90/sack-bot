package com.github.sipe90.sackbot.persistence

import com.github.sipe90.sackbot.config.NitriteConfig
import com.github.sipe90.sackbot.persistence.dto.AudioFile
import com.github.sipe90.sackbot.persistence.dto.Member
import com.github.sipe90.sackbot.util.createParentDirs
import org.dizitart.kno2.getRepository
import org.dizitart.no2.Nitrite

class NitriteDatabase(config: NitriteConfig) {

    private val db: Nitrite

    init {
        createParentDirs(config.dbFile)

        db = Nitrite.builder()
            .compressed()
            .filePath(config.dbFile)
            .autoCommitBufferSize(2048)
            .enableOffHeapStorage()
            .openOrCreate()
    }

    fun getUserRepository() = db.getRepository<Member>()

    fun getAudioFileRepository() = db.getRepository<AudioFile>()

    fun close() {
        db.close()
    }
}
