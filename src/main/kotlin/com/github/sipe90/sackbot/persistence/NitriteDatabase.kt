package com.github.sipe90.sackbot.persistence

import com.github.sipe90.sackbot.config.NitriteConfig
import com.github.sipe90.sackbot.persistence.dto.Member
import org.dizitart.kno2.getRepository
import org.dizitart.kno2.nitrite

class NitriteDatabase(private val config: NitriteConfig) {

    private val db = nitrite {
        path = config.dbFile
        autoCommitBufferSize = 2048
        compress = true
    }

    fun getUserRepository() = db.getRepository<Member>()
}