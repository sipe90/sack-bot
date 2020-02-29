package com.github.sipe90.sackbot.persistence

import org.dizitart.kno2.nitrite
import com.github.sipe90.sackbot.config.NitriteConfig

class NitriteDatabase(private val config: NitriteConfig) {

    val db = nitrite {
        path = config.dbFile
        compress = true
    }
}