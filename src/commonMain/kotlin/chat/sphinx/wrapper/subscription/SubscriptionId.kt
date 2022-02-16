package chat.sphinx.wrapper.subscription

import kotlin.jvm.JvmInline

@JvmInline
value class SubscriptionId(val value: Long) {
    init {
        require(value >= 0) {
            "SubscriptionId must be greater than or equal to 0"
        }
    }
}
