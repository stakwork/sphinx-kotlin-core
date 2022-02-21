package chat.sphinx.wrapper.subscription

import kotlin.jvm.JvmInline

@JvmInline
value class SubscriptionCron(val value: String) {
    init {
        require(value.isNotEmpty()) {
            "SubscriptionCron cannot be empty"
        }
    }
}
