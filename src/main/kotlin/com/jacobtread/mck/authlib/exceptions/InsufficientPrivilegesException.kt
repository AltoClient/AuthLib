package com.jacobtread.mck.authlib.exceptions

open class InsufficientPrivilegesException : AuthException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}