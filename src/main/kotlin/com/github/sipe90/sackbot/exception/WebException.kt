package com.github.sipe90.sackbot.exception

abstract class WebException(message: String?, cause: Throwable?) : Exception(message, cause) {
    constructor(message: String?) : this(message, null)
}
