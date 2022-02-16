package chat.sphinx.wrapper.subscription

import kotlin.jvm.JvmInline

@JvmInline
value class Cron(val value: String) {
    init {
        require(value.isNotEmpty()) {
            "Subscription Cron cannot be empty"
        }
    }
}