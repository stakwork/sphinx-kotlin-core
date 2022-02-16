package chat.sphinx.wrapper.subscription

import kotlin.jvm.JvmInline

@JvmInline
value class EndNumber(val value: Long) {
    init {
        require(value >= 0) {
            "EndNumber must be greater than or equal to 0"
        }
    }
}
