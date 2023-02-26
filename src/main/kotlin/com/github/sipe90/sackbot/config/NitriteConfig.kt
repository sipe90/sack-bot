package com.github.sipe90.sackbot.config

import com.github.sipe90.sackbot.persistence.NitriteDatabase
import com.github.sipe90.sackbot.persistence.dto.AudioFile
import com.github.sipe90.sackbot.persistence.dto.Member
import org.dizitart.no2.objects.ObjectRepository
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import javax.annotation.PreDestroy

@ConfigurationProperties("sackbot.nitrite")
data class NitriteConfig(val dbFile: String) {

    private val db = NitriteDatabase(this)

    @Bean
    fun getNitrate(): NitriteDatabase {
        return db
    }

    @Bean
    fun getMemberRepository(): ObjectRepository<Member> {
        return db.getUserRepository()
    }

    @Bean
    fun getAudioFileRepository(): ObjectRepository<AudioFile> {
        return db.getAudioFileRepository()
    }

    @PreDestroy
    fun closeDb() {
        db.close()
    }
}
