package chat.sphinx.wrapper.chat

@Suppress("NOTHING_TO_INLINE")
inline fun String.toChatAlias(): ChatAlias? =
    try {
        ChatAlias(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@JvmInline
value class ChatAlias(val value: String) {
    init {
        require(value.isNotEmpty()) {
            "ChatAlias cannot be empty"
        }
    }
}
