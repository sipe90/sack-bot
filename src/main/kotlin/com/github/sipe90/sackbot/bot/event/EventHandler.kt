package com.github.sipe90.sackbot.bot.event

import net.dv8tion.jda.api.events.GenericEvent
import reactor.core.publisher.Mono

interface EventHandler<E : GenericEvent> {
    fun handleEvent(event: E): Mono<Unit>
}
