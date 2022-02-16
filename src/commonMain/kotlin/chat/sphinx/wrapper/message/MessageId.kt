package chat.sphinx.wrapper.message

import kotlin.jvm.JvmInline

inline val MessageId.isProvisionalMessage: Boolean
    get() = value < 0

@JvmInline
value class MessageId(val value: Long)
