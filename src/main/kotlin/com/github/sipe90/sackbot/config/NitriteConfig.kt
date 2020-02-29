package com.github.sipe90.sackbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("sackbot.nitrite")
@ConstructorBinding
data class NitriteConfig(val dbFile: String)