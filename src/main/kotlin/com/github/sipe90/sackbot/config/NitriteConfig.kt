package com.github.sipe90.sackbot.config

import com.github.sipe90.sackbot.persistence.NitriteDatabase
import com.github.sipe90.sackbot.persistence.dto.Member
import org.dizitart.no2.objects.ObjectRepository
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean

@ConfigurationProperties("sackbot.nitrite")
@ConstructorBinding
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
}