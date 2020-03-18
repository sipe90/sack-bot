package com.github.sipe90.sackbot.exception

import org.springframework.http.HttpStatus

abstract class WebException(override val message: String?, override val cause: Throwable?) : Exception(message, cause) {
    constructor(message: String?) : this(message, null)

    abstract val httpStatus: HttpStatus
}