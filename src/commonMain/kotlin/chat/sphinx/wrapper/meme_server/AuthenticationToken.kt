package chat.sphinx.wrapper.meme_server

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toAuthenticationToken(): AuthenticationToken? =
    try {
        AuthenticationToken(this)
    } catch (e: IllegalArgumentException) {
        null
    }

inline val AuthenticationToken.headerKey: String
    get() = AuthenticationToken.HEADER_KEY

inline val AuthenticationToken.headerValue: String
    get() = "Bearer $value"

@JvmInline
value class AuthenticationToken(val value: String) {

    companion object {
        const val HEADER_KEY = "Authorization"
    }

    init {
        require(value.isNotEmpty()) {
            "AuthenticationToken cannot be empty"
        }
    }
}
