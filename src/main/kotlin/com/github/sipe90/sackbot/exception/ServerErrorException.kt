package com.github.sipe90.sackbot.exception

import org.springframework.http.HttpStatus

open class ServerErrorException(
    override val message: String?,
    override val httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    override val cause: Throwable?
) : WebException(message, cause) {
    constructor(message: String?) : this(message, HttpStatus.INTERNAL_SERVER_ERROR, null)
}