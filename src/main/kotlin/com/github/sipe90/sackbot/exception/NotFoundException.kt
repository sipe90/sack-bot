package com.github.sipe90.sackbot.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class NotFoundException(message: String) : BadRequestException(message)
