package com.github.sipe90.sackbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("sackbot.files")
@ConstructorBinding
data class FilesConfig(
        val folder: String,
        val watcher: WatcherProps) {

    data class WatcherProps(val enabled: Boolean)
}