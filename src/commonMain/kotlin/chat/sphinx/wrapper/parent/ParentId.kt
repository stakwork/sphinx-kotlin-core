package chat.sphinx.wrapper.parent

import kotlin.jvm.JvmInline

@JvmInline
value class ParentId(val value: Long) {
    init {
        require(value >= 0) {
            "ParentId must be greater than or equal to 0"
        }
    }
}
