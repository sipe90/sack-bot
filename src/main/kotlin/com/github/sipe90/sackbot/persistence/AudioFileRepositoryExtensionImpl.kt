package com.github.sipe90.sackbot.persistence

import com.github.sipe90.sackbot.persistence.dto.AudioFile
import com.github.sipe90.sackbot.persistence.dto.LightAudioFile
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class AudioFileRepositoryExtensionImpl(private val template: ReactiveMongoTemplate) : AudioFileRepositoryExtension {

    override fun getAllAudioFilesWithoutData(guildId: String): Flux<LightAudioFile> {
        val query = allAudioFilesQuery(guildId)
        query.fields().exclude("data")
        return template.find(query, "audio_files")
    }

    override fun audioFileExists(guildId: String, name: String): Mono<Boolean> {
        return template.exists(singleAudioFileQuery(guildId, name), AudioFile::class.java)
    }

    override fun findAudioFile(guildId: String, name: String): Mono<AudioFile> {
        return template.findOne(singleAudioFileQuery(guildId, name), AudioFile::class.java)
    }

    override fun findAllAudioFiles(guildId: String): Flux<AudioFile> {
        return template.find(allAudioFilesQuery(guildId))
    }

    override fun findAllAudioFilesByTags(guildId: String, tags: Set<String>): Flux<AudioFile> {
        if (tags.isEmpty()) {
            return findAllAudioFiles(guildId)
        }
        var criteria = Criteria.where("tags").`is`(tags.first())
        tags.drop(1).forEach {
            criteria = criteria.and("tags").`is`(it)
        }

        return template.find(allAudioFilesQuery(guildId).addCriteria(criteria))
    }

    override fun deleteAudioFile(guildId: String, name: String): Mono<Boolean> {
        return template.findAndRemove(singleAudioFileQuery(guildId, name), AudioFile::class.java).map { true }
    }

    private fun allAudioFilesQuery(guildId: String): Query = Query(Criteria.where("guildId").`is`(guildId))

    private fun singleAudioFileQuery(guildId: String, name: String): Query = Query(Criteria.where("guildId").`is`(guildId).and("name").`is`(name))
}
