package chat.sphinx.wrapper.relay

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toAuthorizationToken(): AuthorizationToken? =
    try {
        AuthorizationToken(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@JvmInline
value class AuthorizationToken(val value: String) {

    companion object {
        const val AUTHORIZATION_HEADER = "X-User-Token"
    }

    init {
        require(value.isNotEmpty()) {
            "AuthorizationToken cannot be empty"
        }
    }
}
