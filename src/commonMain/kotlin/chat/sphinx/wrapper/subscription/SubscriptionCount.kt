package chat.sphinx.wrapper.subscription

import kotlin.jvm.JvmInline

@JvmInline
value class SubscriptionCount(val value: Long) {
    init {
        require(value >= 0) {
            "SubscriptionCount must be greater than or equal to 0"
        }
    }
}