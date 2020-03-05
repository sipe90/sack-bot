package com.github.sipe90.sackbot

class SackException(msg: String?, cause: Throwable?) : Exception(msg, cause) {
    constructor(msg: String) : this(msg, null)
    constructor() : this(null, null)
}

