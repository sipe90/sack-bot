package com.github.sipe90.sackbot.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
open class BadRequestException(message: String?) : WebException(message)
