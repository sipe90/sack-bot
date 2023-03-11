package com.github.sipe90.sackbot.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@Configuration
@EnableReactiveMongoRepositories("com.github.sipe90.sackbot.persistence")
class PersistenceConfig
