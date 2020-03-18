package com.github.sipe90.sackbot.exception

import org.springframework.http.HttpStatus

open class BadRequestException(
    override val message: String?,
    override val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST
) : WebException(message)