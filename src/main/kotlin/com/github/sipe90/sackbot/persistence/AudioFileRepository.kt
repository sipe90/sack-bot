package com.github.sipe90.sackbot.persistence

import com.github.sipe90.sackbot.persistence.dto.AudioFile
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface AudioFileRepository : ReactiveMongoRepository<AudioFile, String>, AudioFileRepositoryExtension
