package com.github.sipe90.sackbot.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
open class ServerErrorException(message: String?, cause: Throwable?) : WebException(message, cause) {
    constructor(message: String?) : this(message, null)
}